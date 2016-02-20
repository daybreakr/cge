package com.cmge.cge.server.dispatch;

import com.cmge.cge.sdk.util.CLog;
import com.cmge.cge.server.api.Command;
import com.cmge.cge.server.http.Http;
import com.cmge.cge.server.http.IHttp;

public class Dispatcher implements IDispatcher {
    
    private static final String TAG = "Cge." + Dispatcher.class.getSimpleName();

    private String mServerUrl;
    
    private IDispatchCallback mCallback;
    
    private IHttp mHttp;

    public Dispatcher(String serverUrl, IDispatchCallback callback) {
        setServerUrl(serverUrl);
        mCallback = callback;
        
        mHttp = Http.getInstance(Http.TYPE_DEFAULT);
    }
    
    @Override
    public void dispatch(final Command command) {
        if (command == null) {
            return;
        }
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                send(command);                
            }
        }).start();
    }
    
    private void send(Command command) {
        String response = null;
        try {
            response = mHttp.post(mServerUrl + command.getAction(), command.getRequestArgs());
        } catch (Exception e) {
            // failed to send command
            CLog.w(TAG, e.getMessage());
        }

        command.tries++;

        if (response != null) { // dispatch successful
            dispatchSuccess(response, command);
        } else { //  // send failed
            int nextRetryDelay = command.getRetryStrategy().nextRetryDelay(command.tries);
            
            if (nextRetryDelay < 0) { // dispatch failed
                dispatchFailed(command);
            } else if (nextRetryDelay > 0) { // retry
                // delay re-send
                try {
                    Thread.sleep(nextRetryDelay);
                } catch (InterruptedException e) {
                    CLog.w(TAG, "waiting have been interrupted, send immediately");
                }
            }
            
            // retry command
            send(command);
        }
    }

    private void dispatchSuccess(String response, Command command) {
        if (mCallback != null) {
            mCallback.onDispatchComplete(true, response, command);
        }
    }
    
    private void dispatchFailed(Command command) {
        if (mCallback != null) {
            mCallback.onDispatchComplete(false, null, command);
        }
    }
    
    private void setServerUrl(String url) {
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        
        mServerUrl = url;
    }
}
