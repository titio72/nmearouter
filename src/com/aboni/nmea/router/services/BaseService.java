package com.aboni.nmea.router.services;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class BaseService
 */
@SuppressWarnings("serial")
public abstract class BaseService extends HttpServlet {
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BaseService() {
        super();
    }
    
    protected abstract void doIt(ServiceConfig config, ServiceOutput response);

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    	doIt(new ServletRequestServiceConfig(request), new ServletResponseOutput(response));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		doGet(request, response);
	}

}
