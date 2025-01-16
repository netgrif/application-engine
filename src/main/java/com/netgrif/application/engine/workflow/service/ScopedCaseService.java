package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.ScopedCase;
import com.netgrif.application.engine.workflow.domain.repositories.ScopedCaseRepository;
import com.netgrif.application.engine.workflow.service.interfaces.IScopedCaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScopedCaseService implements IScopedCaseService {

    private final ScopedCaseRepository repository;

    /**
     * todo javadoc
     * */
    @Override
    public void save(ScopedCase scopedCase) {
        repository.save(scopedCase);
    }

    /**
     * todo javadoc
     * */
    @Override
    public void saveAll(Iterable<ScopedCase> scopedCases) {
       repository.saveAll(scopedCases);
    }
}
