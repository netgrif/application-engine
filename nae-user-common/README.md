# nae-user-common

> Common contracts and infrastructure layer for user management in the [Netgrif Application Engine](../README.md).

This module sits between [`nae-spring-core-adapter`](../nae-spring-core-adapter) and the concrete user management
implementation in [`nae-user-ce`](../nae-user-ce). It defines all **service interfaces**, **Spring Data repositories**,
**web request/response bodies**, and **configuration contracts** that any user management implementation must honour.

Depending only on this module (instead of `nae-user-ce`) lets you swap the entire user management stack by
providing your own service implementations.

---

## Module coordinates

```xml

<dependency>
    <groupId>com.netgrif</groupId>
    <artifactId>nae-user-common</artifactId>
    <version>7.0.0</version>
</dependency>
```

---

## What's inside

### Service interfaces (`auth.service`)

| Interface                 | Responsibility                                                                                                                           |
|---------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| `UserService`             | Full user lifecycle: create, find, update, delete, role/authority assignment, password management, co-member search, multi-realm routing |
| `GroupService`            | Group CRUD, hierarchical parent/child subgroup management, member add/remove, default system group                                       |
| `AuthorityService`        | Authority (permission) CRUD and full-text search                                                                                         |
| `RealmService`            | Realm lifecycle, authentication provider management (add/remove/update), default and admin realm resolution, anonymous access toggle     |
| `PreferencesService`      | Persist and retrieve per-user UI preferences                                                                                             |
| `UserFactory`             | Assemble the REST-layer `User` response DTO with localized process roles                                                                 |
| `UserMapper`              | MapStruct-based mapper for partial user updates (ignores `id`)                                                                           |
| `ProcessRoleFactory`      | Produce the REST-layer `ProcessRole` response DTO from the domain object                                                                 |
| `AnonymousUserRefService` | Get-or-create and delete `AnonymousUserRef` session documents per realm                                                                  |

### Spring Data Repositories (`auth.repository`)

| Repository                   | Backed by               | Purpose                                                                                                              |
|------------------------------|-------------------------|----------------------------------------------------------------------------------------------------------------------|
| `UserRepository`             | MongoDB + MongoTemplate | Per-collection user queries, QueryDSL predicate support, pagination, role-based filtering, expiration-based deletion |
| `GroupRepository`            | MongoDB + QueryDSL      | Group find/save/delete, owner/realm-scoped queries                                                                   |
| `AuthorityRepository`        | MongoDB                 | Authority find-by-name, find-by-ids                                                                                  |
| `RealmRepository`            | MongoDB + Aggregation   | Realm search with full-text + filter aggregation pipeline                                                            |
| `PreferencesRepository`      | MongoDB                 | Preferences find-by-user-id                                                                                          |
| `AnonymousUserRefRepository` | MongoDB                 | Find/save/delete anonymous user references by realm                                                                  |

> **Note:** `UserRepository` deliberately routes all mutations through `saveUser(User, MongoTemplate, String)` rather
> than the standard `save(S)` method, because users are stored in separate per-realm collections.
> Calling `save()` directly throws `UnsupportedOperationException`.

### Authentication Support (`auth.domain`, `auth.provider`)

| Class / Interface              | Purpose                                                                                                          |
|--------------------------------|------------------------------------------------------------------------------------------------------------------|
| `NetgrifAuthenticationToken`   | Extends Spring Security's `UsernamePasswordAuthenticationToken` to carry realm name/object alongside credentials |
| `AuthMethodProvider<T>`        | SPI interface for plugging in custom authentication providers (LDAP, OpenID, etc.)                               |
| `AbstractAuthConfig`           | Base class for provider-specific configuration objects                                                           |
| `ProviderRegistry`             | Thread-safe registry mapping provider type strings to `AuthMethodProvider` instances and their config classes    |
| `AuthMethodConfigDeserializer` | Jackson `StdDeserializer` that uses `ProviderRegistry` to deserialize `AuthMethodConfig` polymorphically         |

### Realm DTOs (`auth.realm`)

| Class                 | Purpose                                                 |
|-----------------------|---------------------------------------------------------|
| `RealmDto`            | Compact read-only view of a realm (name, flags, limits) |
| `RealmSearch`         | Record-based search query parameters for realm listing  |
| `RealmSearchResponse` | Paginated response wrapper for realm search results     |

### Web Layer (`auth.web`)

| Class                    | Purpose                                                                                                          |
|--------------------------|------------------------------------------------------------------------------------------------------------------|
| `UserCreateRequest`      | Request body for creating a new user (username, email, names, password)                                          |
| `UserSearchRequestBody`  | Request body for searching users (realm, fulltext, role inclusion/exclusion)                                     |
| `PreferencesRequest`     | Request body for saving user preferences; converts to `Preferences` domain object                                |
| `User` *(response body)* | REST response DTO for a user — includes id, username, realm, roles, authorities, credentials metadata, and state |
| `PreferencesResource`    | REST response wrapper for `Preferences` with optional error/message fields                                       |

### Configuration (`auth.config`)

| Class                          | Purpose                                                                                                                                                      |
|--------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `GroupConfigurationProperties` | `@ConfigurationProperties(prefix = "netgrif.engine.group")` — enables/disables default user groups and the shared system group; configures their identifiers |
| `JacksonConfiguration`         | Registers `AuthMethodConfigDeserializer` as a Jackson `Module` bean so `AuthMethodConfig` polymorphic deserialization works out-of-the-box                   |

---

## Key configuration property

| Property                                      | Default                | Description                                       |
|-----------------------------------------------|------------------------|---------------------------------------------------|
| `netgrif.engine.group.defaultEnabled`         | `true`                 | Create a personal default group for each new user |
| `netgrif.engine.group.systemEnabled`          | `true`                 | Add each new user to the shared system group      |
| `netgrif.engine.group.defaultGroupIdentifier` | `Default system group` | Identifier of the shared system group             |

---

## Build

```shell
# From the repository root
mvn -pl nae-user-common clean install
```

---

## License

[NETGRIF Community License](../LICENSE.txt)

