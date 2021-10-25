# Tree Case View

The Case ref data variable can be used to create tree structures from cases. The Tree Case View is used to visualise
these tree structures.

The tree structure is governed by the data of the process(es) and the View only displays it. In order for the View to be
able to display the structure, the underlying processes must contain specific elements (mostly data variables), that
contain the data necessary for visualisation of the tree structure.

## Process Interface of Tree nodes

If you want to display the tree structure, then the processes that form it must implement the following "process
interface".

### Data

```xml

<data type="boolean" immediate="true">
  <id>canAddTreeChildren</id>
  <title></title>
  <init>true</init>
</data>

<data type="text" immediate="true">
<id>treeTaskTransitionId</id>
<title></title>
<init>1</init>
</data>

<data type="text" immediate="true">
<id>childNodeCaseTitle</id>
<title></title>
<init>Child Node Name From Net</init>
</data>

<data type="boolean" immediate="true">
<id>canRemoveTreeNode</id>
<title></title>
<init>true</init>
</data>

<data type="caseRef" immediate="true">
<id>treeChildCases</id>
<title></title>
<allowedNets>
  <allowedNet>tree_test</allowedNet>
  <allowedNet>tree_test_2</allowedNet>
  <allowedNet>tree_test_3</allowedNet>
  <allowedNet>tree_test_4</allowedNet>
  <allowedNet>tree_test_5</allowedNet>
  <allowedNet>tree_test_6</allowedNet>
</allowedNets>
</data>

<data type="text" immediate="true">
<id>beforeTextIcon</id>
<title>Icon before text</title>
<init>twitch</init>
</data>

<data type="text" immediate="true">
<id>treeAddIcon</id>
<title>Add Icon</title>
<init>youtube</init>
</data>
```

All data variables in the process interface must be `immediate`, since the View uses case data to access them. If they
are not marked as `immediate`, the frontend won't be able to access them and will fall back to the default behavior.

Note that each node in the tree may have different values of the tree attributes and can therefore behave differently
than other nodes in the tree.

* **canAddTreeChildren** _BOOLEAN_ - decides whether the user can add new children of this node. Note that this does not
  disallow the node from having children in the first place.
* **treeTaskTransitionId** _TEXT_ - decides which task of the node case should be displayed when the node is selected in
  the Tree Case View. If no value is set, no task will be displayed. If the value changes the displayed task will
  change.
* **childNodeCaseTitle** _TEXT_ - decides the name of new child node instances. If no value is set a default value is
  used. The default value is defined in the frontend code and is translated according to the selected language.
  Translation key: `caseTree.newNodeDefaultName`.
* **canRemoveTreeNode** _BOOLEAN_ - decides whether the node can be deleted by the user or not. When a node is deleted,
  its case and all the cases of the subtree rooted at this node are deleted.
* **treeChildCases** _CASE REF_ - the node stores a reference to its children in this variable. In the **allowedNets**
  of this field are stored the identifiers of the nets that can be added as children of this node.
* **beforeTextIcon** _TEXT_ - a reference to the icon that should be displayed in front of the name of this node. The
  icon must first be imported into the frontend, see the "Icon import" section below for more information.
* **treeAddIcon** _TEXT_ - a reference to the icon that should be displayed instead of the standard "add child" icon for
  this node. The icon must first be imported into the frontend, see the "Icon import" section below for more
  information.

The Tree Case View is operational even if all the specified data variables are missing. The available functionality is
limited based on the missing variables. Understandably, if the node lacks the case ref variable for storing children, it
cannot have child nodes.

### Transitions

```xml

<transition>
  <id>treeCaseRefAccessor</id>
  <x>300</x>
  <y>150</y>
  <label></label>
  <dataRef>
    <id>treeChildCases</id>
    <logic>
      <behavior>forbidden</behavior>
    </logic>
    <layout>
      <x>1</x>
      <y>0</y>
      <rows>1</rows>
      <cols>2</cols>
      <template>netgrif</template>
    </layout>
  </dataRef>
</transition>
```

The tree process interface defines one transition in addition to the data variables. This transition must have the ID **
treeCaseRefAccessor** and it must contain a reference to the **treeChildCase** data variable (Case ref). This transition
may not be always executable, but this transition must be able to execute when a new child is added to the node from the
Tree Case View. If the transition cannot be executed, the child node will not be added.

Since the Case ref data variable type currently does not have a frontend representation (other than the Tree Case View)
it cannot be displayed within a task and the frontend will throw an error. We recommend setting the behavior of the Case
ref on this (and any other) task to _FORBIDDEN_, or ensuring that the task will never be displayed on the frontend.

If the process contains any roles, you must assure, that this task can be executed by the user that is adding the new
children to the tree via the Tree Case View.

## Properties of the Tree Case View

The first (root) level of the tree is determined by a filter and may contain a single node. The tree can be configured
in such a way, that the first level is hidden, and its child nodes are then displayed in the first level. If the root
node is hidden there is a way to add children to it even if it is not visible to the user, so that first level children
can be added to the tree.

Node children are lazy loaded when the node is expanded.

The tree is never reloaded in its entirety. Only the currently selected node is reloaded when changes are made to the
task it displays. If the tree detects an unexpected change to any of its nodes, these nodes will be collapsed and must
be reloaded (alongside their children) by expanding them again.

When a node is selected (clicked) the associated task will be displayed, and it will be assigned to the logged user (as
if it were an auto-assign task). When a different node is selected, the currently displayed task will be canceled (if it
is assigned to the logged user). If the associated task cannot be assigned, it will be displayed in its "blocked"
unassigned state, alongside an _assign_ button. If the previously displayed task cannot be canceled it will remain
assigned, but the newly selected task will be displayed to the user regardless.

When a new child node is added from the Tree Case View, the _treeCaseRefAccessor_ task is assigned, the case ref within
it is set with the new value and then this task finishes. If the assign or setData operation fails, the child will not
be added. The success of the finish operation does not influence whether the child will be added or not.

If a backend call fails the frontend will log an error, but will not display it to the user. If a loading spinner
indicator is shown and its call has failed it will never disappear.

## Icon import

If you want to display custom icons in the tree they must be imported into the frontend application project.

The **MatIconRegistry** and the **DomSanitizer** must be added to the application module that uses the Case Tree View
components (such as the `App.module.ts`).

The `addSvgIconSet` and `addSvgIcon` methods of the `MatIconRegistry` can be used to add a set of icons, or a single
icon respectively.

An example for adding the Twitch and YouTube icons and the icon set from
the [Material Design Icons](https://materialdesignicons.com/) website can be seen here:

```typescript
constructor(matIconRegistry:MatIconRegistry, domSanitizer:DomSanitizer) {
    matIconRegistry.addSvgIconSet(domSanitizer.bypassSecurityTrustResourceUrl('./assets/mdi.svg'));
    matIconRegistry.addSvgIcon('twitch', domSanitizer.bypassSecurityTrustResourceUrl(`../../assets/twitch.svg`));
    matIconRegistry.addSvgIcon('youtube', domSanitizer.bypassSecurityTrustResourceUrl(`../../assets/youtube.svg`));
}
```