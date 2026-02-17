package com.netgrif.application.engine.workflow.params;

import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.WorkflowService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * A parameter class for the {@link WorkflowService#deleteCase(DeleteCaseParams)} method.
 */
@Data
@AllArgsConstructor
@Builder(builderMethodName = "with")
public class DeleteCaseParams {

    /// String id of the useCase to be deleted
    private String useCaseId;

    /// useCase to be deleted
    private Case useCase;

    /// If set to true, no event will be triggered.
    private boolean force;

    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    public DeleteCaseParams(Case useCase) {
        this.useCase = useCase;
        if (useCase != null) {
            this.useCaseId = useCase.getStringId();
        }
        this.params = new HashMap<>();
    }

    public DeleteCaseParams(String useCaseId) {
        this.useCaseId = useCaseId;
        this.params = new HashMap<>();
    }

    public static class DeleteCaseParamsBuilder {
        /// Sets the {@link #useCase} and {@link #useCaseId}
        public DeleteCaseParams.DeleteCaseParamsBuilder useCase(Case useCase) {
            this.useCase = useCase;
            if (useCase != null) {
                this.useCaseId = useCase.getStringId();
            }
            return this;
        }
    }
}
