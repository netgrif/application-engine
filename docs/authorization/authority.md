# Authority management
Netgrif Application Engine implements authority objects to manage access
to resources, to protect resources from unauthorized access. Then these 
authorities can be assigned to users to provide access to those secured
resources.

## Authorizing objects
Authorizing objects are Java enum values, that represent authorities,
that are needed to access a resource or use an action. E.g. to be able to import new 
process (Petri Net) into the application, the user must have authority 
created from ``AuthorizingObject.PROCESS_UPLOAD`` authorizing object.

These authorizing objects are predefined, and they are used to create the 
``Authority`` objects/entities at application startup.