package com.cmge.cge.server;

import java.util.Map;

public interface ICommand {

    public String getAction();
    
    public Map<String, String> getRequestParameters();
    
    public IRetryStrategy getRetryStrategy();
}
