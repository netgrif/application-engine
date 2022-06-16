package com.netgrif.application.engine.petrinet.service;

import com.ctc.wstx.shaded.msv_core.util.Uri;
import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.UriType;
import com.netgrif.application.engine.petrinet.domain.repository.UriNodeRepository;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
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
    public UriNode move(UriNode node, String destUri) {
        String newUri = destUri + uriSeparator + node.getUri();
        List<UriNode> uriNodes = getOrCreate(destUri, null);
        UriNode parent = ((LinkedList<UriNode>) uriNodes).getLast();

        node.setParent(parent.getId());
        node.setUri(newUri);
        return uriNodeRepository.save(node);
    }

    @Override
    public List<UriNode> getOrCreate(String uri, UriType type) {
        List<UriNode> uriNodeList = new LinkedList<>();
        String[] uriComponents = uri.split(uriSeparator);
        int pathLength = uriComponents.length - 1;
        StringBuilder uriBuilder = new StringBuilder();
        UriNode parent = null;

        for (int i = 0; i < pathLength; i++) {
            uriBuilder.append(uriComponents[i]);
            UriNode uriNode = uriNodeRepository.findByUri(uriBuilder.toString());
            if (uriNode == null) {
                uriNode = new UriNode();
                uriNode.setName(uriComponents[i]);
                uriNode.setRoot(i == 0);
                uriNode.setUri(uriBuilder.toString());
                uriNode.setParent(parent != null ? parent.getId() : null);
            }
            if (i == pathLength - 1 && type != null) {
                uriNode.setContainsCase(type.equals(UriType.CASE));
                uriNode.setContainsProcess(type.equals(UriType.PROCESS));
            }
            uriNode = uriNodeRepository.save(uriNode);
            if (parent != null) {
                parent.getChildren().add(uriNode.getId());
                uriNodeRepository.save(parent);
            }
            uriBuilder.append(uriSeparator);
            uriNodeList.add(uriNode);
            parent = uriNode;
        }
        return uriNodeList;
    }

}
