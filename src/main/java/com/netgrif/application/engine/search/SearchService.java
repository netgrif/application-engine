package com.netgrif.application.engine.search;

import com.netgrif.application.engine.antlr4.QueryLangLexer;
import com.netgrif.application.engine.antlr4.QueryLangParser;
import com.netgrif.application.engine.auth.domain.repositories.UserRepository;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final IElasticCaseService elasticCaseService;

    private final CaseRepository caseRepository;

    private final TaskRepository taskRepository;

    private final PetriNetRepository petriNetRepository;

    private final UserRepository userRepository;

    private static ParseTree getParseTree(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        QueryLangLexer lexer = new QueryLangLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        QueryLangParser parser = new QueryLangParser(tokens);
        return parser.query();
    }

    public Object search(String input) {
        ParseTree tree = getParseTree(input);
        // todo NAE-1997: implement actual search
        return null;
    }

    public long count(String input) {
        ParseTree tree = getParseTree(input);
        // todo NAE-1997: implement actual count
        ParseTreeWalker walker = new ParseTreeWalker();
        QueryLangEvaluator evaluator2 = new QueryLangEvaluator();

        try {
            walker.walk(evaluator2, tree);

            Predicate predicate = evaluator2.getMongoQuery(tree);
            String elasticQuery = evaluator2.getElasticQuery(tree);
            switch (evaluator2.getType()) {
                case PROCESS:
                    return petriNetRepository.count(predicate);
                case CASE:
                    return caseRepository.count(predicate);
                case TASK:
                    return taskRepository.count(predicate);
                case USER:
                    return userRepository.count(predicate);
            }


        } catch (UnsupportedOperationException | IllegalArgumentException e) {
            // todo NAE-1997: count with elastic?
            log.error(e.getMessage());
        }


        return 0;
    }

}
