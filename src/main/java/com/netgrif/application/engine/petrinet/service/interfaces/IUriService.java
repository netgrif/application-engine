package com.netgrif.application.engine.petrinet.service.interfaces;

import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.UriContentType;

import java.util.List;

public interface IUriService {

    /**
     * Saves UriNode object to database
     * @param uriNode to be saved
     * */
    UriNode save(UriNode uriNode);

    /**
     * Retrieves all UriNode based on parent ID
     * @param parentId ID of parent UriNode
     * @return list of UriNode
     * */
    List<UriNode> findAllByParent(String parentId);

    /**
     * Retrieves UriNode that is root node
     * @return root UriNode
     * */
    UriNode getRoot();

    /**
     * Retrieves UriNode based on level
     * @param level of UriNode
     * @return UriNode
     * */
    List<UriNode> findByLevel(int level);

    /**
     * Retrieves UriNode based on ID
     * @param id ID of UriNode
     * @return UriNode
     * */
    UriNode findById(String id);

    /**
     * Retrieves UriNode based on uri
     * @param path of UriNode
     * @return UriNode
     * */
    UriNode findByUri(String path);

    /**
     * Collects direct relatives (parent and children) of input UriNode and returns filled object
     * @param uriNode to be filled with relatives
     * @return filled UriNode
     * */
    UriNode populateDirectRelatives(UriNode uriNode);

    /**
     * Moves UriNode to other destination
     * @param uri to be moved
     * @param destUri the destination URI
     * @return result UriNode object
     * */
    UriNode move(String uri, String destUri);

    /**
     * Moves UriNode to other destination
     * @param node to be moved
     * @param destUri the destination URI
     * @return result UriNode object
     * */
    UriNode move(UriNode node, String destUri);

    /**
     * Creates new UriNode from PetriNet identifier, or retrieves existing one
     * @param petriNet to be used for creating UriNode
     * @param contentType to decide the content type of UriNode
     * @return the UriNode that was created or modified
     * */
    UriNode getOrCreate(PetriNet petriNet, UriContentType contentType);

    /**
     * Creates new UriNode from URI path, or retrieves existing one
     * @param uri to be used for creating UriNode
     * @param contentType to decide the content type of UriNode
     * @return the UriNode that was created or modified
     * */
    UriNode getOrCreate(String uri, UriContentType contentType);

    /**
     * Creates default UriNode
     * @return the UriNode that was created or modified
     * */
    UriNode createDefault();
}
