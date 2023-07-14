# Frontend Actions
Frontend actions are type of actions that are executed on frontend. The action implementation is done as part of
frontend code and they can be called from inside data or transition events.

## Calling an action from process code

A frontend action is being called from inside events. The syntax is as follows:
```
Frontend.<action_name>(Object args...)
```

Example:
```
<event type="set">
    <id>data_set_event</id>
    <actions phase="post">
        <action id="action_0">
            Frontend.redirec("login")
        </action>
    </actions>
</event>
```

This code is sent to frontend as an attribute of `changedField` object, then frontend parses it 
and calls the required action.