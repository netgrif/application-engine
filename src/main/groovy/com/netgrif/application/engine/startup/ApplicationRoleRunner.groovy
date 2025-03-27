package com.netgrif.application.engine.startup


import com.netgrif.application.engine.authorization.domain.ApplicationRole
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import groovy.transform.CompileStatic
import lombok.Getter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@CompileStatic
class ApplicationRoleRunner extends AbstractOrderedCommandLineRunner {

    public static final String DEFAULT_APP_ROLE = "default"
    public static final String ADMIN_APP_ROLE = "admin"
    public static final String SYSTEM_ADMIN_APP_ROLE = "system_admin"
    public static final String ANONYMOUS_APP_ROLE = "anonymous"

    @Getter
    private Map<String, ApplicationRole> applicationRoles

    @Autowired
    private IRoleService service

    @Override
    void run(String... strings) throws Exception {
        // TODO: release/8.0.0 application id should be from configuration
        final String applicationId = "application"
        Map<String, ApplicationRole> appRoles = new HashMap<>()

        appRoles.put(DEFAULT_APP_ROLE, createAndSaveApplicationRole(applicationId, DEFAULT_APP_ROLE))
        appRoles.put(ADMIN_APP_ROLE, createAndSaveApplicationRole(applicationId, ADMIN_APP_ROLE))
        appRoles.put(SYSTEM_ADMIN_APP_ROLE, createAndSaveApplicationRole(applicationId, SYSTEM_ADMIN_APP_ROLE))
        appRoles.put(ANONYMOUS_APP_ROLE, createAndSaveApplicationRole(applicationId, ANONYMOUS_APP_ROLE))

        this.applicationRoles = Collections.unmodifiableMap(appRoles)
    }

    private ApplicationRole createAndSaveApplicationRole(String applicationId, String importId) {
        if (!service.existsApplicationRoleByImportId(importId)) {
            return (ApplicationRole) service.save(new ApplicationRole(importId, applicationId))
        }
        return null
    }
}