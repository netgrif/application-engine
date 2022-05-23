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
    PROCESS_UPLOAD,
    PROCESS_VIEW_ALL,
    PROCESS_VIEW_MY,
    PROCESS_DELETE_MY,
    PROCESS_DELETE_ALL,
    FILTER_UPLOAD,
    FILTER_DELETE_MY,
    FILTER_DELETE_ALL,
    USER_CREATE,
    USER_DELETE_ALL,
    USER_DELETE_MY,
    USER_EDIT_ALL,
    USER_EDIT_MY,
    USER_VIEW_ALL,
    USER_VIEW_MY,
    GROUP_CREATE,
    GROUP_DELETE_MY,
    GROUP_DELETE_ALL,
    GROUP_ALL_ADD_USER,
    GROUP_MY_ADD_USER,
    GROUP_ALL_REMOVE_USER,
    GROUP_MY_REMOVE_USER,
    GROUP_VIEW_ALL,
    GROUP_VIEW_MY,
    GROUP_MEMBEROF_VIEW,
    ROLE_ASSIGN_TO_USER,
    ROLE_REMOVE_FROM_USER,
    AUTHORITY_CREATE,
    AUTHORITY_DELETE,
    AUTHORITY_VIEW_ALL,
    CASE_VIEW_ALL,
    CASE_VIEW_MY,
    CASE_CREATE,
    CASE_DELETE,
    CASE_DATA_GET_ALL,
    TASK_RELOAD,
    TASK_ASSIGN,
    TASK_FINISH,
    TASK_CANCEL,
    TASK_DELEGATE,
    TASK_SAVE_DATA;

    public static List<String> stringValues() {
        return Arrays.stream(AuthorizingObject.values()).map(Enum::name).collect(Collectors.toList());
    }
}
