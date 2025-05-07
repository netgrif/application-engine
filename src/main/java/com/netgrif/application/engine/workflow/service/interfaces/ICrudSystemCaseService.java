package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.workflow.domain.CaseParams;
import com.netgrif.application.engine.workflow.domain.SystemCase;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ICrudSystemCaseService<T extends SystemCase> {
    void registerForbiddenKeywords(Set<String> keywords);
    void removeFromForbiddenKeywords(Set<String> keywords);
    T create(CaseParams params) throws IllegalArgumentException, IllegalStateException;
    T update(T systemObject, CaseParams params) throws IllegalArgumentException, IllegalStateException;
    Optional<T> findById(String id);
    boolean existsById(String id);
    List<T> findAll();
}
