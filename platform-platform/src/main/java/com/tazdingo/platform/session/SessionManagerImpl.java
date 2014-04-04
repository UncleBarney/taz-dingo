package com.tazdingo.platform.session;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Cynthia
 */
public class SessionManagerImpl implements ISessionManager {

    private final Map<String, ISession> sessionlist;

    public SessionManagerImpl() {
        sessionlist = new HashMap<>();
    }

    @Override
    public ISession getSession(String sessionid) {
        return sessionlist.get(sessionid);
    }

    @Override
    public void addORUpdateSession(ISession session) {
        sessionlist.put(session.getSessionID(), session);
    }

    @Override
    public boolean validateSession(ISession session) {
        return (System.currentTimeMillis() - session.getExpiredTime()) < 0;
    }
    
}
