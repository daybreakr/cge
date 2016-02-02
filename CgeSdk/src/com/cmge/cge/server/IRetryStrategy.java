package com.cmge.cge.server;

public interface IRetryStrategy {

    public int nextRetryDelay(int lastRetryTimes);
}
