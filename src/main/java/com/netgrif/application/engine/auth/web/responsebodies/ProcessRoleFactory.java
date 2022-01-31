package com.netgrif.application.engine.auth.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ProcessRoleFactory implements IProcessRoleFactory {

    @Autowired
    private IPetriNetService petriNetService;

    @Override
    public ProcessRole getProcessRole(com.netgrif.application.engine.petrinet.domain.roles.ProcessRole role, Locale locale) {
        /*if (!role.getStringId().equals(userProcessRole.getRoleId())) {
            throw new IllegalArgumentException(String.format("ProcessRole StringId (%s) and UserProcessRole roleId (%s) must match!", role.getStringId(), userProcessRole.getRoleId()));
        }*/
        ProcessRole result = new ProcessRole(role, locale);
        PetriNet net = petriNetService.get(new ObjectId(role.getNetId()));
        result.setNetStringId(net.getStringId());
        result.setNetImportId(net.getImportId());
        result.setNetVersion(net.getVersion().toString());
        return result;
    }

}
