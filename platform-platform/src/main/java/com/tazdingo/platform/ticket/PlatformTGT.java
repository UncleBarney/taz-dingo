package com.tazdingo.platform.ticket;

import com.tazdingo.ticket.TGT;
import com.tazdingo.core.util.ConstantUtil;

/**
 *
 * @author Cynthia
 */
public class PlatformTGT extends TGT {

    public PlatformTGT(String sessionid, String platformname) {
        super(sessionid);
        super.authenticator = platformname;
        super.expiredTime = System.currentTimeMillis() + ConstantUtil.PLATFORM_TICKET_ACTIVE_TIME;
    }
    
}
