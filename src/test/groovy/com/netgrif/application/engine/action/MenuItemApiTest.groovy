package com.netgrif.application.engine.action

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.application.engine.menu.domain.MenuItemConstants
import com.netgrif.application.engine.menu.domain.configurations.CaseViewConstants
import com.netgrif.application.engine.menu.utils.MenuItemUtils
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.MenuRunner
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static org.junit.jupiter.api.Assertions.assertThrows

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class MenuItemApiTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper helper

    @Autowired
    private MenuRunner filterRunner

    @Autowired
    private IUserService userService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IDataService dataService

    @Autowired
    private IUriService uriService

    @Autowired
    private INextGroupService nextGroupService

    @Autowired
    private IElasticCaseService elasticCaseService

    @Autowired
    private ITaskService taskService

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
        helper.createNet("filter_api_test.xml")
    }

    List<Case> findCasesElastic(String query, Pageable pageable) {
        CaseSearchRequest request = new CaseSearchRequest()
        request.query = query
        List<Case> result = elasticCaseService.search([request], userService.system.transformToLoggedUser(), pageable, LocaleContextHolder.locale, false).content
        return result
    }

    Case createMenuItem(String uri = "/netgrif/test", String identifier = "new_menu_item") {
        Case caze = getCase()
        caze = setData(caze, [
                "uri": uri,
                "title": "FILTER",
                "allowed_nets": "filter,menu_item",
                "query": "processIdentifier:filter OR processIdentifier:menu_item",
                "type": "Case",
                "identifier": identifier,
                "icon": "device_hub",
                "create_filter_and_menu": "0"
        ])
        return caze
    }

    @Test
    void testRemoveMenuItem() {
        String starterUri = "/netgrif/test"
        // todo 23 rework creation
        Case apiCase = createMenuItem(starterUri, "new_menu_item")
        Case leafItemCase = getMenuItem(apiCase)

        sleep(2000)
        Case testFolder = findCasesElastic("processIdentifier:$MenuItemConstants.PROCESS_IDENTIFIER AND dataSet.${MenuItemConstants.FIELD_NODE_PATH}.textValue:\"/netgrif/test\"", PageRequest.of(0, 1))[0]
        String netgrifFolderId = (testFolder.dataSet[MenuItemConstants.FIELD_PARENT_ID].value as ArrayList<String>)[0]

        Case netgrifFolder = workflowService.findOne(netgrifFolderId)
        assert (netgrifFolder.dataSet[MenuItemConstants.FIELD_CHILD_ITEM_IDS].value as ArrayList).contains(testFolder.stringId)
        assert workflowService.findOne(testFolder.stringId) != null
        assert workflowService.findOne(leafItemCase.stringId) != null
        String tabbedCaseViewId = MenuItemUtils.getCaseIdFromCaseRef(leafItemCase, MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID)
        assert tabbedCaseViewId != null
        Case tabbedCaseView = workflowService.findOne(tabbedCaseViewId)
        String tabbedTaskViewId = MenuItemUtils.getCaseIdFromCaseRef(tabbedCaseView, CaseViewConstants.FIELD_VIEW_CONFIGURATION_ID)
        assert tabbedTaskViewId != null

        workflowService.deleteCase(testFolder)
        sleep(2000)
        netgrifFolder = workflowService.findOne(netgrifFolderId)
        assert !(netgrifFolder.dataSet[MenuItemConstants.FIELD_CHILD_ITEM_IDS].value as ArrayList).contains(testFolder.stringId)
        assertThrows(IllegalArgumentException.class, () -> {
            workflowService.findOne(testFolder.stringId)
        })
        assertThrows(IllegalArgumentException.class, () -> {
            workflowService.findOne(leafItemCase.stringId)
        })
        assertThrows(IllegalArgumentException.class, () -> {
            workflowService.findOne(tabbedCaseViewId)
        })
        assertThrows(IllegalArgumentException.class, () -> {
            workflowService.findOne(tabbedTaskViewId)
        })
    }

    Case getCase() {
        return workflowService.searchOne(QCase.case$.processIdentifier.eq("filter_api_test"))
    }

    Case getMenuItem(Case caze) {
        return workflowService.findOne(caze.dataSet["menu_stringId"].value as String)
    }

    def setData(Case caze, Map<String, String> dataSet) {
        dataService.setData(caze.tasks[0].task, ImportHelper.populateDataset(dataSet.collectEntries {
            [(it.key): (["value": it.value, "type": "text"])]
        }))
        return workflowService.findOne(caze.stringId)
    }

}
