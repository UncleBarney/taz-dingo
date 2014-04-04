/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tazdingo.platform.session;

import java.security.SecureRandom;
import java.util.Random;
import javax.crypto.SecretKey;
import com.tazdingo.core.util.Encryption;

/**
 *
 * @author Cynthia
 */
public abstract class Session implements ISession {

    private final String requestSender;
    private final String sessionID;
    private String stepID;
    private final Long createdTime;
    protected Long expiredTime;

    public Session(String requestSender) {
        this.createdTime = System.currentTimeMillis();
        SecureRandom r = new SecureRandom();
        this.sessionID = String.valueOf(Math.abs(r.nextLong()));
        this.stepID = String.valueOf(Math.abs(r.nextLong()));
        this.requestSender = requestSender;
        this.stepID = Encryption.keyToString(Encryption.generateSecretKey(this.stepID));
        setExpiredTime();
    }

    @Override
    public String getRequestSender() {
        return requestSender;
    }

    @Override
    public String getSessionID() {
        return sessionID;
    }

    @Override
    public String getStepID() {
        return stepID;
    }

    @Override
    public Long getCreatedTime() {
        return createdTime;
    }

    @Override
    public Long getExpiredTime() {
        return expiredTime;
    }

    public void setStepID() {
        this.stepID = generatedNewStepID();
    }

    protected abstract void setExpiredTime();

    private String generatedNewStepID() {
        Random r = new Random();
        Long step = r.nextLong();
        String stepid = String.valueOf(step);
        SecretKey shortkey = Encryption.generateSecretKey(stepid);
        return Encryption.keyToString(shortkey);
    }

    @Override
    public void disableSession() {
        expiredTime = System.currentTimeMillis();
    }
    
     @Override
    public void extendExpiredTime() {
        setStepID();
        setExpiredTime();
    }
}
