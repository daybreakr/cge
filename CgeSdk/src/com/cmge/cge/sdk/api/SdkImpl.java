
package com.cmge.cge.sdk.api;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpException;
import org.json.JSONException;
import org.json.JSONObject;

import com.cmge.cge.sdk.callback.CgeCallback;
import com.cmge.cge.sdk.callback.CgeCallback.LoginResult;
import com.cmge.cge.sdk.channel.CgeChannelCallback;
import com.cmge.cge.sdk.channel.Channel;
import com.cmge.cge.sdk.info.CgeConfig;
import com.cmge.cge.sdk.info.CgeOrderInfo;
import com.cmge.cge.sdk.info.CgeUserInfo;
import com.cmge.cge.sdk.network.CustomHttpClient;
import com.cmge.cge.sdk.network.OnServerResultCallback;
import com.cmge.cge.sdk.network.ServerAsyncTask;
import com.cmge.cge.sdk.network.ServerTaskRunnable;
import com.cmge.cge.sdk.util.CLog;
import com.cmge.cge.sdk.util.ParamsUtil;
import com.cmge.cge.sdk.util.SdkInnerKeys;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

class SdkImpl extends CgeSdk {

    private static final String SDK_VERSION = "1.0.1";
    private static final boolean ENCRYPT_CONFIG = false;
    
    private static final boolean NEED_PROTOCOL_VERSION_IN_CUSTOM = false;
    private static final boolean NEED_USER_ID_IN_CUSTOM = false;
    private static final boolean NEED_ORDER_SERIAL_NUMBER = true; 
    
    private static final int PENDING_LOGIN_CANCELED_DELAY = 5 * 1000; // milliseconds

    private Handler mMainHandler;
    private WeakReference<Activity> mActivityRef;

    private CgeConfig mConfig;

    private Channel mChannel;

    private CgeSdkCallback mCallback;

    private boolean mSdkInitialized = false;
    
    private boolean mPendingLoginCanceled = false;

    private LoginHandler mLoginHandler;
    
    private static String sProtocolVersion = "";

    SdkImpl() {
        mLoginHandler = new LoginHandler(this);
    }

    private CgeChannelCallback mChannelCallback = new CgeChannelCallback() {

        @Override
        public void onInitResult(InitResult result, String message, Bundle data) {
            notifyInitResult(result, message);
        }

        @Override
        public void onLoginResult(LoginResult result, String message, Bundle data) {
            // disable if get channel login callback
            mPendingLoginCanceled = false;
            mLoginHandler.removeMessages(LoginHandler.MSG_LOGIN_CANCEL);
            
            switch (result) {
                case SUCCEED:
                    CLog.d(CLog.TAG_CORE, "channel login succeed");

                    final CgeUserInfo user = data.getParcelable(SdkInnerKeys.USER_INFO);
                    boolean mustVerify = data.getBoolean(SdkInnerKeys.MUST_VERIFY_LOGIN);

                    if (mChannel.isNeedVerifyAccount()) {
                        verifyAccount(user, message, mustVerify, new OnVerifyAccountListener() {

                            @Override
                            public void onSucceed(final CgeUserInfo userInfo, final String msg) {
                                runOnMainThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        Activity activity = mActivityRef != null ? mActivityRef.get() : null;
                                        if (activity != null) {
                                            mChannel.onVerifyAccountSucceed(activity, userInfo);
                                        } else {
                                            CLog.e(CLog.TAG_CORE, "no activity reference");
                                        }
                                        notifyLoginResult(CgeCallback.LoginResult.SUCCEED, msg);
                                    }
                                });

                            }

                            @Override
                            public void onFailed(CgeUserInfo userInfo, String msg, boolean mustVerify) {
                                if (mustVerify) {
                                    mChannel.setLogin(false);
                                    mChannel.setUserInfo(null);
                                    notifyLoginResult(CgeCallback.LoginResult.FAILED, msg);
                                } else {
                                    mChannel.setLogin(true);
                                    mChannel.setUserInfo(userInfo);
                                    notifyLoginResult(CgeCallback.LoginResult.SUCCEED, msg);
                                }
                            }

                            @Override
                            public void onCanceled() {
                                notifyLoginResult(CgeCallback.LoginResult.CANCELED, null);
                            }
                        });
                    } else {
                        runOnMainThread(new Runnable() {

                            @Override
                            public void run() {
                                Activity activity = mActivityRef != null ? mActivityRef.get() : null;
                                if (activity != null) {
                                    mChannel.onVerifyAccountSucceed(activity, user);
                                } else {
                                    CLog.e(CLog.TAG_CORE, "no activity reference");
                                }
                                notifyLoginResult(CgeCallback.LoginResult.SUCCEED, null);
                            }
                        });
                    }
                    break;

