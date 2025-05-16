package com.netgrif.application.engine.export.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.export.service.interfaces.IXlsExportService;
import com.netgrif.application.engine.startup.FilterRunner;
import com.netgrif.application.engine.startup.SuperCreator;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.export.web.requestbodies.FilteredCasesRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class XlsExportServiceTest {

    @Autowired
    private IXlsExportService xlsExportService;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private SuperCreator superCreator;

    @Test
    void shouldCreateXlsxFile() throws Exception {
        LoggedUser superUser = superCreator.getSuperUser().transformToLoggedUser();

        IntStream.range(0,5).forEach(idx -> workflowService.createCaseByIdentifier(FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER, "Test case", "", superUser));

        FilteredCasesRequest request = getTestRequest();
        File excel = xlsExportService.getExportFilteredCasesFile(request, superUser, Locale.ENGLISH);
        assertNotNull(excel);
        assertTrue(excel.getName().endsWith(".xlsx"));
        assertTrue(excel.length() > 0);

        // Clean up
        boolean deleted = excel.delete();
        assertTrue(deleted);
    }

    FilteredCasesRequest getTestRequest() {
        FilteredCasesRequest request = new FilteredCasesRequest();
        request.setQuery(List.of(
                CaseSearchRequest.builder()
                        .query("processIdentifier:" + FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER)
                        .build()));
        request.setSelectedDataFieldNames(List.of("Menu Item Identifier", "Item URI", "Menu icon identifier", "Name of the item", "Tab icon identifier", "Name of the item"));
        request.setSelectedDataFieldIds(List.of("menu_item_identifier", "nodePath", "menu_icon", "menu_name", "tab_icon", "tab_name"));
        request.setIsIntersection(true);
        return request;
    }


}
