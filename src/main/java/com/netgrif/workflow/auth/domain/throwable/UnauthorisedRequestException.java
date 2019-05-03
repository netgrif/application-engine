package com.netgrif.workflow.auth.domain.throwable;

public class UnauthorisedRequestException extends Exception {

	public UnauthorisedRequestException(String endpoint) {
		super("Request to '"+endpoint+"' endpoint was not authorised");
	}
}
