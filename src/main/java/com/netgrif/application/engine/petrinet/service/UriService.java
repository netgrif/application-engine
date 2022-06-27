package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.UriContentType;
import com.netgrif.application.engine.petrinet.domain.repository.UriNodeRepository;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing UriNode objects
 * */
@Service
public class UriService implements IUriService {

    private static final String uriSeparator = "/";

    /*TODO: insted of multiple roots, there will be one root with level marking*/
    private static final String defaultRootUri = "root";

    private static final String defaultRootName = "root";

    private static final int firstLevel = 0;
    private final UriNodeRepository uriNodeRepository;

    public UriService(UriNodeRepository uriNodeRepository) {
        this.uriNodeRepository = uriNodeRepository;
    }


    /**
     * Saves UriNode object to database
     * @param uriNode to be saved
     * */
    @Override
    public UriNode save(UriNode uriNode) {
        return uriNodeRepository.save(uriNode);
    }

    /**
     * Retrieves all UriNode based on parent ID
     * @param parentId ID of parent UriNode
     * @return list of UriNode
     * */
    @Override
    public List<UriNode> findAllByParent(String parentId) {
        return uriNodeRepository.findAllByParentId(parentId);
    }

    /**
     * Retrieves all UriNode that are root nodes
     * @return list of UriNode
     * */
    @Override
    public List<UriNode> getRoots() {
        return uriNodeRepository.findAllByLevel(firstLevel);
    }

    /**
     * Retrieves all UriNode based on level
     * @param level of UriNodes
     * @return list of UriNodes
     * */
    @Override
    public List<UriNode> findByLevel(int level) {
        return uriNodeRepository.findAllByLevel(level);
    }

    /**
     * Retrieves UriNode based on ID
     * @param id ID of UriNode
     * @return UriNode
     * */
    @Override
    public UriNode findById(String id) {
        Optional<UriNode> navNodeOptional = uriNodeRepository.findById(id);
        if (navNodeOptional.isEmpty())
            throw new IllegalArgumentException("Could not find NavNode with id [" + id + "]");
        return navNodeOptional.get();
    }

    /**
     * Retrieves UriNode based on uri
     * @param uri ID of UriNode
     * @return UriNode
     * */
    @Override
    public UriNode findByUri(String uri) {
        return uriNodeRepository.findByUriPath(uri);
    }

    /**
     * Collects direct relatives (parent and children) of input UriNode and returns filled object
     * @param uriNode to be filled with relatives
     * @return filled UriNode
     * */
    @Override
    public UriNode populateDirectRelatives(UriNode uriNode) {
        if (uriNode.getLevel() != firstLevel) {
            UriNode parent = findById(uriNode.getParentId());
            uriNode.setParent(parent);
        }
        Set<UriNode> children = uriNode.getChildrenId().stream().map(this::findById).collect(Collectors.toSet());
        uriNode.setChildren(children);
        return uriNode;
    }

    /**
     * Moves UriNode to other destination
     * @param uri to be moved
     * @param destUri the destination URI
     * @return result UriNode object
     * */
    @Override
    public UriNode move(String uri, String destUri) {
        UriNode uriNode = findByUri(uri);
        return move(uriNode, destUri);
    }

    /**
     * Moves UriNode to other destination
     * @param node to be moved
     * @param destUri the destination URI
     * @return result UriNode object
     * */
    @Override
    public UriNode move(UriNode node, String destUri) {
        UriNode newParent = getOrCreate(destUri, null);
        UriNode oldParent = findById(node.getParentId());

        oldParent.getChildrenId().remove(node.getId());
        newParent.getChildrenId().add(node.getId());

        node.setParentId(newParent.getId());
        node.setUriPath(destUri + uriSeparator + node.getName());
        return uriNodeRepository.save(node);
    }

    /**
     * Creates new UriNode from PetriNet identifier, or retrieves existing one
     * @param petriNet to be used for creating UriNode
     * @param contentType to decide the content type of UriNode
     * @return the UriNode that was created or modified
     * */
    @Override
    public UriNode getOrCreate(PetriNet petriNet, UriContentType contentType) {
        String identifier = petriNet.getIdentifier();
        String modifiedUri;
        if (identifier.contains(uriSeparator))
            modifiedUri = identifier.substring(0, identifier.lastIndexOf(uriSeparator));
        else
            modifiedUri = defaultRootUri;

        return getOrCreate(modifiedUri, contentType);
    }

    /**
     * Creates new UriNode from URI path, or retrieves existing one
     * @param uri to be used for creating UriNode
     * @param contentType to decide the content type of UriNode
     * @return the UriNode that was created or modified netgrif/process/test/all_data, netgrif/process
     * */
    @Override
    public UriNode getOrCreate(String uri, UriContentType contentType) {
        LinkedList<UriNode> uriNodeList = new LinkedList<>();
        String[] uriComponents = uri.split(uriSeparator);
        StringBuilder uriBuilder = new StringBuilder();
        int pathLength = uriComponents.length;
        UriNode parent = pathLength > 1 ? uriNodeRepository.findByUriPath(defaultRootUri) : null;

        for (int i = 0; i < pathLength; i++) {
            uriBuilder.append(uriComponents[i]);
            UriNode uriNode = uriNodeRepository.findByUriPath(uriBuilder.toString());
            if (uriNode == null) {
                uriNode = new UriNode();
                uriNode.setName(uriComponents[i]);
                uriNode.setLevel(i + 1);
                uriNode.setUriPath(uriBuilder.toString());
                uriNode.setParentId(parent != null ? parent.getId() : null);
            }
            if (i == pathLength - 1 && contentType != null) {
                uriNode.addContentType(contentType);
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

    /**
     * Creates default UriNode
     * @return the UriNode that was created or modified
     * */
    @Override
    public UriNode createDefault() {
        UriNode uriNode = uriNodeRepository.findByUriPath(defaultRootUri);
        if (uriNode == null) {
            uriNode = new UriNode();
            uriNode.setName(defaultRootName);
            uriNode.setLevel(firstLevel);
            uriNode.setUriPath(defaultRootUri);
            uriNode.setParentId(null);
        }
        uriNode.addContentType(UriContentType.DEFAULT);
        uriNode = uriNodeRepository.save(uriNode);
        return uriNode;
    }

}
