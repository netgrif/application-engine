# Process events

## Upload event

<!-- panels:start -->
<!-- div:left-panel -->

The upload event is a process event that is triggered when a new version of the net is deployed into the application engine.

Upload events are run without context (i.e. `useCase` is `null`). Therefore, it is not possible to run the changed fields actions, 
like the `change <field> value` command does not work as it relies on the `useCase` variable.
If you want to change data on another cases, you need to use the `setData` function, as in inter-process communication.

<!-- div:right-panel -->

```xml
<?xml version="1.0" encoding="UTF-8"?>
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="https://petriflow.com/petriflow.schema.xsd">
    <id>constructor_destructor</id>
    <title>Constructor and Destructor</title>
    <initials>CAD</initials>
    <processEvents>
        <event type="upload">
            <id>upload</id>
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
    </processEvents>
</document>
```

<!-- panels:end -->