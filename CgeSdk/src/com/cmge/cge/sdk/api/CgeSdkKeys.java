
package com.cmge.cge.sdk.api;

public interface CgeSdkKeys {

    interface Login {
        String SERVER_ID = "serverId";
    }

    interface Purchase {
        String SERVER_ID = "serverId";
        String SERVER_NAME = "serverName";
        
        String PRODUCT_ID = "productId";
        String PRODUCT_NAME = "prodName";
        String PRODUCT_DESC = "prodDesc";
        String PRICE = "price";
        String COUNT = "count";
        String EXCHANGE_RATE = "exchangeRate";

        String USER_BALANCE = "userBalance";
        String USER_VIP = "userVip";

        String ROLE_ID = "roleId";
        String ROLE_NAME = "roleName";
        String ROLE_LEVEL = "roleLevel";
        String ROLE_PARTY_NAME = "rolePartyName";

        String CUSTOM = "custom";
    }
    
    interface Submit {
        String SERVER_ID = "serverId";
        String SERVER_NAME = "serverName";
        
        String GAME_USER_ID = "gameUserId";
        
        String ROLE_ID = "roleId";
        String ROLE_NAME = "roleName";
        String ROLE_LEVEL = "roleLevel";
        
        String TYPE = "type";
    }
    
    public String SUBMIT_TYPE_ENTER_SERVER = "enterServer";
    public String SUBMIT_TYPE_CREATE_ROLE = "createRole";
    public String SUBMIT_TYPE_LEVEL_UP = "levelUp";
}
