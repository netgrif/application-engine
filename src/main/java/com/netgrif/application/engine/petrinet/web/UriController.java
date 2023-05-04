package com.netgrif.application.engine.petrinet.web;

import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import com.netgrif.application.engine.petrinet.web.responsebodies.UriNodeResource;
import com.netgrif.application.engine.petrinet.web.responsebodies.UriNodeResources;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/uri")
@Tag(name = "Process URI")
public class UriController {

    private final IUriService uriService;


    public UriController(IUriService uriService) {
        this.uriService = uriService;
    }

    @Operation(summary = "Get root UriNodes", security = {@SecurityRequirement(name = "BasicAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @GetMapping(value = "/root", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<UriNode> getRoot() {
        UriNode uriNode = uriService.getRoot();
        uriService.populateDirectRelatives(uriNode);
        return new UriNodeResource(uriNode);
    }

    @Operation(summary = "Get one UriNode by URI path", security = {@SecurityRequirement(name = "BasicAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @GetMapping(value = "/{uri}", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<UriNode> getOne(@PathVariable("uri") String uri) {
        uri = new String(Base64.getDecoder().decode(uri));
        UriNode uriNode = uriService.findByUri(uri);
        uriNode = uriService.populateDirectRelatives(uriNode);
        return new UriNodeResource(uriNode);
    }

    @Operation(summary = "Get UriNodes by parent id", security = {@SecurityRequirement(name = "BasicAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @GetMapping(value = "/parent/{parentId}", produces = MediaTypes.HAL_JSON_VALUE)
    public CollectionModel<UriNode> getByParent(@PathVariable("parentId") String parentId) {
        List<UriNode> uriNodes = uriService.findAllByParent(parentId);
        uriNodes.forEach(uriService::populateDirectRelatives);
        return new UriNodeResources(uriNodes);
    }

    @Operation(summary = "Get UriNodes by on the same level", security = {@SecurityRequirement(name = "BasicAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @GetMapping(value = "/level/{level}", produces = MediaTypes.HAL_JSON_VALUE)
    public CollectionModel<UriNode> getByLevel(@PathVariable("level") int level) {
        List<UriNode> uriNodes = uriService.findByLevel(level);
        uriNodes.forEach(uriService::populateDirectRelatives);
        return new UriNodeResources(uriNodes);
    }
}
