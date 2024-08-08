package com.netgrif.application.engine.importer.service.validation;


import com.netgrif.application.engine.petrinet.domain.Process;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "nae.importer", value = "validate-process", havingValue = "true", matchIfMissing = true)
public class ProcessValidator implements IProcessValidator {

    @Override
    public void validate(Process process) {
        // TODO: release/8.0.0
        //        transitionValidator.checkDeprecatedAttributes(importTransition);
//        logicValidator.checkConflictingAttributes(logic, logic.isAssigned(), logic.isAssign(), "assigned", "assign");
//        logicValidator.checkDeprecatedAttributes(logic);
        //        logicValidator.checkConflictingAttributes(logic, logic.isAssigned(), logic.isAssign(), "assigned", "assign");
//        logicValidator.checkDeprecatedAttributes(logic);
        ////        documentValidator.checkConflictingAttributes(process, process.getUsersRef(), process.getUserRef(), "usersRef", "userRef");
////        documentValidator.checkDeprecatedAttributes(process);
    }
}
