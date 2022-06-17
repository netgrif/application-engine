package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.UriType;
import com.netgrif.application.engine.petrinet.domain.repository.UriNodeRepository;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    public List<UriNode> findAllByParent(String parentId) {
        return uriNodeRepository.findAllByParent(parentId);
    }

    @Override
    public List<UriNode> getRoots() {
        return uriNodeRepository.findAllByRoot(true);
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
    public UriNode findByUri(String uri) {
        return uriNodeRepository.findByUri(uri);
    }

    @Override
    public UriNode populateDirectRelatives(UriNode uriNode) {
        UriNode parent = findById(uriNode.getParentId());
        Set<UriNode> children = uriNode.getChildrenId().stream().map(this::findById).collect(Collectors.toSet());
        uriNode.setParent(parent);
        uriNode.setChildren(children);
        return uriNode;
    }

    @Override
    public UriNode move(String uri, String destUri) {
        UriNode uriNode = findByUri(uri);
        return move(uriNode, destUri);
    }

    @Override
    public UriNode move(UriNode node, String destUri) {
        UriNode newParent = getOrCreate(destUri, null);
        UriNode oldParent = findById(node.getParentId());

        oldParent.getChildrenId().remove(node.getId());
        newParent.getChildrenId().add(node.getId());

        node.setParentId(newParent.getId());
        node.setUri(destUri);
        return uriNodeRepository.save(node);
    }

    @Override
    public UriNode getOrCreate(String uri, UriType type) {
        LinkedList<UriNode> uriNodeList = new LinkedList<>();
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
                uriNode.setParentId(parent != null ? parent.getId() : null);
            }
            if (i == pathLength - 1 && type != null) {
                uriNode.setContainsCase(type.equals(UriType.CASE));
                uriNode.setContainsProcess(type.equals(UriType.PROCESS));
            }
            uriNode = uriNodeRepository.save(uriNode);
            if (parent != null) {
                parent.getChildrenId().add(uriNode.getId());
                uriNodeRepository.save(parent);
            }
            uriBuilder.append(uriSeparator);
            uriNodeList.add(uriNode);
            parent = uriNode;
        }
        return uriNodeList.getLast();
    }

}
