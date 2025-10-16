package com.netgrif.application.engine.petrinet.params;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(builderMethodName = "with")
public class DeletePetriNetParams {
    // todo javadoc
    private String petriNetId;
    private LoggedUser loggedUser;
//     * @param force whether to force the deletion without running events
    private boolean force;
}
