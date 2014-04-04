package com.tazdingo.platform.session;

import com.tazdingo.core.util.ConstantUtil;

/**
 *
 * @author Cynthia
 */
public class DeviceSession extends Session {

    public DeviceSession(String deviceid) {
        super(deviceid);

    }

    @Override
    public void setExpiredTime() {
        super.expiredTime = System.currentTimeMillis() + ConstantUtil.DEVICE_SESSION_EXTEND;
    }
    
}
