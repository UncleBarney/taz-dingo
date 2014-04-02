package com.tazdingo.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Cynthia
 */
public interface Gateway {

    /**
     * Method to take HttpServletRequest and HttpServletResponse and return
     * HttpServletResponse
     *
     * @param request
     * @param response
     * @return
     */
    public HttpServletResponse takeRequest(HttpServletRequest request, HttpServletResponse response);
}
