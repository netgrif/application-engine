package com.netgrif.workflow.admin.web;

import com.netgrif.workflow.admin.service.AdminActionException;
import com.netgrif.workflow.admin.service.interfaces.IAdminService;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.workflow.web.responsebodies.MessageResource;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.netgrif.workflow.workflow.web.responsebodies.MessageResource.*;

@RestController
@RequestMapping("/api/admin")
@ConditionalOnProperty(
        value = "nae.admin.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Api(tags = {"Admin console"}, authorizations = @Authorization("BasicAuth"))
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class.getName());

    private static Set<String> whitelist = new HashSet<>();

    private boolean activateRun;

    private IAdminService adminService;

    @Autowired
    public AdminController(@Value("${admin.console.ip-list:null}") String[] ipList, @Value("${admin.console.activation:true}") boolean activateRun, IAdminService adminService) {
        whitelist.add("127.0.0.1");
        whitelist.add("0:0:0:0:0:0:0:1");
        whitelist.add("localhost");
        if (ipList != null) {
            Collections.addAll(whitelist, ipList);
        }
        this.adminService = adminService;
        this.activateRun = activateRun;
    }


    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @PostMapping(value = "/run", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiOperation(value = "Remote code execution",
            notes = "Caller must have the SYSTEMADMIN role and the call must be made from the server or one of the whitelisted IP addresses (configurable in properties file).\n" +
                    "\n" +
                    "The provided code is executed within the same context as the process actions. The code has thus access to all the autowired services, that are available when writing Petriflow actions.",
            response = MessageResource.class,
            authorizations = @Authorization("BasicAuth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MessageResource.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements"),
            @ApiResponse(code = 500, message = "Error", response = MessageResource.class)
    })
    public MessageResource adminCode(@RequestBody String code, Authentication auth) {
        if (activateRun) {
            log.warn(auth.getName() + " login to Admin console");
            WebAuthenticationDetails details = (WebAuthenticationDetails) auth.getDetails();
            String userIp = details.getRemoteAddress();
            log.warn("Connecting IP " + userIp);
            if (!whitelist.contains(userIp)) {
                log.error("User "+auth.getName()+ " invalid IP Address " + userIp);
                return errorMessage("Invalid IP Address");
            }
            if (code != null) {
                try {
                    String result = adminService.run(code, (LoggedUser) auth.getPrincipal());
                    return successMessage("OK", result);
                } catch (AdminActionException e) {
                    return errorMessage("ERROR", e.toString());
                }
            }
            return errorMessage("Code is null");
        } else {
            return errorMessage("Admin console is disabled");
        }
    }

}
