package com.microsoft.ocp.storage.drainer.config;

public class ParseException extends Exception {

	private static final long serialVersionUID = 8406161143573809145L;

	public ParseException() {
	}

	public ParseException(String msg) {
		super(msg);
	}

	public ParseException(Throwable t) {
		super(t);
	}

	public ParseException(String msg, Throwable t) {
		super(msg, t);
	}
}
