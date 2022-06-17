package com.netgrif.application.engine.petrinet.web;

import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import com.netgrif.application.engine.petrinet.web.responsebodies.UriNodeResource;
import com.netgrif.application.engine.petrinet.web.responsebodies.UriNodeResources;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.response.EventOutcomeWithMessageResource;
import io.swagger.annotations.*;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/uri")
@Api(tags = {"Process URI"}, authorizations = @Authorization("BasicAuth"))
public class UriController {

    private final IUriService uriService;


    public UriController(IUriService uriService) {
        this.uriService = uriService;
    }

    @ApiOperation(value = "Get root UriNodes")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = EventOutcomeWithMessageResource.class),
    })
    @RequestMapping(value = "/roots", method = GET, produces = MediaTypes.HAL_JSON_VALUE)
    public CollectionModel<UriNode> getRoots() {
        List<UriNode> uriNodes = uriService.getRoots();
        uriNodes.forEach(uriService::populateDirectRelatives);
        return new UriNodeResources(uriNodes);
    }

    @ApiOperation(value = "Get root UriNodes")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = EventOutcomeWithMessageResource.class),
    })
    @RequestMapping(value = "/{uri}", method = GET, produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<UriNode> getOne(@PathVariable("uri") String uri) {
        UriNode uriNode = uriService.findByUri(uri);
        uriNode = uriService.populateDirectRelatives(uriNode);
        return new UriNodeResource(uriNode);
    }
}
