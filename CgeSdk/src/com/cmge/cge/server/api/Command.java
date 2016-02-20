package com.cmge.cge.server.api;

import com.cmge.cge.sdk.util.Security;
import com.cmge.cge.server.dispatch.DefaultRetryStrategy;
import com.cmge.cge.server.dispatch.IRetryStrategy;

import java.util.HashMap;
import java.util.Map;

public abstract class Command {

    private String mAction;
    private IRequest mRequest;
    private IRetryStrategy mRetryStrategy;
    
    private String mVersion;
    private String mChannelId;
    
    private String mSignKey;
    
    public int tries;
    
    public Command(String action, IRequest request) {
        this(action, request, new DefaultRetryStrategy());
    }
    
    public Command(String action, IRequest request, IRetryStrategy retryStrategy) {
        mAction = action;
        mRequest = request;
        mRetryStrategy = retryStrategy;
        
        if (mRetryStrategy == null) {
            mRetryStrategy = new DefaultRetryStrategy();
        }
        
        tries = 0;
    }
    
    public String getAction() {
        return mAction;
    }
    
    public Map<String, String> getRequestArgs() {
        Map<String, String> args = new HashMap<String, String>();
        args.put(Protocols.V, mVersion);
        args.put(Protocols.PID, mChannelId);
        
        String data = mRequest != null ? mRequest.toContent() : null;
        if (data != null) {
            args.put(Protocols.DATA, Security.encrypt(mSignKey, data));
        }
        
        return args;
    }
    
    public IRetryStrategy getRetryStrategy() {
        return mRetryStrategy;
    }
    
    public void onComplete(boolean success, String cookie) {
        if (success) {
            
        } else {
            
        }
    }
    
    public void prepare(String version, String channelId, String signKey) {
        mVersion = version;
        mChannelId = channelId;
        mSignKey = signKey;
        
        tries = 0;
    }
}
