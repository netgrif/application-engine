# Public view

Public view is breaking change in NAE v5.0.0. It provides anonymous access to view, that are marked as public with only anonymous
authorization needed, users can access tasks and cases without any login or registration needed. Public view can be
implemented in case of workflow-view, task-view and single-task-view.

## Frontend

We can define public view components in ``nae.json`` configuration file and then
generate public view component with schematics.

To make work public view, you will need to define following views in ``nae.json`` ``views`` segment:

```json
{
   "public-single-task-view": {
      "layout": {
         "name": "publicSingleTaskView",
         "componentName": "PublicSingleTaskView"
      },
      "access": "public",
      "navigation": {
         "title": "Public view - transition"
      },
      "routing": {
         "path": "process/:petriNetId/:caseId/:transitionId"
      }
   },
   "public-task-view": {
      "layout": {
         "name": "publicTaskView",
         "componentName": "PublicTaskView"
      },
      "access": "public",
      "navigation": {
         "title": "Public view - task"
      },
      "routing": {
         "path": "process/:petriNetId/:caseId"
      }
   },
   "public-case-creator-view": {
      "component": {
         "class": "PublicTaskViewComponent",
         "from": "./views/public-task-view/public-task-view.component"
      },
      "access": "public",
      "navigation": {
         "title": "Public view - case"
      },
      "routing": {
         "path": "process/:petriNetId"
      }
   },
   "public-workflow-view": {
      "layout": {
         "name": "publicWorkflowView",
         "componentName": "PublicWorkflowView"
      },
      "access": "public",
      "navigation": {
         "title": "Public view - process"
      },
      "routing": {
         "path": "process"
      }
   },
   "public-view-resolver": {
      "layout": {
         "name": "publicResolver",
         "componentName": "PublicResolver"
      },
      "access": "public",
      "navigation": true,
      "routing": {
         "path": "public-resolver"
      }
   }
}
```

Then run ``@netgrif/schematics:create-view``, which will lookup ``nae.json`` for missing
views, and will generate them from config.

That's all for frontend.

## Backend

To make JWT authentication work on backend, you will need to create a self-signed private key for JwtService to be able
to create the JWT token. You can create and place the key in your project following these steps:

1. Generate a .CRT (public key) and a .DER (private key) file using OpenSSL
    - On Linux use OpenSSL of the Linux system
    - On Windows, use OpenSSL of Git Bash (if you do not have installed Git or Git bash on Windows, visit Git -
      Installing Git (git-scm.com))
    - You can generate private key and certificate with following command:

```shell
openssl genrsa -out keypair.pem 4096
openssl rsa -in keypair.pem -pubout -out public.crt
openssl pkcs8 -topk8 -inform PEM -outform DER -nocrypt -in keypair.pem -out private.der
```

2. After generating .DER and .CRT files, places them in your project under resources/certificates folder.

3. After your certificates are ready to use, you have to add some JWT-specific properties to your application
   properties:

```properties
nae.security.jwt.expiration=900000
nae.security.jwt.algorithm=RSA
nae.security.jwt.private-key=classpath:certificates/private.der
nae.security.server-patterns=/api/auth/signup,/api/auth/token/verify,/api/auth/reset,/api/auth/recover,/v2/api-docs,/swagger-ui.html,/api/public/**
```

4. To prevent maven to compile these certificate and key files you have to add a maven plugin to your project:

```xml

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-resources-plugin</artifactId>
    <configuration>
        <nonFilteredFileExtensions>
            <nonFilteredFileExtension>crt</nonFilteredFileExtension>
            <nonFilteredFileExtension>der</nonFilteredFileExtension>
            <nonFilteredFileExtension>pem</nonFilteredFileExtension>
        </nonFilteredFileExtensions>
    </configuration>
</plugin>
```

There are public controllers and service method's already implemented on backend, but if your project overrides e.g.
TaskController, some modification may will be needed. As these overridden classes can be very specific for a project, I
will not list every single one of them, instead please contact one of the engine developers.