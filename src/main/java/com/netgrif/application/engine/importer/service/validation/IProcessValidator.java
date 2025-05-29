package com.netgrif.application.engine.importer.service.validation;


import com.netgrif.application.engine.petrinet.domain.Process;

public interface IProcessValidator {

    void validate(Process process);
}
