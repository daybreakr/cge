package com.cmge.cge.server;

import org.json.JSONException;
import org.json.JSONObject;

public class CgeResponse extends JsonResponse {

    private int mCode = -1;
    private String mMessage;
    private String mData;
    
    private String mSignKey;
    
    @Override
    public IResponse fromJson(JSONObject json) throws JSONException {
        mCode = json.getInt(Protocols.CODE);
        mMessage = json.optString(Protocols.MSG);
        mData = json.optString(Protocols.DATA);
        return this;
    }
    
    public int getCode() {
        return mCode;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getData() {
        return mData;
    }
    
    public void setSignKey(String signKey) {
        mSignKey = signKey;
    }
    
    public String getSignKey() {
        return mSignKey;
    }
}
