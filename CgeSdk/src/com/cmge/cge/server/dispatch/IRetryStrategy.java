package com.cmge.cge.server.dispatch;

public interface IRetryStrategy {

    public int nextRetryDelay(int tries);
}
