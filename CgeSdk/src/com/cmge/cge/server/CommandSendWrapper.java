package com.cmge.cge.server;

import com.cmge.cge.sdk.util.CLog;

import java.util.Map;

public class CommandSendWrapper implements ICommand {
    
    private static final String TAG = "Cge." + CommandSendWrapper.class.getSimpleName();
    
    private ICommand mCommand;
    private String mAction;
    private Map<String, String> mParameters;
    private IRetryStrategy mRetryStrategy;
    
    private int mRetry = 0;
    
    public CommandSendWrapper(ICommand command) {
        if (!init(command)) {
            return;
        }
        
        mCommand = command;
    }
    
    public CommandSendWrapper(CommandSendWrapper commandWrapper) {
        if (!init(commandWrapper)) {
            return;
        }
        
        mCommand = commandWrapper.getCommand();
    }
    
    private boolean init(ICommand command) {
        if (command == null) {
            CLog.w(TAG, "construct failed. command is null");
            return false;
        }
        mAction = command.getAction();
        mParameters = command.getRequestParameters();
        mRetryStrategy = command.getRetryStrategy();
        if (mRetryStrategy == null) {
            mRetryStrategy = new DefaultRetryStrategy();
        }
        
        mRetry = 0;
        return true;
    }
    
    @Override
    public String getAction() {
        return mAction;
    }
    
    @Override
    public Map<String, String> getRequestParameters() {
        return mParameters;
    }
    
    @Override
    public IRetryStrategy getRetryStrategy() {
        return mRetryStrategy;
    }
    
    public int retry() {
        return ++mRetry;
    }
    
    public int nextRetryDelay() {
        return mRetryStrategy.nextRetryDelay(mRetry);
    }
    
    public ICommand getCommand() {
        return mCommand;
    }
}
