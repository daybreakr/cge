package com.cmge.cge.server;

public class CgeResponseActionFactory {

    public static ICgeResponseAction getResponseAction(int responseCode) {
        ICgeResponseAction action = null;
        switch (responseCode) {
            case Protocols.RESULT_OK:
                action = new CgeSuccessResponseAction();
                break;
                
            case Protocols.RESULT_ERROR:
                action = new CgeFailureResponseAction();
                break;
                
            case Protocols.RESULT_REDIRECT:
                action = new CgeRedirectResponseAction();
                break;

            default:
                // TODO: default response action
                break;
        }
        
        return action;
    }
}
