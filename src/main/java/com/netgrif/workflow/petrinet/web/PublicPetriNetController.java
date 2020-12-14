package com.netgrif.workflow.petrinet.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.petrinet.domain.version.StringToVersionConverter;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReferenceResourceAssembler;
import com.netgrif.workflow.workflow.web.PublicAbstractController;
import io.swagger.annotations.Authorization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReferenceResource;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping({"/api/public"})
@Slf4j
public class PublicPetriNetController extends PublicAbstractController {

    private final IPetriNetService service;

    private final StringToVersionConverter converter;

    public PublicPetriNetController(IPetriNetService service, IUserService userService, StringToVersionConverter converter) {
        super(userService);
        this.service = service;
        this.converter = converter;
    }

    @GetMapping(value = "/petrinet/{id}", produces = "application/hal+json")
    @ApiOperation(value = "Get process by id")
    public PetriNetReferenceResource getOne(@PathVariable("id") String id, Locale locale) {
        return new PetriNetReferenceResource(IPetriNetService.transformToReference(this.service.getPetriNet(decodeUrl(id)), locale));
    }

    @ApiOperation(
            value = "Get process by identifier and version"
    )
    @GetMapping(
            value = {"/petrinet/{identifier}/{version}"},
            produces = {"application/hal+json"}
    )
    @ResponseBody
    public PetriNetReferenceResource getOne(@PathVariable("identifier") String identifier, @PathVariable("version") String version, Locale locale) {
        return new PetriNetReferenceResource(this.service.getReference(identifier, this.converter.convert(version), getAnonym(), locale));
    }

    @ApiOperation(value = "Search processes")
    @RequestMapping(value = "/petrinet/search", method = POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<PetriNetReferenceResource> searchPetriNets(@RequestBody Map<String, Object> criteria, Pageable pageable, PagedResourcesAssembler<PetriNetReference> assembler, Locale locale) {
        Page<PetriNetReference> nets = service.search(criteria, getAnonym(), pageable, locale);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PublicPetriNetController.class)
                .searchPetriNets(criteria, pageable, assembler, locale)).withRel("search");
        PagedResources<PetriNetReferenceResource> resources = assembler.toResource(nets, new PetriNetReferenceResourceAssembler(), selfLink);
        PetriNetReferenceResourceAssembler.buildLinks(resources);
        return resources;
    }


    private static String decodeUrl(String s1) {
        try {
            return s1 == null ? null : URLDecoder.decode(s1, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException var2) {
            log.error("Decoding URL failed: ", var2);
            return "";
        }
    }
}
