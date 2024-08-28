package com.netgrif.application.engine.workflow.domain.params;

import com.netgrif.application.engine.workflow.domain.Case;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder(builderMethodName = "with")
public class DeleteCaseParams {

    private String useCaseId;
    private Case useCase;
    private Boolean isTransactional;
    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    public DeleteCaseParams(Case useCase) {
        this.useCase = useCase;
        if (useCase != null) {
            this.useCaseId = useCase.getStringId();
        }
    }

    public DeleteCaseParams(String useCaseId) {
        this.useCaseId = useCaseId;
    }

    public static class DeleteCaseParamsBuilder {
        /**
         * Sets the {@link #useCase} and {@link #useCaseId}
         * */
        public DeleteCaseParams.DeleteCaseParamsBuilder useCase(Case useCase) {
            this.useCase = useCase;
            if (useCase != null) {
                this.useCaseId = useCase.getStringId();
            }
            return this;
        }
    }
}
