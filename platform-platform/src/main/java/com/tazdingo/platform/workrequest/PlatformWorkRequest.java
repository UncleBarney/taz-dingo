package com.tazdingo.platform.workrequest;

import com.tazdingo.core.WorkRequest;
import com.tazdingo.core.Request;
import com.tazdingo.platform.session.ISession;

/**
 *
 * @author Cynthia
 */
public class PlatformWorkRequest extends WorkRequest {

    private ISession session;
    private String prestep;

    public PlatformWorkRequest(ISession session, Request request, String prestep) {
        super(request);
        this.session = session;
        this.prestep = prestep;
    }

    public ISession getSession() {
        return session;
    }

    public String getPrestep() {
        return prestep;
    }

    @Override
    public String getSessionid() {
        return this.session.getSessionID();
    }
    
}
