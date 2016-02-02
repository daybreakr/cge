
package com.cmge.cge.sdk.channel;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.cmge.cge.sdk.callback.CgeCallback;
import com.cmge.cge.sdk.info.CgeConfig;
import com.cmge.cge.sdk.info.CgeOrderInfo;
import com.cmge.cge.sdk.info.CgeUserInfo;
import com.cmge.cge.sdk.util.CLog;
import com.cmge.cge.sdk.util.ParamsUtil;
import com.cmge.cge.sdk.util.SdkInnerKeys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class Channel {

    private CgeChannelCallback mCallback;

    protected String mPackageName;
    protected String mPackageVersion;
    protected boolean mLogin = false;

    protected CgeUserInfo mUserInfo = null;

    protected boolean mChannelInitialzed = false;

    private Map<String, String> mProductIdMap = new HashMap<String, String>();

    public static Channel instantiateChannel(Context context, CgeConfig config) {
        CLog.d(CLog.TAG_CHANNEL, "instantiate channel");

        if (config == null) {
            CLog.e(CLog.TAG_CHANNEL, "instantiate channel error, config is null");
            return null;
        }

        try {
            // found channel sdk
            String channelSdkClassName = config.getChannelSdkClass();
            Class.forName(channelSdkClassName);
            CLog.d(CLog.TAG_CHANNEL, "found channel sdk: " + channelSdkClassName);

            // found channel adapter
            String channelClassName = config.getChannelAdapterClass();
            Class<?> channelClass = Class.forName(channelClassName);
            CLog.d(CLog.TAG_CHANNEL, "found channel adapter: " + channelClassName);

            // parse channel parameters
            final Channel channel = (Channel) channelClass.newInstance();
            CLog.d(CLog.TAG_CHANNEL, "parsing channel params: " + config.getChannelParams());
            channel.parseParameters(context, config.getChannelParams());

            return channel;
        } catch (ClassNotFoundException e) {
            CLog.e(CLog.TAG_CHANNEL, "class not found: " + e.getMessage());
        } catch (InstantiationException e) {
            CLog.e(CLog.TAG_CHANNEL, "instantiate error: " + e.getMessage());
        } catch (IllegalAccessException e) {
            CLog.e(CLog.TAG_CHANNEL, "illegalAccess error: " + e.getMessage());
        }

        // fallback by errors
        CLog.e(CLog.TAG_CHANNEL, "instantiate channel error");
        return null;
    }

    public abstract String getChannelId();

    public abstract String getChannelVersion();

    public abstract void init(Activity activity);

    public abstract void login(Activity activity, Map<String, String> loginInfo);

    public abstract void logout(Activity activity);

    public abstract void purchase(Activity activity, Map<String, String> purchaseInfo);

    public final void switchAccount(Activity activity) {
        onSwitchAccount(activity);
    }

    public final void popupExitPage(Activity activity) {
        onPopupExitPage(activity);
    }

    public final void submitGameData(Activity activity, Map<String, String> submitInfo) {
        onSubmitGameData(activity, submitInfo);
    }

    public final void showFloatingPanel(Activity activity) {
        onShowFloatingPanel(activity);
    }

    public final void hideFloatingPanel(Activity activity) {
        onHideFloatingPanel(activity);
    }

    public boolean hasExitPage() {
        // default implementation, could override in subclass
        return false;
    }

    public boolean isPurchaseNeedLogin() {
        // default implementation, could override in subclass
        return true;
    }

    public boolean isNeedOrderId() {
        // default implementation, could override in subclass
        return true;
    }

    public boolean isNeedInitParams() {
        // default implementation, could override in subclass
        return false;
    }
    
    public boolean isNeedVerifyAccount() {
        // default implementation, could override in subclass
        return true;
    }
    
    public boolean isNeedPendinLoginCanceled() {
        // default implementation, could override in subclass
        return false;
    }

    public JSONArray getInitParamsRequestKeys() throws JSONException {
        // override in implementation
        return null;
    }

    public String getPurchaseExtend(Map<String, String> purchaseInfo) {
        // override in implementation
        return null;
    }

    public boolean isChannelInitialized() {
        return mChannelInitialzed;
    }

    public boolean isLogin() {
        return mLogin;
    }

    public CgeUserInfo getUserInfo() {
        return mUserInfo;
    }
    
    public String getPackageName() {
        return mPackageName;
    }

    public String getPackageVersion() {
        return mPackageVersion;
    }

    public void setChannelInitialized(boolean initialized) {
        mChannelInitialzed = initialized;
    }

    public void setLogin(boolean login) {
        mLogin = login;
    }

    public void setUserInfo(CgeUserInfo userInfo) {
        mUserInfo = userInfo;
    }
    
    public void onCreate(Activity activity) {
        // override in implementation
    }

    public void onRestart(Activity activity) {
        // override in implementation
    }

    public void onStart(Activity activity) {
        // override in implementation
    }

    public void onResume(Activity activity) {
        if (isChannelInitialized() && isLogin()) {
            showFloatingPanel(activity);
        }
        // override in implementation
    }

    public void onPause(Activity activity) {
        if (isChannelInitialized()) {
            hideFloatingPanel(activity);
        }
        // override in implementation
    }

    public void onStop(Activity activity) {
        // override in implementation
    }

    public void onDestroy(Activity activity) {
        // override in implementation
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        // override in implementation
    }

    public void onNewIntent(Activity activity, Intent intent) {
        // override in implementation
    }
    
    public void onQuitGame(Activity activity) {
        setChannelInitialized(false);
        // override in implementation
    }

    public void onApplicationCreated(Application application) {
        // override in implementation
    }

    public void onGetInitParamsSucceed(JSONObject values) throws JSONException {
        // override in implementation
    }

    public void onGetInitParamsFailed(String errMsg) {
        // override in implementation
    }

    public void onVerifyAccountSucceed(Activity activity, CgeUserInfo userInfo) {
        // default action while switching account is logout.
        setLogin(true);
        setUserInfo(userInfo);
        
        showFloatingPanel(activity);
    }
    
    protected void onSwitchAccount(Activity activity) {
        // default action while switching account is logout.
        hideFloatingPanel(activity);
        logout(activity);
    }

    protected void onPopupExitPage(Activity activity) {
        // if channel has exit page, override this method and pop-up channel's
        // exit page
    }

    protected void onSubmitGameData(Activity activity, Map<String, String> submitInfo) {
        // override in implementation
    }

    protected void onShowFloatingPanel(Activity activity) {
        // override in implementation
    }

    protected void onHideFloatingPanel(Activity activity) {
        // override in implementation
    }
    
    protected void onParseChannelParameters(JSONObject paramsJson) throws JSONException {
        // override in implementation
    }

    /**
     * map the product ID to channel pay code, move it to server for safety
     * @param jsonMap
     * @throws JSONException
     */
    @Deprecated
    protected void onParseProductIdMap(JSONObject jsonMap) throws JSONException {
        @SuppressWarnings("unchecked")
        Iterator<String> it = jsonMap.keys();
        while (it.hasNext()) {
            String key = it.next();
            setMappingProductId(key, jsonMap.optString(key, ""));
        }
    }

    public final void setChannelCallback(CgeChannelCallback callback) {
        mCallback = callback;
    }

    /**
     * get mapped channel pay code, move it to server for safety
     * @param product ID
     * @return channel pay code
     */
    @Deprecated
    protected final String getMappingProductId(String originalId) {
        return mProductIdMap.get(originalId);
    }

    /**
     * for mapping channel pay code, move it to server for safety
     * @param originalId
     * @param productId
     */
    @Deprecated
    protected final void setMappingProductId(String originalId, String productId) {
        mProductIdMap.put(originalId, productId);
    }

    /* initialized */
    protected final void notifyInitSucceed() {
        setChannelInitialized(true);
        notifyInitResult(CgeCallback.InitResult.SUCCEED, null, null);
    }

    protected final void notifyInitFailed() {
        notifyInitFailed(null);
    }
    
    protected final void notifyInitFailed(String message) {
        // ensure initialized flag
        setChannelInitialized(false);
        notifyInitResult(CgeCallback.InitResult.FAILED, message, null);
    }

    protected final void notifyInitResult(CgeCallback.InitResult result, String message, Bundle data) {
        mCallback.onInitResult(result, message, data);
    }

    /* login */
    protected final void notifyLoginSucceed(CgeUserInfo userInfo) {
        Bundle data = new Bundle();
        data.putParcelable(SdkInnerKeys.USER_INFO, userInfo);
        data.putBoolean(SdkInnerKeys.MUST_VERIFY_LOGIN, true);

        notifyLoginResult(CgeCallback.LoginResult.SUCCEED, null, data);
    }
    
    /**
     * mustVerifyLogin is always true
     * @param userInfo
     * @param mustVerifyLogin
     */
    @Deprecated
    protected final void notifyLoginSucceed(CgeUserInfo userInfo, boolean mustVerifyLogin) {
        Bundle data = new Bundle();
        data.putParcelable(SdkInnerKeys.USER_INFO, userInfo);
        data.putBoolean(SdkInnerKeys.MUST_VERIFY_LOGIN, mustVerifyLogin);

        notifyLoginResult(CgeCallback.LoginResult.SUCCEED, null, data);
    }

    protected final void notifyLoginFailed() {
        notifyLoginFailed(null);
    }
    
    protected final void notifyLoginFailed(String message) {
        notifyLoginResult(CgeCallback.LoginResult.FAILED, message, null);
    }

    protected final void notifyLoginCanceled() {
        notifyLoginResult(CgeCallback.LoginResult.CANCELED, null, null);
    }

    protected final void notifyLoginResult(CgeCallback.LoginResult result, String message,
            Bundle data) {
        mCallback.onLoginResult(result, message, data);
    }

    /* logout */
    protected final void notifyLogoutSucceed() {
        setLogin(false);
        setUserInfo(null);

        notifyLogoutResult(CgeCallback.LogoutResult.SUCCEED, null, null);
    }

    protected final void notifyLogoutFailed() {
        notifyLogoutFailed(null);
    }
    
    protected final void notifyLogoutFailed(String message) {
        notifyLogoutResult(CgeCallback.LogoutResult.FAILED, message, null);
    }

    protected final void notifyLogoutResult(CgeCallback.LogoutResult result, String message,
            Bundle data) {
        mCallback.onLogoutResult(result, message, data);
    }

    /* switch account */
    protected final void notifySwitchAccountSucceed(CgeUserInfo userInfo) {
        Bundle data = new Bundle();
        data.putParcelable(SdkInnerKeys.USER_INFO, userInfo);
        data.putBoolean(SdkInnerKeys.MUST_VERIFY_LOGIN, true);

        notifySwitchAccountResult(CgeCallback.SwitchAccountResult.SUCCEED, null, data);
    }
    
    /**
     * mustVerifyLogin is always true
     * @param userInfo
     * @param mustVerifyLogin
     */
    @Deprecated
    protected final void notifySwitchAccountSucceed(CgeUserInfo userInfo, boolean mustVerify) {
        Bundle data = new Bundle();
        data.putParcelable(SdkInnerKeys.USER_INFO, userInfo);
        data.putBoolean(SdkInnerKeys.MUST_VERIFY_LOGIN, mustVerify);

        notifySwitchAccountResult(CgeCallback.SwitchAccountResult.SUCCEED, null, data);
    }

    protected final void notifySwitchAccountFailed() {
        notifySwitchAccountFailed(null);
    }
    
    protected final void notifySwitchAccountFailed(String message) {
        notifySwitchAccountResult(CgeCallback.SwitchAccountResult.FAILED, message, null);
    }

    protected final void notifySwitchAccountResult(CgeCallback.SwitchAccountResult result,
            String message, Bundle data) {
        mCallback.onSwitchAccountResult(result, message, data);
    }

    /* purchase */
    protected final void notifyPurchaseFinished(String orderId, Map<String, String> purchaseInfo,
            boolean succeed) {
        CgeOrderInfo order = new CgeOrderInfo();
        order.orderId = orderId;
        
        notifyPurchaseFinished(order, purchaseInfo, succeed);
    }
    
    protected final void notifyPurchaseFinished(CgeOrderInfo orderInfo,
            Map<String, String> purchaseInfo, boolean succeed) {
        if (orderInfo != null) {
            orderInfo.status = succeed 
                    ? SdkInnerKeys.Order.STATUS_SUCCESS
                    : SdkInnerKeys.Order.STATUS_SUBMITTED;
        }
        
        Bundle data = new Bundle();
        data.putParcelable(SdkInnerKeys.ORDER_INFO, orderInfo);
        data.putBundle(SdkInnerKeys.PURCHASE_INFO, ParamsUtil.parseBundle(purchaseInfo));
        notifyPurchaseResult(succeed 
                ? CgeCallback.PurchaseResult.SUCCEED
                : CgeCallback.PurchaseResult.SUBMITTED, null, data);
    }

    protected final void notifyPurchaseFailed() {
        notifyPurchaseFailed(null);
    }
    
    protected final void notifyPurchaseFailed(String message) {
        notifyPurchaseResult(CgeCallback.PurchaseResult.FAILED, message, null);
    }

    protected final void notifyPurchaseCanceled() {
        notifyPurchaseResult(CgeCallback.PurchaseResult.CANCELED, null, null);
    }

    protected final void notifyPurchaseResult(CgeCallback.PurchaseResult result, String message) {
        notifyPurchaseResult(result, message, null);
    }

    protected final void notifyPurchaseResult(CgeCallback.PurchaseResult result, String message,
            Bundle data) {
        mCallback.onPurchaseResult(result, message, data);
    }

    /* exit */
    protected final void notifyQuitGame() {
        notifyExitResult(CgeCallback.ExitResult.QUIT, null);
    }

    protected final void notifyResumeGame() {
        notifyExitResult(CgeCallback.ExitResult.RESUME, null);
    }

    protected final void notifyExitResult(CgeCallback.ExitResult result, Bundle data) {
        mCallback.onExitPageResult(result, data);
    }

    private void parseParameters(Context context, JSONObject params) {
        parseChannelParameters(params);
        parseProductIdMap(params);

        setPackageName(context);
        setPackageVersion(context);
    }

    private void parseChannelParameters(JSONObject params) {
        try {
            onParseChannelParameters(params);
        } catch (JSONException e) {
            CLog.e(CLog.TAG_CHANNEL, "parse channel params json error, " + e.getMessage());
        }
    }

    private void setPackageName(Context context) {
        mPackageName = context.getApplicationContext().getPackageName();
    }

    private void setPackageVersion(Context context) {
        String version = null;
        try {
            version = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mPackageVersion = version;
    }
    
    /**
     * move it to server for safety 
     * @param json
     */
    @Deprecated
    private void parseProductIdMap(JSONObject json) {
        try {
            JSONObject jsonMap = json.optJSONObject("productIdMap");
            if (jsonMap != null) {
                onParseProductIdMap(jsonMap);
            }
        } catch (JSONException e) {
            CLog.e(CLog.TAG_CHANNEL, "parse product id map json error, " + e.getMessage());
        }
    }
}
