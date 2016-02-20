package com.cmge.cge.server.api;

import com.cmge.cge.server.dispatch.Dispatcher;
import com.cmge.cge.server.dispatch.IDispatchCallback;
import com.cmge.cge.server.dispatch.IDispatcher;

public class ServerApi implements IDispatchCallback {
    
    public static final String TAG = "Cge." + ServerApi.class.getSimpleName();
    
    private String mServerUrl;
    private String mSecondaryUrl;
    
    private String mChannelId;
    private String mVersion;
    private String mSignKey;
    
    private IDispatcher mDispatcher;
    
    public ServerApi(String primaryUrl, String secondaryUrl, String channelId, String version, String signKey) {
        mServerUrl = primaryUrl;
        mSecondaryUrl = secondaryUrl;
        mChannelId = channelId;
        mVersion = version;
        mSignKey = signKey;
        
        mDispatcher = new Dispatcher(mServerUrl, this);
    }
    
    public void execute(Command command) {
        command.prepare(mVersion, mChannelId, mSignKey);
        
        mDispatcher.dispatch(command);
    }
    
    @Override
    public void onDispatchComplete(boolean success, String content, Command command) {
        boolean failed = !success;
        String message = null;
        if (success) {
            BaseResponse response = new BaseResponse();
            if (response.fromContent(content) != null) {
                switch (response.getCode()) {
                    case Protocols.RESULT_OK:
                        command.onComplete(true, response.getData());
                        break;
                        
                    case Protocols.RESULT_REDIRECT:
                        mDispatcher = new Dispatcher(mSecondaryUrl, this);
                        execute(command);
                        return;
                        
                    case Protocols.RESULT_ERROR:
                    default:
                        // command failed
                        message = "Command failed";
                        failed = true;
                        break;
                }
            } else {
                message = "Failed to parse response";
                failed = true;
            }
        } else {
            // dispatch failed
            message = "Dispatch failed";
        }
        
        if (failed) {
            command.onComplete(failed, message + ". " + content);
        }
    }
}
