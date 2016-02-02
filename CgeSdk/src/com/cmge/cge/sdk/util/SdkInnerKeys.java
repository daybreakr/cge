
package com.cmge.cge.sdk.util;

public interface SdkInnerKeys {

    // response result
    int RESULT_OK = 1;

    // request
    String CHANNEL_ID = "pid";
    String SDK_VERSION = "v";

    // result
    String RESULT_CODE = "code";
    String RESULT_MSG = "msg";

    // data
    String DATA = "data";
    String SIGN = "sign";

    String PACKAGE_NAME = "gamePackage";
    String PACKAGE_VERSION = "gameVersion";

    String USER_INFO = "cgeUserInfo";
    String MUST_VERIFY_LOGIN = "mustVerifyLogin";
    
    String PURCHASE_INFO = "cgePurchaseInfo";
    
    String ORDER_INFO = "cgeOrderInfo";
    
    String PROTOCOL_VERSION = "protocolVersion";
    
    String VERIFY = "verify";
    
    interface Init {
        String KEY = "key";
        String VALUE = "value";
        String TYPE = "type";
    }
    
    interface User {
        String ID = "uid";
        String NAME = "username";
        String TOKEN = "token";
        String EXTEND = "extend";
    }

    interface Purchase {
        String ORDER_ID = "orderId";
        String CALLBACK_URL = "paycallback";
        String EXTEND = "extend";
    }

    interface Order {
        String ORDER_ID = "orderId";
        String CHANNEL_ORDER_ID = "porderId";
        String STATUS = "status";

        String STATUS_SUCCESS = "1";
        String STATUS_SUBMITTED = "2";
        String STATUS_FAILED = "0";
    }
}
