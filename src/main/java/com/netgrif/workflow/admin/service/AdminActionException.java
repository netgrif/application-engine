package com.netgrif.workflow.admin.service;

public class AdminActionException extends Exception {

    AdminActionException(Throwable throwable) {
        super("Could not execute action", throwable);
    }
}