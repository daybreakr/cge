package com.cmge.cge.server;

public class DefaultRetryStrategy implements IRetryStrategy {

    private static final int MAX_RETRY_TIMES = 3;
    
    @Override
    public int nextRetryDelay(int lastRetryTimes) {
        return lastRetryTimes < MAX_RETRY_TIMES ? 0 : -1;
    }

    public int getMaxRetryTimes() {
        return MAX_RETRY_TIMES;
    }
}
