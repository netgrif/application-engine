package com.netgrif.application.engine.petrinet.web;

import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.version.StringToVersionConverter;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.petrinet.web.responsebodies.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.netgrif.application.engine.petrinet.web.PetriNetController.decodeUrl;

@Slf4j
@RestController
@ConditionalOnProperty(
        value = "nae.public.petrinet.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Public PetriNet Controller")
@RequestMapping({"/api/public/petrinet"})
public class PublicPetriNetController {

    private final IPetriNetService petriNetService;

    private final IProcessRoleService roleService;

    private final IUserService userService;

    private final StringToVersionConverter converter;

    public PublicPetriNetController(IPetriNetService petriNetService, IUserService userService, StringToVersionConverter converter, IProcessRoleService roleService) {
        this.petriNetService = petriNetService;
        this.converter = converter;
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Get process by id")
    public PetriNetReferenceResource getOne(@PathVariable("id") String id, Locale locale) {
        return new PetriNetReferenceResource(IPetriNetService.transformToReference(this.petriNetService.getPetriNet(decodeUrl(id)), locale));
    }

    @Operation(summary = "Get process by identifier and version")
    @GetMapping(value = "/{identifier}/{version}", produces = MediaTypes.HAL_JSON_VALUE)
    @ResponseBody
    public PetriNetReferenceResource getOne(@PathVariable("identifier") String identifier, @PathVariable("version") String version, Locale locale) {
        String resolvedIdentifier = Base64.isBase64(identifier) ? new String(Base64.decodeBase64(identifier)) : identifier;
        return new PetriNetReferenceResource(this.petriNetService.getReference(resolvedIdentifier, this.converter.convert(version), userService.getAnonymousLogged(), locale));
    }

    @Operation(summary = "Search processes")
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<PetriNetReferenceResource> searchPetriNets(@RequestBody Map<String, Object> criteria, Pageable pageable, PagedResourcesAssembler<PetriNetReference> assembler, Locale locale) {
        Page<PetriNetReference> nets = petriNetService.search(criteria, userService.getAnonymousLogged(), pageable, locale);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PublicPetriNetController.class)
                .searchPetriNets(criteria, pageable, assembler, locale)).withRel("search");
        PagedModel<PetriNetReferenceResource> resources = assembler.toModel(nets, new PetriNetReferenceResourceAssembler(), selfLink);
        PetriNetReferenceResourceAssembler.buildLinks(resources);
        return resources;
    }

    @Operation(summary = "Get roles of process")
    @GetMapping(value = "/{netId}/roles", produces = MediaTypes.HAL_JSON_VALUE)
    public ProcessRolesResource getRoles(@PathVariable("netId") String netId, Locale locale) {
        netId = decodeUrl(netId);
        return new ProcessRolesResource(roleService.findAll(netId), petriNetService.getPetriNet(netId).getPermissions(), netId, locale);
    }

    @Operation(summary = "Get transactions of process")
    @GetMapping(value = "/{netId}/transactions", produces = MediaTypes.HAL_JSON_VALUE)
    public TransactionsResource getTransactions(@PathVariable("netId") String netId, Locale locale) {
        PetriNet net = petriNetService.getPetriNet(decodeUrl(netId));
        return new TransactionsResource(net.getTransactions().values(), netId, locale);
    }

    @Operation(summary = "Get data fields of transitions")
    @PostMapping(value = "/data", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public DataFieldReferencesResource getDataFieldReferences(@RequestBody List<TransitionReference> referenceBody, Locale locale) {
        return new DataFieldReferencesResource(petriNetService.getDataFieldReferences(referenceBody, locale));
    }

    @Operation(summary = "Get transitions of processes")
    @GetMapping(value = "/transitions", produces = MediaTypes.HAL_JSON_VALUE)
    public TransitionReferencesResource getTransitionReferences(@RequestParam List<String> ids, Locale locale) {
        ids.forEach(PetriNetController::decodeUrl);
        return new TransitionReferencesResource(petriNetService.getTransitionReferences(ids, userService.getAnonymousLogged(), locale));
    }
}
