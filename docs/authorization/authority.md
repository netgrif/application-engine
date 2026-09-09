# Authority System

The Netgrif Application Engine (NAE) uses **authorities** to protect resources and
operations from unauthorized access. Authorities are application-wide permissions that
can be assigned to users to grant them access to secured resources or operations.

Authorities differ from process roles in that they are **not** related to a specific
process. While a process role controls what a user can do *inside* a Petriflow process
(e.g. assign a task, view a case), an authority controls access to application-level
functionality (e.g. importing a process, creating a user, deleting an authority).

## Table of contents

- [Concepts](#concepts)
- [Authorizing objects](#authorizing-objects)
    - [Predefined authorizing objects](#predefined-authorizing-objects)
    - [Custom authorizing objects](#custom-authorizing-objects)
- [Authority scopes](#authority-scopes)
- [Default authorities](#default-authorities)
- [Protecting code with `@Authorize`](#protecting-code-with-authorize)
    - [Where can it be used](#where-can-it-be-used)
    - [Annotation fields](#annotation-fields)
    - [Combining conditions](#combining-conditions)
    - [Multiple `@Authorize` annotations](#multiple-authorize-annotations)
- [How the check is evaluated](#how-the-check-is-evaluated)
- [Managing authorities via REST API](#managing-authorities-via-rest-api)

## Concepts

There are three core concepts in the authority system:

| Concept              | Description                                                                                             |
|----------------------|---------------------------------------------------------------------------------------------------------|
| `AuthorizingObject`  | An enum value describing *what* is being protected (e.g. `PROCESS_UPLOAD`).                              |
| `Authority`          | A persisted entity (stored in MongoDB) created from an authorizing object. It implements Spring Security's `GrantedAuthority`. |
| `@Authorize`         | An annotation placed on a method or a class that requires a user to hold a given authority and/or satisfy an expression. |

At application startup, an `Authority` entity is created for each `AuthorizingObject`
value (and for every custom authorizing object). Users are then granted a set of
authorities, and the `@Authorize` annotation is used across the codebase to enforce them.

## Authorizing objects

An authorizing object is a value of the `AuthorizingObject` enum. It represents an
authority that a user must hold to access a resource or invoke an operation. For example,
to import a new process (Petri net) into the application, the user must hold the authority
created from `AuthorizingObject.PROCESS_UPLOAD`.

Authorizing objects are predefined and are used to create the `Authority` entities during
application startup by the `AuthorityRunner`.

### Predefined authorizing objects

The application engine defines the following authorizing objects:

**Process**

- `PROCESS_UPLOAD` — import a new process
- `PROCESS_VIEW_ALL` — retrieve all processes imported by any user
- `PROCESS_VIEW_OWN` — retrieve only processes imported by the logged user
- `PROCESS_DELETE_ALL` — delete processes imported by any user
- `PROCESS_DELETE_OWN` — delete processes imported by the logged user

**Filter**

- `FILTER_UPLOAD` — upload a filter
- `FILTER_DELETE_OWN` — delete a filter created by the logged user
- `FILTER_DELETE_ALL` — delete a filter created by any user

**User**

- `USER_CREATE` — invite or create a user
- `USER_DELETE` — remove a user
- `USER_EDIT_ALL` — edit any user
- `USER_EDIT_SELF` — edit only the logged user
- `USER_VIEW_ALL` — retrieve all users
- `USER_VIEW_SELF` — retrieve only the logged user

**Group**

- `GROUP_CREATE` — create a group
- `GROUP_DELETE_OWN` — delete a group created by the logged user
- `GROUP_DELETE_ALL` — delete a group created by any user
- `GROUP_ALL_ADD_USER` — add any user to any group
- `GROUP_OWN_ADD_USER` — add a user to a group owned by the logged user
- `GROUP_ALL_REMOVE_USER` — remove any user from any group
- `GROUP_OWN_REMOVE_USER` — remove a user from a group owned by the logged user
- `GROUP_VIEW_ALL` — retrieve any group
- `GROUP_VIEW_OWN` — retrieve a group of the logged user
- `GROUP_MEMBERSHIP_SELF` — manage the logged user's own group membership

**Role**

- `ROLE_ASSIGN_TO_USER` — assign a process role to a user

**Authority**

- `AUTHORITY_CREATE` — create an authority
- `AUTHORITY_DELETE` — delete an authority
- `AUTHORITY_VIEW` — retrieve an authority

**Case**

- `CASE_VIEW_ALL` — view all cases
- `CASE_CREATE` — create a case
- `CASE_DELETE` — delete a case
- `CASE_DATA_GET_ALL` — get all data of a case

**Task**

- `TASK_RELOAD` — reload tasks
- `TASK_ASSIGN` — assign a task
- `TASK_FINISH` — finish a task
- `TASK_CANCEL` — cancel a task
- `TASK_DELEGATE` — delegate a task
- `TASK_SAVE_DATA` — save data on a task

**Elasticsearch**

- `ELASTIC_REINDEX` — reindex the Elasticsearch database

**LDAP**

- `LDAP_GROUP_GET_ALL` — get all LDAP groups
- `LDAP_GROUP_ASSIGN_ROLES` — assign roles to LDAP groups

> The enum also declares the default values as `ADMIN` and `USER` values.

### Custom authorizing objects

You can register your own authorizing objects using the
`nae.authority.authorizing-objects` property in `application.properties`. The listed
values are created as `Authority` entities on startup alongside the predefined ones:

```properties
# Authorities
nae.authority.authorizing-objects=EXAMPLE_AUTHORITY_1,EXAMPLE_AUTHORITY_2
```

## Authority scopes

Authorities can be referenced by **scope**. A scope groups all authorities that share the
same name prefix, denoted by a trailing `*`. For example, the scope `PROCESS_*` covers
`PROCESS_UPLOAD`, `PROCESS_VIEW_ALL`, `PROCESS_DELETE_OWN`, and so on. The single `*`
scope represents all authorities in the system.

Scopes are handy when assigning a set of related authorities (for example, as default
authorities) or when querying authorities via the REST API. Note that scope names are not
valid arguments for operations that expect a single, concrete authority (such as creating
or deleting an authority).

## Default authorities

Newly created users receive a set of default authorities. These defaults are configured
per user type using scopes and concrete authority names:

```properties
nae.authority.defaultUserAuthorities=FILTER_UPLOAD,FILTER_DELETE_OWN,USER_EDIT_OWN,GROUP_OWN_ADD_USER,...
nae.authority.defaultAnonymousAuthorities=...
nae.authority.defaultAdminAuthorities=*
```

- `defaultUserAuthorities` — granted to a standard registered user.
- `defaultAnonymousAuthorities` — granted to the anonymous user.
- `defaultAdminAuthorities` — granted to the super/admin user (`*` grants everything).

These properties are read by `AuthorityProperties` and resolved (including scopes) by the
`AuthorityService`.

## Protecting code with `@Authorize`

Any method in the engine can be protected with the `@Authorize` annotation. When the
annotated method is invoked, an AOP aspect intercepts the call and verifies that the
currently logged user is authorized before the method body runs. If the check fails, an
`AccessDeniedException` is thrown and the method is never executed.

### Where can it be used

The annotation targets both **methods** and **types**, so it can be applied to:

- **REST controllers** — to guard HTTP endpoints. For example, the endpoints of the
  authority management controller are protected with authorities such as
  `AUTHORITY_CREATE`, `AUTHORITY_DELETE`, and `AUTHORITY_VIEW`:

  ```java
  @Authorize(authority = "AUTHORITY_DELETE")
  @DeleteMapping("/delete/{name}")
  public MessageResource delete(@PathVariable String name, Authentication auth) {
      authorityService.delete(name);
      // ...
  }
  ```

- **`ActionDelegate` methods** — the Actions API available in Petriflow actions is backed
  by `ActionDelegate`. Its methods are protected so that action code can only perform an
  operation if the user running the action holds the required authority. For example,
  inviting or deleting a user requires `USER_CREATE` / `USER_DELETE`:

  ```groovy
  @Authorize(authority = "USER_CREATE")
  MessageResource inviteUser(String email) {
      // ...
  }

  @Authorize(authority = "USER_DELETE")
  void deleteUser(String email) {
      // ...
  }
  ```

- **Service methods** — or any other Spring-managed bean method.

> Because the check is implemented with Spring AOP, `@Authorize` only takes effect on
> calls that go through the Spring proxy. Self-invocations (a bean calling its own
> annotated method directly) are not intercepted.

### Annotation fields

`@Authorize` has two optional fields:

- `authority` — an array of authority names the logged user must hold. When multiple
  values are provided, the user must hold **all** of them.
- `expression` — a [Spring Expression Language (SpEL)](https://docs.spring.io/spring-framework/reference/core/expressions.html)
  expression that must evaluate to `true`. Inside the expression you can reference:
    - the arguments of the intercepted method as variables (e.g. `#userId`, `#email`),
    - Spring beans (e.g. `@userService`).

```groovy
@Authorize(authority = "USER_EDIT_OWN", expression = "@userService.getLoggedUser().email.equals(#email)")
def changeUserByEmail(String email) {
    // ...
}
```

If a field is omitted, that part of the check is considered satisfied:

- No `authority` → the authority check returns `true`.
- No `expression` → the expression check returns `true`.

```java
// Requires only the PROCESS_UPLOAD authority
@Authorize(authority = "PROCESS_UPLOAD")
void importPetriNet(File petriNet) { /* code is here */ }
```

```java
// Requires only that the expression evaluates to true
@Authorize(expression = "#canUpload(#userId)")
void importPetriNet(String userId, File petriNet) { /* code is here */ }
```

### Combining conditions

Within a single `@Authorize` annotation, the `authority` and `expression` checks are
combined with a logical **AND**. The user is authorized only if they hold the required
authority **and** the expression evaluates to `true`:

```java
@Authorize(authority = "PROCESS_UPLOAD", expression = "#canUpload(#userId)")
void importPetriNet(String userId, File petriNet) { /* code is here */ }
```

### Multiple `@Authorize` annotations

`@Authorize` is repeatable. When multiple annotations are placed on the same element, they
are combined with a logical **OR** — the user is authorized if **at least one** of the
`@Authorize` statements is satisfied. Under the hood, repeated annotations are grouped in
the `@Authorizations` container annotation.

In the example below the user is authorized if they hold `USER_EDIT_ALL`, **or** if they
hold `USER_EDIT_OWN` and are editing their own account:

```groovy
@Authorize(authority = "USER_EDIT_ALL")
@Authorize(authority = "USER_EDIT_OWN", expression = "@userService.getLoggedUser().stringId.equals(#id)")
def changeUser(String id) {
    // ...
}
```

## How the check is evaluated

Authorization is enforced by the `BaseAuthorizationServiceAspect` bean, implemented using
Spring AOP. The aspect intercepts every call to a method annotated with `@Authorize`
(or its container `@Authorizations`) and evaluates the conditions as follows:

1. For each `@Authorize` statement, the aspect checks:
    - **Authority** — whether the logged user's granted authorities contain all of the
      declared authority names (`hasAnyAuthority`). An empty/omitted `authority` passes.
    - **Expression** — whether the SpEL `expression` evaluates to `true`
      (`isAllowedByExpression`). An empty/omitted `expression` passes. Method arguments
      are bound as SpEL variables and Spring beans are resolvable from the application
      context.
2. A single statement passes when **both** its authority and expression checks pass (AND).
3. When several statements are present, the overall result is `true` if **any** statement
   passes (OR).
4. If the overall result is `true`, the intercepted method proceeds. Otherwise, an
   `AccessDeniedException` (`"Access Denied. User does not have required authorization
   level."`) is thrown.

If the expression cannot be parsed or throws while evaluating, it is treated as `false`
(access is not granted based on that expression).

## Managing authorities via REST API

Authorities can be managed at runtime through the authority REST controller under
`/api/authority`. All endpoints are themselves protected with `@Authorize`:

| Method & path                | Required authority | Description                                   |
|------------------------------|--------------------|-----------------------------------------------|
| `POST /api/authority/create` | `AUTHORITY_CREATE` | Create (or return existing) authority by name |
| `DELETE /api/authority/delete/{name}` | `AUTHORITY_DELETE` | Delete an authority by name          |
| `GET /api/authority/all`     | `AUTHORITY_GET_ALL`* | Retrieve all authorities                    |
| `GET /api/authority/{name}`  | `AUTHORITY_VIEW`   | Retrieve a single authority by name           |
| `GET /api/authority/scope/{scope}` | `AUTHORITY_VIEW` | Retrieve all authorities within a scope    |

These endpoints delegate to `AuthorityService`, which persists authorities in MongoDB via
`AuthorityRepository` and supports scope-based lookups through `findByScope`.

> The controller is only registered when the `nae.user.web.enabled` property is `true`
> (which is the default).

*Note: the "get all" endpoint requires the authority the controller declares for it; make
sure your users are granted the corresponding authority in your configuration.
```