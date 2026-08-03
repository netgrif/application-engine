package com.netgrif.application.engine.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;
import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.elastic.domain.BulkOperationWrapper;
import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository;
import com.netgrif.application.engine.elastic.service.executors.Executor;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCasePrioritySearch;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.elastic.domain.ElasticCase;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNetSearch;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ElasticCaseServiceTest {

    @Test
    void buildFullTextQueryIgnoresNullAndBlankInput() {
        ElasticCaseService service = service(List.of("title"));
        BoolQuery.Builder query = new BoolQuery.Builder();

        service.buildFullTextQuery(new CaseSearchRequest(), query);
        service.buildFullTextQuery(CaseSearchRequest.builder().fullText(" \t\n ").build(), query);

        assertTrue(query.build().must().isEmpty());
    }

    @Test
    void buildFullTextQueryRequiresEveryTermInAtLeastOneConfiguredField() {
        ElasticCaseService service = service(List.of("title^3", "dataSet.*.fulltextValue"));
        CaseSearchRequest request = new CaseSearchRequest(Map.of("fullText", "Alpha beta"));
        BoolQuery.Builder query = new BoolQuery.Builder();

        service.buildFullTextQuery(request, query);

        BoolQuery fullTextQuery = onlyMustClause(query.build()).bool();
        assertEquals(2, fullTextQuery.must().size());
        assertTermQuery(fullTextQuery.must().get(0).bool(), "*Alpha*", 3.0f, 1.0f);
        assertTermQuery(fullTextQuery.must().get(1).bool(), "*beta*", 3.0f, 1.0f);
    }

    @Test
    void buildFullTextQueryEscapesLiteralWildcardCharacters() {
        ElasticCaseService service = service(List.of("title"));
        CaseSearchRequest request = new CaseSearchRequest(Map.of("fullText", "star* question?"));
        BoolQuery.Builder query = new BoolQuery.Builder();

        service.buildFullTextQuery(request, query);

        BoolQuery fullTextQuery = onlyMustClause(query.build()).bool();
        assertEquals("*star\\**", wildcard(fullTextQuery.must().get(0).bool(), 0).value());
        assertEquals("*question\\?*", wildcard(fullTextQuery.must().get(1).bool(), 0).value());
    }

    @Test
    void buildFullTextQueryUsesDefaultBoostForInvalidValues() {
        ElasticCaseService service = service(List.of(
                "valid^2.5",
                "missing",
                "empty^",
                "text^invalid",
                "zero^0",
                "negative^-4",
                "nan^NaN",
                "infinity^Infinity"
        ));
        BoolQuery.Builder query = new BoolQuery.Builder();

        service.buildFullTextQuery(CaseSearchRequest.builder().fullText("term").build(), query);

        BoolQuery termQuery = onlyMustClause(query.build()).bool().must().getFirst().bool();
        assertAll(
                () -> assertEquals(2.5f, wildcard(termQuery, 0).boost()),
                () -> assertEquals(1.0f, wildcard(termQuery, 1).boost()),
                () -> assertEquals(1.0f, wildcard(termQuery, 2).boost()),
                () -> assertEquals(1.0f, wildcard(termQuery, 3).boost()),
                () -> assertEquals(1.0f, wildcard(termQuery, 4).boost()),
                () -> assertEquals(1.0f, wildcard(termQuery, 5).boost()),
                () -> assertEquals(1.0f, wildcard(termQuery, 6).boost()),
                () -> assertEquals(1.0f, wildcard(termQuery, 7).boost())
        );
    }

    @Test
    void buildFullTextQueryIgnoresInputContainingOnlyEscapeCharacters() {
        ElasticCaseService service = service(List.of("title"));
        BoolQuery.Builder query = new BoolQuery.Builder();

        service.buildFullTextQuery(CaseSearchRequest.builder().fullText("\\").build(), query);

        assertTrue(query.build().must().isEmpty());
    }

    @Test
    void buildPetriNetQueryCombinesIdentifiersAndProcessIds() {
        ElasticCaseService service = service(List.of("title"));
        CaseSearchRequest request = CaseSearchRequest.builder()
                .process(List.of(
                        new CaseSearchRequest.PetriNet("invoice", null),
                        new CaseSearchRequest.PetriNet(null, "process-id")
                ))
                .build();
        BoolQuery.Builder query = new BoolQuery.Builder();

        service.buildPetriNetQuery(request, mock(LoggedUser.class), query);

        BoolQuery processQuery = onlyFilterClause(query.build()).bool();
        assertEquals(Set.of("processIdentifier", "processId"), processQuery.should().stream()
                .map(item -> item.terms().field())
                .collect(Collectors.toSet()));
    }

    @Test
    void buildAuthorQueryIncludesAllProvidedAuthorAttributes() {
        ElasticCaseService service = service(List.of("title"));
        CaseSearchRequest request = CaseSearchRequest.builder()
                .author(List.of(new CaseSearchRequest.Author("id", "Name", "username", "realm")))
                .build();
        BoolQuery.Builder query = new BoolQuery.Builder();

        service.buildAuthorQuery(request, query);

        BoolQuery authorsQuery = onlyFilterClause(query.build()).bool();
        assertEquals(1, authorsQuery.should().size());
        assertEquals(5, authorsQuery.should().getFirst().bool().must().size());
    }

    @Test
    void buildFieldQueriesAddsTaskRoleDataTagIdAndUriFilters() {
        ElasticCaseService service = service(List.of("title"));
        CaseSearchRequest request = CaseSearchRequest.builder()
                .transition(List.of("transition-1"))
                .role(List.of("role-1"))
                .data(Map.of("plain", "value", "nested.keyword", "nested-value"))
                .tags(Map.of("key", "tag-value"))
                .stringId(List.of("legacy-id"))
                .id(List.of("case-id"))
                .uriNodeId("uri-node")
                .build();
        BoolQuery.Builder query = new BoolQuery.Builder();

        service.buildTaskQuery(request, query);
        service.buildRoleQuery(request, query);
        service.buildDataQuery(request, query);
        service.buildTagsQuery(request, query);
        service.buildCaseIdQuery(request, query);
        service.buildUriNodeIdQuery(request, query);

        List<Query> filters = query.build().filter();
        assertEquals(6, filters.size());
        assertEquals("taskIds", filters.get(0).terms().field());
        assertEquals("enabledRoles", filters.get(1).terms().field());
        assertEquals(2, filters.get(2).bool().must().size());
        assertEquals(1, filters.get(3).bool().must().size());
        assertEquals("_id", filters.get(4).terms().field());
        assertEquals("uriNodeId", filters.get(5).term().field());
    }

    @Test
    void buildStringQueryReplacesCurrentUserPlaceholder() {
        ElasticCaseService service = service(List.of("title"));
        ObjectId userId = new ObjectId();
        LoggedUser user = mock(LoggedUser.class);
        when(user.getId()).thenReturn(userId);
        BoolQuery.Builder query = new BoolQuery.Builder();

        service.buildStringQuery(CaseSearchRequest.builder().query("author:<<me>>").build(), query, user);

        assertEquals("author:" + userId, onlyMustClause(query.build()).queryString().query());
    }

    @Test
    void buildGroupQueryHandlesEmptyAndResolvedGroups() {
        Fixture fixture = fixture(List.of("title"));
        LoggedUser user = mock(LoggedUser.class);
        CaseSearchRequest request = CaseSearchRequest.builder().group(List.of("group-1")).build();
        PetriNetReference reference = new PetriNetReference();
        reference.setIdentifier("invoice");
        when(fixture.petriNetService().search(any(PetriNetSearch.class), same(user), any(Pageable.class), eq(Locale.ENGLISH)))
                .thenReturn(Page.empty())
                .thenReturn(new PageImpl<>(List.of(reference)));

        assertTrue(fixture.service().buildGroupQuery(request, user, Locale.ENGLISH, new BoolQuery.Builder()));

        BoolQuery.Builder resolvedQuery = new BoolQuery.Builder();
        assertFalse(fixture.service().buildGroupQuery(request, user, Locale.ENGLISH, resolvedQuery));
        assertEquals("processIdentifier", onlyFilterClause(resolvedQuery.build()).terms().field());
    }

    @Test
    void resolveUnmappedSortAttributesPreservesPageAndSortDirection() {
        ElasticCaseService service = service(List.of("title"));
        Pageable original = PageRequest.of(2, 10, Sort.by(
                Sort.Order.asc("title"),
                Sort.Order.desc("createdDate")
        ));

        Pageable resolved = service.resolveUnmappedSortAttributes(original);

        assertEquals(2, resolved.getPageNumber());
        assertEquals(10, resolved.getPageSize());
        assertTrue(resolved.getSort().getOrderFor("title").isAscending());
        assertTrue(resolved.getSort().getOrderFor("createdDate").isDescending());
    }

    @Test
    void buildQuerySupportsIntersectionUnionAndBothSortDirections() {
        ElasticCaseService service = spy(service(List.of("title")));
        LoggedUser user = mock(LoggedUser.class);
        List<CaseSearchRequest> requests = List.of(new CaseSearchRequest(), new CaseSearchRequest());
        Pageable pageable = PageRequest.of(1, 5, Sort.by(
                Sort.Order.asc("title"),
                Sort.Order.desc("createdDate")
        ));
        doAnswer(ignored -> new BoolQuery.Builder()).when(service)
                .buildSingleQuery(any(CaseSearchRequest.class), same(user), eq(Locale.ENGLISH));

        assertNotNull(service.buildQuery(requests, user, pageable, Locale.ENGLISH, true));
        assertNotNull(service.buildQuery(requests, user, pageable, Locale.ENGLISH, false));
    }

    @Test
    void buildSingleQueryInvokesAllEmptyFilterPaths() {
        ElasticCaseService service = service(List.of("title"));
        LoggedUser user = loggedUser();

        BoolQuery query = service.buildSingleQuery(new CaseSearchRequest(), user, Locale.ENGLISH).build();

        assertEquals(1, query.filter().size());
    }

    @Test
    void searchAndCountHandleEmptyQueryAndRejectNullRequests() {
        ElasticCaseService service = spy(service(List.of("title")));
        LoggedUser user = loggedUser();
        Pageable pageable = PageRequest.of(0, 10);
        doReturn(null).when(service).buildQuery(anyList(), same(user), any(Pageable.class), eq(Locale.ENGLISH), eq(true));

        Page<Case> result = service.search(List.of(new CaseSearchRequest()), user, pageable, Locale.ENGLISH, true);

        assertTrue(result.isEmpty());
        assertEquals(0, service.count(List.of(new CaseSearchRequest()), user, Locale.ENGLISH, true));
        assertThrows(IllegalArgumentException.class,
                () -> service.search(null, user, pageable, Locale.ENGLISH, true));
        assertThrows(IllegalArgumentException.class,
                () -> service.count(null, user, Locale.ENGLISH, true));
    }

    @Test
    void removeIndexIndexNowAndStopQueuesDelegateToQueueManagers() {
        Fixture fixture = fixture(List.of("title"));
        ElasticQueueManager indexQueue = mock(ElasticQueueManager.class);
        ElasticQueueManager deleteQueue = mock(ElasticQueueManager.class);
        fixture.service().caseElasticIndexQueueManager = indexQueue;
        fixture.service().caseElasticDeleteQueueManager = deleteQueue;
        ElasticCase useCase = mock(ElasticCase.class);
        when(useCase.getId()).thenReturn("case-1");
        when(fixture.repository().findById("case-1")).thenReturn(Optional.empty());
        ElasticsearchConverter converter = mock(ElasticsearchConverter.class);
        when(fixture.template().getElasticsearchConverter()).thenReturn(converter);
        when(converter.mapObject(useCase)).thenReturn(Document.from(new HashMap<>()));

        fixture.service().remove("case-1");
        fixture.service().index(useCase);
        fixture.service().indexNow(useCase);
        ReflectionTestUtils.invokeMethod(fixture.service(), "stopQueues");

        ArgumentCaptor<BulkOperationWrapper> deleteOperation = ArgumentCaptor.forClass(BulkOperationWrapper.class);
        verify(deleteQueue).push(deleteOperation.capture());
        assertEquals("case-index", deleteOperation.getValue().getOperation().delete().index());
        assertEquals("case-1", deleteOperation.getValue().getOperation().delete().id());

        ArgumentCaptor<BulkOperationWrapper> indexOperations = ArgumentCaptor.forClass(BulkOperationWrapper.class);
        verify(indexQueue, times(2)).push(indexOperations.capture());
        assertTrue(indexOperations.getAllValues().stream()
                .allMatch(operation -> "case-index".equals(operation.getOperation().index().index())));
        verify(indexQueue).shutdown();
        verify(deleteQueue).shutdown();
    }

    private void assertTermQuery(BoolQuery termQuery, String expectedValue, float firstBoost, float secondBoost) {
        assertEquals("1", termQuery.minimumShouldMatch());
        assertEquals(2, termQuery.should().size());
        assertAll(
                () -> assertEquals("title", wildcard(termQuery, 0).field()),
                () -> assertEquals(expectedValue, wildcard(termQuery, 0).value()),
                () -> assertEquals(firstBoost, wildcard(termQuery, 0).boost()),
                () -> assertTrue(wildcard(termQuery, 0).caseInsensitive()),
                () -> assertEquals("dataSet.*.fulltextValue", wildcard(termQuery, 1).field()),
                () -> assertEquals(expectedValue, wildcard(termQuery, 1).value()),
                () -> assertEquals(secondBoost, wildcard(termQuery, 1).boost()),
                () -> assertTrue(wildcard(termQuery, 1).caseInsensitive())
        );
    }

    private Query onlyMustClause(BoolQuery query) {
        assertEquals(1, query.must().size());
        return query.must().getFirst();
    }

    private Query onlyFilterClause(BoolQuery query) {
        assertEquals(1, query.filter().size());
        return query.filter().getFirst();
    }

    private WildcardQuery wildcard(BoolQuery query, int index) {
        return query.should().get(index).wildcard();
    }

    private ElasticCaseService service(List<String> fullTextFields) {
        return fixture(fullTextFields).service();
    }

    private Fixture fixture(List<String> fullTextFields) {
        IElasticCasePrioritySearch prioritySearch = mock(IElasticCasePrioritySearch.class);
        when(prioritySearch.fullTextFields()).thenReturn(fullTextFields);
        ElasticCaseRepository repository = mock(ElasticCaseRepository.class);
        ElasticsearchTemplate template = mock(ElasticsearchTemplate.class);
        IPetriNetService petriNetService = mock(IPetriNetService.class);
        IWorkflowService workflowService = mock(IWorkflowService.class);
        DataConfigurationProperties.ElasticsearchProperties properties = new DataConfigurationProperties.ElasticsearchProperties();
        properties.setIndex(Map.of(DataConfigurationProperties.ElasticsearchProperties.CASE_INDEX, "case-index"));

        ElasticCaseService service = new ElasticCaseService(
                repository,
                template,
                mock(Executor.class),
                properties,
                petriNetService,
                workflowService,
                prioritySearch,
                mock(ApplicationEventPublisher.class),
                mock(ElasticsearchClient.class)
        );
        return new Fixture(service, repository, template, petriNetService, workflowService);
    }

    private LoggedUser loggedUser() {
        LoggedUser user = mock(LoggedUser.class);
        when(user.getId()).thenReturn(new ObjectId());
        when(user.getStringId()).thenReturn("user-id");
        when(user.getProcessRoles()).thenReturn(Set.of());
        when(user.getGroupIds()).thenReturn(Set.of());
        return user;
    }

    private record Fixture(ElasticCaseService service,
                           ElasticCaseRepository repository,
                           ElasticsearchTemplate template,
                           IPetriNetService petriNetService,
                           IWorkflowService workflowService) {
    }
}
