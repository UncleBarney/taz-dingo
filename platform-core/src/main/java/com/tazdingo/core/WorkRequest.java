package com.tazdingo.core;

/**
 *
 * @author Cynthia
 */
public abstract class WorkRequest {

    private Request request;

    public WorkRequest(Request request) {
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }

    public abstract String getSessionid();
}
