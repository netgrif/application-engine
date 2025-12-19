package com.netgrif.application.engine.petrinet.params;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.petrinet.service.PetriNetService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * A parameter class for the {@link PetriNetService#deletePetriNet(DeletePetriNetParams)} method.
 */
@Data
@AllArgsConstructor
@Builder(builderMethodName = "with")
public class DeletePetriNetParams {

    /// String id of the process to be deleted
    private String petriNetId;

    /// User, who performs the process removal
    private LoggedUser loggedUser;
}
