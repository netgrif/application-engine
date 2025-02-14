package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.elastic.domain.ElasticPetriNet;
import com.netgrif.application.engine.elastic.domain.ElasticPetriNetRepository;

import com.netgrif.application.engine.elastic.service.executors.Executor;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetService;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.PetriNetSearch;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.application.engine.utils.FullPageRequest;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
@Slf4j
public class ElasticPetriNetService implements IElasticPetriNetService {

    private final ElasticPetriNetRepository repository;

    private final Executor executors;

    private IPetriNetService petriNetService;

    private final ElasticsearchRestTemplate template;

    @Value("${spring.data.elasticsearch.index.petrinet}")
    protected String netIndex;

    public ElasticPetriNetService(ElasticPetriNetRepository repository, Executor executors, ElasticsearchRestTemplate template) {
        this.repository = repository;
        this.executors = executors;
        this.template = template;
    }

    @Lazy
    @Autowired
    public void setPetriNetService(IPetriNetService petriNetService) {
        this.petriNetService = petriNetService;
    }

    @Override
    public void index(ElasticPetriNet net) {
        executors.execute(net.getStringId(), () -> {
            try {
                ElasticPetriNet elasticPetriNet = repository.findByStringId(net.getStringId());
                if (elasticPetriNet == null) {
                    repository.save(net);
                } else {
                    elasticPetriNet.update(net);
                    repository.save(elasticPetriNet);
                }
                log.debug("[" + net.getStringId() + "]: PetriNet \"" + net.getTitle() + "\" indexed");
            } catch (InvalidDataAccessApiUsageException ignored) {
                log.debug("[" + net.getStringId() + "]: PetriNet \"" + net.getTitle() + "\" has duplicates, will be reindexed");
                repository.deleteAllByStringId(net.getStringId());
                repository.save(net);
                log.debug("[" + net.getStringId() + "]: PetriNet \"" + net.getTitle() + "\" indexed");
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
            repository.deleteAllByStringId(id);
            log.info("[" + id + "]: PetriNet \"" + id + "\" deleted");
        });
    }

    @Override
    public String findUriNodeId(PetriNet net) {
        if (net == null) {
            return null;
        }
        ElasticPetriNet elasticPetriNet = repository.findByStringId(net.getStringId());
        if (elasticPetriNet == null) {
            log.warn("[" + net.getStringId() + "] PetriNet with id [" + net.getStringId() + "] is not indexed.");
            return null;
        }

        return elasticPetriNet.getUriNodeId();
    }

    @Override
    public List<PetriNet> findAllByUriNodeId(String uriNodeId) {
        List<ElasticPetriNet> elasticPetriNets = repository.findAllByUriNodeId(uriNodeId);
        return petriNetService.findAllById(elasticPetriNets.stream().map(ElasticPetriNet::getStringId).collect(Collectors.toList()));
    }

    @Override
    public Page<PetriNetReference> search(PetriNetSearch requests, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection) {
        if (requests == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }
        log.debug("Searching for PetriNet query with logged user [{}]", user.getId());
        LoggedUser loggedOrImpersonated = user.getSelfOrImpersonated();
        NativeSearchQuery query = buildQuery(requests, loggedOrImpersonated, pageable, locale, isIntersection);
        List<PetriNet> netPage;
        long total;
        if (query != null) {
            SearchHits<ElasticPetriNet> hits = template.search(query, ElasticPetriNet.class, IndexCoordinates.of(netIndex));
            Page<ElasticPetriNet> indexedNets = (Page) SearchHitSupport.unwrapSearchHits(SearchHitSupport.searchPageFor(hits, query.getPageable()));
            netPage = petriNetService.findAllById(indexedNets.get().map(ElasticPetriNet::getStringId).collect(Collectors.toList()));
            total = indexedNets.getTotalElements();
            log.debug("Found [{}] total elements of page [{}]", netPage.size(), pageable.getPageNumber());
        } else {
            netPage = Collections.emptyList();
            total = 0;
        }

        return new PageImpl<>(netPage.stream().map(net -> new PetriNetReference(net, locale)).collect(Collectors.toList()), pageable, total);
    }

    protected NativeSearchQuery buildQuery(PetriNetSearch request, LoggedUser user, Pageable pageable, Locale locale, Boolean isIntersection) {
        List<BoolQueryBuilder> singleQueries = new LinkedList<>();
        singleQueries.add(buildSingleQuery(request, user, locale));

        if (isIntersection && !singleQueries.stream().allMatch(Objects::nonNull)) {
            // one of the queries evaluates to empty set => the entire result is an empty set
            return null;
        } else if (!isIntersection) {
            singleQueries = singleQueries.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (singleQueries.size() == 0) {
                // all queries result in an empty set => the entire result is an empty set
                return null;
            }
        }

        BinaryOperator<BoolQueryBuilder> reductionOperator = isIntersection ? BoolQueryBuilder::must : BoolQueryBuilder::should;
        BoolQueryBuilder query = singleQueries.stream().reduce(new BoolQueryBuilder(), reductionOperator);

        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        return builder
                .withQuery(query)
                .withPageable(pageable)
                .build();
    }

    protected BoolQueryBuilder buildSingleQuery(PetriNetSearch request, LoggedUser user, Locale locale) {
        BoolQueryBuilder query = boolQuery();

        buildFullTextQuery(request, query);
        // TODO: NAE-2039 check group
        boolean resultAlwaysEmpty = buildGroupQuery(request, user, locale, query);
        if (resultAlwaysEmpty) {
            return null;
        }
        return query;
    }

    protected void buildFullTextQuery(PetriNetSearch request, BoolQueryBuilder query) {
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            return;
        }

        String searchText = "*" + request.getTitle() + "*";
        Map<String, Float> fields = new HashMap<>();
        fields.put("title.defaultValue", 2f);
        fields.put("identifier", 1f);
        QueryBuilder fullTextQuery = queryStringQuery(searchText).fields(fields);
        query.must(fullTextQuery);
    }

    protected boolean buildGroupQuery(PetriNetSearch request, LoggedUser user, Locale locale, BoolQueryBuilder query) {
        if (request.getGroup() == null || request.getGroup().isEmpty()) {
            return false;
        }

        PetriNetSearch processQuery = new PetriNetSearch();
        processQuery.setGroup(request.getGroup());
        List<PetriNetReference> groupProcesses = this.petriNetService.search(processQuery, user, new FullPageRequest(), locale).getContent();
        if (groupProcesses.size() == 0)
            return true;

        BoolQueryBuilder groupQuery = boolQuery();
        groupProcesses.stream().map(PetriNetReference::getIdentifier)
                .map(netIdentifier -> termQuery("identifier", netIdentifier))
                .forEach(groupQuery::should);
        query.filter(groupQuery);
        return false;
    }
}
