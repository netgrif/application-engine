package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.authorization.domain.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.utils.UniqueKeyMap;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ImportResult {

    private Process process;
    private UniqueKeyMap<String, ProcessRole> roles = new UniqueKeyMap<>();
    // TODO: release/8.0.0 info, warn, error messages - definovat message a ich preklady

    public void addRole(ProcessRole processRole) {
        this.roles.put(processRole.getImportId(), processRole);
    }
}
