# Public view

Public view is breaking change in NAE v5.0.0. It provides anonymous access to view, that are marked as public with no
authorization needed, users can access tasks and cases without any login or registration needed. Public view can be
implemented in case of workflow-view and task-view.

## Frontend

In this manual we will define a public task view. This public task view can be accessed with
`<url>/process/:petriNetId/:caseId`. This route is set to navigate to our task view, that will send a request to
backend, search for case with defined route parameters petriNetId and caseId. Backend then returns tasks of this case
and our task view will list them. Follow these steps to implement such a public task or case view:

- Create a component with any name you want. We will now use PublicTaskViewComponent.
- In the config JSON (where you define views parameters) you can define a new view as follows:
    - the access attribute should be public
    - under component, you have to define the class and path of your component.

```json
{
  "demo-public-view": {
    "layout": {
      "name": "publicTaskView",
      "params": {
        "allowedNets": []
      }
    },
    "component": {
      "class": "PublicTaskViewComponent",
      "from": "./doc/public-task-view/public-task-view.component"
    },
    "access": "public",
    "navigation": false,
    "routing": {
      "path": "process/:petriNetId/:caseId"
    }
  }
}
```

- In our component class, we have to define public services, resolvers and abstract components to be used instead of
  default ones, and then we have to provide them to this component:

```ts
const localTaskViewServiceFactory = (factory: ConfigTaskViewServiceFactory) => {
    return factory.create('demo-public-view');
};

const searchServiceFactory = (router: Router, route: ActivatedRoute, process: ProcessService,
                              caseResourceService: CaseResourceService, snackBarService: SnackBarService) => {
    return publicSearchServiceFactory(router, route, process, caseResourceService, snackBarService);
};

const processServiceFactory = (userService: UserService, sessionService: SessionService, authService: AuthenticationService,
                               router: Router, publicResolverService: PublicUrlResolverService, petriNetResource: PetriNetResourceService,
                               publicPetriNetResource: PublicPetriNetResourceService, loggerService: LoggerService) => {
    return publicFactoryResolver(userService, sessionService, authService, router, publicResolverService,
        new ProcessService(petriNetResource, loggerService),
        new PublicProcessService(publicPetriNetResource, loggerService));
};

const taskResourceServiceFactory = (userService: UserService, sessionService: SessionService, authService: AuthenticationService,
                                    router: Router, publicResolverService: PublicUrlResolverService,
                                    logger: LoggerService, provider: ResourceProvider, config: ConfigurationService,
                                    fieldConverter: FieldConverterService) => {
    return publicFactoryResolver(userService, sessionService, authService, router, publicResolverService,
        new TaskResourceService(provider, config, fieldConverter, logger),
        new PublicTaskResourceService(provider, config, fieldConverter, logger));
};

const caseResourceServiceFactory = (userService: UserService, sessionService: SessionService, authService: AuthenticationService,
                                    router: Router, publicResolverService: PublicUrlResolverService,
                                    provider: ResourceProvider, config: ConfigurationService) => {
    return publicFactoryResolver(userService, sessionService, authService, router, publicResolverService,
        new CaseResourceService(provider, config),
        new PublicCaseResourceService(provider, config));
};

@Component({
    selector: 'nae-app-public-task-view',
    templateUrl: './public-task-view.component.html',
    styleUrls: ['./public-task-view.component.scss'],
    providers: [
        ConfigTaskViewServiceFactory,
        {
            provide: ProcessService,
            useFactory: processServiceFactory,
            deps: [UserService, SessionService, AuthenticationService, Router, PublicUrlResolverService, PetriNetResourceService,
                PublicPetriNetResourceService, LoggerService]
        },
        {
            provide: TaskResourceService,
            useFactory: taskResourceServiceFactory,
            deps: [UserService, SessionService, AuthenticationService, Router, PublicUrlResolverService,
                LoggerService, ResourceProvider, ConfigurationService, FieldConverterService]
        },
        {
            provide: CaseResourceService,
            useFactory: caseResourceServiceFactory,
            deps: [UserService, SessionService, AuthenticationService, Router, PublicUrlResolverService,
                ResourceProvider, ConfigurationService]
        },
        {
            provide: SearchService,
            useFactory: searchServiceFactory,
            deps: [Router, ActivatedRoute, ProcessService, CaseResourceService, SnackBarService]
        },
        {
            provide: TaskViewService,
            useFactory: localTaskViewServiceFactory,
            deps: [ConfigTaskViewServiceFactory]
        },
    ]
})
export class PublicTaskViewComponent extends AbstractTaskView implements AfterViewInit {

    @ViewChild('header') public taskHeaderComponent: HeaderComponent;

    constructor(taskViewService: TaskViewService) {
        super(taskViewService);
    }

    ngAfterViewInit(): void {
        this.initializeHeader(this.taskHeaderComponent);
    }

    logEvent(event: TaskEventNotification) {
        console.log(event);
    }
}
```

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