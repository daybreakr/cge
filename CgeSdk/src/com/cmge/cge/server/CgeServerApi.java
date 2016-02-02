package com.cmge.cge.server;

public class CgeServerApi implements IDispatcher.IDispatchCallback {
    
    public static final String TAG = "CGE_ServerApi";
    
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
        
        mDispatcher = new Dispatcher(mPrimaryUrl, this);
    }
    
    public void send(CgeCommand command) {
        command.prepare(mVersion, mChannelId, mSignKey);
        mDispatcher.execute(command);
    }
    
    @Override
    public void onDispatchComplete(boolean success, String result, ICommand command) {
        if (success) {
            // TODO: handle dispatch success
            CgeCommand cgeCommand = (CgeCommand) command;
            
            CgeResponse cgeResponse = new CgeResponse();
            cgeResponse.fromContent(result);
            
            ICgeResponseAction action = CgeResponseActionFactory.getResponseAction(cgeResponse.getCode());
            action.onResponse(cgeCommand, cgeResponse);
        } else {
            // TODO: handle dispatch failed
        }
    }
}
