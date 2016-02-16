package com.cmge.cge.server;

import com.cmge.cge.sdk.util.CLog;

public class CgeServerApi {
    
    public static final String TAG = "Cge." + CgeServerApi.class.getSimpleName();
    
    private String mPrimaryUrl;
    private String mSecondaryUrl;
    
    private String mChannelId;
    private String mVersion;
    private String mSignKey;
    
    private IDispatcher mDispatcher;
    
    public CgeServerApi(String primaryUrl, String secondaryUrl, String channelId, String version, String signKey) {
        mPrimaryUrl = primaryUrl;
        mSecondaryUrl = secondaryUrl;
        mChannelId = channelId;
        mVersion = version;
        mSignKey = signKey;
        
        mDispatcher = new Dispatcher(mPrimaryUrl, mCgeCommandHandler);
    }
    
    public void send(CgeCommand command) {
        if (!command.isPrepared()) {
            command.prepare(mVersion, mChannelId, mSignKey);
        }
        
        mDispatcher.execute(command);
    }
    
    private IDispatcher.IDispatchCallback mCgeCommandHandler = new IDispatcher.IDispatchCallback() {
        
        @Override
        public void onDispatchComplete(boolean success, String result, ICommand command) {
            CgeCommand cgeCommand = (CgeCommand) command;
            
            if (success) {
                // TODO: handle dispatch success
                CgeResponse cgeResponse = new CgeResponse(mSignKey);
                cgeResponse.fromContent(result);
                
//                ICgeResponseAction action = CgeResponseActionFactory.getResponseAction(cgeResponse.getCode());
//                action.onResponse(cgeCommand, cgeResponse);
                
                switch (cgeResponse.getCode()) {
                    case Protocols.RESULT_OK:
                        // TODO onSuccess
                        break;
                        
                    case Protocols.RESULT_REDIRECT:
                        mDispatcher = new Dispatcher(mSecondaryUrl, mCgeCommandHandler);
                        send(cgeCommand);
                        break;
                        
                    case Protocols.RESULT_ERROR:
                    default:
                        // TODO onFailure
                        break;
                }
                
            } else {
                // TODO: handle dispatch failed
            }
        }
    };
}
