package com.netgrif.workflow.oauth.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRemoteUserResourceService<T> {

    Page<T> listUsers(Pageable pageable);

    Page<T> searchUsers(String searchString, Pageable pageable, boolean small);

    long countUsers();

    long countUsers(String searchString);

    T findUserByUsername(String username);

    T findUser(String id);

}
