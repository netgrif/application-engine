# ActorList and ActorRef

## ActorList

ActorList is a type of data field. Values of this field represent actors (users, groups, ...) in system. Some basic information about
this field:

- this field does not have any init value, because when the process is created or imported, the given actor may not exist
- we can add values to this field via actions and using frontend component
- it can serve as definition of permissions for volume of actors
- value type of this field is ``ActorListFieldValue``

```xml
<data type="actorList">
  <id>actorList1</id>
  <title/>
</data>
```

Example action to change value of this field:
```xml
<action trigger="set">
    actorList: f.actorList1;
    
    <!-- Setting with list of actor IDs -->
    change actorList value { ["userId1", "groupId1"] }
    
    <!-- Setting with ActorListFieldValue -->
    change actorList value {
        new com.netgrif.core.petrinet.domain.dataset.ActorListFieldValue(
            [
                new com.netgrif.core.petrinet.domain.dataset.UserFieldValue(
                    "userId1",
                    "realm1",
                    "John",
                    "Doe",
                    "john@doe.com"
                ),
                new com.netgrif.core.petrinet.domain.dataset.GroupFieldValue(
                    "groupId1",
                    "realm1",
                    "A group name"
                )
            ]
        )
    }
</action>
```

## ActorRef

It is a new property of process and transition in PetriNet. It serves as a roleRef with a difference from it: the
content of the actorList can be changed at runtime.

- actorRef references actorList defined with its ID
- we define permissions for actorRef in the same way as for roleRef

```xml
<document>
  ...
  <data type="actorList">
    <id>actorList1</id>
    <title/>
  </data>
  ...
  <actorRef>
    <id>actorList1</id>
    <caseLogic>
      <view>true</view>
      <delete>true</delete>
    </caseLogic>
  </actorRef>
  ...
  <transition>
    <id>1</id>
    <actorRef>
      <id>actorList1</id>
      <logic>
        <perform>true</perform>
      </logic>
    </actorRef>
  </transition>
</document>
```

## Setting permissions

If we want to define permission only for a set of actors, and we want to change the content of this set runtime, the
actorList-actorRef combo is the best way to do so. You have to follow these steps:

1. Define new data field of type **actorList** - required attribute is only the *id* of field.
2. For case permissions, define **actorRef** in *document* tag, for task permission define it in the corresponding *
   transition* tag
3. Into *logic* property of actorRef we define the permissions with boolean values - true means enable, false mean
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

[Permission resolution](permissions.md?id=permissions)

[ActorRef Petri Net](../_media/roles/actorRef_functions.groovy)

[ActorRef Functions](../_media/roles/actorRef_net.xml)
