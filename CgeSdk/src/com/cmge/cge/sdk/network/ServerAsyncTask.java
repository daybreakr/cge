package com.cmge.cge.sdk.network;

import org.apache.http.HttpException;
import org.json.JSONException;
import org.json.JSONObject;

import com.cmge.cge.sdk.util.CLog;
import com.cmge.cge.sdk.util.SdkInnerKeys;
import com.cmge.cge.sdk.util.Security;

import java.util.HashMap;
import java.util.Map;

public class ServerAsyncTask extends ThreadTask<ServerTaskRunnable, Integer, Boolean> {

    private ServerTaskRunnable mRequest;
    private String mErrorMessage;

    @Override
    protected Boolean doInBackground(ServerTaskRunnable params) {
        if (params != null) {
            mRequest = params;
            return onResponse(onRequest());
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (mRequest != null) {
            if (isCancelled()) {
                mRequest.onCanceled();
            } else if (!result) {
                mRequest.onFailure(getErrorMessage());
            } else {
                CLog.d(CLog.TAG_CORE, "request succeed");
            }
        }

        CLog.d(CLog.TAG_HTTP, "server responsed");
    }

    protected String onRequest() {
        // step 1. set request data
        JSONObject dataJson;
        try {
            dataJson = mRequest.getRequestData();
        } catch (JSONException e) {
            setErrorMessage("set request data json error, " + e.getMessage());
            return null;
        }
        String data = dataJson.toString();
        CLog.d(CLog.TAG_CORE, "request data: " + data);

        // step 2. encrypt data
        String encryption = Security.encrypt(mRequest.getSignKey(), data);
        if (encryption == null) {
            setErrorMessage("encrypt data error");
            return null;
        }
        data = encryption;

        // step 3. set http request parameters
        Map<String, String> params = new HashMap<String, String>();
        mRequest.onSetRequestParameters(params, data);

        // step 4. executing http request
        CustomHttpClient httpClient = new CustomHttpClient();
        final int maxRetryTimes = mRequest.getMaxRetryTimes();
        for (int times = 1; times <= maxRetryTimes; times++) {
            try {
                String response = mRequest.onExecutingRequest(httpClient, params,
                        times);
                return response;
            } catch (HttpException e) {
                CLog.w(CLog.TAG_CORE, "connection failed, " + e.getMessage());
            }
        }

        // fallback by connection failed
        setErrorMessage("connection failed");
        return null;
    }

    protected boolean onResponse(String response) {
        CLog.d(CLog.TAG_CORE, "server response: " + response);

        if (response == null || response.trim().equals("")) {
            return false;
        }

        try {
            // step 1. pre-handle response
            JSONObject responseJson = new JSONObject(response);
            final int code = responseJson.getInt(SdkInnerKeys.RESULT_CODE);
            final String msg = responseJson.getString(SdkInnerKeys.RESULT_MSG);
            CLog.d(CLog.TAG_CORE, "response result. code=" + code + ", msg:" + msg);
            if (mRequest.onPreHandleResponse(code, msg)) {
                CLog.d(CLog.TAG_CORE, "response has been handled");
                return true;
            }

            if (code != SdkInnerKeys.RESULT_OK) {
                setErrorMessage(msg);
                return false;
            }

            // step 2. decode data
            String data = responseJson.getString(SdkInnerKeys.DATA);
            String decryption = Security.decrypt(mRequest.getSignKey(), data);
            CLog.d(CLog.TAG_CORE, "decrypted data: " + decryption);
            if (decryption == null) {
                setErrorMessage("decryption error");
                return false;
            }
            JSONObject dataJson = new JSONObject(decryption);

            // step 3. resolve data
            mRequest.onGetResponseData(code, msg, dataJson);
            return true;
        } catch (JSONException e) {
            setErrorMessage("decode json error, " + e.getMessage());
        }

        return false;
    }

    protected void setErrorMessage(String message) {
        CLog.e(CLog.TAG_CORE, message);
        mErrorMessage = message;
    }

    protected String getErrorMessage() {
        return mErrorMessage == null ? "request failed" : mErrorMessage;
    }

}
