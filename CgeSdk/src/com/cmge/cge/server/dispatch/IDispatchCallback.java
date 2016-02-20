package com.cmge.cge.server.dispatch;

import com.cmge.cge.server.api.Command;

public interface IDispatchCallback {

    public void onDispatchComplete(boolean success, String response, Command command);
}
