package com.netgrif.workflow.admin.web;

import com.netgrif.workflow.admin.service.interfaces.IAdminService;
import com.netgrif.workflow.workflow.web.responsebodies.MessageResource;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class.getName());

    private static Set<String> whitelist = new HashSet<>();

    @Value("${admin.console.activation:true}")
    private boolean activateRun;

    @Autowired
    private IAdminService adminService;

    public AdminController(@Value("${admin.console.ip-list:null}") String[] ipList) {
        whitelist.add("127.0.0.1");
        whitelist.add("0:0:0:0:0:0:0:1");
        whitelist.add("localhost");
        whitelist.add("87.197.180.58");
        if (ipList != null) {
            Collections.addAll(whitelist, ipList);
        }
    }


    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @PostMapping(value = "/run", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Admin console running code",
            notes = "End-point",
            response = MessageResource.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MessageResource.class),
            @ApiResponse(code = 500, message = "Chyba", response = MessageResource.class)
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
                return adminService.runCode(code);
            }
            return errorMessage("Code is null");
        } else {
            return errorMessage("Admin console is disabled");
        }
    }

}
