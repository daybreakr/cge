package com.cmge.cge.server;

import java.util.Map;

public class Http implements IHttp {

    public static final int TYPE_DEFAULT = 1;

    private IHttp mImpl;

    public static IHttp getInstance() {
        return getInstance(TYPE_DEFAULT);
    }
    
    public static IHttp getInstance(int type) {
        IHttp impl = null;
        switch (type) {
            case TYPE_DEFAULT:
                impl = new ApacheHttp();
                break;

            default:
                // FIXME: should throws any exceptions?
                return null;
        }

        return new Http(impl);
    }

    private Http(IHttp impl) {
        mImpl = impl;
    }

    @Override
    public String get(String url, Map<String, String> params) throws Exception {
        return mImpl.get(url, params);
    }

    @Override
    public String get(String url, Map<String, String> params, String encode) throws Exception {
        return mImpl.get(url, params, encode);
    }

    @Override
    public String post(String url, Map<String, String> params) throws Exception {
        return mImpl.post(url, params);
    }

    @Override
    public String post(String url, Map<String, String> params, String encode) throws Exception {
        return mImpl.post(url, params, encode);
    }
}
