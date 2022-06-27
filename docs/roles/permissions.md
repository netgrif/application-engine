# Process permissions

In NAE, permissions represent a layer in security of processes modeled and implemented in Petriflow. Using permissions
the developer can define, who can see and edit the processes, cases and tasks, who can execute events and actions.

## Role

Role is an object in the Petriflow model, that is used for managing permissions of users to work with processes, cases
and tasks. A role can be referenced on a process or a transition to define permissions for its cases and tasks and for using NAE
APIs and controllers. A role can be assigned to a user, thus applying the permissions it defines to them.

### Predefined roles

In NAE, there are two predefined roles: **anonymous** and **default**. Every registered user is automatically a member of
the **default** role and every anonymous user is a member of the **anonymous** role.
These roles cannot be removed from or granted to the users of the application by the developer.

These roles can be referenced in the model just like a normal developer-defined role by using their identifiers (`anonymous` and `default` respectively).

#### Applying predefined roles with default permissions

Since the developer may want to apply these roles to "all" transitions a shorthand syntax exists.
The default and the anonymous role can be applied independently of each other.  

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="https://petriflow.com/petriflow.schema.xsd">
	...
	<defaultRole>true</defaultRole>
	<anonymousRole>true</anonymousRole>
	...
</document>
```

The "enabled" role is applied with the default permissions (see the following section) to any transition and to the process
if no other positive association with another role or a user list is present. The default permissions are also not applied if
the role that is "enabled" already has a custom association of any sort (positive OR negative).

If the `<defaultRole>` or the `<anonymousRole>` tags are not present in the model the roles are not considered to be "enabled" and 
are therefore not automatically applied to the transitions and to the process. Beware that the NETGRIF application builder "enables"
the `<defaultRole>` by default, so that tasks in processes with no roles can be executed by any logged user.

The ability to associate the `anonymous` and the `default` role is **INDEPENDENT** of the values of the `<defaultRole>` and the `<anonymousRole>`
tags. The predefined roles can be associated with anything even if the value of the tags is set to `false`, since the tags only affect the 
automatic application of these roles to the process and to its transitions.

##### Examples

Both of the predefined roles behave in the same manner. If both are "enabled" at the same time, then both will be added if the conditions are met.
The conditions are evaluated independently of each other.

For these examples only the `default` role is used to demonstrate the principles. 

[//]: # (TODO maybe allow the reader to colapsable the examples section)

###### Predefined role will be added

```xml
<document>
    <!-- No process role/user ref -->
</document>
```

```xml
<document>
    <transition>
        <!-- No transition role/user ref -->
    </transition>
</document>
```

```xml
<document>
    <roleRef>
        <id>other</id>
        <caseLogic>
            <view>false</view>
        </caseLogic>
    </roleRef>
</document>
```

```xml
<document>
    <transition>
        <roleRef>
            <id>other</id>
            <logic>
                <view>false</view>
            </logic>
        </roleRef>
    </transition>
</document>
```

```xml
<document>
    <userRef>
        <id>other</id>
        <caseLogic>
            <view>false</view>
        </caseLogic>
    </userRef>
</document>
```

```xml
<document>
    <transition>
        <userRef>
            <id>other</id>
            <logic>
                <view>false</view>
            </logic>
        </userRef>
    </transition>
</document>
```

###### Predefined role will not be added

```xml
<document>
    <roleRef>
        <id>default</id>
        <caseLogic>
            <view>false</view>
        </caseLogic>
    </roleRef>
</document>
```

```xml
<document>
    <transition>
        <roleRef>
            <id>default</id>
            <logic>
                <view>false</view>
            </logic>
        </roleRef>
    </transition>
</document>
```

```xml
<document>
    <roleRef>
        <id>default</id>
        <caseLogic>
            <view>true</view>
        </caseLogic>
    </roleRef>
</document>
```

```xml
<document>
    <transition>
        <roleRef>
            <id>default</id>
            <logic>
                <view>true</view>
            </logic>
        </roleRef>
    </transition>
</document>
```

```xml
<document>
    <roleRef>
        <id>other</id>
        <caseLogic>
            <view>true</view>
        </caseLogic>
    </roleRef>
