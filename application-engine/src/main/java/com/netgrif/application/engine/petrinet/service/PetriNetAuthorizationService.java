package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PetriNetAuthorizationService implements IPetriNetAuthorizationService {

    private final PetriNetRepository processRepository;

    @Override
    public boolean canCallProcessDelete(LoggedUser loggedUser, String processId) {
        Optional<PetriNet> processOpt = processRepository.findById(processId);
        if (processOpt.isEmpty()) {
            return false;
        }

        if (loggedUser.isAdmin()) {
            return true;
        }

        // todo 2072 repository.existsBy_idAndWorkspaceId ?

        return Objects.equals(loggedUser.getActiveWorkspaceId(), processOpt.get().getWorkspaceId());
    }
}
