package com.cmge.cge.server.api;

import org.json.JSONException;
import org.json.JSONObject;

public class BaseResponse implements IResponse {

    private int mCode = Protocols.RESULT_ERROR;
    private String mMessage;
    private String mData;
    
    public int getCode() {
        return mCode;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getData() {
        return mData;
    }

    @Override
    public IResponse fromContent(String content) {
        try {
            JSONObject response = new JSONObject(content);
            
            mCode = response.getInt(Protocols.CODE);
            mMessage = response.optString(Protocols.MSG);
            mData = response.optString(Protocols.DATA);
            
            return this;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
