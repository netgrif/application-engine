package com.netgrif.application.engine.startup

import com.netgrif.application.engine.authorization.domain.ApplicationRole
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class ApplicationRoleRunner extends AbstractOrderedCommandLineRunner {

    public static final String DEFAULT_APP_ROLE = "default"
    public static final String ADMIN_APP_ROLE = "admin"
    public static final String SYSTEM_ADMIN_APP_ROLE = "system_admin"
    public static final String ANONYMOUS_APP_ROLE = "anonymous"
    // TODO: release/8.0.0 application id should be from configuration
    private static final String APPLICATION_ID = "application"


    private Map<String, ApplicationRole> applicationRoles

    @Autowired
    private IRoleService service

    @Override
    void run(String... strings) throws Exception {
        Map<String, ApplicationRole> appRoles = new HashMap<>()

        appRoles.put(DEFAULT_APP_ROLE, createAndSaveApplicationRole(APPLICATION_ID, DEFAULT_APP_ROLE))
        appRoles.put(ADMIN_APP_ROLE, createAndSaveApplicationRole(APPLICATION_ID, ADMIN_APP_ROLE))
        appRoles.put(SYSTEM_ADMIN_APP_ROLE, createAndSaveApplicationRole(APPLICATION_ID, SYSTEM_ADMIN_APP_ROLE))
        appRoles.put(ANONYMOUS_APP_ROLE, createAndSaveApplicationRole(APPLICATION_ID, ANONYMOUS_APP_ROLE))

        this.applicationRoles = Collections.unmodifiableMap(appRoles)
    }

    /**
     * todo javadoc
     * */
    ApplicationRole getAppRole(String roleName) {
        if (roleName == null) {
            return null
        }
        if (applicationRoles && applicationRoles.containsKey(roleName)) {
            return applicationRoles.get(roleName)
        }
        return createAndSaveApplicationRole(APPLICATION_ID, roleName)
    }

    /**
     * todo javadoc
     * */
    Collection<ApplicationRole> getAllAppRoles() {
        return applicationRoles.values()
    }

    private ApplicationRole createAndSaveApplicationRole(String applicationId, String importId) {
        if (!service.existsApplicationRoleByImportId(importId)) {
            return (ApplicationRole) service.save(new ApplicationRole(importId, applicationId))
        }
        return service.findApplicationRoleByImportId(importId)
    }
}