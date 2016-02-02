package com.cmge.cge.server;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class JsonResponse implements IResponse {

    @Override
    public IResponse fromContent(String content) {
        try {
            JSONObject json = new JSONObject(content);
            return fromJson(json);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }

    public abstract IResponse fromJson(JSONObject json) throws JSONException;
}
