package com.tazdingo.platform.ticket;

import com.tazdingo.core.util.ConstantUtil;

/**
 *
 * @author Cynthia
 */
public class ServiceTGT extends TGT {
    
    public ServiceTGT(String sessionid, String servicename) {
        super(sessionid);
        super.authenticator = servicename;
        super.expiredTime = System.currentTimeMillis() + ConstantUtil.SERVICE_TICKET_ACTIVE_TIME;
    }
    
}
