package com.netgrif.application.engine.elastic.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.netgrif.application.engine.configuration.ElasticsearchConfiguration;
import com.netgrif.application.engine.elastic.domain.ElasticPetriNetRepository;
import com.netgrif.application.engine.elastic.service.executors.Executor;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetService;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.elastic.domain.ElasticPetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNetSearch;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.application.engine.utils.FullPageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static org.springframework.data.elasticsearch.client.elc.Queries.termQuery;

@Service
@Slf4j
public class ElasticPetriNetService implements IElasticPetriNetService {

    private final ElasticPetriNetRepository repository;

    private final Executor executors;

    private IPetriNetService petriNetService;

    private final ElasticsearchTemplate template;

    protected ElasticsearchConfiguration elasticsearchConfiguration;

    public ElasticPetriNetService(ElasticPetriNetRepository repository, Executor executors, ElasticsearchTemplate template, ElasticsearchConfiguration elasticsearchConfiguration) {
        this.repository = repository;
        this.executors = executors;
        this.template = template;
        this.elasticsearchConfiguration = elasticsearchConfiguration;
    }

    @Lazy
    @Autowired
    public void setPetriNetService(IPetriNetService petriNetService) {
        this.petriNetService = petriNetService;
    }

    @Override
    public void index(ElasticPetriNet net) {
        executors.execute(net.getId(), () -> {
            try {
                Optional<com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticPetriNet> elasticPetriNetOptional = repository.findById(net.getId());
                if (elasticPetriNetOptional.isEmpty()) {
                    repository.save((com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticPetriNet) net);
                } else {
                    com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticPetriNet elasticNet = elasticPetriNetOptional.get();
                    elasticNet.update(net);
                    repository.save(elasticNet);
                }
                log.debug("[{}]: PetriNet \"{}\" indexed", net.getId(), net.getTitle());
            } catch (InvalidDataAccessApiUsageException ignored) {
                log.debug("[{}]: PetriNet \"{}\" has duplicates, will be reindexed", net.getId(), net.getTitle());
                repository.deleteAllById(net.getId());
                repository.save((com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticPetriNet) net);
                log.debug("[{}]: PetriNet \"{}\" indexed", net.getId(), net.getTitle());
            }
        });
    }

    @Override
    public void indexNow(ElasticPetriNet net) {
        index(net);
    }

    @Override
    public void remove(String id) {
        executors.execute(id, () -> {
            repository.deleteAllById(id);
            log.info("[{}]: PetriNet \"{}\" deleted", id, id);
        });
    }

    /**
     * Method for search of PetriNets in Elastic
     *
     * @param requests       - search body, for now only title working
     * @param user           - logged user
     * @param pageable       - pageable for paging
     * @param locale         - internacionalization
     * @param isIntersection - property for merging filter, not implemented now, use false
     * @return Page<PetriNetReference> - page of PetriNetReferences
     */
    @Override
    public Page<PetriNetReference> search(PetriNetSearch requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection) {
        if (requests == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }
        log.debug("Searching for PetriNet query with logged user [{}]", user.getId());
        // TODO: impersonation
//        LoggedUser loggedOrImpersonated = user.getSelfOrImpersonated();
        LoggedUser loggedOrImpersonated = user;
        NativeQuery query = buildQuery(requests, loggedOrImpersonated, pageable, locale, isIntersection);
        List<PetriNet> netPage;
        long total;
        if (query != null) {
            SearchHits<ElasticPetriNet> hits = template.search(query, ElasticPetriNet.class, IndexCoordinates.of(elasticsearchConfiguration.elasticPetriNetIndex()));
            Page<ElasticPetriNet> indexedNets = (Page) SearchHitSupport.unwrapSearchHits(SearchHitSupport.searchPageFor(hits, query.getPageable()));
            netPage = petriNetService.findAllById(indexedNets.get().map(ElasticPetriNet::getId).collect(Collectors.toList()));
            total = indexedNets.getTotalElements();
            log.debug("Found [{}] total elements of page [{}]", netPage.size(), pageable.getPageNumber());
        } else {
            netPage = Collections.emptyList();
            total = 0;
        }

        return new PageImpl<>(netPage.stream().map(net -> new PetriNetReference(net, locale)).collect(Collectors.toList()), pageable, total);
    }

    protected NativeQuery buildQuery(PetriNetSearch request, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection) {
        List<BoolQuery.Builder> singleQueries = new LinkedList<>();
        singleQueries.add(buildSingleQuery(request, user, locale));

        if (isIntersection && !singleQueries.stream().allMatch(Objects::nonNull)) {
            // one of the queries evaluates to empty set => the entire result is an empty set
            return null;
        } else if (!isIntersection) {
            singleQueries = singleQueries.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (singleQueries.isEmpty()) {
                // all queries result in an empty set => the entire result is an empty set
                return null;
            }
        }

        BinaryOperator<BoolQuery.Builder> reductionOperation = isIntersection ? (a, b) -> a.must(b.build()._toQuery()) : (a, b) -> a.should(b.build()._toQuery());
        BoolQuery.Builder query = singleQueries.stream().reduce(new BoolQuery.Builder(), reductionOperation);

        NativeQueryBuilder builder = new NativeQueryBuilder();
        return builder
                .withQuery(query.build()._toQuery())
                .withPageable(pageable)
                .build();
    }

    protected BoolQuery.Builder buildSingleQuery(PetriNetSearch request, LoggedUser user, Locale locale) {
        BoolQuery.Builder query = new BoolQuery.Builder();

        buildFullTextQuery(request, query);
        boolean resultAlwaysEmpty = buildGroupQuery(request, user, locale, query);
        if (resultAlwaysEmpty) {
            return null;
        }
        return query;
    }

    protected void buildFullTextQuery(PetriNetSearch request, BoolQuery.Builder query) {
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            return;
        }

        // TODO refactor to properties
        String searchText = "*" + request.getTitle() + "*";
        List<String> fullTextFields = List.of(
                "title.textValue^2",
                "identifier^1"
        );

        QueryStringQuery fullTextQuery = QueryStringQuery.of(builder -> builder.fields(fullTextFields).query(searchText));
        query.must(fullTextQuery._toQuery());
    }

    protected boolean buildGroupQuery(PetriNetSearch request, LoggedUser user, Locale locale, BoolQuery.Builder query) {
        if (request.getGroup() == null || request.getGroup().isEmpty()) {
            return false;
        }

        PetriNetSearch processQuery = new PetriNetSearch();
        processQuery.setGroup(request.getGroup());
        List<PetriNetReference> groupProcesses = this.petriNetService.search(processQuery, user, new FullPageRequest(), locale).getContent();
        if (groupProcesses.isEmpty())
            return true;

        BoolQuery.Builder groupQuery = new BoolQuery.Builder();
        groupProcesses.stream().map(PetriNetReference::getIdentifier)
                .map(netIdentifier -> termQuery("identifier", netIdentifier))
                .forEach(termQuery -> groupQuery.should(termQuery._toQuery()));
        query.filter(groupQuery.build()._toQuery());
        return false;
    }
}
