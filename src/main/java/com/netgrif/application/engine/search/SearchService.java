package com.netgrif.application.engine.search;

import com.netgrif.application.engine.auth.domain.repositories.UserRepository;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.search.interfaces.ISearchService;
import com.netgrif.application.engine.search.utils.SearchUtils;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.netgrif.application.engine.search.utils.SearchUtils.evaluateQuery;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService implements ISearchService {

    private final PetriNetRepository petriNetRepository;

    private final IElasticCaseService elasticCaseService;

    private final CaseRepository caseRepository;

    private final TaskRepository taskRepository;

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

    private List<Case> findCasesElastic(String elasticQuery, Pageable pageable) {
        CaseSearchRequest caseSearchRequest = new CaseSearchRequest();
        caseSearchRequest.query = elasticQuery;
        return elasticCaseService.search(
                List.of(caseSearchRequest),
                userService.getLoggedOrSystem().transformToLoggedUser(),
                pageable,
                LocaleContextHolder.getLocale(),
                false
        ).getContent();
    }

    private boolean existsCasesElastic(String elasticQuery) {
        // todo NAE-1997: implement exists to elasticCaseService
        return countCasesElastic(elasticQuery) > 0;
    }

    @Override
    public String explainQuery(String input) {
        return SearchUtils.explainQuery(input);
    }

    @Override
    public Object search(String input) {
        QueryLangEvaluator evaluator = evaluateQuery(input);
        Predicate predicate = evaluator.getFullMongoQuery();
        String elasticQuery = evaluator.getFullElasticQuery();
        Pageable pageable = evaluator.getPageable();

        switch (evaluator.getType()) {
            case PROCESS:
                if (evaluator.getMultiple()) {
                    return petriNetRepository.findAll(predicate, pageable).getContent();
                }
                return petriNetRepository.findAll(predicate, PageRequest.of(0, 1))
                        .getContent().stream().findFirst().orElse(null);
            case CASE:
                if (!evaluator.getSearchWithElastic()) {
                    if (evaluator.getMultiple()) {
                        return caseRepository.findAll(predicate, pageable).getContent();
                    }
                    return caseRepository.findAll(predicate, PageRequest.of(0, 1))
                            .getContent().stream().findFirst().orElse(null);
                }

                List<Case> cases = findCasesElastic(elasticQuery, pageable);
                return evaluator.getMultiple() ? cases : cases.stream().findFirst().orElse(null);
            case TASK:
                if (evaluator.getMultiple()) {
                    return taskRepository.findAll(predicate, pageable).getContent();
                }
                return taskRepository.findAll(predicate, PageRequest.of(0, 1))
                        .getContent().stream().findFirst().orElse(null);
            case USER:
                if (evaluator.getMultiple()) {
                    return userRepository.findAll(predicate, pageable).getContent();
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

    @Override
    public boolean exists(String input) {
        QueryLangEvaluator evaluator = evaluateQuery(input);
        Predicate predicate = evaluator.getFullMongoQuery();
        String elasticQuery = evaluator.getFullElasticQuery();

        switch (evaluator.getType()) {
            case PROCESS:
                return petriNetRepository.exists(predicate);
            case CASE:
                if (!evaluator.getSearchWithElastic()) {
                    return caseRepository.exists(predicate);
                }
                return existsCasesElastic(elasticQuery);
            case TASK:
                return taskRepository.exists(predicate);
            case USER:
                return userRepository.exists(predicate);
        }
        return false;
    }
}