</document>
```

```xml
<document>
    <transition>
        <roleRef>
            <id>other</id>
            <logic>
                <view>true</view>
            </logic>
        </roleRef>
    </transition>
</document>
```

```xml
<document>
    <userRef>
        <id>other</id>
        <caseLogic>
            <view>true</view>
        </caseLogic>
    </userRef>
</document>
```

```xml
<document>
    <transition>
        <userRef>
            <id>other</id>
            <logic>
                <view>true</view>
            </logic>
        </userRef>
    </transition>
</document>
```

#### Default permissions of the predefined roles

##### The default role

On a task the `delegate` and the `perform` permissions are granted (All available permissions).

On a process the `create`, `delete` and `view` permissions are granted (All available permissions).

##### The anonymous role

On a task the `perform` permission is granted.

On a process the `create` and `view` permissions are granted.

### Role definition

In the XML model of the process, you can define roles as child elements of the root element **document** using the **role**
element. The role is connected to the role reference via the role's ID.

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="https://petriflow.com/petriflow.schema.xsd">
	...
	<role>
		<id>process_role</id>
		<name>Proces role</name>
	</role>
	...
</document>
```

In the role definition, the identifier and the name of the role can be defined.

The `default` and `anonymous` are reserved role identifiers and cannot be used when defining roles.

### Role reference

