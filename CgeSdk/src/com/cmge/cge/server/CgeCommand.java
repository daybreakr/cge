package com.cmge.cge.server;

import com.cmge.cge.sdk.util.Security;

import java.util.HashMap;
import java.util.Map;

public abstract class CgeCommand implements ICommand {

    private String mAction;
    
    private IRequest mRequest;
    private IResponse mResponse;
    private IRetryStrategy mRetryStrategy;
    
    private String mVersion;
    private String mChannelId;
    
    private String mSignKey;
    
    private boolean mIsPrepared = false;
    
    public CgeCommand(String action, IRequest request, IResponse response) {
        this(action, request, response, new DefaultRetryStrategy());
    }
    
    public CgeCommand(String action, IRequest request, IResponse response, IRetryStrategy retryStrategy) {
        mAction = action;
        mRequest = request;
        mResponse = response;
        mRetryStrategy = retryStrategy;
    }
    
    @Override
    public String getAction() {
        return mAction;
    }
    
    @Override
    public Map<String, String> getRequestParameters() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Protocols.V, mVersion);
        params.put(Protocols.PID, mChannelId);
        
        String content = mRequest.toContent();
        String data = Security.encrypt(mSignKey, content);
        params.put(Protocols.DATA, data);
        
        return params;
    }
    
    @Override
    public IRetryStrategy getRetryStrategy() {
        return mRetryStrategy;
    }
    
    public boolean isPrepared() {
        return mIsPrepared;
    }
    
    public void prepare(String version, String channelId, String signKey) {
        mVersion = version;
        mChannelId = channelId;
        mSignKey = signKey;
        
        mIsPrepared = true;
    }
}
