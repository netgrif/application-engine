package com.netgrif.workflow.elastic.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.elastic.service.ReindexingTask;
import com.netgrif.workflow.workflow.service.CaseSearchService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import com.netgrif.workflow.workflow.web.responsebodies.MessageResource;
import com.querydsl.core.types.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/elastic")
public class ElasticController {

    private static final Logger log = LoggerFactory.getLogger(ElasticController.class.getName());

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private CaseSearchService searchService;

    @Autowired
    private ReindexingTask reindexingTask;

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/reindex", method = RequestMethod.POST)
    public MessageResource reindex(@RequestBody Map<String, Object> searchBody, Authentication auth, Locale locale) {
        try {
            LoggedUser user = (LoggedUser) auth.getPrincipal();
            long count = workflowService.count(searchBody, user, locale);

            if (count == 0) {
                log.info("No cases to reindex");
            } else {
                long numOfPages = (long) ((count / 100.0) + 1);
                log.info("Reindexing cases: " + numOfPages + " pages");

                for (int page = 0; page < numOfPages; page++) {
                    log.info("Indexing page " + (page + 1));
                    Predicate predicate = searchService.buildQuery(searchBody, user, locale);
                    reindexingTask.forceReindexPage(predicate, page, numOfPages);
                }
            }

            return MessageResource.successMessage("Success");
        } catch (Exception e) {
            log.error("Could not index: ", e);
            return MessageResource.errorMessage(e.getMessage());
        }
    }
}