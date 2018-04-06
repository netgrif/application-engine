package com.netgrif.workflow.auth.service.interfaces;

import com.netgrif.workflow.auth.domain.Authority;

import java.util.List;

public interface IAuthorityService {

    List<Authority> findAll();

    Authority getOrCreate(String name);
}