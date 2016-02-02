package com.cmge.cge.sdk.channel;

import android.os.Bundle;

import com.cmge.cge.sdk.callback.CgeCallback;

public interface CgeChannelCallback extends CgeCallback{
    
    void onInitResult(InitResult result, String message, Bundle data);
    
    void onLoginResult(LoginResult result, String message, Bundle data);
    
    void onLogoutResult(LogoutResult result, String message, Bundle data);
    
    void onSwitchAccountResult(SwitchAccountResult result, String message, Bundle data);
    
    void onPurchaseResult(PurchaseResult result, String message, Bundle data);
    
    void onExitPageResult(ExitResult result, Bundle data);
}
