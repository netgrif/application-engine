package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.history.domain.processevents.ImportProcessEventLog;
import com.netgrif.application.engine.history.service.IHistoryService;
import com.netgrif.application.engine.importer.model.ProcessEventType;
import com.netgrif.application.engine.importer.service.interfaces.IProcessImportService;
import com.netgrif.application.engine.importer.service.outcome.ProcessImportResult;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.rules.domain.facts.ProcessImportedFact;
import com.netgrif.application.engine.rules.service.interfaces.IRuleEngine;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.TemplateCase;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.throwable.MissingProcessMetaDataException;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import com.netgrif.application.engine.workflow.domain.VersionType;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportProcessEventOutcome;
import com.netgrif.application.engine.workflow.service.interfaces.IEventService;
import com.netgrif.application.engine.workflow.service.interfaces.IScopedCaseService;
import com.netgrif.application.engine.workflow.service.interfaces.ITemplateCaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessImportService implements IProcessImportService {

    private final IUriService uriService;
    private final Provider<Importer> importerProvider;
    private final ITemplateCaseService templateCaseService;
    private final IScopedCaseService scopedCaseService;
    private final IEventService eventService;
    private final IRuleEngine ruleEngine;
    private final IHistoryService historyService;

    /**
     * todo javadoc
     * */
    @Override
    public ImportProcessEventOutcome importProcess(InputStream xmlFile, VersionType releaseType, LoggedUser author) throws IOException, MissingProcessMetaDataException, MissingIconKeyException {
        return importProcess(xmlFile, releaseType, author, uriService.getRoot().getStringId());
    }

    /**
     * todo javadoc
     * */
    @Override
    public ImportProcessEventOutcome importProcess(InputStream xmlFile, VersionType releaseType, LoggedUser author, Map<String, String> params) throws IOException, MissingProcessMetaDataException, MissingIconKeyException {
        return importProcess(xmlFile, releaseType, author, uriService.getRoot().getStringId(), params);
    }

    /**
     * todo javadoc
     * */
    @Override
    public ImportProcessEventOutcome importProcess(InputStream xmlFile, VersionType releaseType, LoggedUser author, String uriNodeId) throws IOException, MissingProcessMetaDataException, MissingIconKeyException {
        return importProcess(xmlFile, releaseType, author, uriNodeId, new HashMap<>());
    }

    /**
     * todo javadoc
     * */
    @Override
    public ImportProcessEventOutcome importProcess(InputStream xmlFile, VersionType releaseType, LoggedUser author, String uriNodeId, Map<String, String> params) throws IOException, MissingProcessMetaDataException, MissingIconKeyException {
        ByteArrayOutputStream xmlCopy = new ByteArrayOutputStream();
        IOUtils.copy(xmlFile, xmlCopy);

        ProcessImportResult importResult = getImporter().importProcess(new ByteArrayInputStream(xmlCopy.toByteArray()));
        if (importResult.hasFailed()) {
            // todo 2026 discuss error handling
            return new ImportProcessEventOutcome();
        }

        initializeUriNodes(importResult, uriNodeId);

        try {
            Case existingTemplateCase = templateCaseService.findLatestTemplateCase(importResult.getTemplateCase().getProcessIdentifier());
            importResult.getTemplateCase().setVersion(existingTemplateCase.getVersion());
            importResult.getTemplateCase().incrementVersion(releaseType);
        } catch (IllegalArgumentException ignore) {}

        // todo 2026 roles
//        processRoleService.saveAll(net.getRoles().values());
        initializeAuthor(importResult, author);
        // todo 2026 namespace functions shouldnt be subject here
//        functionCacheService.cachePetriNetFunctions(net);

        ImportProcessEventOutcome outcome = new ImportProcessEventOutcome();

        List<EventOutcome> preUploadOutcomes = eventService.runActions(importResult.getTemplateCase().getPreUploadActions(),
                null, Optional.empty(), params);
        outcome.setOutcomes(preUploadOutcomes);
        evaluateRules(importResult.getTemplateCase(), EventPhase.PRE);
        historyService.save(new ImportProcessEventLog(null, EventPhase.PRE, importResult.getTemplateCase().getId()));

        saveCases(importResult);

        List<EventOutcome> postUploadOutcomes = eventService.runActions(importResult.getTemplateCase().getPostUploadActions(),
                null, Optional.empty(), params);
        outcome.setOutcomes(postUploadOutcomes);
        evaluateRules(importResult.getTemplateCase(), EventPhase.POST);
        historyService.save(new ImportProcessEventLog(null, EventPhase.POST, importResult.getTemplateCase().getId()));

        addMessageToOutcome(importResult.getTemplateCase(), ProcessEventType.UPLOAD, outcome);
        outcome.setTemplateCase(importResult.getTemplateCase());
        outcome.setProcessScopedCase(importResult.getProcessScopedCase());

        return outcome;
    }

    private void initializeUriNodes(ProcessImportResult result, String uriNodeId) {
        // todo 2026 discuss uri nodes
        result.getTemplateCase().setUriNodeId(uriNodeId);
        result.getProcessScopedCase().setUriNodeId(uriNodeId);
    }

    private void initializeAuthor(ProcessImportResult importResult, LoggedUser author) {
        importResult.getTemplateCase().setAuthor(author.transformToAuthor());
        importResult.getProcessScopedCase().setAuthor(author.transformToAuthor());
    }

    private Importer getImporter() {
        return importerProvider.get();
    }

    private void saveCases(ProcessImportResult result) {
        if (result.getTemplateCase() != null) {
            templateCaseService.save(result.getTemplateCase());
        }
        if (result.getProcessScopedCase() != null) {
            scopedCaseService.save(result.getProcessScopedCase());
        }
    }

    private void evaluateRules(TemplateCase templateCase, EventPhase phase) {
        int rulesExecuted = ruleEngine.evaluateRules(templateCase, new ProcessImportedFact(templateCase.getStringId(), phase));
        if (rulesExecuted > 0) {
            templateCaseService.save(templateCase);
        }
    }

    private void addMessageToOutcome(Case templateCase, ProcessEventType type, ImportProcessEventOutcome outcome) {
        if (templateCase.getProcessEvents().containsKey(type)) {
            outcome.setMessage(templateCase.getProcessEvents().get(type).getMessage());
        }
    }
}
