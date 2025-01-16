package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.TemplateCase;
import com.netgrif.application.engine.workflow.domain.repositories.TemplateCaseRepository;
import com.netgrif.application.engine.workflow.service.interfaces.ITemplateCaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateCaseService implements ITemplateCaseService {

    private final TemplateCaseRepository repository;

    /**
     * todo javadoc
     * */
    @Override
    public Case findLatestTemplateCase(String processIdentifier) {
        Optional<TemplateCase> templateCaseOpt = repository.findByProcessIdentifier(processIdentifier, PageRequest.of(0, 1,
                        Sort.Direction.DESC, "version.major", "version.minor", "version.patch")).stream()
                .findFirst();
        return templateCaseOpt.orElseThrow(() -> new IllegalArgumentException(
                String.format("Template case with process identifier %s was not found", processIdentifier)));
    }

    /**
     * todo javadoc
     * */
    @Override
    public Case findOne(String templateCaseId) {
        Optional<TemplateCase> caseOptional = repository.findById(templateCaseId);
        if (caseOptional.isEmpty()) {
            throw new IllegalArgumentException("Could not find Case with id [" + templateCaseId + "]");
        }
        // TODO: release/8.0.0 get or throw?
        return caseOptional.get();
    }

    /**
     * todo javadoc
     * */
    @Override
    public void save(TemplateCase templateCase) {
        repository.save(templateCase);
    }

    /**
     * todo javadoc
     * */
    @Override
    public void saveAll(Iterable<TemplateCase> templateCases) {
        repository.saveAll(templateCases);
    }
}
