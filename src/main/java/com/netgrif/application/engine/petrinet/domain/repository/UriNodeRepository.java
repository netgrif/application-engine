package com.netgrif.application.engine.petrinet.domain.repository;

import com.netgrif.application.engine.petrinet.domain.UriNode;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UriNodeRepository extends ElasticsearchRepository<UriNode, String> {

    UriNode findByName(String name);

    UriNode findByUri(String uri);

    List<UriNode> findAllByParent(String parent);

    List<UriNode> findAllByRoot(boolean root);
}
