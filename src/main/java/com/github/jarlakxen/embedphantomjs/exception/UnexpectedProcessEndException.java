package com.github.jarlakxen.embedphantomjs.exception;

public class UnexpectedProcessEndException extends Exception {
	private static final long serialVersionUID = 6312464814429570001L;
	
	public UnexpectedProcessEndException() {
		
	}
	
	public UnexpectedProcessEndException(Throwable throwable) {
		super(throwable);
	}

}
