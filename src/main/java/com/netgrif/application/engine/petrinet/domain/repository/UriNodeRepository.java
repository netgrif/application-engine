package com.netgrif.application.engine.petrinet.domain.repository;

import com.netgrif.application.engine.petrinet.domain.UriNode;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UriNodeRepository extends ElasticsearchRepository<UriNode, String> {

    List<UriNode> findByName(String name);

    UriNode findByUriPath(String uriPath);

    List<UriNode> findAllByParentId(String parentId);

    List<UriNode> findAllByLevel(int level);
}
