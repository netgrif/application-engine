# nae-spring-core-adapter

> Spring Framework adaptation layer for the [Netgrif Application Engine](../README.md) ecosystem.

This module sits between the framework-agnostic [`nae-object-library`](../nae-object-library) and the
full Spring Boot application. It enriches the plain domain objects from `nae-object-library` with
Spring-specific annotations (`@Document`, `@Id`, `@Transient`, `@Field`, `@QueryEntity`, etc.) so they
can be persisted to **MongoDB**, indexed in **Elasticsearch**, and integrated with **Spring Security** —
without polluting the core domain model with framework concerns.

---

## Module coordinates

```xml

<dependency>
    <groupId>com.netgrif</groupId>
    <artifactId>nae-spring-core-adapter</artifactId>
    <version>7.0.0</version>
</dependency>
```

---

## What's inside

### Authentication & Spring Security (`adapter.spring.auth`)

| Package / Class                         | Purpose                                                                                                              |
|-----------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| `auth.domain.User`                      | Extends the object-library `User`; marks `processRoles` and `authoritySet` as `@Transient` for MongoDB serialization |
| `auth.domain.Group`                     | Same pattern for the `Group` entity                                                                                  |
| `auth.domain.LoggedUserImpl`            | Extends `LoggedUser` and implements Spring Security's `UserDetails`; maps authority set to `GrantedAuthority`        |
| `auth.domain.AuthorityImpl`             | Extends `Authority` and implements `GrantedAuthority` — bridge between NAE and Spring Security                       |
| `auth.domain.AnonymousUser`             | Full Spring-compatible anonymous user for unauthenticated sessions                                                   |
| `auth.domain.AnonymousUserRef`          | MongoDB `@Document` for persisting anonymous user sessions                                                           |
| `auth.domain.Realm`                     | Extends `Realm` with `@QueryEntity` and uses name as `@Id`                                                           |
| `auth.service.DefaultLoggedUserFactory` | Spring `@Component` that registers itself as the `ActorTransformer.LoggedUserFactory`                                |

### Elasticsearch Index Documents (`adapter.spring.elastic`)

Concrete Spring Data Elasticsearch document classes that extend the abstract base types from `nae-object-library`
and add `@Field` type annotations for proper index mapping:

| Class                                                                  | Elasticsearch field types added                                                       |
|------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| `ElasticCase`                                                          | `@Document`, `@Id`, `@Version`, `Keyword`, `Text`, `Flattened`, `Date` field mappings |
| `ElasticTask`                                                          | `@Document`, `@Id`, full field mapping with `Keyword`, `Date`, `Flattened`            |
| `ElasticPetriNet`                                                      | `@Document`, `@Id`, `Keyword`, `Date` field mappings                                  |
| `TextField`, `NumberField`, `DateField`, `BooleanField`, `ButtonField` | Typed `@Field` annotations (`Text`, `Double`, `Long`, `Boolean`, `Integer`, `Date`)   |
| `MapField`, `FilterField`, `I18nField`, `CaseField`, `TaskField`       | `Keyword`, `Text`, `Flattened` field annotations                                      |
| `ActorField`, `ActorListField`, `FileField`, `StringCollectionField`   | `Text` / `Keyword` field annotations                                                  |

### Petri Net Domain with Spring Annotations (`adapter.spring.petrinet`)

| Class                                        | Spring annotations added                                                                                                     |
|----------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| `PetriNet`                                   | `@Document`, `@QueryEntity`, explicit `@Field` names for `places`, `transitions`, `arcs`, `dataset`, `roles`, `transactions` |
| `UriNode`                                    | `@Document`, `@Id` on `path`                                                                                                 |
| `ProcessRole`                                | `@Document`, `@QueryEntity`, `@Id` on composite `ProcessResourceId`                                                          |
| `Arc`, `InhibitorArc`, `ReadArc`, `ResetArc` | `@Transient` on `source` / `destination` to prevent MongoDB serialization of circular references                             |

### Service Interfaces (`adapter.spring.petrinet.service`, `adapter.spring.workflow.service`)

| Interface                   | Description                                                 |
|-----------------------------|-------------------------------------------------------------|
| `ProcessRoleService`        | Full CRUD + assignment operations on `ProcessRole` entities |
| `FilterImportExportService` | Import/export of filter definitions to/from XML files       |

