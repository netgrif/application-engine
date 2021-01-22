package com.netgrif.workflow.auth.web.responsebodies;

import com.netgrif.workflow.auth.domain.UserProcessRole;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ProcessRoleFactory implements IProcessRoleFactory {

    @Autowired
    private IPetriNetService petriNetService;

    @Override
    public ProcessRole getProcessRole(com.netgrif.workflow.petrinet.domain.roles.ProcessRole role, UserProcessRole userProcessRole, Locale locale) {
        if (!role.getStringId().equals(userProcessRole.getRoleId())) {
            throw new IllegalArgumentException(String.format("ProcessRole StringId (%s) and UserProcessRole roleId (%s) must match!", role.getStringId(), userProcessRole.getRoleId()));
        }
        ProcessRole result = new ProcessRole(role, locale);
        PetriNet net = petriNetService.get(new ObjectId(userProcessRole.getNetId()));
        result.setNetStringId(net.getStringId());
        result.setNetImportId(net.getImportId());
        result.setNetVersion(net.getVersion().toString());
        return result;
    }

}
