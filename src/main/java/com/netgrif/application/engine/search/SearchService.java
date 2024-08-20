package com.netgrif.application.engine.search;

import com.netgrif.application.engine.antlr4.QueryLangLexer;
import com.netgrif.application.engine.antlr4.QueryLangParser;
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
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService implements ISearchService {

    private final PetriNetRepository petriNetRepository;

    private final IElasticCaseService elasticCaseService;

    private final CaseRepository caseRepository;

    private final IWorkflowService workflowService;

    private final TaskRepository taskRepository;

    private final ITaskService taskService;

    private final UserRepository userRepository;

    private final IUserService userService;

    private static ParseTree getParseTree(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        QueryLangLexer lexer = new QueryLangLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        QueryLangParser parser = new QueryLangParser(tokens);
        return parser.query();
    }

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
                new FullPageRequest(), LocaleContextHolder.getLocale(),
                false
        ).getContent();
    }

    @Override
    public Object search(String input) {
        ParseTree root = getParseTree(input);
        ParseTreeWalker walker = new ParseTreeWalker();
        QueryLangEvaluator evaluator = new QueryLangEvaluator();

        try {
            walker.walk(evaluator, root);

            Predicate predicate = evaluator.getMongoQuery(root);
            String elasticQuery = evaluator.getElasticQuery(root);
            switch (evaluator.getType()) {
                case PROCESS:
                    if (evaluator.getMultiple()) {
                        return petriNetRepository.findAll(predicate, new FullPageRequest()).getContent();
                    }
                    return petriNetRepository.findOne(predicate);
                case CASE:
                    if (predicate != null) {
                        if (evaluator.getMultiple()) {
                            return workflowService.searchAll(predicate).getContent();
                        }
                        return workflowService.searchOne(predicate);
                    }

                    List<Case> cases = findCasesElastic(elasticQuery);
                    return evaluator.getMultiple() ? cases : cases.get(0);
                case TASK:
                    if (evaluator.getMultiple()) {
                        return taskService.searchAll(predicate).getContent();
                    }
                    return taskService.searchOne(predicate);
                case USER:
                    if (evaluator.getMultiple()) {
                        return userRepository.findAll(predicate, new FullPageRequest()).getContent();
                    }
                    return userRepository.findOne(predicate).orElse(null);
            }
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        }

        return null;
    }

    @Override
    public Long count(String input) {
        ParseTree root = getParseTree(input);
        ParseTreeWalker walker = new ParseTreeWalker();
        QueryLangEvaluator evaluator = new QueryLangEvaluator();

        try {
            walker.walk(evaluator, root);

            Predicate predicate = evaluator.getMongoQuery(root);
            String elasticQuery = evaluator.getElasticQuery(root);
            switch (evaluator.getType()) {
                case PROCESS:
                    return petriNetRepository.count(predicate);
                case CASE:
                    if (predicate != null) {
                        return caseRepository.count(predicate);
                    }
                    return countCasesElastic(elasticQuery);
                case TASK:
                    return taskRepository.count(predicate);
                case USER:
                    return userRepository.count(predicate);
            }
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        }

        return null;
    }

}
