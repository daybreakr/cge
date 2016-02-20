package com.cmge.cge.server.http;

import java.util.Map;

public interface IHttp {
    // TODO: throws concrete exception
    public String get(String url, Map<String, String> params) throws Exception;
    public String get(String url, Map<String, String> params, String encode) throws Exception;
    public String post(String url, Map<String, String> params) throws Exception;
    public String post(String url, Map<String, String> params, String encode) throws Exception;
}
