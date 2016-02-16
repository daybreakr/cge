package com.cmge.cge.server;

import android.text.TextUtils;

import com.cmge.cge.sdk.util.Security;

import org.json.JSONException;
import org.json.JSONObject;

public class CgeResponse extends JsonResponse {

    private int mCode = Protocols.RESULT_ERROR;
    private String mMessage = "";
    private String mData = null;
    
    private String mSignKey;
    
    public CgeResponse(String signKey) {
        mSignKey = signKey;
    }
    
    @Override
    public IResponse fromJson(JSONObject json) throws JSONException {
        mCode = json.getInt(Protocols.CODE);
        mMessage = json.optString(Protocols.MSG);
        
        String encryptedData = json.optString(Protocols.DATA);
        if (!TextUtils.isEmpty(encryptedData)) {
            mData = Security.decrypt(mSignKey, encryptedData);
        }
        
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
}
