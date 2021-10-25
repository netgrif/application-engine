# Redirect to any view

With this feature you can implement a redirect component on frontend to redirect you from anywhere to any view.
`AbstractTaskListComponent` and `AbstractCaseListComponent` is now resolving URL parameters and query parameters. You can
use these parameters to define view that you want to be redirected to. Query parameters can be used to define case and
task that you want to open after redirect.

### Configuration

1. Define your redirect component using `nae.json` in the views section as follows:

```json
{
   "views": {
      "demo-redirect": {
         "component": {
            "class": "ExampleRedirectComponent",
            "from": "./doc/redirect/example-redirect.component"
         },
         "access": "public",
         "navigation": true,
         "routing": {
            "path": "redirect/:view"
         },
         "children": {
            "custom-redirect": {
               "component": {
                  "class": "ExampleRedirectComponent",
                  "from": "./doc/redirect/example-redirect.component"
               },
               "access": "public",
               "navigation": true,
               "routing": {
                  "path": "**"
               }
            }
         }
      }
   }
}
```

It is the usual way for defining a new view or component in NAE. With this you tell Angular to redirect to your
`ExampleRedirectComponent` when you navigate to any address of form `address_to_page/redirect/any_view/other_subpath/`. It
is important to use the `/redirect`, as this is the main path that redirect service parses.

2. Generate the component for your project using schematic
3. Add the following template to the `example-redirect.component.html` template file. This is a component implemented in
   **netgrif-components** library, that calls the RedirectService when the component is reached by navigation.

```html

<nc-redirect></nc-redirect>
```

4. And that’s it! If you have single-level navigation panel, the redirect will work! If you have multi-level navigation,
   you may need to override the `RedirectService` to solve the redirect to other components.

![Redirect example](../_media/views/redirect_to_any_view.gif)