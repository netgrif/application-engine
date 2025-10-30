package com.netgrif.application.engine.action

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.GroupService
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer
import com.netgrif.application.engine.objects.petrinet.domain.I18nString
import com.netgrif.application.engine.objects.petrinet.domain.UriContentType
import com.netgrif.application.engine.objects.petrinet.domain.UriNode
import com.netgrif.application.engine.objects.utils.MenuItemUtils
import com.netgrif.application.engine.objects.workflow.domain.menu.MenuItemConstants
import com.netgrif.application.engine.objects.workflow.domain.menu.MenuItemView
import com.netgrif.application.engine.objects.workflow.domain.menu.configurations.TabbedCaseViewConstants
import com.netgrif.application.engine.objects.workflow.domain.menu.configurations.TabbedTaskViewConstants
import com.netgrif.application.engine.startup.runner.FilterRunner
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.adapter.spring.workflow.domain.QCase
import com.netgrif.application.engine.startup.runner.MenuProcessRunner
import com.netgrif.application.engine.workflow.params.DeleteCaseParams
import com.netgrif.application.engine.workflow.params.TaskParams
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
    private FilterRunner filterRunner

    @Autowired
    private UserService userService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IDataService dataService

    @Autowired
    private GroupService groupService

    @Autowired
    private IElasticCaseService elasticCaseService

    @Autowired
    private ITaskService taskService

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
        helper.createNet("filter_api_test.xml")
    }

    @Test
    void testCreateFilterAndMenuItems() {
        Case caze = createMenuItem()
        Case item = getMenuItem(caze)
        Case filter = getFilter(caze)

        Thread.sleep(4000)
        assert item.dataSet[MenuItemConstants.FIELD_MENU_ICON].value == "device_hub"
        assert item.dataSet[MenuItemConstants.FIELD_MENU_NAME].value == new I18nString("FILTER")
        assert item.dataSet[MenuItemConstants.FIELD_IDENTIFIER].value.toString() == "new_menu_item"
        assert item.dataSet[MenuItemConstants.FIELD_BANNED_ROLES].options.containsKey("role_2:filter_api_test")
        assert item.dataSet[MenuItemConstants.FIELD_ALLOWED_ROLES].options.containsKey("role_1:filter_api_test")
        assert item.dataSet[MenuItemConstants.FIELD_USE_TABBED_VIEW].value == true
        assert item.dataSet[MenuItemConstants.FIELD_VIEW_CONFIGURATION_TYPE].value == MenuItemView.TABBED_CASE_VIEW.identifier

        assert filter.dataSet["filter"].filterMetadata["filterType"] == "Case"
        assert filter.dataSet["filter"].allowedNets == ["filter", "menu_item"]
        assert filter.dataSet["filter"].value == "processIdentifier:filter OR processIdentifier:menu_item"
        assert filter.dataSet["filter_type"].value == "Case"

        String tabbedCaseViewId = MenuItemUtils.getCaseIdFromCaseRef(item, MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID)
        assert tabbedCaseViewId != null
        Case tabbedCaseView = workflowService.findOne(tabbedCaseViewId)
        assert tabbedCaseView.dataSet[TabbedCaseViewConstants.FIELD_VIEW_CONTAINS_FILTER].value == true
        assert tabbedCaseView.dataSet[TabbedCaseViewConstants.FIELD_VIEW_FILTER_CASE].value[0] == filter.stringId
        assert tabbedCaseView.dataSet[TabbedCaseViewConstants.FIELD_DEFAULT_HEADERS].value == "meta-title,meta-title"
        assert tabbedCaseView.dataSet[TabbedCaseViewConstants.FIELD_CONFIGURATION_TYPE].value == MenuItemView.TABBED_TASK_VIEW.identifier

        String tabbedTaskViewId = MenuItemUtils.getCaseIdFromCaseRef(tabbedCaseView, TabbedCaseViewConstants.FIELD_VIEW_CONFIGURATION_ID)
        assert tabbedTaskViewId != null
        Case tabbedTaskView = workflowService.findOne(tabbedTaskViewId)
        assert tabbedTaskView.dataSet[TabbedTaskViewConstants.FIELD_VIEW_CONTAINS_FILTER].value == false
        assert tabbedTaskView.dataSet[TabbedTaskViewConstants.FIELD_VIEW_FILTER_CASE].value == []
        assert tabbedTaskView.dataSet[TabbedTaskViewConstants.FIELD_DEFAULT_HEADERS].value == "meta-title,meta-title"

        Case testFolder = findCasesElastic("processIdentifier:$MenuProcessRunner.MENU_NET_IDENTIFIER AND dataSet.${MenuItemConstants.FIELD_NODE_PATH}.textValue.keyword:\"/netgrif/test\"", PageRequest.of(0, 1))[0]
        Case netgrifFolder = findCasesElastic("processIdentifier:$MenuProcessRunner.MENU_NET_IDENTIFIER AND dataSet.${MenuItemConstants.FIELD_NODE_PATH}.textValue.keyword:\"/netgrif\"", PageRequest.of(0, 1))[0]
        Case rootFolder = findCasesElastic("processIdentifier:$MenuProcessRunner.MENU_NET_IDENTIFIER AND dataSet.${MenuItemConstants.FIELD_NODE_PATH}.textValue.keyword:\"/\"", PageRequest.of(0, 1))[0]

        assert testFolder.dataSet[MenuItemConstants.FIELD_PARENT_ID].value == [netgrifFolder.stringId]
        assert (testFolder.dataSet[MenuItemConstants.FIELD_CHILD_ITEM_IDS].value as ArrayList).contains(item.stringId)
        assert item.dataSet[MenuItemConstants.FIELD_PARENT_ID].value == [testFolder.stringId]
        assert netgrifFolder.dataSet[MenuItemConstants.FIELD_PARENT_ID].value == [rootFolder.stringId]
        assert (netgrifFolder.dataSet[MenuItemConstants.FIELD_CHILD_ITEM_IDS].value as ArrayList).contains(testFolder.stringId)
        assert rootFolder.dataSet[MenuItemConstants.FIELD_PARENT_ID].value == []
        assert (rootFolder.dataSet[MenuItemConstants.FIELD_CHILD_ITEM_IDS].value as ArrayList).contains(netgrifFolder.stringId)
    }

    @Test
    void testChangeFilterAndMenuItems() {
        Case caze = createMenuItem()
        Thread.sleep(3000)

        Case item = getMenuItem(caze)
        String tabbedCaseViewIdBeforeChange = MenuItemUtils.getCaseIdFromCaseRef(item, MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID)
        Case tabbedCaseViewBeforeChange = workflowService.findOne(tabbedCaseViewIdBeforeChange)
        String tabbedTaskViewIdBeforeChange = MenuItemUtils.getCaseIdFromCaseRef(tabbedCaseViewBeforeChange, TabbedCaseViewConstants.FIELD_VIEW_CONFIGURATION_ID)

//        def newUri = uriService.getOrCreate("/netgrif/test_new", UriContentType.DEFAULT)
        caze = setData(caze, [
                "uri": "/netgrif/test_new",
                "title": "CHANGED FILTER",
                "allowed_nets": "filter",
                "query": "processIdentifier:filter",
                "type": "Case",
                "icon": "",
                "change_filter_and_menu": "0"
        ])
        item = getMenuItem(caze)
        Case filter = getFilter(caze)

        assert item.dataSet[MenuItemConstants.FIELD_MENU_NAME].value.toString() == "CHANGED FILTER"
        assert item.dataSet[MenuItemConstants.FIELD_ALLOWED_ROLES].options.entrySet()[0].key.contains("role_2")
        assert item.dataSet[MenuItemConstants.FIELD_USE_TABBED_VIEW].value == true
        assert item.dataSet[MenuItemConstants.FIELD_VIEW_CONFIGURATION_TYPE].value == MenuItemView.TABBED_CASE_VIEW.identifier
//        assert item.uriNodeId == newUri.stringId

        assert filter.dataSet["filter"].allowedNets == ["filter"]
        assert filter.dataSet["filter"].filterMetadata["defaultSearchCategories"] == false
        assert filter.dataSet["filter"].value == "processIdentifier:filter"

        String tabbedCaseViewId = MenuItemUtils.getCaseIdFromCaseRef(item, MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID)
        assert tabbedCaseViewId != null && tabbedCaseViewId.equals(tabbedCaseViewIdBeforeChange)
        Case tabbedCaseView = workflowService.findOne(tabbedCaseViewId)
        assert tabbedCaseView.dataSet[TabbedCaseViewConstants.FIELD_VIEW_CONTAINS_FILTER].value == true
        assert tabbedCaseView.dataSet[TabbedCaseViewConstants.FIELD_VIEW_FILTER_CASE].value[0] == filter.stringId
        assert tabbedCaseView.dataSet[TabbedCaseViewConstants.FIELD_DEFAULT_HEADERS].value == "meta-title,meta-title,meta-title"
        assert tabbedCaseView.dataSet[TabbedCaseViewConstants.FIELD_CONFIGURATION_TYPE].value == MenuItemView.TABBED_TASK_VIEW.identifier

        String tabbedTaskViewId = MenuItemUtils.getCaseIdFromCaseRef(tabbedCaseView, TabbedCaseViewConstants.FIELD_VIEW_CONFIGURATION_ID)
        assert tabbedTaskViewId != null && tabbedTaskViewId.equals(tabbedTaskViewIdBeforeChange)
        Case tabbedTaskView = workflowService.findOne(tabbedTaskViewId)
        assert tabbedTaskView.dataSet[TabbedTaskViewConstants.FIELD_VIEW_CONTAINS_FILTER].value == false
        assert tabbedTaskView.dataSet[TabbedTaskViewConstants.FIELD_VIEW_FILTER_CASE].value == []
        assert tabbedTaskView.dataSet[TabbedTaskViewConstants.FIELD_DEFAULT_HEADERS].value == "meta-title,meta-title,meta-title"
    }

    @Test
    void testFindFilter() {
        Case caze = createMenuItem()
        Case filter = getFilter(caze)

        caze = setData(caze, [
                "find_filter": "0"
        ])

        assert caze.dataSet["found_filter"].value == filter.stringId
    }

    @Test
    void testMoveMenuItem() {
        Case apiCase = createMenuItem("/netgrif/test")
        String viewId = apiCase.dataSet["menu_stringId"].value
        apiCase = createMenuItem("/netgrif2/test2", "new_menu_item2")
        String viewId2 = apiCase.dataSet["menu_stringId"].value

        // move view
        Thread.sleep(2000)
        apiCase = setData(apiCase, [
            "move_dest_uri": "/netgrif2",
            "move_item_id": viewId,
            "move_folder_path": null,
            "move_item": "0"
        ])

        Case viewCase = workflowService.findOne(viewId)
        Thread.sleep(2000)

        Case folderCase = findCasesElastic("processIdentifier:$MenuProcessRunner.MENU_NET_IDENTIFIER AND dataSet.${MenuItemConstants.FIELD_NODE_PATH}.textValue:\"/netgrif2\"", PageRequest.of(0, 1))[0]

        ArrayList<String> childIds = folderCase.dataSet[MenuItemConstants.FIELD_CHILD_ITEM_IDS].value as ArrayList<String>
        assert childIds.contains(viewId) && childIds.size() == 2

        // cyclic move
        assertThrows(IllegalArgumentException.class, () -> {
            setData(apiCase, [
                    "move_dest_uri": "/netgrif2/cyclic",
                    "move_item_id": null,
                    "move_folder_path": "/netgrif2",
                    "move_item": "0"
            ])
        })

        // move folder
        setData(apiCase, [
            "move_dest_uri": "/netgrif/test3",
            "move_item_id": null,
            "move_folder_path": "/netgrif2",
            "move_item": "0"
        ])
        Thread.sleep(2000)

        folderCase = findCasesElastic("processIdentifier:$MenuProcessRunner.MENU_NET_IDENTIFIER AND dataSet.${MenuItemConstants.FIELD_NODE_PATH}.textValue:\"/netgrif/test3\"", PageRequest.of(0, 1))[0]
        Case folderCase2 = findCasesElastic("processIdentifier:$MenuProcessRunner.MENU_NET_IDENTIFIER AND dataSet.${MenuItemConstants.FIELD_NODE_PATH}.textValue:\"/netgrif\"", PageRequest.of(0, 1))[0]
        assert folderCase != null && folderCase.dataSet[MenuItemConstants.FIELD_PARENT_ID].value == [folderCase2.stringId]

        folderCase = findCasesElastic("processIdentifier:$MenuProcessRunner.MENU_NET_IDENTIFIER AND dataSet.${MenuItemConstants.FIELD_NODE_PATH}.textValue:\"/netgrif/test3/netgrif2\"", PageRequest.of(0, 1))[0]
        assert folderCase != null
        assert folderCase.dataSet[MenuItemConstants.FIELD_NODE_PATH].value == "/netgrif/test3/netgrif2"

        childIds = folderCase.dataSet[MenuItemConstants.FIELD_CHILD_ITEM_IDS].value as ArrayList<String>
        assert childIds.size() == 2

        folderCase = workflowService.findOne(childIds[0])
        assert folderCase.dataSet[MenuItemConstants.FIELD_NODE_PATH].value == "/netgrif/test3/netgrif2/test2"

        viewCase = workflowService.findOne(viewId2)
    }

    @Test
    void testDuplicateMenuItem() {
        String starterUri = "/netgrif/test"
        Case apiCase = createMenuItem(starterUri, "new_menu_item")
        Thread.sleep(2000)

        String itemId = apiCase.dataSet["menu_stringId"].value
        Case origin = workflowService.findOne(itemId)
        Case testFolder = workflowService.findOne((origin.dataSet[MenuItemConstants.FIELD_PARENT_ID].value as ArrayList<String>)[0])

        String newTitle = "New title"
        String newIdentifier = "new_identifier"

        String duplicateTaskId = testFolder.tasks.find { it.transition == "duplicate_item" }.task
        taskService.assignTask(new TaskParams(duplicateTaskId))

        assertThrows(IllegalArgumentException.class, () -> {
            testFolder.dataSet[MenuItemConstants.FIELD_DUPLICATE_TITLE].value = new I18nString("")
            testFolder.dataSet[MenuItemConstants.FIELD_DUPLICATE_IDENTIFIER].value = newIdentifier
            testFolder = workflowService.save(testFolder)
            taskService.finishTask(new TaskParams(duplicateTaskId))
        })

        assertThrows(IllegalArgumentException.class, () -> {
            testFolder.dataSet[MenuItemConstants.FIELD_DUPLICATE_TITLE].value = new I18nString(newTitle)
            testFolder.dataSet[MenuItemConstants.FIELD_DUPLICATE_IDENTIFIER].value = "new_menu_item"
            testFolder = workflowService.save(testFolder)
            taskService.finishTask(new TaskParams(duplicateTaskId))
        })

        testFolder.dataSet[MenuItemConstants.FIELD_DUPLICATE_TITLE].value = new I18nString(newTitle)
        testFolder.dataSet[MenuItemConstants.FIELD_DUPLICATE_IDENTIFIER].value = newIdentifier
        testFolder = workflowService.save(testFolder)
        taskService.finishTask(new TaskParams(duplicateTaskId))

        Case duplicated = workflowService.searchOne(QCase.case$.processIdentifier.eq("menu_item")
                .and(QCase.case$.dataSet.get(MenuItemConstants.FIELD_IDENTIFIER).value.eq(newIdentifier)))
        assert duplicated != null


        assert duplicated.dataSet[MenuItemConstants.FIELD_DUPLICATE_TITLE].value == new I18nString("")
        assert duplicated.dataSet[MenuItemConstants.FIELD_DUPLICATE_IDENTIFIER].value == ""
        assert duplicated.title == newTitle
        assert duplicated.dataSet[MenuItemConstants.FIELD_MENU_NAME].value == new I18nString(newTitle)
        assert duplicated.dataSet[MenuItemConstants.FIELD_IDENTIFIER].value == newIdentifier
        assert duplicated.dataSet[MenuItemConstants.FIELD_NODE_PATH].value == "/netgrif/" + newIdentifier
        assert duplicated.dataSet[MenuItemConstants.FIELD_CHILD_ITEM_IDS].value == []
        assert duplicated.dataSet[MenuItemConstants.FIELD_HAS_CHILDREN].value == false
        assert duplicated.activePlaces["initialized"] == 1
    }

    List<Case> findCasesElastic(String query, Pageable pageable) {
        CaseSearchRequest request = new CaseSearchRequest()
        request.query = query
        List<Case> result = elasticCaseService.search([request], ActorTransformer.toLoggedUser(userService.system), pageable, LocaleContextHolder.locale, false).content
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
        Case apiCase = createMenuItem(starterUri, "new_menu_item")
        Case leafItemCase = getMenuItem(apiCase)

        sleep(2000)
        Case testFolder = findCasesElastic("processIdentifier:$MenuProcessRunner.MENU_NET_IDENTIFIER AND dataSet.${MenuItemConstants.FIELD_NODE_PATH}.textValue:\"/netgrif/test\"", PageRequest.of(0, 1))[0]
        String netgrifFolderId = (testFolder.dataSet[MenuItemConstants.FIELD_PARENT_ID].value as ArrayList<String>)[0]

        Case netgrifFolder = workflowService.findOne(netgrifFolderId)
        assert (netgrifFolder.dataSet[MenuItemConstants.FIELD_CHILD_ITEM_IDS].value as ArrayList).contains(testFolder.stringId)
        assert workflowService.findOne(testFolder.stringId) != null
        assert workflowService.findOne(leafItemCase.stringId) != null
        String tabbedCaseViewId = MenuItemUtils.getCaseIdFromCaseRef(leafItemCase, MenuItemConstants.FIELD_VIEW_CONFIGURATION_ID)
        assert tabbedCaseViewId != null
        Case tabbedCaseView = workflowService.findOne(tabbedCaseViewId)
        String tabbedTaskViewId = MenuItemUtils.getCaseIdFromCaseRef(tabbedCaseView, TabbedCaseViewConstants.FIELD_VIEW_CONFIGURATION_ID)
        assert tabbedTaskViewId != null

        workflowService.deleteCase(new DeleteCaseParams(testFolder))
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

    Case getFilter(Case caze) {
        return workflowService.findOne(caze.dataSet["filter_stringId"].value as String)
    }

    def setData(Case caze, Map<String, String> dataSet) {
        dataService.setData(caze.tasks[0].task, ImportHelper.populateDataset(dataSet.collectEntries {
            [(it.key): (["value": it.value, "type": "text"])]
        }))
        return workflowService.findOne(caze.stringId)
    }

}
