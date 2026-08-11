# nae-user-ce

> Community Edition user management module for the [Netgrif Application Engine](../README.md).

This module provides the **default, ready-to-use implementations** of all user, group, authority, realm, and preferences
management services. It depends on [`nae-user-common`](../nae-user-common) for the service interfaces and repositories,
and on [`nae-spring-core-adapter`](../nae-spring-core-adapter) for the Spring-annotated domain classes.

All service beans are registered via `@ConditionalOnMissingBean`, making every implementation fully
**replaceable** — override any bean in your application context and this module's default will be skipped.

---

## Module coordinates

```xml

<dependency>
    <groupId>com.netgrif</groupId>
    <artifactId>nae-user-ce</artifactId>
    <version>7.0.0</version>
</dependency>
```

---

## What's inside

### Service implementations (`auth.service`)

| Service                   | Implementation class          | Responsibility                                                                                                                                                                    |
|---------------------------|-------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `UserService`             | `UserServiceImpl`             | Full user lifecycle: create, find, update, delete, password management, role/authority assignment, system user bootstrap, per-realm MongoDB collection routing                    |
| `GroupService`            | `GroupServiceImpl`            | Group CRUD, hierarchical subgroup management, member add/remove, default system group and default user group management                                                           |
| `AuthorityService`        | `AuthorityServiceImpl`        | Authority (permission) CRUD, get-or-create semantics, full-text search                                                                                                            |
| `RealmService`            | `RealmServiceImpl`            | Realm lifecycle: create (including automatic MongoDB collection provisioning), update, delete, anonymous access toggle, authentication provider (LDAP, etc.) management per realm |
| `PreferencesService`      | `PreferencesServiceImpl`      | Persist and retrieve per-user UI preferences                                                                                                                                      |
| `UserFactory`             | `UserFactoryImpl`             | Assembles the REST response `User` DTO including localized process role names                                                                                                     |
| `AnonymousUserRefService` | `AnonymousUserRefServiceImpl` | Get-or-create and delete `AnonymousUserRef` documents for public/unauthenticated realm access                                                                                     |

### Auto-configuration (`auth.config`)

`AuthBeansConfiguration` registers all of the above service implementations as Spring beans using
`@ConditionalOnMissingBean`. This means you can replace any single service by declaring your own bean
of the same interface type — the CE default will not be loaded.

### Infrastructure support

| Class                    | Purpose                                                                                                                                              |
|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| `CollectionNameProvider` | Resolves the MongoDB collection name for a given realm ID (`users_<realmId>`); supports default realm, admin realm, and multi-realm lookups          |
| `UserMongoEventListener` | Spring Data `AfterConvertCallback` that eagerly resolves `processRoleIds` and `authorityIds` into their full domain objects after every MongoDB read |

### Key design decisions

- **Per-realm user collections** — users are stored in separate MongoDB collections (`users_<realmId>`) rather than a
  single shared collection. `CollectionNameProvider` handles all routing logic.
- **System user** — a built-in `system@netgrif.com` user is bootstrapped on first access and always holds all
  process roles.
- **Default and anonymous roles** — new users automatically receive the `default` process role; anonymous users
  receive the `anonymous` process role.
- **Default group provisioning** — on user creation a personal default group is created (configurable via
  `GroupConfigurationProperties`) and the user is optionally added to the shared system group.

---

## Module packaging

This module uses the **`nae-module-maven-plugin`** to produce a self-contained module JAR for hot-loading
into the engine's `modules/` directory during development. See the root [README](../README.md#development-setup)
for the symlink-based development workflow.

---

## Build

```shell
# From the repository root
mvn -pl nae-user-ce clean install
```

---

## License

[NETGRIF Community License](../LICENSE.txt)

