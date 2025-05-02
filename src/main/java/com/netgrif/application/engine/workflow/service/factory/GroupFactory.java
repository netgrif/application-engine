package com.netgrif.application.engine.workflow.service.factory;

import com.netgrif.application.engine.authorization.domain.Group;
import com.netgrif.application.engine.workflow.domain.Case;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupFactory extends SystemCaseFactory<Group> {
    @Override
    public Group createObject(Case systemCase) {
        return null;
    }

    @Override
    protected void registerFactory() {

    }
}
