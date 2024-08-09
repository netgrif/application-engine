package com.netgrif.application.engine.workflow.domain.params;

import com.netgrif.application.engine.workflow.domain.Case;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * todo javadoc
 * */
@Data
@AllArgsConstructor
@Builder(builderMethodName = "with")
public class DeleteCaseParams {

    private String useCaseId;
    private Case useCase;
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

    /**
     * todo javadoc
     * Builder extension of the {@link Builder} implementation for {@link }. Containing additional logic over the native builder
     * implementation
     * */
    public static class DeleteCaseParamsBuilder {
        /**
         * todo javadoc
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
