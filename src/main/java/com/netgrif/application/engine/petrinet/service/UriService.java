package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.UriType;
import com.netgrif.application.engine.petrinet.domain.repository.UriNodeRepository;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UriService implements IUriService {

    private static final String uriSeparator = "/";

    private final UriNodeRepository uriNodeRepository;

    public UriService(UriNodeRepository uriNodeRepository) {
        this.uriNodeRepository = uriNodeRepository;
    }

    @Override
    public UriNode save(UriNode uriNode) {
        return uriNodeRepository.save(uriNode);
    }

    @Override
    public UriNode findById(String id) {
        Optional<UriNode> navNodeOptional = uriNodeRepository.findById(id);
        if (navNodeOptional.isEmpty())
            throw new IllegalArgumentException("Could not find NavNode with id [" + id + "]");
        return navNodeOptional.get();
    }

    @Override
    public UriNode findByName(String name) {
        return uriNodeRepository.findByName(name);
    }

    @Override
    public List<UriNode> updateOrCreate(String uri, UriType type) {
        List<UriNode> uriNodeList = new ArrayList<>();
        String[] uriComponents = uri.split(uriSeparator);
        int pathLength = uriComponents.length - 1;
        StringBuilder uriBuilder = new StringBuilder();

        for (int i = 0; i < pathLength; i++) {
            uriBuilder.append(uriComponents[i]);
            UriNode uriNode = uriNodeRepository.findByUri(uriBuilder.toString());

            if (uriNode == null) {
                uriNode = new UriNode();
                uriNode.setName(uriComponents[i]);
                uriNode.setRoot(i == 0);
                uriNode.setUri(uriBuilder.toString());
                uriNode.setParent(i > 0 ? uriComponents[i - 1] : null);
            }

            if (i < pathLength - 1)
                uriNode.getChildren().add(uriComponents[i + 1]);

            if (i == pathLength - 1) {
                uriNode.setContainsCase(type.equals(UriType.CASE));
                uriNode.setContainsProcess(type.equals(UriType.PROCESS));
            }

            uriBuilder.append(uriSeparator);
            uriNodeRepository.save(uriNode);
            uriNodeList.add(uriNode);
        }

        return uriNodeList;
    }

}
