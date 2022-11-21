package com.netgrif.application.engine.workflow.web;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.domain.throwable.UnauthorisedRequestException;
import com.netgrif.application.engine.workflow.domain.Filter;
import com.netgrif.application.engine.workflow.domain.MergeFilterOperation;
import com.netgrif.application.engine.workflow.service.interfaces.IFilterService;
import com.netgrif.application.engine.workflow.web.requestbodies.CreateFilterBody;
import com.netgrif.application.engine.workflow.web.responsebodies.FilterResourceAssembler;
import com.netgrif.application.engine.workflow.web.responsebodies.LocalisedFilterResource;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

/**
 * @deprecated since 5.3.0 - Filter engine processes should be used instead of native objects
 */
@Deprecated
@RestController
@RequestMapping("/api/filter")
@ConditionalOnProperty(
        value = "nae.filter.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Api(tags = {"Filter"}, authorizations = @Authorization("BasicAuth"))
public class FilterController {

    @Autowired
    private IFilterService filterService;

    @ApiOperation(value = "Save new filter", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource createFilter(@RequestBody CreateFilterBody newFilter, @RequestParam(required = false) MergeFilterOperation operation, Authentication auth, Locale locale) {
        Filter filter = filterService.saveFilter(newFilter, operation, (LoggedUser) auth.getPrincipal());
        if (filter != null)
            return MessageResource.successMessage("Filter " + newFilter.getTitle() + " successfully created");
        return MessageResource.errorMessage("Filter " + newFilter.getTitle() + " has failed to save");
    }

    @ApiOperation(value = "Delete filter specified by id", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource deleteFilter(@PathVariable("id") String filterId, Authentication auth) throws UnauthorisedRequestException {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        boolean success = filterService.deleteFilter(filterId, loggedUser);
        if (success)
            return MessageResource.successMessage("Filter " + filterId + " successfully deleted");
        return MessageResource.errorMessage("Filter " + filterId + " has failed to delete");
    }

    @ApiOperation(value = "Search for filter by provided criteria", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/search", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<LocalisedFilterResource> search(@RequestBody Map<String, Object> searchCriteria, Authentication auth, Locale locale, Pageable pageable, PagedResourcesAssembler<Filter> assembler) {
        Page<Filter> page = filterService.search(searchCriteria, pageable, (LoggedUser) auth.getPrincipal());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass())
                .search(searchCriteria, auth, locale, pageable, assembler)).withRel("search");
        return assembler.toModel(page, new FilterResourceAssembler(locale), selfLink);
    }

}
