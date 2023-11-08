# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Full Changelog: [https://github.com/netgrif/application-engine/commits/v6.3.2](https://github.com/netgrif/application-engine/commits/v6.3.2)

## [6.3.2](https://github.com/netgrif/application-engine/releases/tag/v6.3.1) (2023-11-08)

### Fixed

- [NAE-1910] Event log is missing change in case's active places

## [6.3.1](https://github.com/netgrif/application-engine/releases/tag/v6.3.1) (2023-07-18)

### Fixed

- [NAE-1807] Reference in Arc is not cloned properly
- [NAE-1836] Actions cache is broken (for multiple versions of the same process)
- [NAE-1864] Deleting process version removes instances of all versions of the same process
- [NAE-1870] Indeterministic order in multichoice and multichoice map values

### Added

- [NAE-1891] Make URL function

## [6.3.0](https://github.com/netgrif/application-engine/releases/tag/v6.3.0) (2023-05-26)

### Fixed

- [NAE-1761] LoggedUser circular dependency
- [NAE-1798] UserRefs resolves everytime when case is saved
- [NAE-1757] Multi-choice and enumeration options not working
- [NAE-1759] Update documentation of public views, PDF generator and user list
- [NAE-1805] Data fields validations are not run on backend
- [NAE-1811] Process delete does not delete ldap group roles
- [NAE-1812] Clicking on a column in the case-view that does not have a value has an error behavior
- [NAE-1829] UserFieldValue does not implements .toString() method
- [NAE-1831] Task with negative permissions displayed
- [NAE-1834] ExportService - Export CSV files in given charset
- [NAE-1840] Configurable timeout for failed login attempts
- [NAE-1853] Passwords loaded from environment variables are shown in application logs
- [NAE-1860] Assigning authority to a user via REST API ends up with error
- [NAE-1861] Requesting unknown endpoint ends with Stackoverflow
- [NAE-1875] Namespace functions cause memory leak
- [NAE-1868] Process role refresh
- [NAE-1723] EventLog classes are not resolved correctly after search in MongoDB collection

### Changed

- [NAE-1638] Elasticsearch executor shutdown
- [NAE-1762] Public view URL encoding
- [NAE-1865] Grid rendering performance improvement

### Added

- [NAE-1607] Allowed nets configuration for newly created filter
- [NAE-1630] Add admin endpoint for invalidating user session
- [NAE-1678] User impersonation
- [NAE-1793] Security filter for HTTP headers validation
- [NAE-1794] Configurable security headers
- [NAE-1833] Configuration priority search
- [NAE-1722] Set default view headers with the CreateMenuItem action method
- [NAE-1874] Filtering users based on roles when selecting users for UserListField
- [NAE-1703] Dashboard
- [NAE-1678] User impersonation

## [6.2.9](https://github.com/netgrif/application-engine/releases/tag/v6.2.9) (2023-05-04)

### Added

- [NAE-1865] Grid rendering performance improvement

## [6.2.8](https://github.com/netgrif/application-engine/releases/tag/v6.2.8) (2023-03-22)

### Fixed

- [NAE-1853] Passwords loaded from environment variables are shown in application logs

## [6.2.7](https://github.com/netgrif/application-engine/releases/tag/v6.2.7) (2022-12-19)

### Fixed

- [NAE-1806] Anonymous user password might not be 'null'

## [6.2.6](https://github.com/netgrif/application-engine/releases/tag/v6.2.6) (2022-12-12)

### Fixed

- [NAE-1799] Multilevel process identifier handling in 6.2.x for public view
- [NAE-1801] Change DataService

## [6.2.5](https://github.com/netgrif/application-engine/releases/tag/v6.2.5) (2022-12-05)

### Fixed

- [NAE-1755] PetriNetService cache no caching "null"
- [NAE-1773] Action that sets null value to field does not trigger next actions
- [NAE-1776] UncaughtExceptionHandler
- [NAE-1777] JWT Token user detail not show password

