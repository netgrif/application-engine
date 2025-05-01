# AssignedUserPolicy

This option determines if the assigned user can reassign or cancel their assigned task.
It can be defined on the transition as follows:

```xml
<assignedUser>
            <cancel>false</cancel>
            <reassign>false</reassign>
</assignedUser>
```

If there is no assigned user policy, the engine behaves the same as normal.
