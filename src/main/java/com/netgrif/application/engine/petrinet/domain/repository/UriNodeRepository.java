package com.netgrif.application.engine.petrinet.domain.repository;

import com.netgrif.application.engine.petrinet.domain.UriNode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UriNodeRepository extends MongoRepository<UriNode, String> {

    List<UriNode> findByName(String name);

    UriNode findByPath(String path);

    List<UriNode> findAllByParentId(String parentId);

    List<UriNode> findAllByLevel(int level);
}
