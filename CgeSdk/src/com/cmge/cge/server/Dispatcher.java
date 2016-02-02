package com.cmge.cge.server;

import android.text.TextUtils;

import com.cmge.cge.sdk.util.CLog;

import java.util.Map;

public class Dispatcher implements IDispatcher {
    
    private static final String TAG = "Cge." + Dispatcher.class.getSimpleName();

    private String mServerUrl = null;
    private IDispatchCallback mCallback = null;
    
    private IHttp mHttp = null;

    public Dispatcher(String serverUrl, IDispatchCallback dispatchCallback) {
        setServerUrl(serverUrl);
        mCallback = dispatchCallback;
        
        mHttp = Http.getInstance(Http.TYPE_DEFAULT);
    }
    
    public void setServerUrl(String url) {
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        
        mServerUrl = url;
    }
    
    @Override
    public void execute(ICommand command) {
        if (!checkCommand(command)) {
            // execute failed, handle failure
            notifyDispatchFailed(command);
            return;
        }
        
        final CommandSendWrapper commandWrapper = new CommandSendWrapper(command);
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                send(commandWrapper);
            }
        }).start();
    }
    
    private void send(CommandSendWrapper command) {
        String url = mServerUrl + command.getAction();
        Map<String, String> params = command.toParameters();
        
        String response = null;
        try {
            response = mHttp.post(url, params);
        } catch (Exception e) {
            // failed to send command
            CLog.w(TAG, e.getMessage());
        }

        if (response != null) {
            // send successful, handle successful
            notifyDispatchSuccess(response, command.getCommand());
        } else {
            int nextRetryDelay = command.nextRetryDelay();
            
            if (nextRetryDelay < 0) {
                // send failed, handle failure
                notifyDispatchFailed(command.getCommand());
                return;
            } else if (nextRetryDelay > 0) {
                // delay re-send
                try {
                    Thread.sleep(nextRetryDelay);
                } catch (InterruptedException e) {
                    CLog.w(TAG, "waiting have been interrupted, send immediately");
                }
            }
            
            // retry command
            command.retry();
            send(command);
        }
    }
    
    private boolean checkCommand(ICommand command) {
        if (command == null) {
            CLog.w(TAG, "check command failed. command is null");
            return false;
        }
        
        if (TextUtils.isEmpty(command.getAction())) {
            CLog.w(TAG, "check command failed. no action");
            return false;
        }
        
        if (command.getRetryStrategy() == null) {
            CLog.w(TAG, "check command no retry strategy");
            // use the default retry strategy
        }
        
        return true;
    }
    
    private void notifyDispatchSuccess(String response, ICommand command) {
        if (mCallback != null) {
            mCallback.onDispatchComplete(true, response, command);
        }
    }
    
    private void notifyDispatchFailed(ICommand command) {
        if (mCallback != null) {
            // XXX: send failed, should provide failure message?
            mCallback.onDispatchComplete(false, null, command);
        }
    }
}
