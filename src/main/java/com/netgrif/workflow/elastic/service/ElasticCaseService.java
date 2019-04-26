package com.netgrif.workflow.elastic.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.elastic.domain.ElasticCase;
import com.netgrif.workflow.elastic.domain.ElasticCaseRepository;
import com.netgrif.workflow.elastic.web.ElasticSearchRequest;
import com.netgrif.workflow.utils.FullPageRequest;
import com.netgrif.workflow.workflow.domain.Case;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ElasticCaseService implements IElasticCaseService {

    private static final Logger log = LoggerFactory.getLogger(ElasticCaseService.class);

    @Autowired
    private ElasticCaseRepository repository;

    @Autowired
    private ElasticsearchTemplate template;

    @Async
    @Override
    public void index(Case useCase) {
        ElasticCase elasticCase = new ElasticCase(useCase);

        repository.save(elasticCase);

        log.info("[" + useCase.getStringId() + "]: Case \"" + useCase.getTitle() + "\" indexed");
    }

    @Override
    public void indexNow(Case useCase) {
        ElasticCase elasticCase = new ElasticCase(useCase);

        repository.save(elasticCase);

        log.info("[" + useCase.getStringId() + "]: Case \"" + useCase.getTitle() + "\" indexed");
    }

    @Override
    public Page<ElasticCase> search(ElasticSearchRequest request, LoggedUser user, Pageable pageable) {
        if (request == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }

        SearchQuery query = buildQuery(request, user, pageable);

        return template.queryForPage(query, ElasticCase.class);
    }

    @Override
    public long count(ElasticSearchRequest request, LoggedUser user) {
        if (request == null) {
            throw new IllegalArgumentException("Request can not be null!");
        }

        SearchQuery query = buildQuery(request, user, new FullPageRequest());

        return template.count(query);
    }

    private SearchQuery buildQuery(ElasticSearchRequest request, LoggedUser user, Pageable pageable) {
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

        return builder.withPageable(pageable).build();
    }
}