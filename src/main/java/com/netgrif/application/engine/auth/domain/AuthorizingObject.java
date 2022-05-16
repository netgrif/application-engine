package com.netgrif.application.engine.auth.domain;

/**
 * The enum of all possible authorizing objects, that are used for creating authority objects. Authorizing object is a
 * term that defines the goal of the authority, e.g. what kind of system process is protected via an authority created
 * using given authorizing object.
 * */
public enum AuthorizingObject {
    PROCESS_UPLOAD,
    PROCESS_DELETE,
    FILTER_UPLOAD,
    FILTER_DELETE,
    USER_CREATE,
    USER_DELETE,
    USER_EDIT,
    GROUP_CREATE,
    GROUP_DELETE,
    GROUP_ADD_USER,
    GROUP_REMOVE_USER,
    GROUP_VIEW_ALL,
    ROLE_CREATE, // refactor for user "assign"
    ROLE_DELETE, // refactor for user "deassign"
    AUTHORITY_CREATE,
    AUTHORITY_DELETE
}