Defined roles can be referenced with **roleRef** element, this element will then contain the definition of permissions.
Permission documentation can be found [here](#Permissions). Roles can be referenced as follows:

- as a child element of the `document` tag for referencing roles on cases:

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="https://petriflow.com/petriflow.schema.xsd">
	...
	<roleRef>
		<id>process_role</id>
		<caseLogic>
			<create>false</view>
			<view>true</view>
		</caseLogic>
	</roleRef>
	...
</document>
```

- as a child element of the `transition` tag for referencing roles on tasks:

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="https://petriflow.com/petriflow.schema.xsd">
	...
	<transition>
		...
		<roleRef>
		<id>process_role</id>
			<logic>
				<finish>false</view>
				<view>true</view>
			</logic>
		</roleRef>
		...
	</transition>
	...
</document>
```

## User list

In NAE, user list is a type of data field, that is used for managing access of a set of users (who's ID is in the
given user list) to Petriflow objects and their actions. User list can be defined where other data fields used to be
defined, as child element of the root **document** element:

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="https://petriflow.com/petriflow.schema.xsd">
	...
	<data type="userList">
		<id>user_list_1</id>
		<title>User list 1</title>
	</data>
	...
</document>
```

The value of this data field can be modified and managed using Actions API, as in case other types of data field.

### User reference

Defined user lists can be referenced with **userRef** element, this element will contain the definition of permissions.
Permission documentation can be found [here](#Permissions). User list can be referenced as follows:

- as a child element of the `document` tag for referencing user list on cases:

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="https://petriflow.com/petriflow.schema.xsd">
	...
	<userRef>
		<id>user_list_1</id>
		<caseLogic>
			<create>false</view>
			<view>true</view>
		</caseLogic>
	</userRef>
	...
</document>
```

- as a child element of the `transition` tag for referencing user list on tasks:

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="https://petriflow.com/petriflow.schema.xsd">
	...
	<transition>
		...
		<userRef>
		<id>user_list_1</id>
			<logic>
				<finish>false</view>
				<view>true</view>
			</logic>
		</userRef>
		...
	</transition>
	...
</document>
```

## Permissions

Permissions can manage access and execution rights to Case and Task objects. These permissions are assigned to user
through **roleRef** and **userRef**. There may be the case, when a user can have multiple role assigned and be present
in multiple user list, and the references of the roles and user lists define the same permission but with other flags (
e.g. one role reference grants the permission, the other forbids it, one user list reference grants the permission, the other
forbids it). These complex situations are always resolved according to the following rule:

$$((R_{p} \setminus R_{n}) \cup U_{p}) \setminus U_{n}$$

- $\setminus$ - seminus, e.g. $A \setminus B$ = every element from A that is not in B
- $\cup$ - union of sets
- $R_{p}$ - set of roles that are assigned to user and define given permission with `true` (grant the permission)
- $R_{n}$ - set of roles that are assigned to user and define given permission with `false` (forbid the permission)
- $U_{p}$ - set of user lists that user is part of and define given permission with `true` (grant the permission)
- $U_{n}$ - set of user lists that user is part of and define given permission with `false` (forbid the permission)

Explained in words:
A user list is stronger than a role and a forbidding/revoking (negative - `false`) association is stronger than a granting (positive - `true`) association. 
A user must be granted a permission from at least one source in order to be allowed to perform an operation.
A granting (positive) user list association overrides a forbidding (negative) role association.
A forbidding (negative) user list association overrides any granting (positive) association.

There are two types of permissions - case permissions and task permissions.

### Case permissions

In the XML model of process, you can define permissions for Case using **roleRef** and **userRef** inside the root **document** element.
Each reference element has a child element called **caseLogic**, which can be used to define the
permissions for case created from process as follows:

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="https://petriflow.com/petriflow.schema.xsd">
...
	<roleRef>
		<id>process_role</id>
		<caseLogic>
			<create>false</view>
			<delete>false</delete>
			<view>false</view>
		</caseLogic>
	</roleRef>
	...
	<userRef>
		<id>user_list_1</id>
		<caseLogic>
			<create>true</view>
			<delete>true</delete>
			<view>true</view>
		</caseLogic>
	</userRef>
	...
</document>
```

#### Create

If this permission is set to **true** in **roleRef**, user with this permission is allowed to create cases from the
process. If it is **false**, user with this permission cannot create cases from the given process. This permission **cannot
be defined** in a **userRef**.

#### Delete

If this permission is set to **true**, user with this permission is allowed to delete cases created from the process. If
it is **false**, user with this permission cannot delete cases created from given process. This permission can be
defined in both **roleRef** and **userRef**.

#### View

If this permission is set to **true**, user with this permission can see cases created from the process. If it is
**false**, user with this permission cannot see cases created from given process. This permission can be defined in
both **roleRef** and **userRef**.

### Task permissions

In the XML model of process, you can define permissions for Task using **roleRef** and **userRef** inside the **
transition** element. Each reference element has a child element called **logic**, which can be used to define the
permissions for task created from transition as follows:

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="https://petriflow.com/petriflow.schema.xsd">
	...
	<transition>
		...
		<roleRef>
			<id>process_role</id>
			<logic>
				<perform>true</perform>
				<finish>false</view>
				<view>true</view>
			</logic>
		</roleRef>
		...
		<userRef>
			<id>user_list_1</id>
			<logic>
				<finish>false</view>
				<view>true</view>
			</logic>
		</userRef>
		...
	</transition>
	...
</document>
```

#### Assign

If this permission is set to **true**, user with this permission can assign task to themselves created from the
transition. If it is **false**, user with this permission cannot assign task to themselves created from the
transition. This permission can be defined in both **roleRef** and **userRef**.

#### Cancel

If this permission is set to **true**, user with this permission can cancel task created from the transition. If it
is **false**, user with this permission cannot cancel task created from the transition. This permission can be defined
in both **roleRef** and **userRef**.

#### Delegate

If this permission is set to **true**, user with this permission can assign task to others created from the
transition. If it is **false**, user with this permission cannot assign task to others created from the transition. This
permission can be defined in both **roleRef** and **userRef**.

#### Finish

If this permission is set to **true**, user with this permission can finish task created from the transition. If it
is **false**, user with this permission cannot finish task created from the transition. This permission can be defined
in both **roleRef** and **userRef**.

#### View

If this permission is set to **true**, user with this permission can see task created from the transition. If it
is **false**, user with this permission cannot see task created from the transition. This permission can be defined in
both **roleRef** and **userRef**.

#### Set

If this permission is set to **true**, user with this permission can set data on task created from the transition. If
it is **false**, user with this permission cannot set data on task created from the transition. This permission can be
defined in both **roleRef** and **userRef**.

#### Perform

It is a shortcut to define **assign, cancel, finish, view** and **set** permissions with single line of code. This
shortcut can be defined in both **roleRef** and **userRef**. A perform permission does not exist by itself, instead it is
translated into its components when the process is imported.
