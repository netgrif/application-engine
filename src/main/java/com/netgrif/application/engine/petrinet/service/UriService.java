package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.configuration.properties.UriProperties;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.UriContentType;
import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.repository.UriNodeRepository;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service for managing UriNode objects
 */
@Service
public class UriService implements IUriService {


    private static final String DEFAULT_ROOT_URI = "root";

    private static final String DEFAULT_ROOT_NAME = "root";

    private static final int FIRST_LEVEL = 0;
    private final UriNodeRepository uriNodeRepository;
    private final UriProperties uriProperties;

    public UriService(UriNodeRepository uriNodeRepository, UriProperties uriProperties) {
        this.uriNodeRepository = uriNodeRepository;
        this.uriProperties = uriProperties;
    }


    /**
     * Saves UriNode object to database
     *
     * @param uriNode to be saved
     */
    @Override
    public UriNode save(UriNode uriNode) {
        return uriNodeRepository.save(uriNode);
    }

    /**
     * Retrieves all UriNode based on parent ID
     *
     * @param parentId ID of parent UriNode
     * @return list of UriNode
     */
    @Override
    public List<UriNode> findAllByParent(String parentId) {
        return uriNodeRepository.findAllByParentId(parentId);
    }

    /**
     * Retrieves all UriNode that are root nodes
     *
     * @return list of UriNode
     */
    @Override
    public UriNode getRoot() {
        List<UriNode> nodes = uriNodeRepository.findAllByLevel(FIRST_LEVEL);
        if (nodes.size() != 1)
            throw new IllegalStateException("Excatly one root uri node must exist!");
        return nodes.get(0);
    }

    /**
     * Retrieves all UriNode based on level
     *
     * @param level of UriNodes
     * @return list of UriNodes
     */
    @Override
    public List<UriNode> findByLevel(int level) {
        return uriNodeRepository.findAllByLevel(level);
    }

    /**
     * Retrieves UriNode based on ID
     *
     * @param id ID of UriNode
     * @return UriNode
     */
    @Override
    public UriNode findById(String id) {
        Optional<UriNode> navNodeOptional = uriNodeRepository.findById(id);
        if (navNodeOptional.isEmpty())
            throw new IllegalArgumentException("Could not find NavNode with id [" + id + "]");
        return navNodeOptional.get();
    }

    /**
     * Retrieves UriNode based on uri
     *
     * @param uri ID of UriNode
     * @return UriNode
     */
    @Override
    public UriNode findByUri(String uri) {
        return uriNodeRepository.findByUriPath(uri);
    }

    /**
     * Collects direct relatives (parent and children) of input UriNode and returns filled object
     *
     * @param uriNode to be filled with relatives
     * @return filled UriNode
     */
    @Override
    public UriNode populateDirectRelatives(UriNode uriNode) {
        if (uriNode == null) return null;
        if (uriNode.getLevel() != FIRST_LEVEL) {
            UriNode parent = findById(uriNode.getParentId());
            uriNode.setParent(parent);
        }
        Set<UriNode> children = StreamSupport.stream(uriNodeRepository.findAllById(uriNode.getChildrenId()).spliterator(), false).collect(Collectors.toSet());
        uriNode.setChildren(children);
        return uriNode;
    }

    /**
     * Moves UriNode to other destination
     *
     * @param uri     to be moved
     * @param destUri the destination URI
     * @return result UriNode object
     */
    @Override
    public UriNode move(String uri, String destUri) {
        UriNode uriNode = findByUri(uri);
        return move(uriNode, destUri);
    }

    /**
     * Moves UriNode to other destination
     *
     * @param node    to be moved
     * @param destUri the destination URI
     * @return result UriNode object
     */
    @Override
    public UriNode move(UriNode node, String destUri) {
        UriNode newParent = getOrCreate(destUri, null);
        UriNode oldParent = findById(node.getParentId());

        oldParent.getChildrenId().remove(node.getId());
        newParent.getChildrenId().add(node.getId());
        uriNodeRepository.saveAll(List.of(oldParent, newParent));

        node.setParentId(newParent.getId());
        node.setUriPath(destUri + uriProperties.getSeparator() + node.getName());
        return uriNodeRepository.save(node);
    }

    /**
     * Creates new UriNode from PetriNet identifier, or retrieves existing one
     *
     * @param petriNet    to be used for creating UriNode
     * @param contentType to decide the content type of UriNode
     * @return the UriNode that was created or modified
     */
    @Override
    public UriNode getOrCreate(PetriNet petriNet, UriContentType contentType) {
        String identifier = petriNet.getIdentifier();
        String modifiedUri;
        if (identifier.contains(uriProperties.getSeparator()))
            modifiedUri = identifier.substring(0, identifier.lastIndexOf(uriProperties.getSeparator()));
        else
            modifiedUri = DEFAULT_ROOT_URI;

        return getOrCreate(modifiedUri, contentType);
    }

    /**
     * Creates new UriNode from URI path, or retrieves existing one
     *
     * @param uri         to be used for creating UriNode
     * @param contentType to decide the content type of UriNode
     * @return the UriNode that was created or modified netgrif/process/test/all_data, netgrif/process
     */
    @Override
    public UriNode getOrCreate(String uri, UriContentType contentType) {
        LinkedList<UriNode> uriNodeList = new LinkedList<>();
        String[] uriComponents = uri.split(uriProperties.getSeparator());
        StringBuilder uriBuilder = new StringBuilder();
        int pathLength = uriComponents.length;
        UriNode parent = pathLength > 1 || !uri.equals(DEFAULT_ROOT_URI) ? uriNodeRepository.findByUriPath(DEFAULT_ROOT_URI) : null;

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
            uriBuilder.append(uriProperties.getSeparator());
            uriNodeList.add(uriNode);
            parent = uriNode;
        }
        return uriNodeList.getLast();
    }

    /**
     * Creates default UriNode
     *
     * @return the UriNode that was created or modified
     */
    @Override
    public UriNode createDefault() {
        UriNode uriNode = uriNodeRepository.findByUriPath(DEFAULT_ROOT_URI);
        if (uriNode == null) {
            uriNode = new UriNode();
            uriNode.setName(DEFAULT_ROOT_NAME);
            uriNode.setLevel(FIRST_LEVEL);
            uriNode.setUriPath(DEFAULT_ROOT_URI);
            uriNode.setParentId(null);
            uriNode.addContentType(UriContentType.DEFAULT);
            uriNode = uriNodeRepository.save(uriNode);
        }
        return uriNode;
    }

}
