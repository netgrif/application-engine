package com.netgrif.workflow.petrinet.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.importer.service.Importer;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.version.StringToVersionConverter;
import com.netgrif.workflow.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.workflow.petrinet.web.responsebodies.*;
import com.netgrif.workflow.workflow.domain.FileStorageConfiguration;
import com.netgrif.workflow.workflow.web.responsebodies.MessageResource;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/petrinet")
@ConditionalOnProperty(
        value = "nae.petrinet.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Api(tags = {"Petri net"}, authorizations = @Authorization("BasicAuth"))
public class PetriNetController {

    private static final Logger log = LoggerFactory.getLogger(PetriNetController.class);

    @Autowired
    private FileStorageConfiguration fileStorageConfiguration;

    @Autowired
    private IPetriNetService service;

    @Autowired
    private IProcessRoleService roleService;

    @Autowired
    private StringToVersionConverter converter;

    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation(value = "Import new process",
            notes = "Caller must have the ADMIN role. Imports an entirely new process or a new version of an existing process.",
            authorizations = @Authorization("BasicAuth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PetriNetReferenceWithMessageResource.class),
            @ApiResponse(code = 400, message = "Process model is invalid", response = MessageResource.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements")
    })
    @RequestMapping(value = "/import", method = POST, produces = MediaTypes.HAL_JSON_VALUE)
    public PetriNetReferenceWithMessageResource importPetriNet(
            @RequestParam(value = "file", required = true) MultipartFile multipartFile,
            @RequestParam(value = "meta", required = false) String releaseType,
            Authentication auth, Locale locale) throws MissingPetriNetMetaDataException {
        try {
            File file = new File(fileStorageConfiguration.getStorageArchived() + multipartFile.getOriginalFilename());
            file.createNewFile();
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(multipartFile.getBytes());
            String release = releaseType == null ? "major" : releaseType;

            Optional<PetriNet> newPetriNet = service.importPetriNet(new FileInputStream(file), release, (LoggedUser) auth.getPrincipal());
            fout.close();
            return PetriNetReferenceWithMessageResource.successMessage("Petri net " + multipartFile.getOriginalFilename() + " imported successfully", newPetriNet.get(), locale);
        } catch (IOException e) {
            log.error("Importing Petri net failed: ", e);
            return PetriNetReferenceWithMessageResource.errorMessage("IO error");
        }
    }

    @ApiOperation(value = "Get all processes", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(method = GET, produces = MediaTypes.HAL_JSON_VALUE)
    public PetriNetReferenceResources getAll(@RequestParam(value = "indentifier", required = false) String identifier, @RequestParam(value = "version", required = false) String version, Authentication auth, Locale locale) {
        LoggedUser user = (LoggedUser) auth.getPrincipal();
        if (identifier != null && version == null) {
            return new PetriNetReferenceResources(service.getReferencesByIdentifier(identifier, user, locale));
        } else if (identifier == null && version != null) {
            return new PetriNetReferenceResources(service.getReferencesByVersion(converter.convert(version), user, locale));
        } else if (identifier != null && version != null) {
            return new PetriNetReferenceResources(Collections.singletonList(service.getReference(identifier, converter.convert(version), user, locale)));
        } else {
            return new PetriNetReferenceResources(service.getReferences(user, locale));
        }
    }

    @ApiOperation(value = "Get process by id", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}", method = GET, produces = MediaTypes.HAL_JSON_VALUE)
    public PetriNetReferenceResource getOne(@PathVariable("id") String id, Authentication auth, Locale locale) {
        return new PetriNetReferenceResource(IPetriNetService.transformToReference(service.getPetriNet(decodeUrl(id)), locale));
    }

    @ApiOperation(value = "Get process by identifier and version", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{identifier}/{version}", method = GET, produces = MediaTypes.HAL_JSON_VALUE)
    public PetriNetReferenceResource getOne(@PathVariable("identifier") String identifier, @PathVariable("version") String version, Authentication auth, Locale locale) {
        return new PetriNetReferenceResource(service.getReference(identifier, converter.convert(version), (LoggedUser) auth.getPrincipal(), locale));
    }

    @ApiOperation(value = "Get transitions of processes", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/transitions", method = GET, produces = MediaTypes.HAL_JSON_VALUE)
    public TransitionReferencesResource getTransitionReferences(@RequestParam List<String> ids, Authentication auth, Locale locale) {
        ids.forEach(id -> id = decodeUrl(id));
        return new TransitionReferencesResource(service.getTransitionReferences(ids, (LoggedUser) auth.getPrincipal(), locale));
    }

    @ApiOperation(value = "Get data fields of transitions", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/data", method = POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public DataFieldReferencesResource getDataFieldReferences(@RequestBody List<TransitionReference> referenceBody, Locale locale) {
        return new DataFieldReferencesResource(service.getDataFieldReferences(referenceBody, locale));
    }

    @ApiOperation(value = "Get roles of process", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{netId}/roles", method = GET, produces = MediaTypes.HAL_JSON_VALUE)
    public ProcessRolesResource getRoles(@PathVariable("netId") String netId, Locale locale) {
        netId = decodeUrl(netId);
        return new ProcessRolesResource(roleService.findAll(netId), netId, locale);
    }

    @ApiOperation(value = "Get transactions of process", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{netId}/transactions", method = GET, produces = MediaTypes.HAL_JSON_VALUE)
    public TransactionsResource getTransactions(@PathVariable("netId") String netId, Locale locale) {
        PetriNet net = service.getPetriNet(decodeUrl(netId));
        return new TransactionsResource(net.getTransactions().values(), netId, locale);
    }

    @ApiOperation(value = "Download process model", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{netId}/file", method = GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public FileSystemResource getNetFile(@PathVariable("netId") String netId, @RequestParam(value = "title", required = false) String title, Authentication auth, HttpServletResponse response) {
        FileSystemResource fileResource = service.getFile(decodeUrl(netId), decodeUrl(title));
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileResource.getFilename() + Importer.FILE_EXTENSION);
        response.setHeader("Content-Length", String.valueOf(fileResource.getFile().length()));
        log.info("Downloading Petri net file: " + fileResource.getFilename() + " [" + netId + "]");
        return fileResource;
    }

    @ApiOperation(value = "Search processes", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/search", method = POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<PetriNetReferenceResource> searchPetriNets(@RequestBody Map<String, Object> criteria, Authentication auth, Pageable pageable, PagedResourcesAssembler<PetriNetReference> assembler, Locale locale) {
        LoggedUser user = (LoggedUser) auth.getPrincipal();
        Page<PetriNetReference> nets = service.search(criteria, user, pageable, locale);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PetriNetController.class)
                .searchPetriNets(criteria, auth, pageable, assembler, locale)).withRel("search");
        PagedResources<PetriNetReferenceResource> resources = assembler.toResource(nets, new PetriNetReferenceResourceAssembler(), selfLink);
        PetriNetReferenceResourceAssembler.buildLinks(resources);
        return resources;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation(value = "Delete process",
            notes = "Caller must have the ADMIN role. Removes the specified process, along with it's cases, tasks and process roles.",
            authorizations = @Authorization("BasicAuth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MessageResource.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements")
    })
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource deletePetriNet(@PathVariable("id") String processId, Authentication auth) {
        String decodedProcessId = decodeUrl(processId);
        if (Objects.equals(decodedProcessId, "")) {
            log.error("Deleting Petri net [" + processId + "] failed: could not decode process ID from URL");
            return MessageResource.errorMessage("Deleting Petri net " + processId + " failed!");
        }
        LoggedUser user = (LoggedUser) auth.getPrincipal();
        this.service.deletePetriNet(decodedProcessId, user);
        return MessageResource.successMessage("Petri net " + decodedProcessId + " was deleted");
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