### Added

- [NAE-1753] Currency code in PDF
- [NAE-1756] Delete PetriNet does not call case delete events
- [NAE-1780] Configurable CORS security settings

## [6.2.4](https://github.com/netgrif/application-engine/releases/tag/v6.2.4) (2022-10-12)

### Fixed

- [NAE-1747] PDF Generator: incorrect file name shortening
- [NAE-1748] Incorrect handling for long lines in PDF generator

## [6.2.3](https://github.com/netgrif/application-engine/releases/tag/v6.2.3) (2022-10-06)

### Fixed

- [NAE-1728] Simplify value for changing user list
- [NAE-1730] PetriNetService cache fix upload bug
- [NAE-1733] Organization group process has deprecated attributes and change import system user
- [NAE-1735] Enumeration parsing error when trying to use icons with option keys

## [6.2.2](https://github.com/netgrif/application-engine/releases/tag/v6.2.2) (2022-09-28)

### Fixed

- [NAE-1693] TaskController deleteFile endpoint lacks data about tasks
- [NAE-1714] Parameter processIdentifier in class CaseSearchRequest

## [6.2.1](https://github.com/netgrif/application-engine/releases/tag/v6.2.1) (2022-09-15)

### Fixed

- [NAE-1720] PDF number field is not null value

## [6.2.0](https://github.com/netgrif/application-engine/releases/tag/v6.2.0) (2022-09-12)

### Fixed

- [NAE-1626] Trigger of set event for fileList value deletion is not implemented
- [NAE-1666] PDF Generator template file issue
- [NAE-1691] Default value number field after the change behavior
- [NAE-1694] ProcessRoleService.findByImportId doesn't return list of process roles
- [NAE-1699] Create/upload event action is loaded only from the cache
- [NAE-1702] LdapUser unique dn

### Changed

- [NAE-1156] View Deprecation and All Data
- [NAE-1606] Configuration of case creation button from menu item
- [NAE-1646] Overridable data field render in PDF generator
- [NAE-1663] Optional constructor
- [NAE-1668] PDF Generator 2022 Summer Update
- [NAE-1677] Message from exception thrown in SET event on data field is not propagated in EventOutcome
- [NAE-1696] PDF Generator - legacy and flow layout

### Added

- [NAE-1625] PetriNetService cache
- [NAE-1631] Process download in workflow view
- [NAE-1647] Elastic mapping for I18N data field
- [NAE-1665] Generated PDF colors properties
- [NAE-1660] Filter action API
- [NAE-1659] Process URI
- [NAE-1679] User list frontend component
- [NAE-1698] Springdoc OpenAPI 3 generation

## [6.1.1](https://github.com/netgrif/application-engine/releases/tag/v6.1.1) (2022-06-09)

### Fixed

- [NAE-1652] For some PDF documents the file preview is not generating
- [NAE-1649] File field preview component bug
- [NAE-1641] Generate PDF with special characters

### Changed

- [NAE-1655] Better Active Directory compatibility

## [6.1.0](https://github.com/netgrif/application-engine/releases/tag/v6.1.0) (2022-06-01)

### Fixed

- [NAE-1585] Security update to resolve vulnerabilities
- [NAE-1049] Case name doesn't change
- [NAE-1637] PDF generator with template does not work
- [NAE-1636] resolveTaskRefOrderOnGrid with forbidden dataField

### Changed

- [NAE-1440] setData on button without value
- [NAE-1569] Update Groovy 3.0.10
- [NAE-1624] Upgrade Elasticsearch Connector
- [NAE-1562] Improved logging of deleting resources
- [NAE-1521] Authentication Providers
- [NAE-1623] Action API consolidation
- [NAE-1622] Change behavior action improvements
- [NAE-1640] Update spring boot 2.7.0
- [NAE-1478] ActionDelegate functions overloading

### Added

