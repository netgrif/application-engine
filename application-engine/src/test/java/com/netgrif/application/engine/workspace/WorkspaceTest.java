package com.netgrif.application.engine.workspace;

import com.netgrif.application.engine.adapter.spring.workspace.Workspace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class WorkspaceTest {

    @Test
    public void testWorkspaceCreation() {
        assertThrows(IllegalArgumentException.class, () -> new Workspace(null));
        assertThrows(IllegalArgumentException.class, () -> new Workspace(""));
        assertThrows(IllegalArgumentException.class, () -> new Workspace("a"));
        assertThrows(IllegalArgumentException.class, () -> new Workspace("a1"));
        assertThrows(IllegalArgumentException.class, () -> new Workspace(Workspace.FORBIDDEN_ID));

        new Workspace("work-space_Id1");

        assertThrows(IllegalArgumentException.class, () -> new Workspace("work-*space_Id1"));
    }
}
