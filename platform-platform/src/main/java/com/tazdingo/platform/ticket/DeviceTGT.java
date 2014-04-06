package com.tazdingo.platform.ticket;

import com.tazdingo.ticket.TGT;
import com.tazdingo.core.util.ConstantUtil;

/**
 *
 * @author Cynthia
 */
public class DeviceTGT extends TGT {

    public DeviceTGT(String sessionid, String deviceid) {
        super(sessionid);
        super.authenticator = deviceid;
        super.expiredTime = System.currentTimeMillis() + ConstantUtil.DEVICE_TICKET_ACTIVE_TIME;
    }
    
}
