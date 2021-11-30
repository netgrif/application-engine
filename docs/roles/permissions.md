# Process permissions

In NAE, permissions represent a layer in security of processes modeled and implemented in Petriflow. Using permissions
the developer can define, who can see and edit the processes, cases and task, who can execute events and actions.

## Role

Role is an object in the Petriflow model, that is used for managing permissions of users to work with processes, cases
and tasks. A role can be referenced to process or transition to define permissions for cases and tasks and using NAE
APIs and controllers a role can be assigned to user.

### Predefined roles

In NAE, there are two predefined roles: **anonymous** and **default**. Every registered user has automatically assigned
the **default** role and every anonymous user has assigned the **anonymous** role. In XML, each can be enabled in the
root document element as follows:

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="petriflow_schema.xsd">
	...
	<defaultRole>true</defaultRole>
	<anonymousRole>true</anonymousRole>
	...
</document>
```

From NAE version 6.0.0, it is possible to reference these roles and defined permissions for them. If these roles are
enabled on process, but never referenced, each permission on these role will have default value **true**.

### Role definition

In the XML model of process, you can define roles as child elements of root element **document**  using **role**
element. The role is connected to role reference via the role's ID.

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="petriflow_schema.xsd">
	...
	<role>
		<id>process_role</id>
		<name>Proces role</name>
	</role>
	...
</document>
```

In the role definitions, the identifier and name of role can be defined.

### Role reference

Defined roles can be referenced with **roleRef** element, this element then will contain definition of permissions.
Permissions are explained [here](#Permissions). Roles can be referenced as follows:

- as child element of document for referencing roles on cases:

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="petriflow_schema.xsd">
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

- as child element of transition for referencing roles on tasks:

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="petriflow_schema.xsd">
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

In NAE, user list is a new type of data field, that is used for managing access of set of users (who's ID is in the
given user list) to Petriflow objects and their actions. User list can be defined where other data fields used to be
defined, as child element of the root **document** element:

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="petriflow_schema.xsd">
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

Defined user lists can be referenced with **userRef** element, this element will contain definition of permissions.
Permissions are explained [here](#Permissions). User list can be referenced as follows:

- as child element of document for referencing user list on cases:

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="petriflow_schema.xsd">
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

- as child element of transition for referencing user list on tasks:

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="petriflow_schema.xsd">
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
e.g. one role reference enables the permission, the other disables, one user reference enables the permission, the other
disables). Because of that, in NAE there is a rule to decide the final permission value for user:
$$((R_{p} \setminus R_{n}) \cup U_{p}) \setminus U_{n}$$

- $\setminus$ - seminus, e.g. $A \setminus B$ = every element from A that is not in B
- $\cup$ - union of sets
- $R_{p}$ - set of roles that are assigned to user and define given permission with true
- $R_{n}$ - set of roles that are assigned to user and define given permission with false
- $U_{p}$ - set of user lists that user is part of and define given permission with true
- $U_{n}$ - set of user lists that user is part of and define given permission with true

Explained in words: user will have the required permission ***IF** **HAS A ROLE** that's reference defines the
permission with **TRUE** **AND** in the same time **DOES NOT HAVE** a role that's reference defines the permission
with **FALSE**, **OR** the user **IS PART OF A USER LIST** that's reference defines the permission with **TRUE**, **
AND** **IS NOT PART OF A USER LIST** that's reference defines the permission with **FALSE**.*

There are two groups of permissions: for cases and for tasks.

### Case permissions

In the XML model of process, you can define permissions for Case using **roleRef** and **userRef** inside the root **
document** element. Each reference element has a child element called **caseLogic**, which can be used to define the
permissions for case created from process as follows:

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="petriflow_schema.xsd">
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

If a permission is never defined in any **roleRef** of **userRef**, the default value of permission is **true**.

#### Create

If this permission is set to **true** in **roleRef**, user with this permission is allowed to create cases from the
process. If it is **false**, user with this permission cannot create cases from given process. This permission **cannot
be defined** in **userRef**.

#### Delete

If this permission is set to **true**, user with this permission is allowed to delete cases created from the process. If
it is **false**, user with this permission cannot delete cases created from given process. This permission can be
defined in both **roleRef** and **userRef**.

#### View

If this permission is set to **true**, user with this permission is can see cases created from the process. If it is **
false**, user with this permission cannot see cases created from given process. This permission can be defined in
both **roleRef** and **userRef**.

### Task permissions

In the XML model of process, you can define permissions for Task using **roleRef** and **userRef** inside the **
transition** element. Each reference element has a child element called **logic**, which can be used to define the
permissions for task created from transition as follows:

```
<document xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="petriflow_schema.xsd">
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

If a permission is never defined in any **roleRef** of **userRef**, the default value of permission is **true**.

#### Assign

If this permission is set to **true**, user with this permission is can assign task to himself/herself created from the
transition. If it is **false**, user with this permission cannot assign task to himself/herself created from the
transition. This permission can be defined in both **roleRef** and **userRef**.

#### Cancel

If this permission is set to **true**, user with this permission is can cancel task created from the transition. If it
is **false**, user with this permission cannot cancel task created from the transition. This permission can be defined
in both **roleRef** and **userRef**.

#### Delegate

If this permission is set to **true**, user with this permission is can assign task to others created from the
transition. If it is **false**, user with this permission cannot assign task to others created from the transition. This
permission can be defined in both **roleRef** and **userRef**.

#### Finish

If this permission is set to **true**, user with this permission is can finish task created from the transition. If it
is **false**, user with this permission cannot finish task created from the transition. This permission can be defined
in both **roleRef** and **userRef**.

#### View

If this permission is set to **true**, user with this permission is can see task created from the transition. If it
is **false**, user with this permission cannot see task created from the transition. This permission can be defined in
both **roleRef** and **userRef**.

#### Set

If this permission is set to **true**, user with this permission is can set data on task created from the transition. If
it is **false**, user with this permission cannot set data on task created from the transition. This permission can be
defined in both **roleRef** and **userRef**.

#### Perform

It is a shortcut to define **assign, cancel, finish, view** and **set** permissions with single line of code. This
shortcut can be defined in both **roleRef** and **userRef**.

