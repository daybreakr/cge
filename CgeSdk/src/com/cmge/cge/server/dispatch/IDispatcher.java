package com.cmge.cge.server.dispatch;

import com.cmge.cge.server.api.Command;

public interface IDispatcher {
    
    public void dispatch(Command command);
}
