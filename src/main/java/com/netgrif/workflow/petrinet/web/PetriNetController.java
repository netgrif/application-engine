package com.netgrif.workflow.petrinet.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.importer.service.Importer;
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
import java.util.List;
import java.util.Map;
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

            service.importPetriNetAndDeleteFile(file, fileMeta, (LoggedUser) auth.getPrincipal());
            return MessageResource.successMessage("Petri net "+fileMeta.name+" imported successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return MessageResource.errorMessage("IO error");
        }
    }

    @RequestMapping(value = "/refs", method = GET)
    public
    @ResponseBody
    PetriNetReferencesResource getAllReferences(Authentication auth, Locale locale) {
        List<PetriNetReference> refs = service.getReferences((LoggedUser) auth.getPrincipal(), locale);
        return new PetriNetReferencesResource(refs);
    }

    @RequestMapping(value = "/ref", method = POST)
    public @ResponseBody
    PetriNetReference getReference(Authentication auth, @RequestBody PetriNetCriteria criteria, Locale locale) {
        if (criteria.title != null)
            return service.getReference(criteria.title, "1.0.0", (LoggedUser) auth.getPrincipal(), locale);
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
    LocalisedRolesResource getRoles(@PathVariable("netId") String netId, Locale locale) {
        netId = decodeUrl(netId);
        return new LocalisedRolesResource(roleService.findAll(netId), netId, locale);
    }

    @RequestMapping(value = "/{netId}/transactions", method = GET)
    public @ResponseBody
    TransactionsResource getTransactions(@PathVariable("netId") String netId, Locale locale) {
        PetriNet net = service.getPetriNet(decodeUrl(netId));
        return new TransactionsResource(net.getTransactions().values(), netId, locale);
    }

    @RequestMapping(value = "/{netId}/file", method = GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public FileSystemResource getNetFile(@PathVariable("netId") String netId, @RequestParam(value = "title", required = false) String title, Authentication auth, HttpServletResponse response) {
        StringBuilder titleBuilder = new StringBuilder();
        if (title != null && !title.isEmpty())
            titleBuilder.append(decodeUrl(title));
        FileSystemResource fileResource = service.getNetFile(decodeUrl(netId), titleBuilder);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=" + titleBuilder.toString() + Importer.FILE_EXTENSION);
        response.setHeader("Content-Length", String.valueOf(fileResource.getFile().length()));
        log.info("Downloading Petri net file: " + titleBuilder.toString() + " [" + netId + "]");
        return fileResource;
    }

    @RequestMapping(value = "/search", method = POST)
    public @ResponseBody
    PagedResources<PetriNetSmallResource> searchPetriNets(Authentication auth, @RequestBody Map<String, Object> criteria, Pageable pageable, PagedResourcesAssembler<PetriNetSmall> assembler, Locale locale) {
        LoggedUser user = (LoggedUser) auth.getPrincipal();
        Page<PetriNetSmall> nets = service.searchPetriNet(criteria, user, pageable, locale);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PetriNetController.class)
                .searchPetriNets(auth, criteria, pageable, assembler, locale)).withRel("search");
        PagedResources<PetriNetSmallResource> resources = assembler.toResource(nets, new PetriNetSmallResourceAssembler(), selfLink);
        return resources;
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