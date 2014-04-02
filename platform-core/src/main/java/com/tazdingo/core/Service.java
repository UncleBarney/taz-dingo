package com.tazdingo.core;

/**
 *
 * @author Cynthia
 */
public abstract class Service implements IService {

    protected WorkQueue workqueue = new WorkQueue();

    public Service() {
    }

    /**
     * Method to pull work request and return response
     *
     * @param request
     * @return
     */
    @Override
    public Response takeRequest(WorkRequest request) {
        workqueue.addWorkRequest(request.getSessionid(), request);
        return excuteRequest(request.getSessionid());
    }

    /**
     * Method to execute request and return response
     *
     * @param sessionid
     * @return
     */
    public abstract Response excuteRequest(String sessionid);
}