- [NAE-1627] LDAP Groups resolving and mapping
- [NAE-1468] i18n Data field
- [NAE-1290] Export actions
- [NAE-1477] New pdfGeneration action in actionDelegate

### Removed

- [NAE-1584] Remove petriflow schema

## [6.0.5](https://github.com/netgrif/application-engine/releases/tag/v6.0.5) (2022-05-12)

### Fixed

- [NAE-1621] Required data group layout tag if layout type is “grid”

## [6.0.4](https://github.com/netgrif/application-engine/releases/tag/v6.0.4) (2022-04-12)

### Fixed

- [NAE-1600] Parallel auto-trigger tasks
- [NAE-1614] Advanced search substring query
- [NAE-1556] Post-set error prevents set
- [NAE-1616] Cannot upload process with action id
- [NAE-1596] Nets with transitions without titles cannot be read
- [NAE-1598] DataGroups without ID cannot be saved into mongo
- [NAE-1599] Task-reffed fields are not placed in correct position

### Changed

- [NAE-1620] Change workflowService findAllById method

## [6.0.3](https://github.com/netgrif/application-engine/releases/tag/v6.0.3) (2022-04-01)

### Fixed

- [NAE-1589] Role changes for logged users don't take effect
- [NAE-1593] Remove user does not remove group from user groups
- [NAE-1590] Taskref with file field must be assigned
- [NAE-1618] SpringFox swagger failing in combination with spring actuator

### Changed

- [NAE-1571] Change the Version.NEWEST from "^" to "latest"

## [6.0.2](https://github.com/netgrif/application-engine/releases/tag/v6.0.2) (2022-03-07)

### Fixed

- [NAE-1557] Cannot clear optional enumeration
- [NAE-1559] Keyword 'this' in event actions
- [NAE-1572] FilterImportExportService service does not assign import_filter tasks
- [NAE-1573] GET event is initialized with SET type
- [NAE-1577] Task reffed change behavior does not propagate
- [NAE-1579] Group is not added into User.nextGroups when invited
- [NAE-1581] Public view create case permission check
- [NAE-1582] PetriNetService search method returns just first page
- [NAE-1588] DefaultFiltersRunner creates new cases every time

### Changed

- [NAE-1578] Make createSetDataEventOutcome protected

## [6.0.1](https://github.com/netgrif/application-engine/releases/tag/v6.0.1) (2022-02-15)

