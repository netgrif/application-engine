package com.netgrif.application.engine.elastic.web;

import com.netgrif.application.engine.configuration.properties.ElasticsearchProperties;
import com.netgrif.application.engine.elastic.service.ReindexingTask;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import com.querydsl.core.types.Predicate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/elastic")
@ConditionalOnProperty(
        value = "nae.elastic.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Elasticsearch")
public class ElasticController {

    private final IWorkflowService workflowService;
    private final ReindexingTask reindexingTask;
    private final ElasticsearchProperties properties;

    public ElasticController(IWorkflowService workflowService, ReindexingTask reindexingTask, ElasticsearchProperties properties) {
        this.workflowService = workflowService;
        this.reindexingTask = reindexingTask;
        this.properties = properties;
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Reindex specified cases",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/reindex", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public MessageResource reindex(@RequestBody Predicate predicate) {
        try {
            long count = workflowService.count(predicate);

            if (count == 0) {
                log.info("No cases to reindex");
                return MessageResource.successMessage("No cases to reindex");
            }

            long numOfPages = (count / properties.getReindexExecutor().getSize()) + 1;
            log.info("Reindexing cases: " + numOfPages + " pages");

            for (int page = 0; page < numOfPages; page++) {
                log.info("Indexing page " + (page + 1));
                reindexingTask.forceReindexPage(predicate, page, numOfPages);
            }
            return MessageResource.successMessage("Success");
        } catch (Exception e) {
            log.error("Could not index: ", e);
            return MessageResource.errorMessage(e.getMessage());
        }
    }
}
