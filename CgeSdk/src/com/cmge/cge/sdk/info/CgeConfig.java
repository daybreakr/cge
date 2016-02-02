
package com.cmge.cge.sdk.info;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.cmge.cge.sdk.util.CLog;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Base64;

public class CgeConfig {

    private static final String COMMON_CONFIG_FILE = "CgeSdkConfigCommon";
    private static final String CHANNEL_CONFIG_FILE_PREFIX = "CgeSdkConfig_";

    private String mChannelId;
    private String mChannelSdkClassName;
    private String mChannelAdapterClassName;
    private String mSignKey;

    private String mServerUrl;
    private String mInitParamsUrl;
    private String mAccountVerifyUrl;
    private String mOrderCreateUrl;
    private String mOrderCallbackUrl;

    private boolean mDebug = false;

    private String mGameMainActivity = null;

    private JSONObject mChannelParams;

    private CgeConfig() {
        // prevents from instantiate
    }

    public static CgeConfig decodeConfigInfo(Context context, boolean encrypt) {
        AssetManager assets = context.getAssets();

        // decode common config file
        JSONObject common = getRootJsonObject(assets, COMMON_CONFIG_FILE, encrypt);
        if (common == null) {
            throw new RuntimeException("decode common config file error");
        }
        
        // decode channel config file
        String[] files = null;
        try {
            files = assets.list("");
        } catch (IOException e) {
            throw new RuntimeException("list asset files error, " + e.getMessage());
        }
        
        List<String> channelConfigFiles = new ArrayList<String>();
        for (String file : files) {
            if (file.startsWith(CHANNEL_CONFIG_FILE_PREFIX)) {
                channelConfigFiles.add(file); 
            }
        }
        if (channelConfigFiles.size() != 1) {
            throw new RuntimeException("no or more than one channel config file");
        }
        
        final String channelConfigFile = channelConfigFiles.get(0);
        JSONObject channel = getRootJsonObject(assets, channelConfigFile, encrypt);
        if (channel == null) {
            throw new RuntimeException("decode channel config file error");
        }
        
        final String channelId = channelConfigFile.substring(CHANNEL_CONFIG_FILE_PREFIX.length());
        if (TextUtils.isEmpty(channelId)) {
            throw new RuntimeException("decode channel id error");
        }
        
        // construct config
        CgeConfig config = new CgeConfig();
        
        config.setChannelId(channelId);
        
        // parse common parameters
        try {
            config.setSignKey(common.getString("clientSignKey"));
            config.setServerUrl(common.getString("serverUrl"));
            config.setinitParamsUrl(common.getString("initParamsUrl"));
            config.setAccountVerifyUrl(common.getString("accountVerifyUrl"));
            config.setOrderCreateUrl(common.getString("orderCreateUrl"));
            config.setOrderCallbackUrl(common.getString("orderCallbackUrl"));
            config.setDebug(common.getBoolean("debugMode"));
            config.setGameMainActivity(common.getString("gameMainActivity"));
        } catch (JSONException e) {
            CLog.e(CLog.TAG_CORE, "decode common config json error, " + e.getMessage());
            return null;
        }
        
        // parse channel parameters
        try {
            config.setChannelSdkClass(channel.getString("channelSdkClass"));
            config.setChannelAdapterClass(channel.getString("channelAdapterClass"));
            config.setChannelParams(channel.getJSONObject("channelParams"));
        } catch (JSONException e) {
            CLog.e(CLog.TAG_CORE, "decode channel config json error, " + e.getMessage());
            return null;
        }

        return config;
    }

    public String getChannelId() {
        return mChannelId;
    }

    public String getChannelSdkClass() {
        return mChannelSdkClassName;
    }

    public String getChannelAdapterClass() {
        return mChannelAdapterClassName;
    }

    public String getSignKey() {
        return mSignKey;
    }

    public String getServerUrl() {
        return mServerUrl;
    }
    
    public String getInitParamsUrl() {
        return mInitParamsUrl;
    }

    public String getAccountVerifyUrl() {
        return mAccountVerifyUrl;
    }

    public String getOrderCreateUrl() {
        return mOrderCreateUrl;
    }

    public String getOrderCallbackUrl() {
        return mOrderCallbackUrl;
    }

    public boolean isDebug() {
        return mDebug;
    }

    public String getGameMainActivity() {
        return mGameMainActivity;
    }

    public JSONObject getChannelParams() {
        return mChannelParams;
    }

    private void setChannelId(String channelTag) {
        mChannelId = channelTag;
    }

    private void setChannelSdkClass(String name) {
        mChannelSdkClassName = name;
    }

    private void setChannelAdapterClass(String name) {
        mChannelAdapterClassName = name;
    }

    private void setSignKey(String signKey) {
        mSignKey = signKey;
    }
    
    private void setServerUrl(String url) {
        if (url == null || url.trim().equals("")) {
            return;
        }

        mServerUrl = url + (url.endsWith("/") ? "" : "/");
    }

    private void setinitParamsUrl(String url) {
        mInitParamsUrl = confirmUrl(url);
    }
    
    private void setAccountVerifyUrl(String url) {
        mAccountVerifyUrl = confirmUrl(url);
    }

    private void setOrderCreateUrl(String url) {
        mOrderCreateUrl = confirmUrl(url);
    }

    private void setOrderCallbackUrl(String url) {
        mOrderCallbackUrl = confirmUrl(url);
    }

    private void setDebug(boolean debug) {
        mDebug = debug;
    }

    private void setGameMainActivity(String activityName) {
        mGameMainActivity = activityName;
    }

    private void setChannelParams(JSONObject params) {
        mChannelParams = params;
    }

    private String confirmUrl(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        } else if (url.startsWith("/")) {
            url = url.substring(1, url.length());
        }

        return getServerUrl() + url;
    }

    private static JSONObject getRootJsonObject(AssetManager assets, String filename, boolean encrypt) {
        JSONObject json = null;
        
        InputStream input = null;
        try {
            input = assets.open(filename);
            CLog.d(CLog.TAG_CORE, "found " + filename);
            
            json = decodeConfigJsonObject(input, encrypt);
        } catch (IOException e) {
            CLog.e(CLog.TAG_CORE, "get " + filename + " error, " + e.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    CLog.e(CLog.TAG_CORE, e.getMessage());
                }
            }
        }
        
        return json;
    }
    
    private static JSONObject decodeConfigJsonObject(InputStream input, boolean encrypt) {
        StringBuilder builder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));
        String line = null;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            CLog.e(CLog.TAG_CORE, e.getMessage());
        }

        String jsonStr = builder.toString();
        if (encrypt) {
            byte[] decode = Base64.decode(jsonStr, Base64.DEFAULT);
            jsonStr = new String(decode);
        }

        JSONObject json = null;
        try {
            json = new JSONObject(jsonStr);
        } catch (JSONException e) {
            CLog.e(CLog.TAG_CORE, e.getMessage());
        }
        
        return json;
    }
}
