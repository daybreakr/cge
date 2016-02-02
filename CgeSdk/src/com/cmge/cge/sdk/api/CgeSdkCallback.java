
package com.cmge.cge.sdk.api;

import com.cmge.cge.sdk.callback.CgeCallback;

public interface CgeSdkCallback extends CgeCallback {

    /**
     * 初始化结果回调
     * 
     * @param result 初始化结果
     * @param message 详细信息，一般不成功会有详细信息。
     */
    void onInitializedResult(InitResult result, String message);
    
    /**
     * 登陆结果回调
     * 
     * @param result 登陆结果
     * @param message 详细信息，一般不成功会有详细信息。
     */
    void onLoginResult(LoginResult result, String message);

    /**
     * 注销结果回调
     * 
     * @param result 注销结果
     * @param message 详细信息，一般不成功会有详细信息。
     */
    void onLogoutResult(LogoutResult result, String message);

    /**
     * 切换用户回调
     * 
     * @param result 切换用户结果
     * @param message 详细信息， 一般不成功会有详细信息。
     */
    void onSwitchAccountResult(SwitchAccountResult result, String message);
    
    /**
     * 客户端支付结果回调
     * 
     * @param result 客户端支付结果
     * @param message 详细信息，一般不成功会有详细信息。
     */
    void onPurchaseResult(PurchaseResult result, String message);

    /**
     * 游戏退出结果回调
     * 
     * @param result 游戏退出结果
     */
    void onExitResult(ExitResult result);
}
