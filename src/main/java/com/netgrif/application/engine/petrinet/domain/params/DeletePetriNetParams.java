package com.netgrif.application.engine.petrinet.domain.params;


import com.netgrif.application.engine.auth.domain.LoggedUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(builderMethodName = "with")
public class DeletePetriNetParams {

    private String petriNetId;
    private LoggedUser loggedUser;
    @Builder.Default
    private boolean isTransactional = false;

    public DeletePetriNetParams(String petriNetId, LoggedUser loggedUser) {
        this.petriNetId = petriNetId;
        this.loggedUser = loggedUser;
    }
}
