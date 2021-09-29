package com.netgrif.workflow.oauth.service.interfaces;

import com.netgrif.workflow.oauth.domain.RemoteUserResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface IRemoteUserResourceService<T extends RemoteUserResource> {

    Page<T> listUsers(Pageable pageable);

    Page<T> searchUsers(String searchString, Pageable pageable, boolean small);

    long countUsers();

    long countUsers(String searchString);

    T findUserByUsername(String username);

    T findUser(String id);

    List<T> findUsers(Set<String> ids);

    T findUserByEmail(String email);
}
