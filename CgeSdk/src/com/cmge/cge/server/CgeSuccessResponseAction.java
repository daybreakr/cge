package com.cmge.cge.server;

import com.cmge.cge.sdk.util.Security;

import org.json.JSONException;
import org.json.JSONObject;

public class CgeSuccessResponseAction implements ICgeResponseAction {

    @Override
    public void onResponse(CgeCommand command, CgeResponse response) {
        String dataStr = Security.decrypt(response.getSignKey(), response.getData());
        try {
            JSONObject data = new JSONObject(dataStr);
            command.toRes
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
