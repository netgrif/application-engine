package com.netgrif.application.engine.elastic.web;

import com.netgrif.application.engine.elastic.service.ReindexingTask;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/elastic")
@ConditionalOnProperty(
        value = "nae.elastic.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Api(tags = {"Elasticsearch"}, authorizations = @Authorization("BasicAuth"))
public class ElasticController {

    private static final Logger log = LoggerFactory.getLogger(ElasticController.class.getName());

    private final IWorkflowService workflowService;
    private final ReindexingTask reindexingTask;

    @Value("${spring.data.elasticsearch.reindex-size}")
    private int pageSize;

    public ElasticController(IWorkflowService workflowService, ReindexingTask reindexingTask) {
        this.workflowService = workflowService;
        this.reindexingTask = reindexingTask;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation(value = "Reindex specified cases",
            notes = "Caller must have the ADMIN role",
            authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/reindex", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MessageResource.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements"),
    })
    public MessageResource reindex(@RequestBody Predicate predicate) {
        try {
            long count = workflowService.count(predicate);

            if (count == 0) {
                log.info("No cases to reindex");
            } else {
                long numOfPages = (count / pageSize) + 1;
                log.info("Reindexing cases: " + numOfPages + " pages");

                for (int page = 0; page < numOfPages; page++) {
                    log.info("Indexing page " + (page + 1));
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