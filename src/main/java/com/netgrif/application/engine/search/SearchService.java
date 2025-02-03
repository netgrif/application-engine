package com.netgrif.application.engine.search;

import com.netgrif.application.engine.auth.domain.repositories.UserRepository;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.search.interfaces.ISearchService;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.netgrif.application.engine.search.SearchUtils.evaluateQuery;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService implements ISearchService {

    public static final int DEFAULT_ELASTIC_PAGE_SIZE = 100000;

    private final PetriNetRepository petriNetRepository;

    private final IElasticCaseService elasticCaseService;

    private final CaseRepository caseRepository;

    private final IWorkflowService workflowService;

    private final TaskRepository taskRepository;

    private final ITaskService taskService;

    private final UserRepository userRepository;

    private final IUserService userService;

    private Long countCasesElastic(String elasticQuery) {
        CaseSearchRequest caseSearchRequest = new CaseSearchRequest();
        caseSearchRequest.query = elasticQuery;
        return elasticCaseService.count(
                List.of(caseSearchRequest),
                userService.getLoggedOrSystem().transformToLoggedUser(),
                LocaleContextHolder.getLocale(),
                false
        );
    }

    private List<Case> findCasesElastic(String elasticQuery) {
        CaseSearchRequest caseSearchRequest = new CaseSearchRequest();
        caseSearchRequest.query = elasticQuery;
        return elasticCaseService.search(
                List.of(caseSearchRequest),
                userService.getLoggedOrSystem().transformToLoggedUser(),
                PageRequest.of(0, DEFAULT_ELASTIC_PAGE_SIZE), LocaleContextHolder.getLocale(),
                false
        ).getContent();
    }

    @Override
    public Object search(String input) {
        QueryLangEvaluator evaluator = evaluateQuery(input);
        Predicate predicate = evaluator.getFullMongoQuery();
        String elasticQuery = evaluator.getFullElasticQuery();

        switch (evaluator.getType()) {
            case PROCESS:
                if (evaluator.getMultiple()) {
                    return petriNetRepository.findAll(predicate, new FullPageRequest()).getContent();
                }
                return petriNetRepository.findAll(predicate, PageRequest.of(0, 1))
                        .getContent().stream().findFirst().orElse(null);
            case CASE:
                if (!evaluator.getSearchWithElastic()) {
                    if (evaluator.getMultiple()) {
                        return workflowService.searchAll(predicate).getContent();
                    }
                    return workflowService.searchAll(predicate).getContent().stream().findFirst().orElse(null);
                }

                List<Case> cases = findCasesElastic(elasticQuery);
                return evaluator.getMultiple() ? cases : cases.get(0);
            case TASK:
                if (evaluator.getMultiple()) {
                    return taskService.searchAll(predicate).getContent();
                }
                return taskService.searchAll(predicate).getContent().stream().findFirst().orElse(null);
            case USER:
                if (evaluator.getMultiple()) {
                    return userRepository.findAll(predicate, new FullPageRequest()).getContent();
                }
                return userRepository.findAll(predicate, PageRequest.of(0, 1))
                        .getContent().stream().findFirst().orElse(null);
        }
        return null;
    }

    @Override
    public Long count(String input) {
        QueryLangEvaluator evaluator = evaluateQuery(input);
        Predicate predicate = evaluator.getFullMongoQuery();
        String elasticQuery = evaluator.getFullElasticQuery();

        switch (evaluator.getType()) {
            case PROCESS:
                return petriNetRepository.count(predicate);
            case CASE:
                if (!evaluator.getSearchWithElastic()) {
                    return caseRepository.count(predicate);
                }
                return countCasesElastic(elasticQuery);
            case TASK:
                return taskRepository.count(predicate);
            case USER:
                return userRepository.count(predicate);
        }
        return null;
    }

}
