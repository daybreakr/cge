package com.cmge.cge.sdk.callback;

public interface CgeCallback {

    enum InitResult {
        SUCCEED,
        FAILED
    }

    enum LoginResult {
        SUCCEED,
        FAILED,
        CANCELED
    }

    enum LogoutResult {
        SUCCEED,
        FAILED,
        CANCELED
    }

    enum SwitchAccountResult {
        SUCCEED,
        FAILED,
        CANCELED
    }
    
    enum PurchaseResult {
        SUCCEED,
        SUBMITTED,
        FAILED,
        CANCELED,
        NOT_LOGIN,
        TOKEN_EXPIRED
    }

    enum ExitResult {
        QUIT,
        RESUME,
        NO_EXIT_PAGE
    }
}
