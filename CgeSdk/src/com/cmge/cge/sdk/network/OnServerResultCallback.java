package com.cmge.cge.sdk.network;

public interface OnServerResultCallback {
    int RESULT_ERROR = 0; // equivalent to server's result code
    int RESULT_OK = 1; // equivalent to server's result code

    void onResult(int resultCode, final String msg);
}
