package com.cmge.cge.sdk.util;

import android.annotation.SuppressLint;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Security {

    private static final String DEFAULT_CHARSET = "UTF-8";

    public static String encrypt(String signKey, String data) {
        return encrypt(signKey, data, null);
    }

    @SuppressLint("NewApi")
	public static String encrypt(String signKey, String data, String charset) {
        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }

        try {
            // sign data
            JSONObject jsonData = new JSONObject(data);
            jsonData.put(SdkInnerKeys.SIGN, getSign(signKey, jsonData, charset));

            // return Base64 encoded data
            return Base64.encodeToString(jsonData.toString().getBytes(charset), Base64.DEFAULT);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String signKey, String encdedData) {
        return decrypt(signKey, encdedData, null);
    }

    @SuppressLint("NewApi")
	public static String decrypt(String signKey, String encodedData, String charset) {
        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }

        try {
            String data = new String(Base64.decode(encodedData, Base64.DEFAULT));
            CLog.d(CLog.TAG_SECURITY, "decrypted data: " + data);

            // verifying data sign
            JSONObject jsonData = new JSONObject(data);
            String originalSign = jsonData.getString(SdkInnerKeys.SIGN);
            String sign = getSign(signKey, jsonData, charset);
            if (sign.equals(originalSign)) {
                return data;
            } else {
                CLog.d(CLog.TAG_SECURITY, "found different sign");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            CLog.e(CLog.TAG_SECURITY, "decryption decode json error, " + e.getMessage());
        }

        return null;
    }

    private static String getSign(String signKey, JSONObject data, String charset)
            throws JSONException {
        final int len = data.length();

        // original message not include the sign field
        data.remove(SdkInnerKeys.SIGN);

        // get key list
        @SuppressWarnings("unchecked")
		Iterator<String> keys = data.keys();
        List<String> keyList = new ArrayList<String>(len);
        while (keys.hasNext()) {
            keyList.add(keys.next());
        }
        // sort key list
        Collections.sort(keyList, new MyComparator());

        // get value list order by sorted keys
        List<String> valueList = new ArrayList<String>(len);
        for (String key : keyList) {
        	String value = data.getString(key);
        	if (value != null) {
                valueList.add(value);
        	}
        }

        return sign(signKey, valueList, charset);
    }

    private static String sign(String signKey, List<String> valueList, String charset) {
        // concat value and sign key
        StringBuilder builder = new StringBuilder();
        final int size = valueList.size();
        for (int i = 0; i < size; i++) {
            builder.append(valueList.get(i));
            if (i != size - 1) {
                builder.append(".");
            }
        }

        builder.append(signKey);

        String concat = builder.toString();

        return getMd5HexString(concat, charset);
    }

    private static String getMd5HexString(String data, String charset) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(data.getBytes(charset));

            byte[] digest = messageDigest.digest();
            StringBuilder digestHexString = new StringBuilder();
            for (byte b : digest) {
                int value = b & 0xff;
                if (value <= 0xf) {
                    digestHexString.append("0");
                }
                digestHexString.append(Integer.toHexString(value));
            }

            return digestHexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static class MyComparator implements Comparator<String> {

        @Override
        public int compare(String lhs, String rhs) {
            return lhs.compareTo(rhs);
        }
    }
}
