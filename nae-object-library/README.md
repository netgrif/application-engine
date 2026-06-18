# nae-object-library

> Framework-agnostic domain object library for the [Netgrif Application Engine](../README.md) ecosystem.

This is the **lowest-level, zero-Spring module** in the NAE multi-module build. It contains only pure Java domain
classes, interfaces, and value objects that are shared across all other NAE modules. It has no dependency on Spring
Framework, making it suitable for use in any Java 21+ project that interoperates with the NAE platform.

---

## Module coordinates

```xml

<dependency>
    <groupId>com.netgrif</groupId>
    <artifactId>nae-object-library</artifactId>
    <version>7.0.0</version>
</dependency>
```

---

## What's inside

### Authentication & Authorization (`objects.auth`)

| Package             | Contents                                                                                                                                      |
|---------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `auth.domain`       | `AbstractActor`, `AbstractUser`, `User`, `LoggedUser`, `Group`, `Authority`, `ActorRef`, `Realm`                                              |
| `auth.domain`       | Credential hierarchy: `Credential<T>`, `PasswordCredential`, `TokenCredential`, `MFAStringCredential`, `MFAMapCredential`, `StringCredential` |
| `auth.domain.enums` | `UserState` (ACTIVE / INACTIVE / BLOCKED)                                                                                                     |
| `auth.provider`     | `AuthMethod`, `AuthMethodConfig`, `RealmUpdate`                                                                                               |
| `auth.dto`          | `AuthPrincipalDto`, `AuthoritySearchDto`, `GroupSearchDto`                                                                                    |

### Petri Net Domain (`objects.petrinet`)

| Package                         | Contents                                                                                                                                                                                                                                                                         |
|---------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `petrinet.domain`               | `PetriNet`, `Place`, `Transition`, `Arc` (PT / Read / Reset / Inhibitor), `Transaction`, `Node`, `Function`, `DataGroup`                                                                                                                                                         |
| `petrinet.domain.dataset`       | Full data field type hierarchy: `Field<T>`, `TextField`, `NumberField`, `BooleanField`, `DateField`, `EnumerationField`, `MultichoiceField`, `FileField`, `FileListField`, `ActorField`, `CaseField`, `TaskField`, `FilterField`, `I18nField`, `StringCollectionField`, and more |
| `petrinet.domain.dataset.logic` | `FieldBehavior`, `FieldLayout`, `ChangedField`, `ChangedFieldsTree`, `DataFieldLogic`, `Action`, `Validation`, `DynamicValidation`                                                                                                                                               |
| `petrinet.domain.events`        | Process-level event types: `CaseEvent`, `DataEvent`, `ProcessEvent`, `Event` with their type enums                                                                                                                                                                               |
| `petrinet.domain.roles`         | `ProcessRole`, `RolePermission`, `ProcessRolePermission`                                                                                                                                                                                                                         |
| `petrinet.domain.version`       | `Version` (semantic versioning with comparison support)                                                                                                                                                                                                                          |
| `petrinet.domain.layout`        | `TaskLayout`, `DataGroupLayout`, `FormLayout`                                                                                                                                                                                                                                    |
| `petrinet.domain.policies`      | `AssignPolicy`, `DataFocusPolicy`, `FinishPolicy`                                                                                                                                                                                                                                |
| `petrinet.domain.views`         | `View`, `ListView`, `TableView`, `TreeView`, `BooleanImageView`, `EditorView`                                                                                                                                                                                                    |

### Workflow Domain (`objects.workflow`)

| Package                               | Contents                                                                                                                                                                  |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `workflow.domain`                     | `Case`, `Task`, `DataField`, `TaskPair`, `ProcessResourceId`                                                                                                              |
| `workflow.domain.eventoutcomes`       | Full outcome hierarchy: `EventOutcome`, task outcomes (Assign / Cancel / Finish / Delegate), case outcomes (Create / Delete), data outcomes (Get / Set), process outcomes |
| `workflow.domain.filter`              | Filter import/export model: `FilterImportExport`, `FilterMetadataExport`, `Predicate`, `PredicateArray`                                                                   |
| `workflow.domain.menu`                | Menu item domain: `Menu`, `MenuEntry`, `MenuItemBody`, `MenuItemConstants`, `MenuItemView`, `FilterBody`, `ToDataSetOutcome`                                              |
| `workflow.domain.menu.configurations` | View body builders: `TabbedCaseViewBody`, `TabbedTaskViewBody`, `TabbedTicketViewBody`, `TabbedSingleTaskViewBody`                                                        |
| `workflow.domain.menu.dashboard`      | Dashboard management: `DashboardManagementBody`, `DashboardItemBody`                                                                                                      |
| `workflow.domain.triggers`            | `Trigger`, `AutoTrigger`, `UserTrigger`, `TimeTrigger`, `DateTimeTrigger`, `DelayTimeTrigger`, `MessageTrigger`                                                           |

### Elasticsearch Index Domain (`objects.elastic`)

Abstract index document classes mirroring the workflow domain for Elasticsearch indexing:
`ElasticCase`, `ElasticTask`, `ElasticPetriNet`, `ElasticTaskPair`, and all abstract `DataField` subtypes
(`TextField`, `NumberField`, `DateField`, `BooleanField`, `ActorField`, `FileField`, `MapField`, `I18nField`, etc.)

### Event System (`objects.event`)

| Package             | Contents                                                                                                |
|---------------------|---------------------------------------------------------------------------------------------------------|
| `event.dispatchers` | `AbstractDispatcher`, `RegisteredListener` — synchronous and asynchronous event dispatch infrastructure |
| `event.listeners`   | `Listener`, `ContextEditingListener`                                                                    |
| `event.events`      | Full application event hierarchy: case, task, data, process, user, and action events                    |

### Supporting Packages

| Package                 | Contents                                                                         |
|-------------------------|----------------------------------------------------------------------------------|
| `objects.annotations`   | `@EnsureCollection`, `@Indexed` — custom field-level annotations                 |
| `objects.plugin`        | `Plugin`, `EntryPoint`, `Method`, `ListenerFilter` — plugin integration model    |
| `objects.preferences`   | `Preferences` — user application preferences                                     |
| `objects.impersonation` | `Impersonator` — user impersonation session state                                |
| `objects.utils`         | `DateUtils`, `MenuItemUtils`, `CopyConstructorUtil`, `Nullable<T>`, `Serializer` |

### Resources

- `petriflow_schema.xsd` — Petriflow 1.1.0 XML schema (extends the official [petriflow.org](https://petriflow.org)
  schema), used by JAXB to generate the process importer model at build time into
  `com.netgrif.application.engine.objects.importer.model`

---

## Build

```shell
# From the repository root
mvn -pl nae-object-library clean install
```

JAXB source generation from `petriflow_schema.xsd` runs automatically during the `generate-sources` phase via the
`jaxb2-maven-plugin` and produces classes into `target/generated-sources/java` under the package
`com.netgrif.application.engine.objects.importer.model`.

---

## License

[NETGRIF Community License](../LICENSE.txt)

