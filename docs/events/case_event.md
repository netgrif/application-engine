# Case events

## Create event

<!-- panels:start -->
<!-- div:left-panel -->
The create event is a case event that is triggered when a new process instance is created.

The create event in its PRE phase is run without case context (i.e. `useCase` is `null`), like the upload event. The
create POST event has its case context set, since their case does exist during their execution, it is the newly created
case.

<!-- div:right-panel -->

```xml
<?xml version="1.0" encoding="UTF-8"?>
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:noNamespaceSchemaLocation="https://petriflow.com/petriflow.schema.xsd">
  <id>case_events</id>
  <title>Case Events example</title>
  <initials>CEE</initials>
  <caseEvents>
    <event type="create">
      <id>create</id>
      <actions phase="pre">
        <action>
          ...
        </action>
      </actions>
      <actions phase="post">
        <action>
          ...
        </action>
      </actions>
    </event>
  </caseEvents>
</document>
```

<!-- panels:end -->

### Create event permission

<!-- panels:start -->
<!-- div:left-panel -->
Roles can be associated with the create event and can be used to restrict who is able to create new instances of the
process.

User lists cannot be associated with the create event, since they don't exist before a case instance is created.

<!-- div:right-panel -->

```xml
<?xml version="1.0" encoding="UTF-8"?>
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:noNamespaceSchemaLocation="https://petriflow.com/petriflow.schema.xsd">
  <id>case_events</id>
  <title>Case Events example</title>
  <initials>CEE</initials>
  <!-- ROLES -->
  <role>
    <id>some_role</id>
    <title>Some Role</title>
  </role>
  <roleRef>
    <id>some_role</id>
    <caseLogic>
      <create>true</create>
    </caseLogic>
  </roleRef>
</document>
```

<!-- panels:end -->

## Delete event

<!-- panels:start -->
<!-- div:left-panel -->
The delete event is a case event that is triggered when a process instance is deleted.

The delete event in its POST phase is run without case context (i.e. `useCase` is `null`), like the upload event and
case create event. The delete event in PRE phase has its case context set, since its case does exist during their
execution.

<!-- div:right-panel -->

```xml
<?xml version="1.0" encoding="UTF-8"?>
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:noNamespaceSchemaLocation="https://petriflow.com/petriflow.schema.xsd">
  <id>case_events</id>
  <title>Case Events example</title>
  <initials>CEE</initials>
  <caseEvents>
    <event type="delete">
      <id>pdf</id>
      <actions phase="pre">
        <action>
          ...
        </action>
      </actions>
      <actions phase="post">
        <action>
          ...
        </action>
      </actions>
    </event>
  </caseEvents>
</document>
```

<!-- panels:end -->

### Create event permission

<!-- panels:start -->
<!-- div:left-panel -->
Roles can be associated with the delete event and can be used to restrict who is able to delete existing instances of
the process.

User lists can be associated with the delete event

<!-- div:right-panel -->

```xml
<?xml version="1.0" encoding="UTF-8"?>
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:noNamespaceSchemaLocation="https://petriflow.com/petriflow.schema.xsd">
  <id>case_events</id>
  <title>Case Events example</title>
  <initials>CEE</initials>
  <!-- ROLES -->
  <role>
    <id>some_role</id>
    <title>Some Role</title>
  </role>
  <roleRef>
    <id>some_role</id>
    <caseLogic>
      <delete>true</delete>
    </caseLogic>
  </roleRef>
</document>
```

<!-- panels:end -->