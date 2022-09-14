# Authority management
Netgrif Application Engine implements authority objects to manage access
to resources, to protect resources from unauthorized access. Then these 
authorities can be assigned to users to provide access to those secured
resources.

## Authorizing objects
Authorizing objects are Java enum values, that represent authorities,
that are needed to access a resource or use an action. E.g. to be able to import new 
process (Petri Net) into the application, the user must have authority 
created from `AuthorizingObject.PROCESS_UPLOAD` authorizing object.

These authorizing objects are predefined, and they are used to create the 
`Authority` objects/entities at application startup. There followings are predefined:

- `PROCESS_UPLOAD`- to import new process
- `PROCESS_VIEW_ALL`- to retrieve all the processes imported by any user
- `PROCESS_VIEW_OWN`- to retrieve only processes imported by logged user
- `PROCESS_DELETE_ALL`- to delete processes imported by any user
- `PROCESS_DELETE_OWN`- to delete processes imported by logged user
- `FILTER_UPLOAD`- to upload filter
- `FILTER_DELETE_OWN`- to delete filter imported by logged user
- `FILTER_DELETE_ALL`- to delete filter imported by any user
- `USER_CREATE`- to invite or create user
- `USER_DELETE`- to remove user
- `USER_EDIT_ALL`- to edit any user
- `USER_EDIT_SELF`- to edit only logged user
- `USER_VIEW_ALL`- to retrieve all users
- `USER_VIEW_SELF`- to retrieve only logged user
- `GROUP_CREATE`- to create group
- `GROUP_DELETE_OWN`- to delete group created by logged user
- `GROUP_DELETE_ALL`- to delete group create by any user
- `GROUP_ADD_USER`- to add any user to any group
- `GROUP_REMOVE_USER`- to remove any user from any group
- `GROUP_VIEW_ALL`- to retrieve any group
- `GROUP_VIEW_OWN`- to retrieve group of logged user
- `ROLE_ASSIGN_TO_USER`- to assign role to user
- `ROLE_REMOVE_FROM_USER`- to remove role from user
- `AUTHORITY_CREATE`- to create authority
- `AUTHORITY_DELETE`- to delete authority
- `AUTHORITY_VIEW_ALL`- to retrieve any authority
- `CASE_VIEW_ALL`- to view all cases
- `CASE_CREATE`- to create case
- `CASE_DELETE`- to delete case
- `CASE_DATA_GET_ALL`- to get all data of case
- `TASK_RELOAD`- to reload tasks
- `TASK_ASSIGN`- to assign task
- `TASK_FINISH`- to finish task
- `TASK_CANCEL`- to cancel task
- `TASK_DELEGATE`- to delegate task
- `TASK_SAVE_DATA`- to save data on task
- `ELASTIC_REINDEX` - to reindex Elasticsearch database
- `LDAP_GROUP_GET_ALL` - to get all LDAP groups 
- `LDAP_GROUP_ASSIGN_ROLES` - to assign roles to LDAP groups

However, it is possible to add custom authorizing objects using `nae.authority.authorizing-objects` 
property in `application.properties` files as following:

```
# Authorities
nae.authority.authorizing-objects=EXAMPLE_AUTHORITY_1,EXAMPLE_AUTHORITY_2
```

## Authorization definition

You can define authorization for any method in the Netgrif Application Engine project.
You can use the new `@Authorize` annotation over the method definitions. This annotation can
be defined once or multiple times over a method:

```
@Authorize(authority = "PROCESS_UPLOAD", expression = "#canUpload(#userId)"
void importPetriNet(File petriNet) {
    ...
}
```

The ``@Authorize`` annotation has two fields: 
- `authority` - an authority, that the authenticated user is needed to be checked for
- `expression` - an expression that returns boolean and serves as additional authorization

## Authorization check

The statements are evaluated in `BaseAuthorizationServiceAspect` bean that is implemented using AOP. The user will have 
valid authorization if it has the required `authority` AND fulfills additional authorization defined 
in `expression` (authority check returns `true` AND expression check returns `true`).


If there is no `authority` defined, the authorization method returns `true` for authority check.
In the following example, user must fulfill addition authorization implemented in `canUpload(String userId)` 
function:
```
@Authorize(expression = "#canUpload(#userId))
void importPetriNet(File petriNet) {
    ...
}
```

If there is no `expression` defined, the authorization method always returns`true` for expression check.
In the following example, user must have `PROCESS_UPLOAD` authority:
```
@Authorize(authority = "PROCESS_UPLOAD")
void importPetriNet(File petriNet) {
    ...
}
```


If there are more authorizations defined using `@Authorize`, then OR statement is valid
between the multiple `@Authorize` statements, so user must fulfill at least one `@Authorize` statement.
In the following example, the logged user must have `PROCESS_UPLOAD` authority AND must fulfill `canUpload(String userId)`
expression, OR must fulfill `isAdmin(String userId)` expression:
```
@Authorize(authority = "PROCESS_UPLOAD", expression = "#canUpload(#userId)",
@Authorize(expression = "isAdmin(#userId)")
void importPetriNet(File petriNet) {
    ...
}
```


