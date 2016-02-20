package com.cmge.cge.server.api;

public interface ICommandCallback {

    public void onCommandSuccess(IResponse response);

    public void onCommandFailure(int code, String message);
}
