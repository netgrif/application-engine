package com.netgrif.workflow.workflow.web;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class WorkflowControllerTest {

    @Autowired
    private WorkflowController controller;

    @Test
    @Disabled("TODO: 4. 2. 2017")
    public void createCase() throws Exception {
        // TODO: 4. 2. 2017
//        workflowService.createCase(net.getStringId(), "Storage Unit " + i, randomColor());
    }
}