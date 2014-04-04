package com.tazdingo.platform.session;

import com.tazdingo.core.util.ConstantUtil;

/**
 *
 * @author Cynthia
 */
public class UserSession extends Session {

    public UserSession(String username, String deviceid) {
        super(username + ConstantUtil.FROM + deviceid);

    }

    @Override
    public void setExpiredTime() {
        super.expiredTime = System.currentTimeMillis() + ConstantUtil.USER_SESSION_EXTEND;
    }
    
}
