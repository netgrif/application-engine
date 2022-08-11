package com.netgrif.application.engine.petrinet.web;

import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import com.netgrif.application.engine.petrinet.web.responsebodies.UriNodeResource;
import com.netgrif.application.engine.petrinet.web.responsebodies.UriNodeResources;
import io.swagger.annotations.*;
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
@Api(tags = {"Process URI"}, authorizations = @Authorization("BasicAuth"))
public class UriController {

    private final IUriService uriService;


    public UriController(IUriService uriService) {
        this.uriService = uriService;
    }

    @ApiOperation(value = "Get root UriNodes", authorizations = @Authorization("BasicAuth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = UriNodeResource.class),
    })
    @GetMapping(value = "/root", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<UriNode> getRoot() {
        UriNode uriNode = uriService.getRoot();
        uriService.populateDirectRelatives(uriNode);
        return new UriNodeResource(uriNode);
    }

    @ApiOperation(value = "Get one UriNode by URI path", authorizations = @Authorization("BasicAuth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = UriNodeResource.class),
    })
    @GetMapping(value = "/{uri}", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<UriNode> getOne(@PathVariable("uri") String uri) {
        uri = new String(Base64.getDecoder().decode(uri));
        UriNode uriNode = uriService.findByUri(uri);
        uriNode = uriService.populateDirectRelatives(uriNode);
        return new UriNodeResource(uriNode);
    }

    @ApiOperation(value = "Get UriNodes by parent id", authorizations = @Authorization("BasicAuth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = UriNodeResources.class),
    })
    @GetMapping(value = "/parent/{parentId}", produces = MediaTypes.HAL_JSON_VALUE)
    public CollectionModel<UriNode> getByParent(@PathVariable("parentId") String parentId) {
        List<UriNode> uriNodes = uriService.findAllByParent(parentId);
        uriNodes.forEach(uriService::populateDirectRelatives);
        return new UriNodeResources(uriNodes);
    }

    @ApiOperation(value = "Get UriNodes by on the same level", authorizations = @Authorization("BasicAuth"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = UriNodeResources.class),
    })
    @GetMapping(value = "/level/{level}", produces = MediaTypes.HAL_JSON_VALUE)
    public CollectionModel<UriNode> getByLevel(@PathVariable("level") int level) {
        List<UriNode> uriNodes = uriService.findByLevel(level);
        uriNodes.forEach(uriService::populateDirectRelatives);
        return new UriNodeResources(uriNodes);
    }
}
