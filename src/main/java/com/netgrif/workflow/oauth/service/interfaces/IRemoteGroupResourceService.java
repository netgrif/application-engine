package com.netgrif.workflow.oauth.service.interfaces;

import com.netgrif.workflow.oauth.domain.RemoteGroupResource;
import com.netgrif.workflow.oauth.domain.RemoteUserResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IRemoteGroupResourceService<T extends RemoteGroupResource, U extends RemoteUserResource> {

    Page<T> listGroups(Pageable pageable);

    Page<T> searchGroups(String searchString, Pageable pageable, boolean small);

    long countGroups();

    long countGroups(String searchString);

    T find(String id);

    List<U> members(String id);

    List<T> groupsOfUser(String id);
}
