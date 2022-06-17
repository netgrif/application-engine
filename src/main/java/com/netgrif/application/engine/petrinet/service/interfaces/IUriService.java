package com.netgrif.application.engine.petrinet.service.interfaces;

import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.UriType;

import java.util.List;

public interface IUriService {
    UriNode save(UriNode uriNode);

    UriNode findById(String id);

    UriNode findByName(String name);

    UriNode move(UriNode node, String destUri);

    UriNode getOrCreate(String uri, UriType type);
}
