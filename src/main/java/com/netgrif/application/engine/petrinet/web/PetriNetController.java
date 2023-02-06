package com.netgrif.application.engine.petrinet.web;

import com.netgrif.application.engine.AsyncRunner;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.eventoutcomes.LocalisedEventOutcomeFactory;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.domain.version.StringToVersionConverter;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.petrinet.web.responsebodies.*;
import com.netgrif.application.engine.workflow.domain.FileStorageConfiguration;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.response.EventOutcomeWithMessage;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.response.EventOutcomeWithMessageResource;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
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

@RestController
@RequestMapping("/api/petrinet")
@ConditionalOnProperty(
        value = "nae.petrinet.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "PetriNet")
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

    @Autowired
    private AsyncRunner asyncRunner;

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Import new process",
            description = "Caller must have the ADMIN role. Imports an entirely new process or a new version of an existing process.",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Process model is invalid"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements")
    })
    @PostMapping(value = "/import", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<EventOutcomeWithMessage> importPetriNet(
            @RequestParam(value = "file", required = true) MultipartFile multipartFile,
            @RequestParam(value = "meta", required = false) String releaseType,
            Authentication auth, Locale locale) throws MissingPetriNetMetaDataException, MissingIconKeyException {
        try {
            VersionType release = releaseType == null ? VersionType.MAJOR : VersionType.valueOf(releaseType.trim().toUpperCase());
            ImportPetriNetEventOutcome importPetriNetOutcome = service.importPetriNet(multipartFile.getInputStream(), release, (LoggedUser) auth.getPrincipal());
            return EventOutcomeWithMessageResource.successMessage("Petri net " + multipartFile.getOriginalFilename() + " imported successfully",
                    LocalisedEventOutcomeFactory.from(importPetriNetOutcome, locale));
        } catch (IOException e) {
            log.error("Importing Petri net failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage("IO error while importing Petri net");
        }
    }

    @Operation(summary = "Get all processes", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
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

    @Operation(summary = "Get process by id", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public PetriNetReferenceResource getOne(@PathVariable("id") String id, Authentication auth, Locale locale) {
        return new PetriNetReferenceResource(IPetriNetService.transformToReference(service.getPetriNet(decodeUrl(id)), locale));
    }

    @Operation(summary = "Get process by identifier and version", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{identifier}/{version}", produces = MediaTypes.HAL_JSON_VALUE)
    public PetriNetReferenceResource getOne(@PathVariable("identifier") String identifier, @PathVariable("version") String version, Authentication auth, Locale locale) {
        String resolvedIdentifier = Base64.isBase64(identifier) ? new String(Base64.decodeBase64(identifier)) : identifier;
        return new PetriNetReferenceResource(service.getReference(resolvedIdentifier, converter.convert(version), (LoggedUser) auth.getPrincipal(), locale));
    }

    @Operation(summary = "Get transitions of processes", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/transitions", produces = MediaTypes.HAL_JSON_VALUE)
    public TransitionReferencesResource getTransitionReferences(@RequestParam List<String> ids, Authentication auth, Locale locale) {
        ids.forEach(id -> id = decodeUrl(id));
        return new TransitionReferencesResource(service.getTransitionReferences(ids, (LoggedUser) auth.getPrincipal(), locale));
    }

    @Operation(summary = "Get data fields of transitions", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/data", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public DataFieldReferencesResource getDataFieldReferences(@RequestBody List<TransitionReference> referenceBody, Locale locale) {
        return new DataFieldReferencesResource(service.getDataFieldReferences(referenceBody, locale));
    }

    @Operation(summary = "Get roles of process", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{netId}/roles", produces = MediaTypes.HAL_JSON_VALUE)
    public ProcessRolesResource getRoles(@PathVariable("netId") String netId, Locale locale) {
        netId = decodeUrl(netId);
        return new ProcessRolesResource(roleService.findAll(netId), service.getPetriNet(decodeUrl(netId)).getPermissions(), netId, locale);
    }

    @Operation(summary = "Get transactions of process", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{netId}/transactions", produces = MediaTypes.HAL_JSON_VALUE)
    public TransactionsResource getTransactions(@PathVariable("netId") String netId, Locale locale) {
        PetriNet net = service.getPetriNet(decodeUrl(netId));
        return new TransactionsResource(net.getTransactions().values(), netId, locale);
    }

    @Operation(summary = "Download process model", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{netId}/file", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public FileSystemResource getNetFile(@PathVariable("netId") String netId, @RequestParam(value = "title", required = false) String title, Authentication auth, HttpServletResponse response) {
        FileSystemResource fileResource = service.getFile(decodeUrl(netId), decodeUrl(title));
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileResource.getFilename() + Importer.FILE_EXTENSION + "\"");
        response.setHeader("Content-Length", String.valueOf(fileResource.getFile().length()));
        log.info("Downloading Petri net file: " + fileResource.getFilename() + " [" + netId + "]");
        return fileResource;
    }

    @Operation(summary = "Search processes", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/search", produces = MediaTypes.HAL_JSON_VALUE)
    public @ResponseBody
    PagedModel<PetriNetReferenceResource> searchPetriNets(@RequestBody Map<String, Object> criteria, Authentication auth, Pageable pageable, PagedResourcesAssembler<PetriNetReference> assembler, Locale locale) {
        LoggedUser user = (LoggedUser) auth.getPrincipal();
        Page<PetriNetReference> nets = service.search(criteria, user, pageable, locale);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PetriNetController.class)
                .searchPetriNets(criteria, auth, pageable, assembler, locale)).withRel("search");
        PagedModel<PetriNetReferenceResource> resources = assembler.toModel(nets, new PetriNetReferenceResourceAssembler(), selfLink);
        PetriNetReferenceResourceAssembler.buildLinks(resources);
        return resources;
    }

    @PreAuthorize("@petriNetAuthorizationService.canCallProcessDelete(#auth.getPrincipal(), #processId)")
    @Operation(summary = "Delete process",
            description = "Caller must have the ADMIN role. Removes the specified process, along with it's cases, tasks and process roles.",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements")
    })
    @DeleteMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource deletePetriNet(@PathVariable("id") String processId, Authentication auth) {
        String decodedProcessId = decodeUrl(processId);
        if (Objects.equals(decodedProcessId, "")) {
            log.error("Deleting Petri net [" + processId + "] failed: could not decode process ID from URL");
            return MessageResource.errorMessage("Deleting Petri net " + processId + " failed!");
        }
        LoggedUser user = (LoggedUser) auth.getPrincipal();
        asyncRunner.execute(() -> this.service.deletePetriNet(decodedProcessId, user));
        return MessageResource.successMessage("Petri net " + decodedProcessId + " is being deleted");
    }

    @Operation(summary = "Get net by case id", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/case/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public PetriNetImportReference getOne(@PathVariable("id") String caseId) {
            return service.getNetFromCase(decodeUrl(caseId));
    }

    public static String decodeUrl(String s1) {
        try {
            if (s1 == null)
                return null;
            return URLDecoder.decode(s1, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            log.error("Decoding URL failed: ", e);
            return "";
        }
    }
}
