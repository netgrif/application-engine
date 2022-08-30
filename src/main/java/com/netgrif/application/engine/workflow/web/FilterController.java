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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Deprecated(since = "5.3.0")
@RestController
@RequestMapping("/api/filter")
@ConditionalOnProperty(
        value = "nae.filter.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Filter")
public class FilterController {

    @Autowired
    private IFilterService filterService;

    @Operation(summary = "Save new filter", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource createFilter(@RequestBody CreateFilterBody newFilter, @RequestParam(required = false) MergeFilterOperation operation, Authentication auth, Locale locale) {
        Filter filter = filterService.saveFilter(newFilter, operation, (LoggedUser) auth.getPrincipal());
        if (filter != null)
            return MessageResource.successMessage("Filter " + newFilter.getTitle() + " successfully created");
        return MessageResource.errorMessage("Filter " + newFilter.getTitle() + " has failed to save");
    }

    @Operation(summary = "Delete filter specified by id", security = {@SecurityRequirement(name = "BasicAuth")})
    @DeleteMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource deleteFilter(@PathVariable("id") String filterId, Authentication auth) throws UnauthorisedRequestException {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        boolean success = filterService.deleteFilter(filterId, loggedUser);
        if (success)
            return MessageResource.successMessage("Filter " + filterId + " successfully deleted");
        return MessageResource.errorMessage("Filter " + filterId + " has failed to delete");
    }

    @Operation(summary = "Search for filter by provided criteria", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<LocalisedFilterResource> search(@RequestBody Map<String, Object> searchCriteria, Authentication auth, Locale locale, Pageable pageable, PagedResourcesAssembler<Filter> assembler) {
        Page<Filter> page = filterService.search(searchCriteria, pageable, (LoggedUser) auth.getPrincipal());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass())
                .search(searchCriteria, auth, locale, pageable, assembler)).withRel("search");
        return assembler.toModel(page, new FilterResourceAssembler(locale), selfLink);
    }

}
