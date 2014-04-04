package com.tazdingo.platform.session;

/**
 *
 * @author Cynthia
 */
public interface ISessionManager {

    public ISession getSession(String sessionid);

    public void addORUpdateSession(ISession session);

    public boolean validateSession(ISession session);
    
}
