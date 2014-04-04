package com.tazdingo.platform.session;

import com.tazdingo.core.util.ConstantUtil;

/**
 *
 * @author Cynthia
 */
public class ServiceSession extends Session {

    public ServiceSession(String serviceid) {
        super(serviceid);
    }

    @Override
    public void setExpiredTime() {
        super.expiredTime = System.currentTimeMillis() + ConstantUtil.SERVICE_SESSION_EXTEND;
    }

    @Override
    public void extendExpiredTime() {
        setExpiredTime();
    }
    
}
