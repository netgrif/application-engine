package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.ApplicationEngine;
import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ApplicationEngine.class)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.yaml")
class ElasticCaseServiceIntegrationTest {

    private static final String FIRST_TERM = "TOTOK";
    private static final String SECOND_TERM = "Pistok";
    private static final Duration SEARCH_TIMEOUT = Duration.ofSeconds(15);

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IElasticCaseService elasticCaseService;

    @Autowired
    private IElasticCaseMappingService caseMappingService;

    @Autowired
    private ElasticCaseRepository elasticCaseRepository;

    @Autowired
    private SuperCreatorRunner superCreator;

    private PetriNet net;

    @BeforeEach
    void before() {
        testHelper.truncateDbs();
        net = importHelper.createNet("all_data.xml").orElseThrow();
    }

    @Test
    void fullTextSearchIsCaseInsensitiveAndRequiresEveryTerm() throws InterruptedException {
        Case matchingCase = createAndIndexCase(FIRST_TERM + " " + SECOND_TERM);
        Case nonMatchingCase = createAndIndexCase(FIRST_TERM + " hentok");
        waitForIndexedCases(List.of(matchingCase.getStringId(), nonMatchingCase.getStringId()));

        CaseSearchRequest request = fullTextRequest(
                FIRST_TERM.toLowerCase(Locale.ROOT) + " " + SECOND_TERM.toUpperCase(Locale.ROOT)
        );
        Page<Case> result = waitForSearchResult(List.of(request), true, 1);

        assertEquals(1, result.getTotalElements());
        assertCaseIds(result, matchingCase);
        assertEquals(1, elasticCaseService.count(
                List.of(request),
                superCreator.getLoggedSuper(),
                Locale.ENGLISH,
                true
        ));
    }

    @Test
    void fullTextSearchNormalizesWhitespaceAndBackslashes() throws InterruptedException {
        Case matchingCase = createAndIndexCase(FIRST_TERM + " " + SECOND_TERM);
        waitForIndexedCases(List.of(matchingCase.getStringId()));

        CaseSearchRequest request = fullTextRequest(
                "  \\" + FIRST_TERM + "\\  \t\n " + SECOND_TERM + "\\  "
        );
        Page<Case> result = waitForSearchResult(List.of(request), true, 1);

        assertEquals(1, result.getTotalElements());
        assertCaseIds(result, matchingCase);
    }

    @Test
    void fullTextSearchTreatsAsteriskAsLiteralCharacter() throws InterruptedException {
        Case literalMatch = createAndIndexCase("Asterisk*Marker");
        Case wildcardLookalike = createAndIndexCase("AsteriskXMarker");
        waitForIndexedCases(List.of(literalMatch.getStringId(), wildcardLookalike.getStringId()));

        Page<Case> result = waitForSearchResult(
                List.of(fullTextRequest("Asterisk*Marker")),
                true,
                1
        );

        assertEquals(1, result.getTotalElements());
        assertCaseIds(result, literalMatch);
    }

    @Test
    void fullTextSearchTreatsQuestionMarkAsLiteralCharacter() throws InterruptedException {
        Case literalMatch = createAndIndexCase("Question?Marker");
        Case wildcardLookalike = createAndIndexCase("QuestionXMarker");
        waitForIndexedCases(List.of(literalMatch.getStringId(), wildcardLookalike.getStringId()));

        Page<Case> result = waitForSearchResult(
                List.of(fullTextRequest("Question?Marker")),
                true,
                1
        );

        assertEquals(1, result.getTotalElements());
        assertCaseIds(result, literalMatch);
    }

    @Test
    void multipleFullTextRequestsSupportIntersectionAndUnion() throws InterruptedException {
        Case matchingBoth = createAndIndexCase(FIRST_TERM + " " + SECOND_TERM);
        Case matchingFirst = createAndIndexCase(FIRST_TERM + " OnlyFirst");
        Case matchingSecond = createAndIndexCase("OnlySecond " + SECOND_TERM);
        waitForIndexedCases(List.of(
                matchingBoth.getStringId(),
                matchingFirst.getStringId(),
                matchingSecond.getStringId()
        ));

        List<CaseSearchRequest> requests = List.of(
                fullTextRequest(FIRST_TERM),
                fullTextRequest(SECOND_TERM)
        );

        Page<Case> intersection = waitForSearchResult(requests, true, 1);
        Page<Case> union = waitForSearchResult(requests, false, 3);

        assertEquals(1, intersection.getTotalElements());
        assertCaseIds(intersection, matchingBoth);
        assertEquals(3, union.getTotalElements());
        assertCaseIds(union, matchingBoth, matchingFirst, matchingSecond);
        assertEquals(1, elasticCaseService.count(
                requests,
                superCreator.getLoggedSuper(),
                Locale.ENGLISH,
                true
        ));
        assertEquals(3, elasticCaseService.count(
                requests,
                superCreator.getLoggedSuper(),
                Locale.ENGLISH,
                false
        ));
    }

