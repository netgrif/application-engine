package com.netgrif.application.engine.petrinet.service.interfaces;

import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.UriType;

public interface IUriService {
    UriNode save(UriNode uriNode);

    UriNode findById(String id);

    UriNode findByName(String name);

    UriNode findByUri(String name);

    UriNode populateDirectRelatives(UriNode uriNode);

    UriNode move(String uri, String destUri);

    UriNode move(UriNode node, String destUri);

    UriNode getOrCreate(String uri, UriType type);
}