                default:
                    notifyLoginResult(result, message);
                    break;
            }

        }

        @Override
        public void onLogoutResult(LogoutResult result, String message, Bundle data) {
            notifyLogoutResult(result, message);
        }

        @Override
        public void onSwitchAccountResult(SwitchAccountResult result, String message, Bundle data) {
            switch (result) {
                case SUCCEED:
                    CLog.d(CLog.TAG_CORE, "channel switch account succeed");

                    final CgeUserInfo user = data.getParcelable(SdkInnerKeys.USER_INFO);
                    boolean mustVerify = data.getBoolean(SdkInnerKeys.MUST_VERIFY_LOGIN);
                    
                    if (mChannel.isNeedVerifyAccount()) {
                        verifyAccount(user, message, mustVerify, new OnVerifyAccountListener() {

                            @Override
                            public void onSucceed(final CgeUserInfo userInfo, String msg) {
                                runOnMainThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        Activity activity = mActivityRef != null ? mActivityRef.get() : null;
                                        if (activity != null) {
                                            mChannel.onVerifyAccountSucceed(activity, userInfo);
                                        } else {
                                            CLog.e(CLog.TAG_CORE, "no activity reference");
                                        }
                                        notifySwitchAccountResult(CgeCallback.SwitchAccountResult.SUCCEED, null);
                                    }
                                });

                            }

                            @Override
                            public void onFailed(CgeUserInfo userInfo, String msg, boolean mustVerify) {
                                if (mustVerify) {
                                    mChannel.setLogin(false);
                                    mChannel.setUserInfo(null);
                                    notifySwitchAccountResult(CgeCallback.SwitchAccountResult.FAILED,
                                            msg);
                                } else {
                                    mChannel.setLogin(true);
                                    mChannel.setUserInfo(userInfo);
                                    notifySwitchAccountResult(CgeCallback.SwitchAccountResult.SUCCEED,
                                            msg);
                                }
                            }

                            @Override
                            public void onCanceled() {
                                notifySwitchAccountResult(CgeCallback.SwitchAccountResult.CANCELED,
                                        null);
                            }
                        });
                    } else {
                        runOnMainThread(new Runnable() {

                            @Override
                            public void run() {
                                Activity activity = mActivityRef != null ? mActivityRef.get() : null;
                                if (activity != null) {
                                    mChannel.onVerifyAccountSucceed(activity, user);
                                } else {
                                    CLog.e(CLog.TAG_CORE, "no activity reference");
                                }
                                notifySwitchAccountResult(CgeCallback.SwitchAccountResult.SUCCEED, null);
                            }
                        });
                    }
                    break;

                default:
                    notifySwitchAccountResult(result, message);
                    break;
            }
        }

        @Override
        public void onPurchaseResult(PurchaseResult result, String message, Bundle data) {
            Bundle purchaseBundle = null;
            Map<String, String> purchaseInfo = null;
            CgeOrderInfo orderInfo = null;
            switch (result) {
                case SUCCEED:
                    purchaseBundle = data.getBundle(SdkInnerKeys.PURCHASE_INFO);
                    purchaseInfo = ParamsUtil.parseMap(purchaseBundle);
                    orderInfo = data.getParcelable(SdkInnerKeys.ORDER_INFO);
                    
                    notifyPurchaseResult(CgeCallback.PurchaseResult.SUCCEED, orderInfo.orderId);
                    
                    notifyServerPurchaseResult(purchaseInfo, orderInfo);
                    break;

                case SUBMITTED:
                    purchaseBundle = data.getBundle(SdkInnerKeys.PURCHASE_INFO);
                    purchaseInfo = ParamsUtil.parseMap(purchaseBundle);
                    orderInfo = data.getParcelable(SdkInnerKeys.ORDER_INFO);
                    
                    notifyPurchaseResult(CgeCallback.PurchaseResult.SUBMITTED, orderInfo.orderId);
                    
                    notifyServerPurchaseResult(purchaseInfo, orderInfo);
                    break;

                case TOKEN_EXPIRED:
                    mChannel.setLogin(false);
                    mChannel.setUserInfo(null);
                    notifyPurchaseResult(result, message);
                    break;

                default:
                    notifyPurchaseResult(result, message);
                    break;
            }

        }

        @Override
        public void onExitPageResult(ExitResult result, Bundle data) {
            notifyExitResult(result);
        }
    };

    @Override
    /* package */void initSdk(Application application) {
        CLog.init(application);
        
        CLog.d(CLog.TAG_CORE, "initSdk");

        mMainHandler = new Handler(Looper.getMainLooper());

        // decode config file
        mConfig = CgeConfig.decodeConfigInfo(application, ENCRYPT_CONFIG);
        if (mConfig == null) {
            throw new RuntimeException("decode config file failed");
        }

        // instantiate channel
        mChannel = Channel.instantiateChannel(application, mConfig);
        if (mChannel == null) {
            throw new RuntimeException("instantiate channel failed");
        }
        mChannel.setChannelCallback(mChannelCallback);

        if (mChannel.isNeedInitParams()) {
            getInitParametersFromServer();
        }

        mChannel.onApplicationCreated(application);

        mSdkInitialized = true;
    }
    
    @Override
    public void setProtocolVersion(String protocolVersion) {
        sProtocolVersion = protocolVersion;
    }

    @Override
    public void init(final Activity activity, CgeSdkCallback callback) {
        CLog.d(CLog.TAG_CORE, "init");

        if (!mSdkInitialized) {
            throw new RuntimeException("sdk not initailze, check the application setup");
        }
        
        if (mChannel.isChannelInitialized()) {
            CLog.d(CLog.TAG_CORE, "channel has been initailized");
            return;
        }

        if (callback == null) {
            throw new RuntimeException("no sdk callback");
        }
        mCallback = callback;

        mPendingLoginCanceled = false;
        
        runOnMainThread(new Runnable() {
            
            @Override
            public void run() {
                mChannel.init(activity);
            }
        });
    }

    @Override
    public void login(final Activity activity, final Map<String, String> loginInfo) {
        CLog.d(CLog.TAG_CORE, "login");

        if (!mChannel.isChannelInitialized()) {
            notifyLoginResult(CgeCallback.LoginResult.FAILED, "channel not initialize");
            return;
        }

        // FIXME: should prevents from duplicate login?

        runOnMainThread(new Runnable() {

            @Override
            public void run() {
                if (mChannel.isNeedPendinLoginCanceled()) {
                    mPendingLoginCanceled = true;
                }
                mChannel.login(activity, loginInfo);
            }
        });
    }

    @Override
    public void logout(final Activity activity) {
        CLog.d(CLog.TAG_CORE, "logout");

        if (!mChannel.isChannelInitialized()) {
            notifyLogoutResult(CgeCallback.LogoutResult.FAILED, "channel not initialize");
            return;
        }

        if (!mChannel.isLogin()) {
            notifyLogoutResult(CgeCallback.LogoutResult.SUCCEED, "already offline");
            return;
        }

        runOnMainThread(new Runnable() {

            @Override
            public void run() {
                mChannel.logout(activity);
            }
        });
    }

    @Override
    public void switchAccount(final Activity activity) {
        CLog.d(CLog.TAG_CORE, "switch account");

        if (!mChannel.isChannelInitialized()) {
            notifySwitchAccountResult(CgeCallback.SwitchAccountResult.FAILED,
                    "channel not initialize");
            return;
        }

        runOnMainThread(new Runnable() {
            
            @Override
            public void run() {
                mChannel.switchAccount(activity);
            }
        });
    }

    @Override
    public void purchase(final Activity activity, final Map<String, String> purchaseInfo) {
        CLog.d(CLog.TAG_CORE, "purchase");

        if (!mChannel.isChannelInitialized()) {
            notifyPurchaseResult(CgeCallback.PurchaseResult.FAILED, "channel not initialize");
            return;
        }

        if (purchaseInfo == null) {
            notifyPurchaseResult(CgeCallback.PurchaseResult.FAILED, "empty purchase info");
            return;
        }
        CLog.d(CLog.TAG_CORE, "purchase info: " + purchaseInfo.toString());
        
        if (!ParamsUtil.checkRequiredParamsByStaticFields(purchaseInfo, CgeSdkKeys.Purchase.class)) {
            throw new RuntimeException("missing required params");
        }
        
        processPurchaseInfo(purchaseInfo);

        if (mChannel.isPurchaseNeedLogin() && (!mChannel.isLogin())) {
            notifyPurchaseResult(CgeCallback.PurchaseResult.NOT_LOGIN, null);
            return;
        }

        if (mChannel.isNeedOrderId() && purchaseInfo.get(SdkInnerKeys.Purchase.ORDER_ID) == null) {
            // get order ID from server first
            getPurchaseOrderId(purchaseInfo, new OnServerResultCallback() {

                @Override
                public void onResult(int code, String msg) {
                    if (code == OnServerResultCallback.RESULT_OK) {
                        doChannelPurchasing(activity, purchaseInfo);
                    } else {
                        notifyPurchaseResult(CgeCallback.PurchaseResult.FAILED, msg);
                    }
                }
            });
        } else {
            // do channel purchasing directly
            doChannelPurchasing(activity, purchaseInfo);
        }
    }

    @Override
    public void exit(final Activity activity) {
        CLog.d(CLog.TAG_CORE, "exit");

        if (!mChannel.isChannelInitialized()) {
            CLog.e(CLog.TAG_CORE, "channel not initialize");
            return;
        }

        if (mChannel.hasExitPage()) {
            runOnMainThread(new Runnable() {
                
                @Override
                public void run() {
                    mChannel.popupExitPage(activity);
                }
            });
        } else {
            notifyExitResult(CgeCallback.ExitResult.NO_EXIT_PAGE);
        }
    }

    @Override
    public void onCreate(Activity activity) {
        mChannel.onCreate(activity);
    }
    
    @Override
    public void onRestart(Activity activity) {
        mChannel.onRestart(activity);
    }

    @Override
    public void onStart(Activity activity) {
        mChannel.onStart(activity);

        mActivityRef = new WeakReference<Activity>(activity);
    }

    @Override
    public void onResume(Activity activity) {
        mChannel.onResume(activity);
        
        if (mPendingLoginCanceled) {
            mPendingLoginCanceled = false;
            mLoginHandler.removeMessages(LoginHandler.MSG_LOGIN_CANCEL);
            mLoginHandler.sendEmptyMessageDelayed(LoginHandler.MSG_LOGIN_CANCEL, PENDING_LOGIN_CANCELED_DELAY);
        }
    }

    @Override
    public void onPause(Activity activity) {
        mChannel.onPause(activity);
    }

    @Override
    public void onStop(Activity activity) {
        mChannel.onStop(activity);
    }

    @Override
    public void onDestroy(Activity activity) {
        mChannel.onDestroy(activity);
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode,
            int resultCode, Intent data) {
        mChannel.onActivityResult(activity, requestCode, resultCode, data);
    }

    @Override
    public void onNewIntent(Activity activity, Intent intent) {
        mChannel.onNewIntent(activity, intent);
    }
    
    @Override
    public void onQuitGame(Activity activity) {
        CLog.d(CLog.TAG_CORE, "onQutiGame");
        mChannel.onQuitGame(activity);
    }

    @Override
    public void submitGameData(final Activity activity, final Map<String, String> submitInfo) {
        CLog.d(CLog.TAG_CORE, "submit game data");

        if (!mChannel.isChannelInitialized()) {
            CLog.e(CLog.TAG_CORE, "channel not initialize");
            return;
        }

        if (!ParamsUtil.checkRequiredParamsByStaticFields(submitInfo, CgeSdkKeys.Submit.class)) {
            throw new RuntimeException("missing required params");
        }
        
        runOnMainThread(new Runnable() {
            
            @Override
            public void run() {
                mChannel.submitGameData(activity, submitInfo);
            }
        });

    }

    @Override
    public void showFloatingPanel(final Activity activity) {
        CLog.d(CLog.TAG_CORE, "show floating panel");

        if (!mChannel.isChannelInitialized()) {
            CLog.e(CLog.TAG_CORE, "channel not initialize");
            return;
        }

        runOnMainThread(new Runnable() {
            
            @Override
            public void run() {
                mChannel.showFloatingPanel(activity);
            }
        });
    }

    @Override
    public void hideFloatingPanel(final Activity activity) {
        CLog.d(CLog.TAG_CORE, "hide floating panel");

        if (!mChannel.isChannelInitialized()) {
            CLog.e(CLog.TAG_CORE, "channel not initialize");
            return;
        }

        runOnMainThread(new Runnable() {
            
            @Override
            public void run() {
                mChannel.hideFloatingPanel(activity);
            }
        });
    }

    @Override
    public String getUserId() {
        try {
            return mChannel.getUserInfo().id;
        } catch (NullPointerException e) {
            return "";
        }
    }

    @Override
    public String getUserName() {
        try {
            return mChannel.getUserInfo().name;
        } catch (NullPointerException e) {
            return "";
        }
    }

    @Override
    public String getUserToken() {
        try {
            return mChannel.getUserInfo().token;
        } catch (NullPointerException e) {
            return "";
        }
    }

    @Override
    public String getUserExtend() {
        try {
            return mChannel.getUserInfo().extend;
        } catch (NullPointerException e) {
            return "";
        }
    }

    @Override
    public String getSdkVersion() {
        return SDK_VERSION;
    }

    @Override
    public String getChannelId() {
        return mChannel.getChannelId();
    }

    @Override
    /* package */String getGameMainActivity() {
        return mConfig.getGameMainActivity();
    }

    private void doChannelPurchasing(final Activity activity, final Map<String, String> purchaseInfo) {
        runOnMainThread(new Runnable() {

            @Override
            public void run() {
                CLog.d(CLog.TAG_CORE, "doChannelPurchasing.purchaseInfo=" + purchaseInfo.toString());
                mChannel.purchase(activity, purchaseInfo);
            }
        });
    }

    private void getInitParametersFromServer() {
        CLog.d(CLog.TAG_CORE, "getInitParametersFromServer.");

        final String url = mConfig.getInitParamsUrl();
        final String signKey = mConfig.getSignKey();
        new ServerAsyncTask().execute(new ServerTaskRunnable(url, signKey) {

            @Override
            protected JSONObject getRequestData() throws JSONException {
                JSONObject data = new JSONObject();
                data.put(SdkInnerKeys.Init.KEY, mChannel.getInitParamsRequestKeys());
                return data;
            }

            @Override
            protected void onGetResponseData(int code, String msg, JSONObject data)
                    throws JSONException {
                mChannel.onGetInitParamsSucceed(data);
            }

            @Override
            protected void onFailure(String errMsg) {
                CLog.e(CLog.TAG_CHANNEL, "get inti params from server failed");
                mChannel.onGetInitParamsFailed(errMsg);
            }

            @Override
            protected void onCanceled() {
                CLog.e(CLog.TAG_CHANNEL, "get inti params from server canceled");
                mChannel.onGetInitParamsFailed("canceled");
            }
        });
    }

    private void verifyAccount(final CgeUserInfo userInfo, final String message,
            final boolean mustVerify, final OnVerifyAccountListener listener) {
        CLog.d(CLog.TAG_CORE, "verify account."
                + "\nuserInfo:" + (userInfo == null ? "null" : userInfo.toString())
                + ", message: " + message
                + ", mustVerify=" + mustVerify);

        if (userInfo == null) {
            listener.onSucceed(null, "empty userInfo, maybe in offline mode");
            return;
        }

        final String url = mConfig.getAccountVerifyUrl();
        final String signKey = mConfig.getSignKey();
        new ServerAsyncTask().execute(new ServerTaskRunnable(url, signKey) {

            @Override
            protected JSONObject getRequestData() throws JSONException {
                JSONObject data = new JSONObject();

                Map<String, String> params = new HashMap<String, String>();
                params.put(SdkInnerKeys.User.ID, userInfo.id);
                params.put(SdkInnerKeys.User.NAME, userInfo.name);
                params.put(SdkInnerKeys.User.TOKEN, userInfo.token);
                params.put(SdkInnerKeys.User.EXTEND, userInfo.extend);
                params.put(SdkInnerKeys.PROTOCOL_VERSION, sProtocolVersion);
                ParamsUtil.bindJsonData(params, data);

                return data;
            }

            @Override
            protected void onGetResponseData(int code, String msg, JSONObject data)
                    throws JSONException {
                final CgeUserInfo verifiedUser = new CgeUserInfo();
                verifiedUser.id = data.optString(SdkInnerKeys.User.ID);
                verifiedUser.name = data.optString(SdkInnerKeys.User.NAME);
                verifiedUser.token = data.optString(SdkInnerKeys.User.TOKEN);
                verifiedUser.extend = data.optString(SdkInnerKeys.User.EXTEND);
                
                listener.onSucceed(verifiedUser, data.optString(SdkInnerKeys.VERIFY));
            }

            @Override
            protected void onFailure(String errMsg) {
                listener.onFailed(userInfo, errMsg, mustVerify);
            }

            @Override
            protected void onCanceled() {
                listener.onCanceled();
            }
        });
    }

    private void getPurchaseOrderId(final Map<String, String> purchaseInfo,
            final OnServerResultCallback callback) {
        CLog.d(CLog.TAG_CORE, "getPurchaseOrderId.");

        if (purchaseInfo == null) {
            callback.onResult(OnServerResultCallback.RESULT_ERROR, "empty purchase info");
            return;
        }

        final String url = mConfig.getOrderCreateUrl();
        final String signKey = mConfig.getSignKey();
        new ServerAsyncTask().execute(new ServerTaskRunnable(url, signKey) {

            @Override
            protected JSONObject getRequestData() throws JSONException {
                JSONObject data = new JSONObject();

                Map<String, String> params = new HashMap<String, String>();
                ParamsUtil.bindStringFromInfo(purchaseInfo, params, CgeSdkKeys.Purchase.SERVER_ID);
                ParamsUtil.bindStringFromInfo(purchaseInfo, params, CgeSdkKeys.Purchase.CUSTOM);
                params.put(SdkInnerKeys.User.ID, getUserId());
                params.put(SdkInnerKeys.PACKAGE_NAME, mChannel.getPackageName());
                params.put(SdkInnerKeys.PACKAGE_VERSION, mChannel.getPackageVersion());
                params.put(SdkInnerKeys.Purchase.EXTEND, mChannel.getPurchaseExtend(purchaseInfo));
                ParamsUtil.bindJsonData(params, data);

                return data;
            }

            @Override
            protected void onGetResponseData(int code, String msg, JSONObject data)
                    throws JSONException {
                final String orderField = data.getString(SdkInnerKeys.Purchase.ORDER_ID);
                final String callbackUrl = data.optString(SdkInnerKeys.Purchase.CALLBACK_URL);
                final String extend = data.optString(SdkInnerKeys.Purchase.EXTEND);

                processOrderField(purchaseInfo, orderField);
                
                purchaseInfo.put(SdkInnerKeys.Purchase.CALLBACK_URL, callbackUrl);
                purchaseInfo.put(SdkInnerKeys.Purchase.EXTEND, extend);

                callback.onResult(OnServerResultCallback.RESULT_OK, null);
            }

            @Override
            protected void onFailure(String errMsg) {
                callback.onResult(OnServerResultCallback.RESULT_ERROR, "get order ID failed! "
                        + errMsg);
            }

            @Override
            protected void onCanceled() {
                callback.onResult(OnServerResultCallback.RESULT_ERROR, "get order ID canceled!");
            }
        });
    }
    
    private void processPurchaseInfo(Map<String, String> purchaseInfo) {
        String custom = purchaseInfo.get(CgeSdkKeys.Purchase.CUSTOM);
        if (NEED_PROTOCOL_VERSION_IN_CUSTOM) {
            custom = sProtocolVersion + "|" + custom;
        }
        
        if (NEED_USER_ID_IN_CUSTOM) {
            custom = custom + "|" + getUserId();
        }
        
        purchaseInfo.put(CgeSdkKeys.Purchase.CUSTOM, custom);
        
        CLog.d(CLog.TAG_CORE, "purchase custom:" + purchaseInfo.get(CgeSdkKeys.Purchase.CUSTOM));
    }
    
    private void processOrderField(Map<String, String> purchaseInfo, String orderField) {
        String orderId = orderField;
        if (NEED_ORDER_SERIAL_NUMBER) {
            String[] orderFieldItems = orderField.split("\\|"); // orderId|orderSerialNumber
            if (orderFieldItems.length > 1) {
                orderId = orderFieldItems[0];
                String orderSerialNumber = orderFieldItems[1];
                
                // append order serial number to custom field
                String custom = purchaseInfo.get(CgeSdkKeys.Purchase.CUSTOM);
                custom = custom + "|" + orderSerialNumber;
                purchaseInfo.put(CgeSdkKeys.Purchase.CUSTOM, custom);
                
                CLog.d(CLog.TAG_CORE, "purchase custom:" + custom);
            }
        }
        purchaseInfo.put(SdkInnerKeys.Purchase.ORDER_ID, orderId);
        
        CLog.d(CLog.TAG_CORE, "purchase orderId:" + orderId);
    }

    private void notifyServerPurchaseResult(Map<String, String> purchaseInfo, CgeOrderInfo orderInfo) {
        sendPurchaseResultToServer(purchaseInfo, orderInfo, new OnServerResultCallback() {

            @Override
            public void onResult(int resultCode, String msg) {
                CLog.d(CLog.TAG_CORE, "resultCode=" + resultCode + ", msg:" + msg);
            }
        });
    }

    private void sendPurchaseResultToServer(final Map<String, String> purchaseInfo,
            final CgeOrderInfo orderInfo, final OnServerResultCallback callback) {
        final String url = mConfig.getOrderCallbackUrl();
        final String signKey = mConfig.getSignKey();
        new ServerAsyncTask().execute(new ServerTaskRunnable(url, signKey) {

            @Override
            protected JSONObject getRequestData() throws JSONException {
                JSONObject data = new JSONObject();

                Map<String, String> params = new HashMap<String, String>();
                ParamsUtil.bindStringFromInfo(purchaseInfo, params, CgeSdkKeys.Purchase.SERVER_ID);
                ParamsUtil.bindStringFromInfo(purchaseInfo, params,
                        CgeSdkKeys.Purchase.PRODUCT_NAME);
                ParamsUtil.bindStringFromInfo(purchaseInfo, params,
                        CgeSdkKeys.Purchase.PRODUCT_DESC);
                ParamsUtil.bindStringFromInfo(purchaseInfo, params, CgeSdkKeys.Purchase.PRICE);
                ParamsUtil.bindStringFromInfo(purchaseInfo, params, CgeSdkKeys.Purchase.COUNT);
                ParamsUtil.bindStringFromInfo(purchaseInfo, params, CgeSdkKeys.Purchase.CUSTOM);
                params.put(SdkInnerKeys.User.ID, getUserId());
                params.put(SdkInnerKeys.Order.ORDER_ID, orderInfo.orderId);
                params.put(SdkInnerKeys.Order.CHANNEL_ORDER_ID, orderInfo.channelOrderId);
                params.put(SdkInnerKeys.Order.STATUS, orderInfo.status);

                ParamsUtil.bindJsonData(params, data);

                return data;
            }

            @Override
            protected boolean onPreHandleResponse(int code, String msg) {
                callback.onResult(code, msg);
                return true;
            }

            @Override
            protected void onGetResponseData(int code, String msg, JSONObject data)
                    throws JSONException {
                // onPreHandleResponse have been handled.
            }

            @Override
            protected void onFailure(String errMsg) {
                callback.onResult(OnServerResultCallback.RESULT_ERROR, errMsg);
            }

            @Override
            protected void onCanceled() {
                callback.onResult(OnServerResultCallback.RESULT_ERROR, "canceled");
            }

            @Override
            protected String onExecutingRequest(CustomHttpClient httpClient,
                    Map<String, String> params, int retryTime)
                    throws HttpException {
                if (retryTime == 2) {
                    SystemClock.sleep(1000 * 60); // 1 minute
                } else if (retryTime == 3) {
                    SystemClock.sleep(1000 * 60 * 5); // 5 minutes
                } else if (retryTime >= 4) {
                    SystemClock.sleep(1000 * 60 * 10); // 10 minutes
                }

                return super.onExecutingRequest(httpClient, params, retryTime);
            }
        });
    }

    private void notifyInitResult(final CgeCallback.InitResult result,
            final String message) {
        CLog.d(CLog.TAG_CORE, "init result:" + result + ", message:" + message);
        
        if (mCallback == null) {
            CLog.d(CLog.TAG_CORE, "no callback yet");
            return;
        }
        
        runOnMainThread(new Runnable() {

            @Override
            public void run() {
                mCallback.onInitializedResult(result, message);
            }
        });
    }

    private void notifyLoginResult(final CgeCallback.LoginResult result, final String message) {
        CLog.d(CLog.TAG_CORE, "login result:" + result + ", message:" + message);
        
        if (mCallback == null) {
            CLog.d(CLog.TAG_CORE, "no callback yet");
            return;
        }
        
        runOnMainThread(new Runnable() {

            @Override
            public void run() {
                mCallback.onLoginResult(result, message);
            }
        });
    }

    private void notifyLogoutResult(final CgeCallback.LogoutResult result, final String message) {
        CLog.d(CLog.TAG_CORE, "logout result:" + result + ", message:" + message);
        
        if (mCallback == null) {
            CLog.d(CLog.TAG_CORE, "no callback yet");
            return;
        }
        
        runOnMainThread(new Runnable() {

            @Override
            public void run() {
                mCallback.onLogoutResult(result, message);
            }
        });
    }

    private void notifySwitchAccountResult(final CgeCallback.SwitchAccountResult result,
            final String message) {
        CLog.d(CLog.TAG_CORE, "switch account result:" + result + ", message:" + message);
        
        if (mCallback == null) {
            CLog.d(CLog.TAG_CORE, "no callback yet");
            return;
        }
        
        runOnMainThread(new Runnable() {

            @Override
            public void run() {
                mCallback.onSwitchAccountResult(result, message);
            }
        });
    }

    private void notifyPurchaseResult(final CgeCallback.PurchaseResult result, final String message) {
        CLog.d(CLog.TAG_CORE, "purchase result:" + result + ", message:" + message);
        
        if (mCallback == null) {
            CLog.d(CLog.TAG_CORE, "no callback yet");
            return;
        }
        
        runOnMainThread(new Runnable() {

            @Override
            public void run() {
                mCallback.onPurchaseResult(result, message);
            }
        });
    }

    private void notifyExitResult(final CgeCallback.ExitResult result) {
        CLog.d(CLog.TAG_CORE, "exit result:" + result);
        
        if (mCallback == null) {
            CLog.d(CLog.TAG_CORE, "no callback yet");
            return;
        }
        
        runOnMainThread(new Runnable() {

            @Override
            public void run() {
                mCallback.onExitResult(result);
            }
        });
    }

    private void runOnMainThread(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        } else {
            mMainHandler.post(r);
        }
    }

    private interface OnVerifyAccountListener {

        void onSucceed(CgeUserInfo userInfo, String msg);

        void onFailed(CgeUserInfo userInfo, String msg, boolean mustVerify);

        void onCanceled();
    }
    
    private static class LoginHandler extends Handler {
        public static final int MSG_LOGIN_CANCEL = 0xff;
        
        private WeakReference<SdkImpl> mSdk;
        
        public LoginHandler(SdkImpl impl) {
            mSdk = new WeakReference<SdkImpl>(impl);
        }
        
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOGIN_CANCEL:
                    if (mSdk != null && mSdk.get() != null) {
                        mSdk.get().notifyLoginResult(LoginResult.CANCELED, null);
                    }
                    break;

                default:
                    break;
            }
        }
    }
}
