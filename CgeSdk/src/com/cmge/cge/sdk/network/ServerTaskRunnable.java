package com.cmge.cge.sdk.network;

import org.apache.http.HttpException;
import org.json.JSONException;
import org.json.JSONObject;

import com.cmge.cge.sdk.api.CgeSdk;
import com.cmge.cge.sdk.util.CLog;
import com.cmge.cge.sdk.util.SdkInnerKeys;

import java.util.Map;

public abstract class ServerTaskRunnable {
    private static final int DEF_MAX_RETRY_TIMES = 4;
    private String mUrl;
    private String mSignKey;

    public ServerTaskRunnable(String url, String signKey) {
        mUrl = url;
        mSignKey = signKey;
    }

    // set data fields
    protected abstract JSONObject getRequestData() throws JSONException;

    protected abstract void onGetResponseData(int code, String msg, JSONObject data)
            throws JSONException;

    // handle the failure of the response.
    protected abstract void onFailure(String errMsg);

    protected abstract void onCanceled();

    // default implementation
    protected void onSetRequestParameters(Map<String, String> params, String data) {
    	// XXX: dependency optimize
        params.put(SdkInnerKeys.CHANNEL_ID, CgeSdk.getInstance().getChannelId());
        params.put(SdkInnerKeys.SDK_VERSION, CgeSdk.getInstance().getSdkVersion());
        params.put(SdkInnerKeys.DATA, data);
    }

    // default implementation
    protected String onExecutingRequest(CustomHttpClient httpClient,
                                        Map<String, String> params,
                                        int retryTime) throws HttpException {
        CLog.d(CLog.TAG_CORE, "requesting to server: " + mUrl + ", with params: " + params);
        return httpClient.post(mUrl, params);
    }

    // return true if handled the response, then it would stop the task.
    protected boolean onPreHandleResponse(int code, String msg) {
        return false;
    }

    protected int getMaxRetryTimes() {
        return DEF_MAX_RETRY_TIMES;
    }

    protected String getSignKey() {
        return mSignKey;
    }
}
