package com.cmge.cge.server;

import android.text.TextUtils;

import com.cmge.cge.sdk.util.CLog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

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

public class ApacheHttp implements IHttp {
    
    private static final String TAG = "Cge." + ApacheHttp.class.getSimpleName();
    
    private static final int CONSTRUCT_TIMEOUT = 30 * 1000;
    private static final int CONNECTION_TIMEOUT = 30 * 1000;
    private static final int SOCKET_TIMEOUT = 30 * 1000;
    
    private static final String DEFAULT_ENCODE = HTTP.UTF_8;
    
    private static HttpClient sClient;
    
    public ApacheHttp() {
        sClient = constructHttpClient();
    }
    
    @Override
    public String get(String url, Map<String, String> params) throws HttpException {
        return get(url, params, null);
    }
    
    @Override
    public String get(String url, Map<String, String> params, String encode) throws HttpException {
        if (!checkUrl(url)) {
            return null;
        }

        encode = defaultEncode(encode);
        
        url = bindGetParameters(url, params);
        
        HttpGet get = new HttpGet(url);
        
        return request(get, encode);
    }
    
    @Override
    public String post(String url, Map<String, String> params) throws HttpException {
        return post(url, params, null);
    }
    
    @Override
    public String post(String url, Map<String, String> params, String encode) throws HttpException {
        if (!checkUrl(url)) {
            return null;
        }
        
        encode = defaultEncode(encode);

        HttpPost post = new HttpPost(url);
        
        HttpEntity entity = bindPostParameters(params, encode);
        if (entity != null) {
            post.setEntity(entity);
        }
        
        return request(post, encode);
    }
    
    private HttpClient constructHttpClient() {
        HttpClient client = null;
        
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
        ConnManagerParams.setTimeout(params, CONSTRUCT_TIMEOUT);
        // set the connection timeout
        HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
        // set the socket timeout
        HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);

        /* supported for http and https */
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        
        SSLSocketFactory sslFactory = constructSSLFactory();
        if (sslFactory == null) {
            CLog.e(TAG, "construct SSLSocketFactory failed");
        }
        schemeRegistry.register(new Scheme("https", sslFactory, 443));

        /* use the thread-safe connection manager for creating the http client */
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        
        client = new DefaultHttpClient(cm, params);
        
        return client;
    }
    
    private SSLSocketFactory constructSSLFactory() {
        SSLSocketFactory sslFactory = null;
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            sslFactory = new CustomSSLSocketFactory(trustStore);
            sslFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            e.printStackTrace();
            CLog.e(TAG, e.getMessage());
        }
        
        return sslFactory;
    }
    
    private String request(HttpUriRequest request, String responseEncode) throws HttpException {
        if (sClient == null) {
            throw new HttpException("HttpClient is null, init failed");
        }
        
        try {
            HttpResponse response = sClient.execute(request);
            
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String content = readEntity(response.getEntity(), responseEncode);
                return content;
            } else {
                throw new HttpException("error status code, " + statusCode);
            }
        } catch (ClientProtocolException e) {
            throw new HttpException("ClientProtocolException" + e.getMessage());
        } catch (IOException e) {
            throw new HttpException("IOException" + e.getMessage());
        }
    }
    
    private String bindGetParameters(String url, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }
        
        StringBuilder builder = new StringBuilder(url);
        if (params.size() > 0) {
            builder.append("?");
        }
        for (String key : params.keySet()) {
            builder.append(key).append("=").append(params.get(key));
        }
        return builder.toString();
    }
    
    private HttpEntity bindPostParameters(Map<String, String> params, String encode) throws HttpException {
        if (params == null || params.isEmpty()) {
            return null;
        }

        List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
        for (String key : params.keySet()) {
            paramsList.add(new BasicNameValuePair(key, params.get(key)));
        }
        
        HttpEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(paramsList, encode);
        } catch (UnsupportedEncodingException e) {
            throw new HttpException("UnsupportedEncodingException");
        }
        return entity;
    }
    
    private String readEntity(HttpEntity entity, String encode) throws HttpException {
        String content = null;
        try {
            InputStream input = entity.getContent();
            ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = input.read(buffer)) > 0) {
                byteArrayOutput.write(buffer, 0, len);
            }
            content = byteArrayOutput.toString(encode);
        } catch (IllegalStateException e) {
            throw new HttpException("IllegalStateException while read entity");
        } catch (IOException e) {
            throw new HttpException("IOException while read entity");
        }
        return content;
    }
    
    private boolean checkUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            CLog.w(TAG, "check url failed. url is null");
            return false;
        }
        
        if (url.startsWith("http") || url.startsWith("https")) {
            CLog.w(TAG, "check url failed. not using http protocol, " + url);
            return false;
        }
        
        return true;
    }
    
    private String defaultEncode(String encode) {
        return TextUtils.isEmpty(encode) ? DEFAULT_ENCODE : encode;
    }

    private static class CustomSSLSocketFactory extends SSLSocketFactory {

        SSLContext mSSLContext = SSLContext.getInstance("TLS");

        public CustomSSLSocketFactory(KeyStore keystore) throws NoSuchAlgorithmException,
                KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(keystore);

            TrustManager trustManager = new CustomTrustManager();
            mSSLContext.init(null, new TrustManager[] { trustManager}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
                throws IOException, UnknownHostException {
            return mSSLContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return mSSLContext.getSocketFactory().createSocket();
        }
    }
    
    private static class CustomTrustManager implements X509TrustManager {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
        
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            // do nothing
            
        }
        
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            // do nothing
        }
    }
}
