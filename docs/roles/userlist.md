# UserList and UsersRef

## UserList

UserList is a type of data field. Values of this field represent users in system. Some basic information about
this field:

- this field does not have any init value, because when the process is created or imported, the given user may not exist
- we can add values to this field via actions and using frontend component
- it can serve as definition of permissions for volume of users
- value type of this field is ``UserListFieldValue``

```xml
<data type="userList">
  <id>userList1</id>
  <title/>
</data>
```

Example action to change value of this field:
```xml
<action trigger="set">
    userList: f.userList1;
    
    <!-- Setting with list of user IDs -->
    change userList value { ["userId1", "userId2"] }
    
    <!-- Setting with UserListFieldValue -->
    change userList value {
        new com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue(
            [
                new com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue(
                    "userId1",
                    "John",
                    "Doe",
                    "john@doe.com"
                ),
                new com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue(
                    "userId2",
                    "Alice",
                    "Doe",
                    "alice@doe.com"
                )
            ]
        )
    }
</action>
```

## UserRef

It is a new property of process and transition in PetriNet. It serves as a roleRef with a difference from it: the
content of the userList can be changed at runtime.

- userRef references userList defined with its ID
- we define permissions for usersRef in a same way as for roleRef

```xml
<document>
  ...
  <data type="userList">
    <id>userList1</id>
    <title/>
  </data>
  ...
  <userRef>
    <id>userList1</id>
    <caseLogic>
      <view>true</view>
      <delete>true</delete>
    </caseLogic>
  </userRef>
  ...
  <transition>
    <id>1</id>
    <usersRef>
      <id>userList1</id>
      <logic>
        <perform>true</perform>
      </logic>
    </usersRef>
  </transition>
</document>
```

## Setting permissions

If we want to define permission only for a set of users, and we want to change the content of this set runtime, the
userList-usersRef combo is the best way to do so. You have to follow these steps:

1. Define new data field of type **userList** - required attribute is only the *id* of field.
2. For case permissions, define **usersRef** in *document* tag, for task permission define it in the corresponding *
   transition* tag
3. Into *logic* property of usersRef we define the permissions with boolean values - true means enable, false mean
   disable the given permission for user. The permissions can be the following:
    1. for cases:
        1. *view* - enable or disable the displaying of cases
        2. *delete* - enable or disable the deletion of cases
    2. for tasks:
        1. *perform* - enable or disable all the permissions
        2. *delegate* - enable or disable delegating tasks
        3. *assign* - enable or disable assign of tasks
        4. *cancel* - enable or disable canceling of tasks
        5. *finish* - enable or disable finish of tasks

[Permission resolution](roles/permissions.md?id=permissions)

[UsersRef Petri Net](../_media/roles/usersRef_functions.groovy)

[UsersRef Functions](../_media/roles/usersRef_net.xml)