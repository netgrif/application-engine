package com.netgrif.application.engine.auth.web.requestbodies;

import lombok.Data;

import java.util.List;

/**
 * Represents the request body for searching users with specific criteria.
 */
@Data
public class UserSearchRequestBody {

    /**
     * The ID of the realm in which to search for users.
     * This field is used to scope the search to a particular multi-tenancy realm.
     */
    private String realmId;

    /**
     * A full-text search string to filter the users.
     * The search is typically applied to user attributes like name, username, or email.
     */
    private String fulltext;

    /**
     * A list of roles the users must have.
     * The search will include users who possess all of the specified roles.
     */
    private List<String> roles;

    /**
     * A list of roles the users must not have.
     * The search will exclude users who possess any of the specified roles.
     */
    private List<String> negativeRoles;
}