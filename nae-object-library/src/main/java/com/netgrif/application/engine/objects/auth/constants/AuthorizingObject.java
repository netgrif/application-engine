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
    GROUP_CREATE,
    GROUP_CREATE_OWN,
    GROUP_DELETE,
    GROUP_DELETE_OWN,
    GROUP_ADD_USER,
    GROUP_ADD_USER_OWN,
    GROUP_REMOVE_USER,
    GROUP_REMOVE_USER_OWN,
    GROUP_ADD_SUBGROUP,
    GROUP_ADD_SUBGROUP_OWN,
    GROUP_REMOVE_SUBGROUP,
    GROUP_REMOVE_SUBGROUP_OWN,
    ROLE_ASSIGN_TO_USER,
    ROLE_ASSIGN_TO_GROUP,
    AUTHORITY_ASSIGN_TO_USER,
    AUTHORITY_ASSIGN_TO_GROUP,
    AUTHORITY_CREATE,
    AUTHORITY_DELETE,
    ELASTIC_REINDEX;

    public static List<String> stringValues() {
        return Arrays.stream(AuthorizingObject.values()).map(Enum::name).collect(Collectors.toList());
    }
}
