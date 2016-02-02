package com.cmge.cge.sdk.util;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParamsUtil {

    public static Map<String, String> parseMap(Bundle bundle) {
        Map<String, String> map = new HashMap<String, String>();
        for (String key : bundle.keySet()) {
            map.put(key, bundle.getString(key));
        }
        return map;
    }
    
    public static Bundle parseBundle(Map<String, String> map) {
        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        return bundle;
    }
    
	public static void bindStringFromInfo(Map<String, String> info,
			Map<String, String> target, String key) {
		if (key == null || key.trim().equalsIgnoreCase("")) {
			return;
		}
		target.put(key, info.get(key));
	}
	
    public static void bindJsonData(Map<String, String> params, JSONObject json)
            throws JSONException {
	    if (json == null || params == null || params.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : params.entrySet()) {
            json.put(entry.getKey(), confirmRequestData(entry.getValue()));
        }
	}
	
    public static boolean checkRequiredParamsByStaticFields(Map<String, String> info, Class<?> clazz) {
        Set<Object> missingKeys = new HashSet<Object>();
        try {
            for (Field field : clazz.getDeclaredFields()) {
                Object key = field.get(null);
                if (info.containsKey(key)) {
                    continue;
                }
                missingKeys.add(key);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        
        if (missingKeys.size() > 0) {
            CLog.e(CLog.TAG_CORE, "missing required params: " + missingKeys.toString());
            return false;
        }
        
        return true;
    }
    
	public static boolean checkRequiredParams(Map<String, String> info,
			String... keys) {
		if (keys == null || keys.length == 0) {
			return true;
		}

		List<String> missing = new ArrayList<String>(keys.length);
		for (String key : keys) {
			if (info.containsKey(key)) {
				continue;
			}
			missing.add(key);
		}

		if (missing.size() > 0) {
			CLog.e(CLog.TAG_CORE,
					"missing required params " + missing.toString());
			return false;
		}

		return true;
	}
	
	public static JSONObject decodeBase64(String extend) throws JSONException {
        byte[] decodedBytes = Base64.decode(extend, Base64.DEFAULT);
        String decoded = new String(decodedBytes);
        JSONObject json = new JSONObject(decoded);
        return json;
    }
	
	public static String encodeBase64(JSONObject json) {
	    return encodeBase64(json != null ? json.toString() : null);
	}
	
	public static String encodeBase64(String src) {
	    if (TextUtils.isEmpty(src)) {
	        return "";
	    }
	    return Base64.encodeToString(src.getBytes(), Base64.DEFAULT);
	}
	
	private static String confirmRequestData(String str) {
        if (str == null || str.trim().equalsIgnoreCase("")) {
            return "";
        } else {
            return str;
        }
    }
}
