package com.fmworkflow.petrinet.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmworkflow.auth.service.IUserService;
import com.fmworkflow.json.JsonBuilder;
import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.service.IPetriNetService;
import com.fmworkflow.petrinet.service.IProcessRoleService;
import com.fmworkflow.petrinet.web.requestbodies.AssignedRolesBody;
import com.fmworkflow.petrinet.web.requestbodies.UploadedFileMeta;
import com.fmworkflow.petrinet.web.responsebodies.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/res/petrinet")
public class PetriNetController {

    private static final Logger log = Logger.getLogger(PetriNetController.class);

    @Autowired
    private IPetriNetService service;

    @Autowired
    private IProcessRoleService roleService;

    @Autowired
    private IUserService userService;

    @RequestMapping(value = "/import", method = POST)
    public
    @ResponseBody
    String importPetriNet(
            @RequestParam(value = "file", required = true) MultipartFile multipartFile,
            @RequestParam(value = "meta", required = false) String fileMetaJSON) {
        try {
            File file = new File(multipartFile.getOriginalFilename());
            file.createNewFile();
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(multipartFile.getBytes());
            fout.close();

            ObjectMapper mapper = new ObjectMapper();
            UploadedFileMeta fileMeta = mapper.readValue(fileMetaJSON, UploadedFileMeta.class);

            service.importPetriNet(file, fileMeta.name, fileMeta.initials);
            return JsonBuilder.successMessage("Petri net imported successfully");
        } catch (SAXException e) {
            e.printStackTrace();
            return JsonBuilder.errorMessage("Invalid xml file");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return JsonBuilder.errorMessage("Invalid parser configuration");
        } catch (IOException e) {
            e.printStackTrace();
            return JsonBuilder.errorMessage("IO error");
        }
    }

    @RequestMapping(value = "/all", method = GET)
    public List<PetriNet> getAll() {
        return service.loadAll();
    }

    @RequestMapping(value = "/refs", method = GET)
    public
    @ResponseBody
    PetriNetReferencesResource getAllReferences() {
        List<PetriNetReference> refs = service.getAllReferences();
        return new PetriNetReferencesResource(refs);
    }

    @RequestMapping(value = "/transition/refs/{ids}", method = GET)
    public
    @ResponseBody
    TransitionReferencesResource getTransitionReferences(@PathVariable List<String> ids) {
        ids.forEach(id -> {
            try {
                id = URLDecoder.decode(id, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
        return new TransitionReferencesResource(service.getTransitionReferences(ids));
    }

    @RequestMapping(value = "/roles/assign/{netId}", method = GET)
    public
    @ResponseBody
    UsersRolesListResponse getUsersAndRoles(@PathVariable String netId) {
        UsersRolesListResponse response = new UsersRolesListResponse();
        response.setUsers(userService.findAll());
        response.setRoles(roleService.findAll(netId));
        return response;
    }

    @RequestMapping(value = "/roles/assign/{netId}", method = POST)
    public
    @ResponseBody
    String assignRoleToUser(@RequestBody AssignedRolesBody assignedRoles, @PathVariable String netId) {
        try {
            roleService.assignRoleToUser(assignedRoles.email, netId, assignedRoles.roleIds);
            return JsonBuilder.successMessage("Role assigned");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonBuilder.errorMessage("Unable to assign role");
        }
    }

    @RequestMapping(value = "/roles/users/{id}", method = GET)
    public @ResponseBody
    ProcessRolesUsersListResponse getUsersWithProcessRole(Authentication auth, @PathVariable("id") String roleId) {
        try {
            ProcessRolesUsersListResponse response = new ProcessRolesUsersListResponse();
            response.setUsers(userService.findByProcessRole(roleId));
            return response;
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
    }
}