package com.netgrif.workflow.auth.domain.throwable;

public class UnauthorisedRequestException extends Exception {

	public UnauthorisedRequestException(String message) {
		super(message);
	}
}
