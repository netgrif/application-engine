package com.netgrif.application.engine.objects.dto.request.user;

import java.io.Serializable;
import java.util.List;

/**
 * Represents the request body for searching users with specific criteria.
 *
 * @param fulltext      A full-text search string to filter the users.
 *                      The search is typically applied to user attributes like name, username, or email.
 * @param realmId       The ID of the realm in which to search for users.
 *                      This field is used to scope the search to a particular multi-tenancy realm.
 * @param roles         A list of roles the users must have.
 *                      The search will include users who possess all the specified roles.
 */
public record UserSearchRequestBody(String realmId, String fulltext, List<String> roles
) implements Serializable {
}