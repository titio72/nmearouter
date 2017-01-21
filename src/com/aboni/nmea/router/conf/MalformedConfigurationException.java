package com.aboni.nmea.router.conf;

public class MalformedConfigurationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1244439763139640511L;

	public MalformedConfigurationException() {
	}

	public MalformedConfigurationException(String arg0) {
		super(arg0);
	}

	public MalformedConfigurationException(Throwable arg0) {
		super(arg0);
	}

	public MalformedConfigurationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public MalformedConfigurationException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
