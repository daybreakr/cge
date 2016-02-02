package com.cmge.cge.server;

public class Protocols {

    /** Client version */
    public static final String V = "v";
    /** Channel ID */
    public static final String PID = "pid";
    /** Response code */
    public static final String CODE = "code";
    /** Response message */
    public static final String MSG = "msg";
    /** Request data field */
    public static final String DATA = "data";
    /** Request sign field */
    public static final String SIGN = "sign";
    
    public static final int RESULT_OK = 1;
    public static final int RESULT_ERROR = 0;
    public static final int RESULT_REDIRECT = 2;
}
