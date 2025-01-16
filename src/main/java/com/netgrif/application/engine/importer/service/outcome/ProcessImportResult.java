package com.netgrif.application.engine.importer.service.outcome;

import com.netgrif.application.engine.workflow.domain.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class ProcessImportResult {
    public static ProcessImportResult EMPTY = new ProcessImportResult();

    private TemplateCase templateCase;
    private ScopedCase processScopedCase;

    private List<String> errors;
    private List<String> warnings;
    private List<String> infos;

    public ProcessImportResult() {
        templateCase = new TemplateCase(Scope.USECASE);
        processScopedCase = new ScopedCase(Scope.PROCESS);
        errors = new ArrayList<>();
        warnings = new ArrayList<>();
        infos = new ArrayList<>();
    }

    /**
     * todo javadoc
     * */
    public void deleteCases() {
        templateCase = null;
        processScopedCase = null;
    }

    /**
     * todo javadoc
     * */
    public void addToErrors(Collection<String> errors) {
        this.errors.addAll(errors);
    }

    /**
     * todo javadoc
     * */
    public void addToErrors(String error) {
        this.errors.add(error);
    }

    /**
     * todo javadoc
     * */
    public void addToWarnings(Collection<String> warnings) {
        this.warnings.addAll(warnings);
    }

    /**
     * todo javadoc
     * */
    public void addToInfos(Collection<String> infos) {
        this.infos.addAll(infos);
    }

    /**
     * todo javadoc
     * */
    public boolean hasFailed() {
        return templateCase == null && processScopedCase == null && !errors.isEmpty();
    }

    /**
     * todo javadoc
     * */
    public String formatErrors() {
        return formatMessages(errors);
    }

    /**
     * todo javadoc
     * */
    public String formatWarnings() {
        return formatMessages(errors);
    }

    /**
     * todo javadoc
     * */
    public String formatInfos() {
        return formatMessages(infos);
    }

    private static String formatMessages(List<String> messages) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messages.size(); i++) {
            sb.append(i + 1);
            sb.append(". ");
            sb.append(messages.get(i));
            sb.append("\n");
        }
        return sb.toString();
    }
}
