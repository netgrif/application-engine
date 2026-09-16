package com.netgrif.application.engine.objects.auth.constants;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The enum of all possible authorizing objects, that are used for creating authority objects. Authorizing object is a
 * term that defines the goal of the authority, e.g. what kind of system process is protected via an authority created
 * using given authorizing object.
 * */
public enum AuthorizingObject {
    ADMIN,
    USER,
    SYSTEMADMIN,
    ANONYMOUS,
    PROCESS_UPLOAD,
    PROCESS_DOWNLOAD,
    PROCESS_VIEW,
    PROCESS_DELETE,
    USER_CREATE,
    USER_DELETE,
    USER_EDIT_ALL,
    USER_EDIT_SELF,
    USER_VIEW_ALL,
    USER_VIEW_SELF,
    GROUP_CREATE,
    GROUP_DELETE_OWN,
    GROUP_DELETE_ALL,
    GROUP_ALL_ADD_USER,
    GROUP_OWN_ADD_USER,
    GROUP_ALL_REMOVE_USER,
    GROUP_OWN_REMOVE_USER,
    GROUP_VIEW_ALL,
    GROUP_VIEW_OWN,
    GROUP_MEMBERSHIP_SELF,
    ROLE_ASSIGN_TO_USER,
    AUTHORITY_ASSIGN_TO_USER,
    AUTHORITY_CREATE,
    AUTHORITY_DELETE,
    AUTHORITY_VIEW,
    ELASTIC_REINDEX;

    public static List<String> stringValues() {
        return Arrays.stream(AuthorizingObject.values()).map(Enum::name).collect(Collectors.toList());
    }
}
