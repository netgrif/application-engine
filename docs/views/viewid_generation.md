# View ID generation

A view must define a view ID in order for it to be able to store its preferences.

Views created by the `create-view` schematic are generated with the necessary providers for dynamic view ID generation.
These are configured in such a way, that the generated IDs copy the key structure of the `nae.json` they were generated
from.

## View ID generation

The following components contribute to the ID generation:

* `ViewIdService`
* `NAE_VIEW_ID_SEGMENT` injection token

### ViewId Service

Dynamically generates the view ID by combining the view ID of the parent component with the view ID segment provided by
the injection token. The parent ID is not necessary for the generation of a new view ID, only the ID segment is
required.

If we want to access the view ID of the current component/view, we must inject the `ViewIdService`, that provides this
ID.

Since the view ID is generated dynamically, one component can have different IDs based on its location in the
component/provider tree of the application.

### NAE\_VIEW\_ID\_SEGMENT

The injection token, that provides the ID segment for some component.

### Mechanism of view ID generation

We will demonstrate the mechanism for view ID generation on an example. Let's assume, that we have an application with 4
components with the following hierarchy.

![View Id generation diagram](../_media/views/View-ID-generation-guide-graph.svg)

3 Components provide the `ViewIdService` and the segment injection token. The fourth component provides neither the
service, nor the token.

The view ID of the root component will be identical to its ID segment, since none of its parents provide an ID. The view
ID of this component will be **foo**.

The view ID of the left child component will be a combination of the parent view ID and the provided ID segment. The
parent ID is combined with the segment by adding a dash (-) character. The view ID of the left child will be **foo-bar**
.

The right child component does not participate in the dynamic ID generation since it does not provide
the `ViewIdService`. If we were to inject the `ViewIdService` in this component we would get the ID of the closest
ancestor that provides an ID. In this case it would be the parent component and the ID would therefore be **foo**.

The view ID of the last component is determined by following the same rules as the left child. The ancestor ID is
combined with the provided ID segment to create the ID for this component. The view ID of this component is therefore **
foo-baz**.

## View IDs of tabbed views

The mechanism for view ID generation does not change for tabbed views.

However, the tab view component provides view ID segments automatically for all the tabs it opens.

Tabs that are initially opened are provided with segments equal to their index.

The tab content may therefore only provide the ViewIdService and a view ID will be generated for the tab.

If we want to override the ID segment for some tab, then we must provide the injection token with the new segment inside
the content component.

All the tabs that are not initially opened are provided with the same ID segment - **dynamic**. Therefore, all the
dynamically opened tabs would share the same view ID if the `ViewIdService` were provided in them.

## FAQ

### How to define a static view ID?

If we want a component to have a static view ID regardless of its location in the component/provider tree, we must
simply provide something in place of the `ViewIdService` that will return the desired ID.

```json
{
  provide: ViewIdService,
  useValue: {
    viewId: 'staticViewId'
  }
}
```

Keep in mind however, that all descendants of this component will base their dynamic view IDs on this static ID.

### How to disable saving of preferences?

Preferences are saved if and only if the component is able to inject the `ViewIdService`. Therefore, if it cannot be
injected, no preferences are stored. We can assure that the `ViewIdService` won't be injected by providing **null** in
its place.

```json
{
  provide: ViewIdService,
  useValue: null
}
```

Keep in mind however, that all descendants of this component will be unable to inject a parent view ID and will
therefore start generating their ID as if they were the root component.