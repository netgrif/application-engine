# UserList and UsersRef

## UserList

UserList is a type of data field. Values of this field represent ID of users in the system. Some basic information about
this field:

- this field currently does not have frontend representation
- this field does not have any init value, because when the process is created or imported, the given user may not exist
- we can add values to this field via actions
- it can serve as definition of permissions for volume of users

```xml
<data type="userList">
  <id>userList1</id>
  <title/>
</data>
```

## UsersRef

It is a new property of process and transition in PetriNet. It serves as a roleRef with a difference from it: the
content of the userList can be changed runtime.

- usersRef references userList defined with its ID
- we define permissions for usersRef in a same way as for roleRef

```xml
<document>
  ...
  <data type="userList">
    <id>userList1</id>
    <title/>
  </data>
  ...
  <usersRef>
    <id>userList1</id>
    <caseLogic>
      <view>true</view>
      <delete>true</delete>
    </caseLogic>
  </usersRef>
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

## Permissions and their flags

If *delete* permission is **NOT** defined in usersRef or roleRef a **OR**
it is set for **TRUE**, then user, who is part of userList that is referenced in usersRef **OR** has the role, that is
referenced in roleRef, **HAS**
permission to delete given case. In other case when *delete* is set to **FALSE**, the user does not have the right to
delete the case.

If *view* is not defined in any usersRef and roleRef, it does not have any influence to displaying cases. If there is
one or more usersRef, that have the view permission set for **TRUE**, then for users, who are part of the userList, that
is referenced in usersRef, the cases will be displayed. In this case there is a union of volumes of users, that are
included in one of the userLists. Users, that are not in any of the usersList, will not see the case. The situation is
the same in case of roleRefs.

If *view* is defined in usersRef or roleRef, and it is set to **FALSE**, members of userList that is referenced in
usersRef or users that have the role that is referenced in roleRef, will not see the case. In this case there is a union
of users that are not allowed to view the case. Users outside these volumes are allowed to see the cases.

It is possible to combine the volumes defined with usersRef and roleRef. In case that there are view permissions defined
on usersRef and roleRefs in the same time but with different flags, tha **FALSE** flag will have priority.

### Table of permissions

|               |              |              | Does user see case when… ? |          |                            |                           |
|---------------|--------------|--------------|----------------------------|----------|----------------------------|---------------------------|
| UsersRef View | RoleRef View | Default Role | is in UserRef              | has Role | is in UserRef and Has role | does not have any of this |
| -             | -            | TRUE            | -                          | -        | -                          | Yes                       |
| -             | TRUE            | TRUE            | -                          | Yes      | -                          | No                        |
| -             | FALSE            | TRUE            | -                          | No       | -                          | Yes                       |
| TRUE             | -            | TRUE            | Yes                        | -        | -                          | No                        |
| TRUE             | TRUE            | TRUE            | Yes                        | Yes      | Yes                        | No                        |
| TRUE             | FALSE            | TRUE            | Yes                        | No       | No                         | No                        |
| FALSE             | -            | TRUE            | No                         | -        | -                          | Yes                       |
| FALSE             | TRUE            | TRUE            | No                         | Yes      | No                         | No                        |
| FALSE             | FALSE            | TRUE            | No                         | No       | No                         | Yes                       |
| -             | -            | FALSE            | -                          | -        | -                          | No                        |
| -             | TRUE            | FALSE            | -                          | Yes      | -                          | No                        |
| -             | FALSE            | FALSE            | -                          | No       | -                          | No                        |
| TRUE             | -            | FALSE            | Yes                        | -        | -                          | No                        |
| TRUE             | TRUE            | FALSE            | Yes                        | Yes      | Yes                        | No                        |
| TRUE             | FALSE            | FALSE            | Yes                        | No       | No                         | No                        |
| FALSE             | -            | FALSE            | No                         | -        | -                          | No                        |
| FALSE             | TRUE            | FALSE            | No                         | Yes      | No                         | No                        |
| FALSE             | FALSE            | FALSE            | No                         | No       | No                         | No                        |

[UsersRef Petri Net](../_media/roles/usersRef_functions.groovy)

[UsersRef Functions](../_media/roles/usersRef_net.xml)