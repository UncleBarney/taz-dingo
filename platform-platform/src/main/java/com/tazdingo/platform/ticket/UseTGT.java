package com.tazdingo.platform.ticket;

import com.tazdingo.ticket.TGT;
import com.tazdingo.core.util.ConstantUtil;

/**
 *
 * @author Cynthia
 */
public class UseTGT extends TGT {

    public UseTGT(String sessionid, String deviceid, String username) {
        super(sessionid);
        super.authenticator = username + ConstantUtil.FROM + deviceid;
        super.expiredTime = System.currentTimeMillis() + ConstantUtil.USER_TICKET_ACTIVE_TIME;
    }
    
}
