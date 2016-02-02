package com.cmge.cge.sdk.network;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import com.cmge.cge.sdk.util.CLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class CustomHttpClient {

    private static final int sTimeout = (CLog.DEBUG ? 120 : 60) * 1000; // milliseconds
    
    private static final HttpClient mClient;

    static {
        SSLSocketFactory socketFactory = null;
            KeyStore trustStore;
            try {
                trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                socketFactory = new CustomSSLSocketFactory(trustStore);
                socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            } catch (KeyStoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (CertificateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (KeyManagementException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        
        if (socketFactory == null) {
            CLog.e(CLog.TAG_HTTP, "sockFactory is null");
        }
        
        HttpParams params = new BasicHttpParams();

        /* basic parameters */
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpProtocolParams.setUseExpectContinue(params, true);
        String userAgent = "Mozilla/5.0 "
                + "(Windows NT 5.1) "
                + "AppleWebKit/537.36 "
                + "(KHTML, like Gecko) "
                + "Chrome/29.0.1547.62 "
                + "Safari/537.36";
        HttpProtocolParams.setUserAgent(params, userAgent);

        /* timeout setup */
        // set the timeout of obtaining connection from the connection pool
        ConnManagerParams.setTimeout(params, sTimeout);
        // set the connection timeout
        HttpConnectionParams.setConnectionTimeout(params, sTimeout);
        // set the socket timeout
        HttpConnectionParams.setSoTimeout(params, sTimeout);

        /* supported for http and https */
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", socketFactory, 443));

        /* use the thread-safe connection manager for creating the http client */
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);

        mClient = new DefaultHttpClient(cm, params);
    }

    public CustomHttpClient() {

    }

    public String post(String url, String params) throws HttpException {
        return post(url, null, params, null);
    }

    public String post(String url, Map<String, String> headers, String params, String encode)
            throws HttpException {
        if (url == null || url.equals("")) {
            return "url is null";
        }
        if (encode == null || encode.equals("")) {
            encode = "UTF-8";
        }

        HttpEntity paramsEntity = getParamsEntity(params, encode);

        return postInner(url, headers, paramsEntity, encode);
    }

    public String post(String url, Map<String, String> params)
            throws HttpException {
        return post(url, null, params, null);
    }

    public String post(String url, Map<String, String> headers, Map<String, String> params
            , String encode) throws HttpException {
        if (url == null || url.equals("")) {
            return "url is null";
        }
        if (encode == null || encode.equals("")) {
            encode = "UTF-8";
        }

        HttpEntity entity = getParamsEntity(params, encode);

        return postInner(url, headers, entity, encode);
    }

    public String get(String url, Map<String, String> headers, String encode)
            throws HttpException {
        if (url == null || url.equals("")) {
            return "url is null";
        }
        if (encode == null || encode.equals("")) {
            encode = "UTF-8";
        }

        HttpGet httpGet = new HttpGet(url);

        loadHeaders(httpGet, headers);

        return request(httpGet, encode);
    }

    private String postInner(String url, Map<String, String> headers, HttpEntity paramsEntity
            , String encode) throws HttpException {
        HttpPost httpPost = new HttpPost(url);

        loadHeaders(httpPost, headers);

        if (paramsEntity != null) {
            httpPost.setEntity(paramsEntity);
        }

        return request(httpPost, encode);
    }

    private String request(HttpUriRequest httpRequest, String encode) throws HttpException {
        try {
            HttpResponse httpResponse = mClient.execute(httpRequest);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            CLog.d(CLog.TAG_HTTP, "statusCode=" + statusCode);

            if (statusCode == 200) {
                InputStream inputStream = httpResponse.getEntity().getContent();
                String response = getResponse(inputStream, encode);
                CLog.d(CLog.TAG_HTTP, "request response: " + response);
                return response;
            } else {
                throw new HttpException("statusCode=" + statusCode + ", error status code");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            throw new HttpException("ClientProtocolException");
        } catch (IllegalStateException e) {
            e.printStackTrace();
            throw new HttpException("IllegalStateException");
        } catch (IOException e) {
            e.printStackTrace();
            throw new HttpException("IOException");
        }
    }

    private String getResponse(InputStream inputStream, String encode) {
        String result = "";
        if (inputStream != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            try {
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                result = new String(outputStream.toByteArray(), encode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private void loadHeaders(HttpRequest request, Map<String, String> headers) {
        if (headers != null && headers.size() != 0) {
            for (String key : headers.keySet()) {
                request.setHeader(key, headers.get(key));
            }
        }
    }

    private HttpEntity getParamsEntity(String params, String encode) throws HttpException {
        if (params == null || params.equals("")) {
            return null;
        }

        byte[] bytes;
        try {
            bytes = params.getBytes(encode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new HttpException("UnsupportedEncodingException");
        }

        HttpEntity entity = new ByteArrayEntity(bytes);
        CLog.d(CLog.TAG_HTTP, "Thread:" + Thread.currentThread().getName()
                + ", params.length=" + bytes.length);
        return entity;
    }

    private HttpEntity getParamsEntity(Map<String, String> params, String encode)
            throws HttpException {
        if (params == null || params.isEmpty()) {
            return null;
        }

        List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramsList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        HttpEntity entity;
        try {
            entity = new UrlEncodedFormEntity(paramsList, encode);
            CLog.d(CLog.TAG_HTTP, "Thread:" + Thread.currentThread().getName()
                    + ", params.length=" + paramsList.size());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new HttpException("UnsupportedEncodingException");
        }
        return entity;
    }
    
    private static class CustomSSLSocketFactory extends SSLSocketFactory {

        SSLContext mSslContext = SSLContext.getInstance("TLS");
        
        public CustomSSLSocketFactory(KeyStore keystore)
                throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException,
                UnrecoverableKeyException {
            super(keystore);
            
            TrustManager trustManager = new X509TrustManager() {
                
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                    
                }
                
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                    
                }
            };
            
            mSslContext.init(null, new TrustManager[] { trustManager }, null);
        }
        
        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
                throws IOException, UnknownHostException {
            return mSslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }
        
        @Override
        public Socket createSocket() throws IOException {
            return mSslContext.getSocketFactory().createSocket();
        }
    }
}
