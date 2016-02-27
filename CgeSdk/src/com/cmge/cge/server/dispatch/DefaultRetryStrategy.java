package com.cmge.cge.server.dispatch;

public class DefaultRetryStrategy implements IRetryStrategy {

    private static final int MAX_RETRY = 3;
    
    @Override
    public int nextRetryDelay(int tries) {
        return tries < MAX_RETRY ? 0 : -1;
    }

    public int getMaxRetry() {
        return MAX_RETRY;
    }
}
