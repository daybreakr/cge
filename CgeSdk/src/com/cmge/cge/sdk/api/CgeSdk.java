
package com.cmge.cge.sdk.api;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import java.util.Map;

public abstract class CgeSdk {

    private static CgeSdk sInstance = null;

    CgeSdk() {
        // prevents from instantiate
    }

    /**
     * 获得CgeSdk单例
     * 
     * @return CgeSdk单例
     */
    public static CgeSdk getInstance() {
        if (sInstance == null) {
            synchronized (CgeSdk.class) {
                if (sInstance == null) {
                    sInstance = new SdkImpl();
                }
            }
        }
        return sInstance;
    }
    
    /**
     * 设置客户端协议号，透传给服务端以判断连接服务器
     * 
     * @param protocolVersion 游戏客户端协议版本
     */
    public abstract void setProtocolVersion(String protocolVersion);

    /**
     * 只在{@link Activity}中调用一次，渠道初始化。
     * 
     * @param activity Activity对象
     * @param callback 设置CgeSdk统一回调函数，所有SDK请求结果都会统一在此回调
     */
    public abstract void init(Activity activity, CgeSdkCallback callback);

    /**
     * 渠道登陆
     * 
     * @param activity activity对象，渠道需要该对象来调用相关接口
     * @param loginInfo 登陆参数
     *           
     */
    public abstract void login(Activity activity, Map<String, String> loginInfo);

    /**
     * 渠道注销
     * 
     * @param activity activity对象，渠道需要该对象来调用相关接口
     */
    public abstract void logout(Activity activity);

    /**
     * 主动切换用户接口
     * 
     * @param activity activity对象
     */
    public abstract void switchAccount(Activity activity);
    
    /**
     * 渠道支付
     * 
     * @param activity activity对象，渠道需要该对象来调用相关接口
     * @param purchaseInfo 支付参数
     */
    public abstract void purchase(Activity activity, Map<String, String> purchaseInfo);

    /**
     * 退出游戏时调用渠道退出界面。结果会通过统一回调函数通知游戏， 若渠道无退出界面，游戏可按需要弹出退出确认提示后做销毁工作，
     * 若渠道有退出界面，回调时用户已确认退出，游戏可直接做销毁工作。
     * 如不需要渠道退出界面可以不调用此接口，自行处理销毁工作，但某些渠道必须调用其退出界面
     * 
     * @param activity activity对象，渠道需要该对象来调用相关接口
     */
    public abstract void exit(Activity activity);

    /**
     * 需要在{@link Activity}对应的生命周期中回调
     * 
     * @param activity
     */
    public abstract void onCreate(Activity activity);
    
    /**
     * 需要在{@link Activity}对应的生命周期中回调
     * 
     * @param activity 关联的Activity对象
     */
    public abstract void onRestart(Activity activity);

    /**
     * 需要在{@link Activity}对应的生命周期中回调
     * 
     * @param activity 关联的Activity对象
     */
    public abstract void onStart(Activity activity);

    /**
     * 需要在{@link Activity}对应的生命周期中回调
     * 
     * @param activity 关联的Activity对象
     */
    public abstract void onResume(Activity activity);

    /**
     * 需要在{@link Activity}对应的生命周期中回调
     * 
     * @param activity 关联的Activity对象
     */
    public abstract void onPause(Activity activity);

    /**
     * 需要在{@link Activity}对应的生命周期中回调
     * 
     * @param activity 关联的Activity对象
     */
    public abstract void onStop(Activity activity);

    /**
     * 需要在{@link Activity}对应的生命周期中回调
     * 
     * @param activity 关联的Activity对象
     */
    public abstract void onDestroy(Activity activity);

    /**
     * 需要在{@link Activity}对应的生命周期中回调
     * 
     * @param activity 关联的Activity对象
     * @param requestCode 传入Activity中回调的参数
     * @param resultCode 传入Activity中回调的参数
     * @param data 传入Activity中回调的参数
     */
    public abstract void onActivityResult(Activity activity, int requestCode, int resultCode,
            Intent data);

    /**
     * 需要在{@link Activity}对应的生命周期中回调
     * 
     * @param activity 关联的Activity对象
     * @param intent 传入Activity中回调的参数
     */
    public abstract void onNewIntent(Activity activity, Intent intent);
    
    /**
     * 在退出游戏前调用，执行渠道的销毁工作
     * 
     * @param activity 关联的Activity对象
     */
    public abstract void onQuitGame(Activity activity);
    
    /**
     * 向渠道提交游戏信息，有些渠道必须调用此接口。一般在分配角色后进入游戏时调用该接口提交游戏信息
     * 
     * @param activity
     * @param submitInfo
     */
    public abstract void submitGameData(Activity activity, Map<String, String> submitInfo);
    
    /**
     * 显示悬浮窗，视渠道支持情况而定
     * 
     * @param activity activity对象
     */
    public abstract void showFloatingPanel(Activity activity);
    
    /**
     * 隐藏悬浮窗，视渠道支持情况而定
     * 
     * @param activity activity对象
     */
    public abstract void hideFloatingPanel(Activity activity);
    
    /**
     * 获得用户ID
     * 
     * @return 如果已登录则返回用户ID(可能为null)，否则返回null
     */
    public abstract String getUserId();

    /**
     * 获取用户名
     * 
     * @return 如果已登录则返回用户名字(可能为null)，否则返回null
     */
    public abstract String getUserName();

    /**
     * 获得渠道返回的用户会话token
     * 
     * @return 如果已登录则返回渠道返回的用户会话token(可能为null)，否则返回null
     */
    public abstract String getUserToken();

    /**
     * 获得用户信息中的附加字段，由游戏服务器在登录验证时传入，原样透传
     * 
     * @return 如果已登录则原样透传游戏服务端返回的用户信息中的extend字段
     */
    public abstract String getUserExtend();

    /**
     * 获得当前CgeSdk版本号
     * 
     * @return CgeSdk版本号字符串
     */
    public abstract String getSdkVersion();

    /**
     * 获得当前接入渠道ID
     * 
     * @return 当前接入渠道ID
     */
    public abstract String getChannelId();

    /**
     * 只在{@link Application}中调用一次，CgeSdk初始化。由渠道适配包中的自定义Application负责调用，接入游戏无需调用
     * 
     * @param application application对象，某些渠道初始化需要提供
     */
    /*package*/abstract void initSdk(Application application);
    
    /*
     * 获得游戏主Activity完整类名
     * 
     * @return 游戏主Activity完整类名
     */
    /*package*/abstract String getGameMainActivity();
}
