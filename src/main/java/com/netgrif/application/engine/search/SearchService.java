package com.netgrif.application.engine.search;

import com.netgrif.application.engine.antlr4.QueryLangLexer;
import com.netgrif.application.engine.antlr4.QueryLangParser;
import com.netgrif.application.engine.auth.domain.repositories.UserRepository;
import com.netgrif.application.engine.elastic.service.ElasticCaseService;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private ElasticCaseService elasticCaseService;

    private CaseRepository caseRepository;

    private TaskRepository taskRepository;

    private PetriNetRepository petriNetRepository;

    private UserRepository userRepository;

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
        QueryLangMongoEvaluator evaluator = new QueryLangMongoEvaluator();

        Predicate predicate;
        try {
            predicate = evaluator.visit(tree);
            switch (evaluator.getType()) {
                case PROCESS:
                    return petriNetRepository.count(predicate);
                case CASE:
                    return caseRepository.count(predicate);
                case TASK:
                    return taskRepository.count(predicate);
                case USER:
                    return userRepository.count(predicate);
            }
        } catch (UnsupportedOperationException e) {
            // todo NAE-1997: count with elastic?
            log.error(e.getMessage());
        }


        return 0;
    }

    public static String convertDate(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
    }

    public static String convertDate(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
    }

    public static String convertDateTime(LocalDate localDate) {
        return localDate.atStartOfDay().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
    }

    public static String convertDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
    }

}
