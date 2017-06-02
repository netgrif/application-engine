package com.fmworkflow.petrinet.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmworkflow.auth.service.interfaces.IUserService;
import com.fmworkflow.json.JsonBuilder;
import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.service.interfaces.IPetriNetService;
import com.fmworkflow.petrinet.service.interfaces.IProcessRoleService;
import com.fmworkflow.petrinet.web.requestbodies.AssignedRolesBody;
import com.fmworkflow.petrinet.web.requestbodies.PetriNetReferenceBody;
import com.fmworkflow.petrinet.web.requestbodies.UploadedFileMeta;
import com.fmworkflow.petrinet.web.responsebodies.*;
import com.fmworkflow.workflow.web.responsebodies.MessageResource;
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
    MessageResource importPetriNet(
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
            return MessageResource.successMessage("Petri net imported successfully");
        } catch (SAXException e) {
            e.printStackTrace();
            return MessageResource.errorMessage("Invalid xml file");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return MessageResource.errorMessage("Invalid parser configuration");
        } catch (IOException e) {
            e.printStackTrace();
            return MessageResource.errorMessage("IO error");
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

    @RequestMapping(value = "/transition/refs", method = POST)
    public
    @ResponseBody
    TransitionReferencesResource getTransitionReferences(@RequestBody List<String> ids) {
        ids.forEach(id -> id = decodeUrl(id));
        return new TransitionReferencesResource(service.getTransitionReferences(ids));
    }

    @RequestMapping(value = "/data/refs", method = POST)
    public
    @ResponseBody
    DataFieldReferencesResource getDataFieldReferences(@RequestBody PetriNetReferenceBody referenceBody){
        referenceBody.petriNets.forEach(net -> net = decodeUrl(net));
        referenceBody.transitions.forEach(trans -> trans = decodeUrl(trans));
        return new DataFieldReferencesResource(service.getDataFieldReferences(referenceBody.petriNets, referenceBody.transitions));
    }

    //TODO: prerobiť! nemôže jeden endpoint vraciat dve rôzne veci
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
    MessageResource assignRoleToUser(@RequestBody AssignedRolesBody assignedRoles, @PathVariable String netId) {
        try {
            roleService.assignRoleToUser(assignedRoles.email, netId, assignedRoles.roleIds);
            return MessageResource.successMessage("Role assigned");
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResource.errorMessage("Unable to assign role");
        }
    }

    @RequestMapping(value = "/roles/users/{id}", method = GET)
    public @ResponseBody
    ProcessRolesUsersListResponse getUsersWithProcessRole(Authentication auth, @PathVariable("id") String roleId) {
        try {
            ProcessRolesUsersListResponse response = new ProcessRolesUsersListResponse();
            roleId = URLDecoder.decode(roleId, StandardCharsets.UTF_8.name());
            response.setUsers(userService.findByProcessRole(roleId));
            return response;
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
    }

    public static String decodeUrl(String s1){
        try {
            return URLDecoder.decode(s1, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
}