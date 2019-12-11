package com.netgrif.workflow.petrinet.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.importer.service.Importer;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.workflow.petrinet.web.responsebodies.*;
import com.netgrif.workflow.workflow.web.responsebodies.MessageResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/api/petrinet")
public class PetriNetController {

    private static final Logger log = LoggerFactory.getLogger(PetriNetController.class);

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
            @RequestParam(value = "meta", required = false) String releaseType,
            Authentication auth) throws MissingPetriNetMetaDataException {
        try {
            File file = new File(multipartFile.getOriginalFilename());
            file.createNewFile();
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(multipartFile.getBytes());
            fout.close();
            String release = releaseType == null ? "major" : releaseType;

            service.importPetriNetAndDeleteFile(file, release, (LoggedUser) auth.getPrincipal());
            return MessageResource.successMessage("Petri net imported successfully");
        } catch (IOException e) {
            log.error("Importing Petri net failed: ", e);
            return MessageResource.errorMessage("IO error");
        }
    }

    @RequestMapping(method = GET)
    public @ResponseBody
    PetriNetReferenceResources getAll(@RequestParam(value = "indentifier", required = false) String identifier, @RequestParam(value = "version", required = false) String version, Authentication auth, Locale locale) {
        LoggedUser user = (LoggedUser) auth.getPrincipal();
        if (identifier != null && version == null) {
            return new PetriNetReferenceResources(service.getReferencesByIdentifier(identifier, user, locale));
        } else if (identifier == null && version != null) {
            return new PetriNetReferenceResources(service.getReferencesByVersion(version, user, locale));
        } else if (identifier != null && version != null) {
            return new PetriNetReferenceResources(Collections.singletonList(service.getReference(identifier, version, user, locale)));
        } else {
            return new PetriNetReferenceResources(service.getReferences(user, locale));
        }
    }

    @RequestMapping(value = "/{id}", method = GET)
    public @ResponseBody
    PetriNetReferenceResource getOne(@PathVariable("id") String id, Authentication auth, Locale locale) {
        return new PetriNetReferenceResource(IPetriNetService.transformToReference(service.getPetriNet(decodeUrl(id)), locale));
    }

    @RequestMapping(value = "/{identifier}/{version}", method = GET)
    public @ResponseBody
    PetriNetReferenceResource getOne(@PathVariable("identifier") String identifier, @PathVariable("version") String version, Authentication auth, Locale locale) {
        return new PetriNetReferenceResource(service.getReference(identifier, version, (LoggedUser) auth.getPrincipal(), locale));
    }

    @RequestMapping(value = "/transitions", method = GET)
    public
    @ResponseBody
    TransitionReferencesResource getTransitionReferences(@RequestParam List<String> ids, Authentication auth, Locale locale) {
        ids.forEach(id -> id = decodeUrl(id));
        return new TransitionReferencesResource(service.getTransitionReferences(ids, (LoggedUser) auth.getPrincipal(), locale));
    }

    @RequestMapping(value = "/data", method = POST)
    public
    @ResponseBody
    DataFieldReferencesResource getDataFieldReferences(@RequestBody List<TransitionReference> referenceBody, Locale locale) {
        return new DataFieldReferencesResource(service.getDataFieldReferences(referenceBody, locale));
    }

    @RequestMapping(value = "/{netId}/roles", method = GET)
    public @ResponseBody
    ProcessRolesResource getRoles(@PathVariable("netId") String netId, Locale locale) {
        netId = decodeUrl(netId);
        return new ProcessRolesResource(roleService.findAll(netId), netId, locale);
    }

    @RequestMapping(value = "/{netId}/transactions", method = GET)
    public @ResponseBody
    TransactionsResource getTransactions(@PathVariable("netId") String netId, Locale locale) {
        PetriNet net = service.getPetriNet(decodeUrl(netId));
        return new TransactionsResource(net.getTransactions().values(), netId, locale);
    }

    @RequestMapping(value = "/{netId}/file", method = GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public FileSystemResource getNetFile(@PathVariable("netId") String netId, @RequestParam(value = "title", required = false) String title, Authentication auth, HttpServletResponse response) {
        FileSystemResource fileResource = service.getFile(decodeUrl(netId), decodeUrl(title));
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileResource.getFilename() + Importer.FILE_EXTENSION);
        response.setHeader("Content-Length", String.valueOf(fileResource.getFile().length()));
        log.info("Downloading Petri net file: " + fileResource.getFilename() + " [" + netId + "]");
        return fileResource;
    }

    @RequestMapping(value = "/search", method = POST)
    public @ResponseBody
    PagedResources<PetriNetReferenceResource> searchPetriNets(@RequestBody Map<String, Object> criteria, Authentication auth, Pageable pageable, PagedResourcesAssembler<PetriNetReference> assembler, Locale locale) {
        LoggedUser user = (LoggedUser) auth.getPrincipal();
        Page<PetriNetReference> nets = service.search(criteria, user, pageable, locale);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PetriNetController.class)
                .searchPetriNets(criteria, auth, pageable, assembler, locale)).withRel("search");
        PagedResources<PetriNetReferenceResource> resources = assembler.toResource(nets, new PetriNetReferenceResourceAssembler(), selfLink);
        PetriNetReferenceResourceAssembler.buildLinks(resources);
        return resources;
    }

    public static String decodeUrl(String s1) {
        try {
            if (s1 == null)
                return null;
            return URLDecoder.decode(s1, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            log.error("Decoding URL failed: ",e);
            return "";
        }
    }
}