- Equalizing release to match version
  of [Netgrif Components libraries](https://github.com/netgrif/components/releases/tag/v6.0.1)

## [6.0.0](https://github.com/netgrif/application-engine/releases/tag/v6.0.0) (2022-02-09)

### Added

- [NAE-1292] Anonym role

### Changed

- [NAE-1565] Update spring boot to 2.6.2
- [NAE-1490] Default role permissions
- [NAE-1503] Permission refactor phase 1
- [NAE-315] Group management migration to MongoDB
- [NAE-435] Update Spring Boot 2.3.x, rewrite tests
- [NAE-448] User management migration to MongoDB
- [NAE-1401] Configurable LDAP integration

### Fixed

- [NAE-1552] File field/file list field post event action breaks file upload
- [NAE-1561] Action tags with ID
- [NAE-1564] File field value change is not seen on frontend
- [NAE-1107] ImportHelper & superCreator

## [5.8.0](https://github.com/netgrif/application-engine/releases/tag/5.8.0) (2021-11-26)

### Changed

- [NAE-1469] Data group layouts refactor

### Added

- [NAE-1296] Event Outcome refactor

## [5.7.5](https://github.com/netgrif/application-engine/releases/tag/5.7.5) (2022-01-11)

### Fixed

- [NAE-1542] PDF generator generating wrong date format
- [NAE-1543] Customization of PDF generation for new layout system on a data group

## [5.7.4](https://github.com/netgrif/application-engine/releases/tag/5.7.4) (2021-12-21)

### Fixed

- [NAE-1533] Mongo case search exceeds maximum query nesting level

## [5.7.3](https://github.com/netgrif/application-engine/releases/tag/5.7.3) (2021-11-23)

### Added

- [NAE-1493] i18n divider data field

## [5.7.2](https://github.com/netgrif/application-engine/releases/tag/5.7.2) (2021-11-23)

### Fixed

- [NAE-1518] Export of chained filters

### Changed

- [NAE-1439] Deprecate values

## [5.7.1](https://github.com/netgrif/application-engine/releases/tag/5.7.1) (2021-11-08)

### Fixed

- [NAE-1487] Assign button on assigned task
- [NAE-1485] Case data field behavior is not initially set
- [NAE-1492] View permission on case is broken when delete permission is present
- [NAE-1483] TaskRef init value setting deletes new value of task field
- [NAE-1520] Security Configuration Ldap fix

### Changed

- [NAE-1515] Filter process UX improvements
- [NAE-1516] Group process UX improvements

## [5.7.0](https://github.com/netgrif/application-engine/releases/tag/5.7.0) (2021-10-29)

### Fixed

- [NAE-1375] Form field layout does not respect rows
- [NAE-1422] Case view permission query with no users
- [NAE-1509] Broken jackson dependencies
- [NAE-1281] Immediate file field
- [NAE-1332] Cancel task action does not check if user has permission to cancel the task
- [NAE-1460] Data field Spinner Attribute Refactor

### Changed

- [NAE-1390] Update "Process file could not be uploaded" message
- [NAE-1438] Unique key constraint on options

### Added

- [NAE-1402] Configurable group navigation with role constraints
- [NAE-1406] Filter import/export
- [NAE-1417] Menu import/export

## [5.6.3](https://github.com/netgrif/application-engine/releases/tag/5.6.3) (2021-09-27)

### Fixed

- [NAE-1467] Fix table creation for user
- [NAE-1453] Example app sign-up broken
- [NAE-1465] Display saved filter options with empty input

### Changed

- [NAE-1454] Dynamic filter chaining

## [5.6.2](https://github.com/netgrif/application-engine/releases/tag/5.6.2) (2021-09-03)

### Fixed

- [NAE-1441] Var arcs backwards incompatibility
- [NAE-1442] LdapUserRef incorrect @Id annotation

### Changed

- [NAE-1443] LDAP objectClasses not loaded from properties

## [5.6.1](https://github.com/netgrif/application-engine/releases/tag/5.6.1) (2021-08-30)

### Fixed

- [NAE-1424] LDAP integration security vulnerability

## [5.6.0](https://github.com/netgrif/application-engine/releases/tag/5.6.0) (2021-08-26)

### Fixed

- [NAE-1396] Broken property to forbid group creation for users
- [NAE-1382] Case with first transition set to auto trigger make deadlock in process
- [NAE-1372] Change user list does not work in post create action
- [NAE-1324] Delete Petri net with many instances crashes the application
- [NAE-1312] Merge issue from NAE-1238_usersRef

### Changed

- [NAE-1400] Fix multiple access to repository while getting data
- [NAE-1399] PDF generator improvement
- [NAE-1392] Implement GroovyShell Factory
- [NAE-1104] User filters
- [NAE-335] Variable arc definition rework
- [NAE-297] Petriflow functions

### Added

- [NAE-1407] Static functions

## [5.5.5](https://github.com/netgrif/application-engine/releases/tag/5.5.5) (2022-01-06)

### Fixed

- [NAE-1530] Visual ID of a case is not available in simple search

## [5.5.4](https://github.com/netgrif/application-engine/releases/tag/5.5.4) (2021-12-20)

### Fixed

- [NAE-1522] Files with comma in name cannot be downloaded
- [NAE-1536] Values with dash cannot be typed into simple search

## [5.5.2](https://github.com/netgrif/application-engine/releases/tag/5.5.2) (2021-07-19)

## [5.5.1](https://github.com/netgrif/application-engine/releases/tag/5.5.1) (2021-07-09)

### Fixed

- [NAE-1374] setData does not propagate changed allowedNets to frontend
- [NAE-1323] Initial value of collection fields is broken

## [5.5.0](https://github.com/netgrif/application-engine/releases/tag/5.5.0) (2021-06-11)

### Fixed

- [NAE-1319] Default caseRef value is set to null
- [NAE-1316] Currency format is not shown on number field

### Added

- [NAE-1305] Loading na set data pre button

## [5.4.1](https://github.com/netgrif/application-engine/releases/tag/5.4.1) (2021-05-31)

## [5.4.0](https://github.com/netgrif/application-engine/releases/tag/5.4.0) (2021-05-27)

### Fixed

- [NAE-1211] Search in several data fields does not work as intended

### Changed

- [NAE-1228] Re-enable class name minification
- [NAE-1207] Filter management
- [NAE-412] Process and Case loading refactor

## [5.3.1](https://github.com/netgrif/application-engine/releases/tag/5.3.1) (2021-05-20)

### Fixed

- [NAE-1306] Custom message of dynamic validation is not propagated in import
- [NAE-1308] Cancel Task permission validation error

## [5.3.0](https://github.com/netgrif/application-engine/releases/tag/5.3.0) (2021-05-05)

### Changed

- [NAE-58] Dynamic Init values
- [NAE-1276] Init value as choice

### Added

- [NAE-1251] Dynamic validations

## [5.2.0](https://github.com/netgrif/application-engine/releases/tag/5.2.0) (2021-05-03)

### Fixed

- [NAE-1171] File-list Field has initial value type of string
- [NAE-1280] Default case name i18n
- [NAE-1279] Browser login prompt

### Changed

- [NAE-1242] TaskRef init value
- [NAE-1238] Case event view
- [NAE-1269] Enable user preferences for anonym user

## [5.1.2](https://github.com/netgrif/application-engine/releases/tag/5.1.2) (2021-04-16)

## [5.1.1](https://github.com/netgrif/application-engine/releases/tag/5.1.1) (2021-04-12)

### Fixed

- [NAE-1265] Anonym user inconsistency

### Changed

- [NAE-1248] Icon enum
- [NAE-1247] Backend autocomplete enumeration
- [NAE-1242] TaskRef init valueNone

## [5.1.0](https://github.com/netgrif/application-engine/releases/tag/5.1.0) (2021-3-25)

### Fixed

- [NAE-1216] Wrong render of FileListFieldValue in generated PDF
- [NAE-1227] Update loggedOrSystem
- [NAE-1257] Time trigger exact broken
- [NAE-1275] Multi-choiceMap init value

### Improvement

- [NAE-1217] File upload na public view
- [NAE-1235] MailDraft improvement
- [NAE-1236] Anonym user persistence
- [NAE-1240] Add remove role to Action Delegate
- [NAE-1250] TaskRef of the same case propagation

## [5.0.7](https://github.com/netgrif/application-engine/releases/tag/5.0.7) (2021-03-01)

### Fixed

- [NAE-1257] Time trigger exact broken

## [5.0.6](https://github.com/netgrif/application-engine/releases/tag/5.0.6) (2021-02-24)

### Changed

- [NAE-1250] TaskRef of the same case propagation

## [5.0.5](https://github.com/netgrif/application-engine/releases/tag/5.0.5) (2021-02-19)

## [5.0.4](https://github.com/netgrif/application-engine/releases/tag/5.0.4) (2021-02-19)

## [5.0.3](https://github.com/netgrif/application-engine/releases/tag/5.0.3) (2021-02-09)

## [5.0.2](https://github.com/netgrif/application-engine/releases/tag/5.0.2) (2021-02-07)

## [5.0.1](https://github.com/netgrif/application-engine/releases/tag/5.0.1) (2021-02-04)

## [5.0.0](https://github.com/netgrif/application-engine/releases/tag/5.0.0) (2021-02-28)

### Fixed

- [NAE-1097] Null file field value set by assign action
- [NAE-1144] Elastic runner doesn't create indices without drop
- [NAE-1181] Broken regex validation in required data field
- [NAE-1185] Auto-open subtree not working correctly
- [NAE-1194] File preview is triggering error message 'downloading failed'
- [NAE-1197] Excessive memory consuption
- [NAE-1199] Loading data task
- [NAE-1201] MailDraft null values
- [NAE-1206] Borken login

### Changed

- [NAE-1028] Anonymous access
- [NAE-1149] Immediate task data
- [NAE-1186] Negatively defined roles permissions
- [NAE-492] MongoDB v4
- [NAE-1092] Unifying attributes names in filters
- [NAE-1180] Allow for developer to define which fields to be exported to PDF
- [NAE-1195] Update user lists according to roleRef update
- [NAE-1196] Additional actions for user management

### Added

- [NAE-1054] Public view
- [NAE-1056] File view with content preview
- [NAE-1059] JWT Authentication
- [NAE-1115] User list on task instead of roles
- [NAE-1119] Constructor and destructor as process meta-data
- [NAE-1175] Import helper upsert
- [NAE-1203] User delete

## [4.6.0](https://github.com/netgrif/application-engine/releases/tag/4.6.0) (2021-01-20)

### Fixed

- [NAE-1202] Bug in ChangedFieldTree

### Changed

- [NAE-1109] Task ref set data propagation

## [4.5.0](https://github.com/netgrif/application-engine/releases/tag/4.5.0) (2020-12-22)

### Fixed

- [NAE-1161] PDF tool wrongly generated enumerations, multi-choice, and HTML
- [NAE-1162] PDF tool null-pointer on layout

### Changed

- [NAE-1178] Cancel and finish error changed fields propagation

### Added

- [NAE-1168] Task ref representation
- [NAE-1172] Frontend control from process actions

## [4.4.0](https://github.com/netgrif/application-engine/releases/tag/4.4.0) (2020-12-14)

### Fixed

- [NAE-1096] Change MultichoiceMapField/EnumerationMapField value na null hodnotu nefunguje
- [NAE-1100] Set UserField data after finish
- [NAE-1102] Broken definition of Multichoice map
- [NAE-1136] Delegate endpoint doesn't work

### Changed

- [NAE-1109] Task ref set data propagation
- [NAE-1139] Improve overridability of petri net service
- [NAE-1142] Whitelist URLs trough application properties

## [4.3.1](https://github.com/netgrif/application-engine/releases/tag/4.3.1) (2020-11-13)

## [4.3.0](https://github.com/netgrif/application-engine/releases/tag/4.3.0) (2020-11-11)

### Fixed

- [NAE-986] Virtual scroll tab initialization broken
- [NAE-1006] Broken headers' alignment with 9 columns
- [NAE-1035] PDF Generator issue due to layout
- [NAE-1039] PetriNetResourceService bad return type
- [NAE-1041] Page interface not exported
- [NAE-1062] Extra process page loaded
- [NAE-1064] Required
- [NAE-1065] Fix bug with redirect service
- [NAE-1066] Fix Empty list text when list has loading
- [NAE-1069] Enumeration and multi-choice options
- [NAE-1070] Nested taskRef position calculating
- [NAE-1073] Registration component stuck with bad token
- [NAE-1074] User cannot register with new groups
- [NAE-1076] SignUp endpoint doesn't use email from token
- [NAE-1078] Tab labels are unaffected by language change
- [NAE-1081] Deprecated view attributes are ignored
- [NAE-1082] Incorrect German translation in workflow view
- [NAE-1083] Enforce minimum password length
- [NAE-1084] LoadAllPages utility function break if there is no content
- [NAE-1094] Current CaseTree node reloads incorrectly

### Changed

- [NAE-997] Engine docker image
- [NAE-1002] Boolean field visible values
- [NAE-1008] Update demo application
- [NAE-1014] Pass DI context into ComponentPortals
- [NAE-1020] Change empty value of number field from 0 to 'empty'
- [NAE-1023] Password view for text field
- [NAE-1025] Update Petriflow XSD schema
- [NAE-1027] Event phases for data fields events
- [NAE-1063] Display only the newest process version in create case
- [NAE-1077] Component tag sending all included information

### Added

- [NAE-994] Group Management
- [NAE-1021] App to Docker guidelines
- [NAE-1026] Filter by group
- [NAE-1031] Case delete button
- [NAE-1048] Process delete
- [NAE-1075] Recover account component

## [4.2.1](https://github.com/netgrif/application-engine/releases/tag/4.2.1) (2020-9-23)

### Fixed

- [NAE-1024] Redis namespace is not set from application properties
- [NAE-1001] Security Config
- [NAE-1010] 500 error po zavolaní getData v taskRefe
- [NAE-1011] Redis deserialization error
- [NAE-1012] Broken parsing of data fields
- [NAE-1013] Reindex via ElasticController ignores some cases
- [NAE-1015] Data group's title is not showing
- [NAE-1034] Reindex size property not loaded

### Changed

- [NAE-1022] Repair and update swagger definitions

### Added

None

## [4.2.0](https://github.com/netgrif/application-engine/releases/tag/4.2.0) (2020-9-21)

### Fixed

- [NAE-979] Set data on component Rich Text Area

### Changed

- [NAE-966] Task search on MongoDB refactor
- [NAE-495] Dashboard
- [NAE-301] Add key param to data-fields of type enumeration and multi-choice
- [NAE-267] Action result propagation

### Added

- [NAE-980] Create HTML textarea field
- [NAE-542] Dashboard Card

## [4.1.2](https://github.com/netgrif/application-engine/releases/tag/4.1.2) (2020-9-10)

### Fixed

- [NAE-970] TaskReference File Upload ResolveActions problem
- [NAE-977] Taskref layout broken

## [4.1.1](https://github.com/netgrif/application-engine/releases/tag/4.1.1) (2020-8-20)

### Fixed

- [NAE-959] Broken getReferencesByVersion if version is null
- [NAE-962] Quartz scheduler duplicate initialisation

### Changed

- [NAE-480] Task Reindex Task

## [4.1.0](https://github.com/netgrif/application-engine/releases/tag/4.1.0) (2020-08-17)

### Fixed

- [NAE-912] Task with no data cannot be finished
- [NAE-824] Action param parsing regex

### Changed

- [NAE-949] Case ID search param
- [NAE-928] Add conditional property for all controllers
- [NAE-927] Add getOne Case endpoint
- [NAE-874] Customisable data field offset
- [NAE-804] Overridable endpoint authorisation

### Added

- [NAE-952] Search tasks by transition
- [NAE-917] File list field
- [NAE-852] Rule engine
- [NAE-876] Tree Case View
- [NAE-396] PDF generator

## [4.0.1](https://github.com/netgrif/application-engine/releases/tag/4.0.1) (2020-08-10)

### Fixed

- [NAE-930] Duplicite roles after import of new net
- [NAE-926] ElasticTaskService creates cyclic dependency
- [NAE-883] Net Versioning Broken
- [NAE-484] Version is stored as String
- [NAE-878] UserAssignComponent With Pagination

## [4.0.0](https://github.com/netgrif/application-engine/releases/tag/4.0.0) (2020-07-24)

### Fixed

- [NAE-897] Action Delegate Context Leak

### Changed

- [NAE-849] Boolean data-field validation

### Added

- [NAE-822] Data field layout data

<!-- Template
## [version](https://github.com/netgrif/application-engine/releases/tag/v) (YYYY-MM-dd)

### Added
 - for new features.

### Changed
 - for changes in existing functionality.

### Deprecated
 - for soon-to-be removed features.

### Removed
 - for now removed features.

### Fixed
 - for any bug fixes.

### Security
 - in case of vulnerabilities.
-->
