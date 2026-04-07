package com.netgrif.application.engine.elastic.web;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.elastic.service.ReindexingTask;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticIndexService;
import com.netgrif.application.engine.elastic.web.requestbodies.IndexParams;
import com.netgrif.application.engine.workflow.service.CaseSearchService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import com.querydsl.core.types.Predicate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/elastic")
@ConditionalOnProperty(
        value = "nae.elastic.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Elasticsearch")
public class ElasticController {

    private static final Logger log = LoggerFactory.getLogger(ElasticController.class.getName());

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private CaseSearchService searchService;

    @Autowired
    private ReindexingTask reindexingTask;

    @Autowired
    private IElasticIndexService indexService;

    @Value("${spring.data.elasticsearch.reindexExecutor.size:20}")
    private int pageSize;

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Reindex specified cases",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/reindex", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public MessageResource reindex(@RequestBody Map<String, Object> searchBody, Authentication auth, Locale locale) {
        try {
            LoggedUser user = (LoggedUser) auth.getPrincipal();
            long count = workflowService.count(searchBody, user, locale);

            if (count == 0) {
                log.info("No cases to reindex");
            } else {
                long numOfPages = (count / pageSize) + 1;
                log.info("Reindexing cases: {} pages", numOfPages);

                for (int page = 0; page < numOfPages; page++) {
                    log.info("Indexing page {}", (page + 1));
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

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Reindex all or stale cases with bulk index",
            description = "Reindex all or stale cases (specified by IndexParams.indexAll param) with bulk index. Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    @PostMapping(value = "/reindex/bulk", produces = MediaType.APPLICATION_JSON_VALUE)
    public MessageResource bulkReindex(IndexParams indexParams) {
        try {
            indexService.bulkIndex(indexParams.isIndexAll(), null, indexParams.getCaseBatchSize(), indexParams.getTaskBatchSize());
            return MessageResource.successMessage("Success");
        } catch (Exception e) {
            log.error("Could not index: ", e);
            return MessageResource.errorMessage(e.getMessage());
        }
    }
}
