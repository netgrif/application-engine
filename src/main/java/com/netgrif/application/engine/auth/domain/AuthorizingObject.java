package com.netgrif.application.engine.auth.domain;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The enum of all possible authorizing objects, that are used for creating authority objects. Authorizing object is a
 * term that defines the goal of the authority, e.g. what kind of system process is protected via an authority created
 * using given authorizing object.
 * */
public enum AuthorizingObject {
    DEFAULT,
    PROCESS_UPLOAD,
    PROCESS_VIEW_ALL,
    PROCESS_VIEW_OWN,
    PROCESS_DELETE_ALL,
    PROCESS_DELETE_OWN,
    FILTER_UPLOAD,
    FILTER_DELETE_ALL,
    FILTER_DELETE_OWN,
    USER_CREATE,
    USER_DELETE_ALL,
    USER_DELETE_SELF,
    USER_EDIT_ALL,
    USER_EDIT_SELF,
    USER_VIEW_ALL,
    USER_VIEW_SELF,
    GROUP_CREATE,
    GROUP_DELETE_SELF,
    GROUP_DELETE_ALL,
    GROUP_ALL_ADD_USER,
    GROUP_OWN_ADD_USER,
    GROUP_ALL_REMOVE_USER,
    GROUP_OWN_REMOVE_USER,
    GROUP_VIEW_ALL,
    GROUP_VIEW_OWN,
    GROUP_MEMBERSHIP_SELF,
    ROLE_ASSIGN_TO_USER,
    ROLE_REMOVE_FROM_USER,
    AUTHORITY_CREATE,
    AUTHORITY_DELETE,
    AUTHORITY_VIEW,
    CASE_VIEW_ALL,
    CASE_VIEW_OWN,
    CASE_CREATE,
    CASE_DELETE,
    CASE_DATA_GET_ALL,
    TASK_RELOAD,
    TASK_ASSIGN,
    TASK_FINISH,
    TASK_CANCEL,
    TASK_DELEGATE,
    TASK_SAVE_DATA,
    ELASTIC_REINDEX,
    LDAP_GROUP_GET_ALL,
    LDAP_GROUP_ASSIGN_ROLES;

    public static List<String> stringValues() {
        return Arrays.stream(AuthorizingObject.values()).map(Enum::name).collect(Collectors.toList());
    }
}
