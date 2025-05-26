package com.netgrif.application.engine.petrinet.domain.params;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(builderMethodName = "with")
public class DeleteProcessParams {

    private String processId;
    @Builder.Default
    private boolean isTransactional = false;

    public DeleteProcessParams(String processId) {
        this.processId = processId;
    }
}
