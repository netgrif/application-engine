package com.netgrif.application.engine.petrinet.service.interfaces;

import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.UriType;

import java.util.List;

public interface IUriService {
    UriNode save(UriNode uriNode);

    List<UriNode> findAllByParent(String parentId);

    List<UriNode> getRoots();

    UriNode findById(String id);

    UriNode findByName(String name);

    UriNode findByUri(String name);

    UriNode populateDirectRelatives(UriNode uriNode);

    UriNode move(String uri, String destUri);

    UriNode move(UriNode node, String destUri);

    UriNode getOrCreate(PetriNet petriNet, UriType type);

    UriNode getOrCreate(String uri, UriType type);
}