    @Test
    void searchIdsReturnsPageOfStrings() throws InterruptedException {
        Case case1 = createAndIndexCase(FIRST_TERM + " SearchIdsAlpha");
        Case case2 = createAndIndexCase(FIRST_TERM + " SearchIdsBeta");
        waitForIndexedCases(List.of(case1.getStringId(), case2.getStringId()));

        CaseSearchRequest request = fullTextRequest(FIRST_TERM);
        Page<String> idPage = waitForSearchIdsResult(List.of(request), true, 2);

        assertEquals(2, idPage.getTotalElements());
        assertTrue(idPage.getContent().contains(case1.getStringId()));
        assertTrue(idPage.getContent().contains(case2.getStringId()));
    }

    @Test
    void searchIdsSupportsPagination() throws InterruptedException {
        Case case1 = createAndIndexCase(FIRST_TERM + " PagingAlpha");
        Case case2 = createAndIndexCase(FIRST_TERM + " PagingBeta");
        waitForIndexedCases(List.of(case1.getStringId(), case2.getStringId()));

        CaseSearchRequest request = fullTextRequest(FIRST_TERM);
        waitForSearchIdsResult(List.of(request), true, 2);

        Page<String> page0 = elasticCaseService.searchIds(
                List.of(request),
                superCreator.getLoggedSuper(),
                PageRequest.of(0, 1),
                Locale.ENGLISH,
                true
        );
        Page<String> page1 = elasticCaseService.searchIds(
                List.of(request),
                superCreator.getLoggedSuper(),
                PageRequest.of(1, 1),
                Locale.ENGLISH,
                true
        );

        assertEquals(2, page0.getTotalElements());
        assertEquals(1, page0.getContent().size());
        assertEquals(2, page1.getTotalElements());
        assertEquals(1, page1.getContent().size());
        assertNotEquals(page0.getContent().get(0), page1.getContent().get(0));
    }

    @Test
    void savePartialUpdatesElasticsearchWithoutFullRead() throws InterruptedException {
        Case initialCase = createAndIndexCase("InitialPartialUpdateCase");
        waitForIndexedCases(List.of(initialCase.getStringId()));

        Case partial = workflowService.findOneProjected(initialCase.getStringId(), List.of("text"));
        partial.setTitle("UpdatedPartialElasticCase");
        workflowService.savePartial(partial);

        CaseSearchRequest request = fullTextRequest("UpdatedPartialElasticCase");
        Page<Case> result = waitForSearchResult(List.of(request), true, 1);

        assertEquals(1, result.getTotalElements());
        assertCaseIds(result, initialCase);
    }

    private Case createAndIndexCase(String title) {
        Case useCase = importHelper.createCaseAsSuper(title, net);
        Case savedCase = workflowService.save(useCase);
        elasticCaseService.indexNow(caseMappingService.transform(savedCase));
        return savedCase;
    }

    private CaseSearchRequest fullTextRequest(String fullText) {
        return new CaseSearchRequest(Map.of("fullText", fullText));
    }

    private void assertCaseIds(Page<Case> result, Case... expectedCases) {
        assertEquals(
                List.of(expectedCases).stream()
                        .map(Case::getStringId)
                        .sorted()
                        .toList(),
                result.getContent().stream()
                        .map(Case::getStringId)
                        .sorted()
                        .toList(),
                "The search result contains unexpected case IDs"
        );
    }

    private void waitForIndexedCases(List<String> caseIds) throws InterruptedException {
        long deadline = System.nanoTime() + SEARCH_TIMEOUT.toNanos();
        while (System.nanoTime() < deadline) {
            if (caseIds.stream().allMatch(caseId -> elasticCaseRepository.findById(caseId).isPresent())) {
                return;
            }
            Thread.sleep(100);
        }
        assertTrue(caseIds.stream().allMatch(caseId -> elasticCaseRepository.findById(caseId).isPresent()),
                "The test cases were not indexed before the timeout");
    }

    private Page<Case> waitForSearchResult(List<CaseSearchRequest> requests,
                                           boolean intersection,
                                           long expectedCount) throws InterruptedException {
        long deadline = System.nanoTime() + SEARCH_TIMEOUT.toNanos();
        Page<Case> result;
        do {
            result = elasticCaseService.search(
                    requests,
                    superCreator.getLoggedSuper(),
                    PageRequest.of(0, 10),
                    Locale.ENGLISH,
                    intersection
            );
            if (result.getTotalElements() == expectedCount) {
                return result;
            }
            Thread.sleep(100);
        } while (System.nanoTime() < deadline);
        return result;
    }

    private Page<String> waitForSearchIdsResult(List<CaseSearchRequest> requests,
                                               boolean intersection,
                                               long expectedCount) throws InterruptedException {
        long deadline = System.nanoTime() + SEARCH_TIMEOUT.toNanos();
        Page<String> result;
        do {
            result = elasticCaseService.searchIds(
                    requests,
                    superCreator.getLoggedSuper(),
                    PageRequest.of(0, 10),
                    Locale.ENGLISH,
                    intersection
            );
            if (result.getTotalElements() == expectedCount) {
                return result;
            }
            Thread.sleep(100);
        } while (System.nanoTime() < deadline);
        return result;
    }
}
