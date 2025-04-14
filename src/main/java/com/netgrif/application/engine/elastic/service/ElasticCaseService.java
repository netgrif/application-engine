package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import com.netgrif.application.engine.elastic.domain.ElasticCase;
import com.netgrif.application.engine.elastic.domain.repoitories.ElasticCaseRepository;
import com.netgrif.application.engine.elastic.service.executors.Executor;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseSearchService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.service.query.ElasticPermissionQueryBuilder;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.workflow.domain.Case;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * todo javadoc
 * */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticCaseService implements IElasticCaseService {

    private final ElasticCaseRepository repository;
    private final Executor executors;
    private final IElasticCaseMappingService mappingService;
    private final IElasticCaseSearchService searchService;
    private final ElasticPermissionQueryBuilder permissionQueryBuilder;

    @Override
    public void index(Case useCase) {
        executors.execute(useCase.getStringId(), () -> {
            ElasticCase elasticCase = mappingService.transform(useCase);
            try {
                ElasticCase savedCase = repository.findByStringId(useCase.getStringId());
                if (savedCase == null) {
                    repository.save(elasticCase);
                } else {
                    savedCase.update(elasticCase);
                    repository.save(savedCase);
                }
            } catch (InvalidDataAccessApiUsageException ignored) {
                log.debug("[{}]: Case \"{}\" has duplicates, will be reindexed", useCase.getStringId(), useCase.getTitle());
                repository.deleteAllByStringId(useCase.getStringId());
                repository.save(elasticCase);
            }
            log.debug("[{}]: Case \"{}\" indexed", useCase.getStringId(), useCase.getTitle());
        });
    }

    @Override
    public void indexNow(Case useCase) {
        index(useCase);
    }

    @Override
    public Page<Case> search(List<CaseSearchRequest> requests, LoggedIdentity identity, Pageable pageable, Locale locale,
                             Boolean isIntersection) {
        BoolQueryBuilder permissionQuery = permissionQueryBuilder.buildSingleQuery(identity.getActiveActorId());
        return searchService.search(requests, identity, pageable, locale, isIntersection, permissionQuery);
    }

    @Override
    public long count(List<CaseSearchRequest> requests, LoggedIdentity identity, Locale locale, Boolean isIntersection) {
        BoolQueryBuilder permissionQuery = permissionQueryBuilder.buildSingleQuery(identity.getActiveActorId());
        return searchService.count(requests, identity, locale, isIntersection, permissionQuery);
    }

    @Override
    public void remove(String caseId) {
        executors.execute(caseId, () -> {
            repository.deleteAllByStringId(caseId);
            log.info("[{}]: Case \"{}\" deleted", caseId, caseId);
        });
    }

    @Override
    public void removeByPetriNetId(String processId) {
        executors.execute(processId, () -> {
            repository.deleteAllByProcessId(processId);
            log.info("[{}]: All cases of Petri Net with id \"{}\" deleted", processId, processId);
        });
    }

    @Override
    public String findUriNodeId(Case aCase) {
        // todo 2058 autorisation
        return searchService.findUriNodeId(aCase);
    }
}
