package com.netgrif.workflow.workflow.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.throwable.UnauthorisedRequestException;
import com.netgrif.workflow.workflow.domain.Filter;
import com.netgrif.workflow.workflow.domain.MergeFilterOperation;
import com.netgrif.workflow.workflow.service.interfaces.IFilterService;
import com.netgrif.workflow.workflow.web.requestbodies.CreateFilterBody;
import com.netgrif.workflow.workflow.web.responsebodies.FilterResourceAssembler;
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedFilterResource;
import com.netgrif.workflow.workflow.web.responsebodies.MessageResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/filter")
public class FilterController {

    @Autowired
    private IFilterService filterService;

    @RequestMapping(method = RequestMethod.POST)
    public MessageResource createFilter(@RequestBody CreateFilterBody newFilter, @RequestParam(defaultValue = "AND") MergeFilterOperation operation, Authentication auth, Locale locale) {
        Filter filter = filterService.saveFilter(newFilter, operation, (LoggedUser) auth.getPrincipal());
        if (filter != null)
            return MessageResource.successMessage("Filter " + newFilter.getTitle() + " successfully created");
        return MessageResource.errorMessage("Filter " + newFilter.getTitle() + " has failed to save");
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public MessageResource deleteFilter(@PathVariable("id") String filterId, Authentication auth) throws UnauthorisedRequestException {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        boolean success = filterService.deleteFilter(filterId, loggedUser);
        if (success)
            return MessageResource.successMessage("Filter " + filterId + " successfully deleted");
        return MessageResource.errorMessage("Filter " + filterId + " has failed to delete");
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public PagedResources<LocalisedFilterResource> search(@RequestBody Map<String, Object> searchCriteria, Authentication auth, Locale locale, Pageable pageable, PagedResourcesAssembler<Filter> assembler) {
        Page<Filter> page = filterService.search(searchCriteria,pageable,(LoggedUser)auth.getPrincipal());
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(this.getClass())
                .search(searchCriteria,auth,locale,pageable,assembler)).withRel("search");
        PagedResources<LocalisedFilterResource> resources = assembler.toResource(page,new FilterResourceAssembler(locale),selfLink);
        return resources;
    }

}
