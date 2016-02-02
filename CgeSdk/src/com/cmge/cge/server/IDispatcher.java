package com.cmge.cge.server;

public interface IDispatcher {

    public void execute(ICommand command);
    
    interface IDispatchCallback {
        public void onDispatchComplete(boolean success, String result, ICommand command);
    }
}
