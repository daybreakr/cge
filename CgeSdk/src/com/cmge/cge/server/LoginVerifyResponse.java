package com.cmge.cge.server;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginVerifyResponse extends JsonResponse {

    private String mUid;
    private String mName;
    private String mToken;
    private String mExtend;
    
    public String getUid() {
        return mUid;
    }

    public String getName() {
        return mName;
    }

    public String getToken() {
        return mToken;
    }

    public String getExtend() {
        return mExtend;
    }
    
    @Override
    public IResponse fromJson(JSONObject json) throws JSONException {
        mUid = json.optString(Protocols.UID);
        mName = json.optString(Protocols.NAME);
        mToken = json.optString(Protocols.TOKEN);
        mExtend = json.optString(Protocols.EXTEND);
        
        return this;
    }
}
