package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.objects.auth.domain.Authority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuthorityService {

    Page<Authority> findAll(Pageable pageable);

    Authority getOrCreate(String name);

    Authority getOne(String id);

    Page<Authority> findAllByIds(List<String> ids, Pageable pageable);
}
