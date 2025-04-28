# Elastic mapping

This page contains information about the structure and properties of Elasticsearch mapping of Case and Task objects.

## Elasticsearch field types

An overview of index field types supported by Elasticsearch can be found in
their [documentation](https://www.elastic.co/guide/en/elasticsearch/reference/6.6/mapping-types.html).

Refer to this documentation when index types are mentioned throughout this document. Some fields are designated
as `Arrays`, as per the referenced documentation, any index in Elasticsearch can contain multiple values with the same
type. When this document mentions `Array` type fields, we mean that the Application Engine is actively storing multiple
entries into the value of this field. The Application Engine stores only single values in non-array fields mentioned in
this document.

Every field that uses the `TEXT` type is also indexed as a `KEYWORD` type field, for other purposes (such as sorting).
When an attribute maps to some `TEXT` field `some.field.textValue` then its `KEYWORD` representation is always mapped
to `some.field.textValue.keyword`.

All the mapping configurations are implemented with class annotations. If you want to modify the default mapping, you
can extend the default engine classes, inherit the default mapping and override only some parts of it.

## Task

Tasks are converted into their `ElasticTask` counterparts by the `IElasticTaskMappingService`. You should only
create `ElasticTask` instances by converting an existing `Task` object with the service. You should never use the
constructor to create instances of this class.

### Indexed fields

Case is always the “parent“ case to which the task belongs.

Process is always the “parent“ process from which the case and the task were created.

|index|type|value|
|-----|----|-----|
|`stringId`|Keyword|the mongo ID of the task|
|`processId`|Keyword|the mongo ID of the process|
|`caseId`|Keyword|the mongo ID of the case|
|`transitionId`|Keyword|the import ID of the transition represented by the task|
|`title`|Text|title of the task|
|`titleSortable`|Keyword|title of the task|
|`caseColor`|Keyword|color code of the case|
|`caseTitle`|Text|title of the case|
|`caseTitleSortable`|Keyword|title of the case|
|`priority`|Integer|task priority|
|`userId`|Long|assignee user ID|
|`startDate`|Integer array|date of last assignment stored in the nae date format - an array with the year, month, day, hour, minute, second and timestamp|
|`transactionId`|Keyword|ID of the transaction|
|`roles`|Keyword array|mongo IDs of positive roles associated with the task|
|`negativeViewRoles`|Keyword array|mongo IDs of negative view roles associated with the task|
|`users`|Keyword array|user IDs of users in user lists associated with the task|
|`negativeViewUsers`|Long array|user IDs of users in negative view user lists associated with the task|
|`icon`|Keyword|task material icon|
|`assignPolicy`|Keyword|task assign policy|
|`dataFocusPolicy`|Keyword|task data focus policy|
|`finishPolicy`|Keyword|task finish policy|

## Case

Cases are converted into their `ElasticCase` counterpart by the `IElasticCaseMappingService`. You should only
create `ElasticCase` instances by converting an existing `Case` instance with the service. You should never use the
constructor to create instances of this class.

ElasticCase contains the indexed data variable information of any data variable that is marked as `immediate` in the
process net. An overview of data variable indexation is provided in a separate section.

### Indexed fields

Process is always the “parent” process from which the case and its tasks were created.

Tasks are always the “child“ tasks owned by the case.

|index|type|value|
|-----|----|-----|
|`lastModified`|Long|timestamp of the last modification|
|`stringId`|Keyword|the mongo ID of the case|
|`visualId`|Text|visual ID of the case|
|`processIdentifier`|Keyword|the import ID of the process|
|`processId`|Keyword|the mongo ID of the process|
|`title`|Text|case title|
|`creationDate`|Integer array|creation date stored in the nae date format - an array with the year, month, day, hour, minute, second and timestamp as its elements in this order|
|`creationDateSortable`|Long|creation date stored as a timestamp|
|`authorName`|Text|name of the author|
|`authorEmail`|Text|author email|
|`dataSet`|Object|_see the next section for information about data variable indexation_|
|`taskIds`|Keyword array|import IDs of transitions with existing task instances|
|`taskMongoIds`|Keyword array|mongo IDs of existing task instances|
|`enabledRoles`|Keyword array|mongo IDs of roles associated with the case|
|`negativeViewRoles`|Keyword array|mongo Ids of negative view roles associated with the case|

## Dataset

All data variables marked as `immediate` are indexed according to the rules described in this section.

All data variables are stored in the `ElasticCase` entry under the `dataSet` key. The individual attributes that are
saved are prefixed by the import ID of the data variable. E.g. if we have an immediate text variable with the
ID `myTextVariable` its value would be indexed under: `dataSet.myTextVariable.textValue`

Each data variable type has a different attribute mapping associated with it. All the mappings follow a common pattern.

You can implement your own data variable mappings by overriding the `IElasticCaseMappingService`.

An overview of all mapping configuration classes follows. The associated data variable types are listed with each class.
A data variable might be associated with more than one class because of inheritance.

If a data variable has an `I18nString` value, all the associated translations are set as values. For example an
enumeration field with the default translation value _Dog_ and both a German and a Slovak translation, would be stored
as _\[Dog, Hund, Pes\]_ in the index. This way, the fields value can be searched in all supported locales.

### DataField

Base class for data field mapping. All indexed data variables have these attributes available.

|index|type|value|
|-----|----|-----|
|`dataSet.<fieldID>.fulltextValue`|Text array|value depends on the field type:<br>**boolean**: textual representation of the value<br>**date** & **dateTime**: date formated as ISO-8601 basic local date format<br>**file** & **fileList**: names of the contained files<br>**number**: the value stored as decimal string<br>**text**: the value itself<br>**multichoice** & **enumeration**: all translations of the selected options<br>**multichoiceMap** & **enumerationMap**: all translated values of the selected key-value pairs<br>**user** & **userList**: full name followed by email as a single string for each selected user|

### BooleanField - boolean

|index|type|value|
|-----|----|-----|
|`dataSet.<fieldID>.booleanValue`|Boolean|value of the data variable stored as boolean|

### DateField - date, dateTime

|index|type|value|
|-----|----|-----|
|`dataSet.<fieldID>.dateValue`|Date|value of the data variable stored as date|
|`dataSet.<fieldID>.timestampValue`|Long|value of the data variable stored as timestamp|

### FileField - file, fileList

|index|type|value|
|-----|----|-----|
|`dataSet.<fieldID>.fileNameValue`|Text array|file names of the contained files|
|`dataSet.<fieldID>.fileExtensionValue`|Keyword array|file extensions of the contained files|

### NumberField - number

|index|type|value|
|-----|----|-----|
|`dataSet.<fieldID>.numberValue`|Double|value of the data variable|

### TextField - multichoice, multichoiceMap, enumeration, enumerationMap, text

This serves as the fallback type for any unsupported data variable type. If an unsupported type is found
its `toString()` value is stored.

|index|type|value|
|-----|----|-----|
|`dataSet.<fieldID>.textValue`|Text array|value of the data variable **multichoice** variants store all the selected values **map** variants store the _value_ part of the key-value pair (i.e. the part visible to the user)|

### MapField - multichoiceMap, enumerationMap

|index|type|value|
|-----|----|-----|
|`dataSet.<fieldID>.keyValue`|Keyword array|the _key_ part of the selected key-value pairs (i.e. the part stored as the map field value)|

### UserField - user, userList

|index|type|value|
|-----|----|-----|
|`dataSet.<fieldID>.emailValue`|Text array|emails of the selected users|
|`dataSet.<fieldID>.fullNameValue`|Text array|full names of the selected users|
|`dataSet.<fieldID>.userIdValue`|Long array|IDs of the selected users|