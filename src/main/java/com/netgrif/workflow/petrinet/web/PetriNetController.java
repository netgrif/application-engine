package com.netgrif.workflow.petrinet.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.workflow.petrinet.web.requestbodies.PetriNetCriteria;
import com.netgrif.workflow.petrinet.web.requestbodies.PetriNetReferenceBody;
import com.netgrif.workflow.petrinet.web.requestbodies.UploadedFileMeta;
import com.netgrif.workflow.petrinet.web.responsebodies.*;
import com.netgrif.workflow.workflow.web.responsebodies.MessageResource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.util.Locale;

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

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/import", method = POST)
    public
    @ResponseBody
    MessageResource importPetriNet(
            @RequestParam(value = "file", required = true) MultipartFile multipartFile,
            @RequestParam(value = "meta", required = false) String fileMetaJSON,
            Authentication auth) {
        try {
            File file = new File(multipartFile.getOriginalFilename());
            file.createNewFile();
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(multipartFile.getBytes());
            fout.close();

            ObjectMapper mapper = new ObjectMapper();
            UploadedFileMeta fileMeta = mapper.readValue(fileMetaJSON, UploadedFileMeta.class);

            service.importPetriNet(file, fileMeta.name, fileMeta.initials, (LoggedUser) auth.getPrincipal());
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

    @RequestMapping(method = GET)
    public List<PetriNet> getAll() {
        return service.loadAll();
    }

    @RequestMapping(value = "/refs", method = GET)
    public
    @ResponseBody
    PetriNetReferencesResource getAllReferences(Authentication auth, Locale locale) {
        List<PetriNetReference> refs = service.getAllReferences((LoggedUser) auth.getPrincipal(), locale);
        return new PetriNetReferencesResource(refs);
    }

    @RequestMapping(value = "/ref", method = POST)
    public @ResponseBody
    PetriNetReference getReference(Authentication auth, @RequestBody PetriNetCriteria criteria, Locale locale) {
        if (criteria.title != null)
            return service.getReferenceByTitle((LoggedUser) auth.getPrincipal(), criteria.title, locale);
        return new PetriNetReference(null, null);
    }

    @RequestMapping(value = "/transition/refs", method = POST)
    public
    @ResponseBody
    TransitionReferencesResource getTransitionReferences(Authentication auth, @RequestBody List<String> ids, Locale locale) {
        ids.forEach(id -> id = decodeUrl(id));
        return new TransitionReferencesResource(service.getTransitionReferences(ids, (LoggedUser) auth.getPrincipal(), locale));
    }

    @RequestMapping(value = "/data/refs", method = POST)
    public
    @ResponseBody
    DataFieldReferencesResource getDataFieldReferences(@RequestBody PetriNetReferenceBody referenceBody, Locale locale) {
        referenceBody.petriNets.forEach(net -> net = decodeUrl(net));
        referenceBody.transitions.forEach(trans -> trans = decodeUrl(trans));
        return new DataFieldReferencesResource(service.getDataFieldReferences(referenceBody.petriNets, referenceBody.transitions, locale));
    }

    @RequestMapping(value = "/{netId}/roles", method = GET)
    public @ResponseBody
    RolesResource getRoles(@PathVariable("netId") String netId) {
        netId = decodeUrl(netId);
        return new RolesResource(roleService.findAll(netId), netId);
    }

    @RequestMapping(value = "/{netId}/transactions", method = GET)
    public @ResponseBody
    TransactionsResource getTransactions(@PathVariable("netId") String netId) {
        PetriNet net = service.loadPetriNet(decodeUrl(netId));
        return new TransactionsResource(net.getTransactions().values(), netId);
    }

    public static String decodeUrl(String s1) {
        try {
            return URLDecoder.decode(s1, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
}