package com.netgrif.application.engine.navtree.domain.repository;

import com.netgrif.application.engine.elastic.domain.ElasticCase;
import com.netgrif.application.engine.navtree.domain.NavNode;
import org.apache.poi.ss.formula.functions.Na;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NavNodeRepository extends ElasticsearchRepository<NavNode, String> {

    NavNode findByName(String name);

    List<NavNode> findAllByParent(String parent);
}
