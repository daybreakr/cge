package com.cmge.cge.server;

public class LoginVerifyCommand extends CgeCommand {

    private static final String ACTION = "loadEvent.action";
    
    public LoginVerifyCommand(CgeRequest request) {
        super(ACTION, request, new LoginVerifyResponse());
        // TODO Auto-generated constructor stub
    }

}