### Workflow Domain with Spring Annotations (`adapter.spring.workflow`)

| Class       | Spring annotations added                                                                                                      |
|-------------|-------------------------------------------------------------------------------------------------------------------------------|
| `Case`      | `@Document`, `@QueryEntity`, `@Id` on `ProcessResourceId`, `@LastModifiedDate`, `@Transient` on `PetriNet` and immediate data |
| `Task`      | `@Document`, `@QueryEntity`, `@Id`, `@Transient` on immediate data and user                                                   |
| `DataGroup` | `@Transient` on fields, parent task/transition/case references                                                                |

### Action API (`adapter.spring.actions`)

`ActionApi` — a high-level service interface for Groovy action code inside NAE processes. Provides
framework-independent methods callable from process actions at runtime:

| Method group         | Operations                                                                                   |
|----------------------|----------------------------------------------------------------------------------------------|
| Cases                | `findCase`, `searchCases`, `countCases`, `createCaseByIdentifier`, `deleteCase`              |
| Tasks                | `findTask`, `searchTasks`, `countTasks`, `assignTask`, `cancelTask`, `finishTask`            |
| Data                 | `getData`, `setData`                                                                         |
| Files                | `saveFile`, `saveFiles`, `deleteFile`, `deleteFileByName`, `getFile`, `getFileByCaseAndName` |
| Users                | `searchUsers`, `getSystemUser`, `getSystemUserDto`                                           |
| Process availability | `isProcessUp`, `isProcessDown`, `getProcessAvailability`                                     |

The `ProcessAvailability` / `ProcessAvailabilities` records provide a fluent API for checking whether
process definitions are currently deployed and accessible.

### Configuration (`adapter.spring.configuration`)

| Class                                 | Purpose                                                                                                      |
|---------------------------------------|--------------------------------------------------------------------------------------------------------------|
| `AbstractMongoCollectionConfigurator` | Base class for ensuring MongoDB collections and indexes exist for all `@EnsureCollection`-annotated entities |
| `LoggedUserConfiguration`             | Spring `@Configuration` that wires `DefaultLoggedUserFactory` into `ActorTransformer` on startup             |
| `CustomLevelColorConverter`           | Logback converter for ANSI-colored log level output                                                          |
| `CustomMdcConverter`                  | Logback converter for colorized MDC fields (`traceId`, `userId`)                                             |

### Plugin System (`adapter.spring.plugin`)

| Component                         | Purpose                                                                                    |
|-----------------------------------|--------------------------------------------------------------------------------------------|
| `@EntryPoint`                     | Spring `@Service` meta-annotation marking a bean as a remotely callable plugin entry point |
| `@EntryPointMethod`               | Marks individual methods within an entry point as externally invocable                     |
| `@ListenerFilter`                 | Annotation for binding entry point methods to specific NAE events and dispatch methods     |
| `PluginRegistrationConfiguration` | Interface for registering plugin metadata (name, version, entry points)                    |
| `PluginService`                   | Interface for calling entry point methods on registered plugins                            |

### Utilities (`adapter.spring.utils`)

| Class                  | Purpose                                                                                                                                           |
|------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| `NaeReflectionUtils`   | AOP-proxy-aware reflection utilities (`resolveClass`, `findMethod`, `indexOfClass`)                                                               |
| `PageableUtils`        | Converts a `List<T>` to a Spring Data `Page<T>`; provides a full-page `Pageable`                                                                  |
| `PaginationProperties` | `@ConfigurationProperties(prefix = "netgrif.engine.pagination")` — configures `backendPageSize` (default 100) and `frontendPageSize` (default 20) |

### QueryDSL

The `package-info.java` registers `@QueryEntities` for `Case`, `Task`, `PetriNet`, `ProcessRole`,
`Group`, and `User`, enabling type-safe QueryDSL predicate generation via the APT Maven plugin
(output: `target/generated-sources/java`).

---

## Build

```shell
# From the repository root
mvn -pl nae-spring-core-adapter clean install
```

QueryDSL Q-classes are generated automatically during `generate-sources` via the `apt-maven-plugin`
into `target/generated-sources/java`.

---

## License

[NETGRIF Community License](../LICENSE.txt)

