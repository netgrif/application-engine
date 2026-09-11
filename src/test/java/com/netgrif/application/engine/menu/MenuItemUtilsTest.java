package com.netgrif.application.engine.menu;

import com.netgrif.application.engine.menu.domain.MenuItemConstants;
import com.netgrif.application.engine.menu.utils.MenuItemUtils;
import com.netgrif.application.engine.workflow.domain.Case;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class MenuItemUtilsTest {

    @Test
    public void testIsCyclicNodePath_withCyclicPath_returnsTrue() {
        Case folderItem = mock(Case.class);

        when(folderItem.getFieldValue(MenuItemConstants.FIELD_NODE_PATH)).thenReturn("/node1/node2");
        boolean result = MenuItemUtils.isCyclicNodePath(folderItem, "/node1/node2/node3");
        assertTrue(result);

        when(folderItem.getFieldValue(MenuItemConstants.FIELD_NODE_PATH)).thenReturn("/node1/node2");
        result = MenuItemUtils.isCyclicNodePath(folderItem, "/node4/node1/node2/node3");
        assertTrue(result);
    }

    @Test
    public void testIsCyclicNodePath_withNonCyclicPath_returnsFalse() {
        Case folderItem = mock(Case.class);

        when(folderItem.getFieldValue(MenuItemConstants.FIELD_NODE_PATH)).thenReturn("/node1/node2");
        boolean result = MenuItemUtils.isCyclicNodePath(folderItem, "/node3/node4");
        assertFalse(result);

        when(folderItem.getFieldValue(MenuItemConstants.FIELD_NODE_PATH)).thenReturn("/node1/node2");
        result = MenuItemUtils.isCyclicNodePath(folderItem, null);
        assertFalse(result);

        when(folderItem.getFieldValue(MenuItemConstants.FIELD_NODE_PATH)).thenReturn("/node1/node2");
        result = MenuItemUtils.isCyclicNodePath(folderItem, "/node1/node2_2/node3");
        assertFalse(result);


        when(folderItem.getFieldValue(MenuItemConstants.FIELD_NODE_PATH)).thenReturn(null);
        result = MenuItemUtils.isCyclicNodePath(folderItem, "/node1/node2");
        assertFalse(result);
    }

}
