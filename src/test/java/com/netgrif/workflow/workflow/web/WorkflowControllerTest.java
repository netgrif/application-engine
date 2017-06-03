package com.netgrif.workflow.workflow.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
public class WorkflowControllerTest {

    @Autowired
    private WorkflowController controller;

    @Test
    public void createCase() throws Exception {
        // TODO: 4. 2. 2017
//        workflowService.createCase(net.getStringId(), "Storage Unit " + i, randomColor());
    }
}