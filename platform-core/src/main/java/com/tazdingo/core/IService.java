package com.tazdingo.core;

/**
 *
 * @author Cynthia
 */
public interface IService {

    /**
     * Method to pull work request and return response
     *
     * @param request
     * @return
     */
    public Response takeRequest(WorkRequest request);
}
