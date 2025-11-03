package com.netgrif.application.engine.auth.web.responsebodies;

import com.netgrif.application.engine.auth.service.ProcessRoleFactory;
import com.netgrif.application.engine.objects.dto.response.petrinet.ProcessRoleDto;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ProcessRoleFactoryImpl implements ProcessRoleFactory {

    @Autowired
    private IPetriNetService petriNetService;

    @Override
    public ProcessRoleDto getProcessRole(com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole role, Locale locale) {
        /*if (!role.getStringId().equals(userProcessRole.getRoleId())) {
            throw new IllegalArgumentException(String.format("ProcessRole StringId (%s) and UserProcessRole roleId (%s) must match!", role.getStringId(), userProcessRole.getRoleId()));
        }*/
        if (!role.isGlobal()) {
            PetriNet net = petriNetService.get(new ObjectId(role.getProcessId()));
            return new ProcessRoleDto(role, locale, net.getImportId(), net.getVersion().toString(), net.getStringId());
        }
        return new ProcessRoleDto(role, locale);
    }

}
