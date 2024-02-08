package com.netgrif.application.engine.action


import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.UriContentType
import com.netgrif.application.engine.petrinet.domain.UriNode
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService
import com.netgrif.application.engine.startup.FilterRunner
import com.netgrif.application.engine.startup.ImportHelper
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

    private static final String PREFERENCE_ITEM_FIELD_PARENT_ID = "parentId"
    private static final String PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS= "childItemIds"
    private static final String PREFERENCE_ITEM_FIELD_HAS_CHILDREN= "hasChildren"
    private static final String PREFERENCE_ITEM_FIELD_IDENTIFIER = "menu_item_identifier"
    private static final String PREFERENCE_ITEM_FIELD_ALLOWED_ROLES = "allowed_roles"
    private static final String PREFERENCE_ITEM_FIELD_NAME = "menu_name"
    private static final String PREFERENCE_ITEM_FIELD_ICON = "menu_icon"
    private static final String PREFERENCE_ITEM_FIELD_NODE_PATH = "nodePath"
    private static final String PREFERENCE_ITEM_FIELD_DUPLICATE_TITLE= "duplicate_new_title"
    private static final String PREFERENCE_ITEM_FIELD_DUPLICATE_IDENTIFIER = "duplicate_view_identifier"
    private static final String PREFERENCE_ITEM_FIELD_CASE_DEFAULT_HEADERS = "case_default_headers"
    private static final String PREFERENCE_ITEM_FIELD_TASK_DEFAULT_HEADERS = "task_default_headers"

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ImportHelper helper

    @Autowired
    private FilterRunner filterRunner

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

    @Test
    void testCreateFilterAndMenuItems() {
        Case caze = createMenuItem()
        Case item = getMenuItem(caze)
        Case filter = getFilter(caze)

        Thread.sleep(4000)
        item = workflowService.populateUriNodeId(item)
        UriNode leafNode = uriService.findByUri("/netgrif/test/new_menu_item")
        assert item.uriNodeId == uriService.findByUri("/netgrif/test").id
        assert item.dataSet[PREFERENCE_ITEM_FIELD_ICON].value == "filter_alt"
        assert item.dataSet[PREFERENCE_ITEM_FIELD_NAME].value.toString() == "FILTER"
        assert item.dataSet[PREFERENCE_ITEM_FIELD_IDENTIFIER].value.toString() == "new_menu_item"

        assert filter.dataSet["filter"].filterMetadata["filterType"] == "Case"
        assert filter.dataSet["filter"].allowedNets == ["filter", "preference_item"]
        assert filter.dataSet["filter"].value == "processIdentifier:filter OR processIdentifier:preference_item"
        assert filter.dataSet["filter_type"].value == "Case"
        assert leafNode != null

        Case testFolder = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.${PREFERENCE_ITEM_FIELD_NODE_PATH}.textValue:\"/netgrif/test\"", PageRequest.of(0, 1))[0]
        testFolder = workflowService.populateUriNodeId(testFolder)
        Case netgrifFolder = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.${PREFERENCE_ITEM_FIELD_NODE_PATH}.textValue:\"/netgrif\"", PageRequest.of(0, 1))[0]
        netgrifFolder = workflowService.populateUriNodeId(netgrifFolder)
        UriNode testNode = uriService.findByUri("/netgrif")
        UriNode netgrifNode = uriService.getRoot()

        assert testFolder != null && testNode != null
        assert testFolder.uriNodeId == testNode.id
        assert testFolder.dataSet[PREFERENCE_ITEM_FIELD_PARENT_ID].value == netgrifFolder.stringId
        assert testFolder.dataSet[PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS].options.keySet().contains(item.stringId)
        assert item.dataSet[PREFERENCE_ITEM_FIELD_PARENT_ID].value == testFolder.stringId
        assert netgrifFolder.uriNodeId == netgrifNode.id
        assert netgrifFolder.dataSet[PREFERENCE_ITEM_FIELD_PARENT_ID].value == null
        assert netgrifFolder.dataSet[PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS].options.keySet().contains(testFolder.stringId)
    }

    @Test
    void testChangeFilterAndMenuItems() {
        Case caze = createMenuItem()
        def newUri = uriService.getOrCreate("/netgrif/test_new", UriContentType.DEFAULT)
        caze = setData(caze, [
                "uri": newUri.uriPath,
                "title": "CHANGED FILTER",
                "allowed_nets": "filter",
                "query": "processIdentifier:filter",
                "type": "Case",
                "icon": "",
                "change_filter_and_menu": "0"
        ])
        Case item = getMenuItem(caze)
        Thread.sleep(3000)
        item = workflowService.populateUriNodeId(item)
        Case filter = getFilter(caze)

        assert item.dataSet[PREFERENCE_ITEM_FIELD_NAME].value.toString() == "CHANGED FILTER"
        assert item.dataSet[PREFERENCE_ITEM_FIELD_ALLOWED_ROLES].options.entrySet()[0].key.contains("role_2")
        assert item.dataSet[PREFERENCE_ITEM_FIELD_CASE_DEFAULT_HEADERS].value == "meta-title,meta-title,meta-title"
        assert item.dataSet[PREFERENCE_ITEM_FIELD_TASK_DEFAULT_HEADERS].value == "meta-title,meta-title,meta-title"
        assert item.uriNodeId == newUri.id

        assert filter.dataSet["filter"].allowedNets == ["filter"]
        assert filter.dataSet["filter"].filterMetadata["defaultSearchCategories"] == false
        assert filter.dataSet["filter"].value == "processIdentifier:filter"
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
        viewCase = workflowService.populateUriNodeId(viewCase)

        UriNode node = uriService.findByUri("/netgrif2")
        Case folderCase = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.${PREFERENCE_ITEM_FIELD_NODE_PATH}.textValue:\"/netgrif2\"", PageRequest.of(0, 1))[0]

        assert viewCase.uriNodeId == node.id
        Set<String> childIds = folderCase.dataSet[PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS].options.keySet()
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

        folderCase = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.${PREFERENCE_ITEM_FIELD_NODE_PATH}.textValue:\"/netgrif/test3\"", PageRequest.of(0, 1))[0]
        Case folderCase2 = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.${PREFERENCE_ITEM_FIELD_NODE_PATH}.textValue:\"/netgrif\"", PageRequest.of(0, 1))[0]
        assert folderCase != null && folderCase.dataSet[PREFERENCE_ITEM_FIELD_PARENT_ID].value == folderCase2.stringId

        folderCase = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.${PREFERENCE_ITEM_FIELD_NODE_PATH}.textValue:\"/netgrif/test3/netgrif2\"", PageRequest.of(0, 1))[0]
        assert folderCase != null
        folderCase = workflowService.populateUriNodeId(folderCase)
        node = uriService.findByUri("/netgrif/test3")
        assert node != null
        assert folderCase.uriNodeId == node.id
        assert folderCase.dataSet[PREFERENCE_ITEM_FIELD_NODE_PATH].value == "/netgrif/test3/netgrif2"

        childIds = folderCase.dataSet[PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS].options.keySet()
        assert childIds.size() == 2

        folderCase = workflowService.findOne(childIds[0])
        folderCase = workflowService.populateUriNodeId(folderCase)
        node = uriService.findByUri("/netgrif/test3/netgrif2")
        assert folderCase.dataSet[PREFERENCE_ITEM_FIELD_NODE_PATH].value == "/netgrif/test3/netgrif2/test2"
        assert folderCase.uriNodeId == node.id

        viewCase = workflowService.findOne(viewId2)
        viewCase = workflowService.populateUriNodeId(viewCase)
        node = uriService.findByUri("/netgrif/test3/netgrif2/test2")
        assert viewCase.uriNodeId == node.id
    }

    @Test
    void testDuplicateMenuItem() {
        String starterUri = "/netgrif/test"
        Case apiCase = createMenuItem(starterUri, "new_menu_item")
        String itemId = apiCase.dataSet["menu_stringId"].value
        Case origin = workflowService.findOne(itemId)
        Case testFolder = workflowService.findOne(origin.dataSet[PREFERENCE_ITEM_FIELD_PARENT_ID].value as String)

        String newTitle = "New title"
        String newIdentifier = "new_identifier"

        String duplicateTaskId = testFolder.tasks.find { it.transition == "duplicate_item" }.task
        taskService.assignTask(duplicateTaskId)

        assertThrows(IllegalArgumentException.class, () -> {
            testFolder.dataSet[PREFERENCE_ITEM_FIELD_DUPLICATE_TITLE].value = new I18nString("")
            testFolder.dataSet[PREFERENCE_ITEM_FIELD_DUPLICATE_IDENTIFIER].value = newIdentifier
            testFolder = workflowService.save(testFolder)
            taskService.finishTask(duplicateTaskId)
        })

        assertThrows(IllegalArgumentException.class, () -> {
            testFolder.dataSet[PREFERENCE_ITEM_FIELD_DUPLICATE_TITLE].value = new I18nString(newTitle)
            testFolder.dataSet[PREFERENCE_ITEM_FIELD_DUPLICATE_IDENTIFIER].value = "new_menu_item"
            testFolder = workflowService.save(testFolder)
            taskService.finishTask(duplicateTaskId)
        })

        testFolder.dataSet[PREFERENCE_ITEM_FIELD_DUPLICATE_TITLE].value = new I18nString(newTitle)
        testFolder.dataSet[PREFERENCE_ITEM_FIELD_DUPLICATE_IDENTIFIER].value = newIdentifier
        testFolder = workflowService.save(testFolder)
        taskService.finishTask(duplicateTaskId)

        Case duplicated = workflowService.searchOne(QCase.case$.processIdentifier.eq("preference_item").and(QCase.case$.dataSet.get(PREFERENCE_ITEM_FIELD_IDENTIFIER).value.eq(newIdentifier)))
        assert duplicated != null

        duplicated = workflowService.populateUriNodeId(duplicated)
        testFolder = workflowService.populateUriNodeId(testFolder)
        UriNode leafNode = uriService.findByUri("/netgrif/" + newIdentifier)

        assert duplicated.uriNodeId == testFolder.uriNodeId
        assert leafNode != null
        assert duplicated.dataSet[PREFERENCE_ITEM_FIELD_DUPLICATE_TITLE].value == null
        assert duplicated.dataSet[PREFERENCE_ITEM_FIELD_DUPLICATE_IDENTIFIER].value == null
        assert duplicated.title == newTitle
        assert duplicated.dataSet[PREFERENCE_ITEM_FIELD_NAME].value == new I18nString(newTitle)
        assert duplicated.dataSet[PREFERENCE_ITEM_FIELD_IDENTIFIER].value == newIdentifier
        assert duplicated.dataSet[PREFERENCE_ITEM_FIELD_NODE_PATH].value == "/netgrif/" + newIdentifier
        assert duplicated.dataSet[PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS].options == [:]
        assert duplicated.dataSet[PREFERENCE_ITEM_FIELD_HAS_CHILDREN].value == false
        assert duplicated.activePlaces["initialized"] == 1
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
                "allowed_nets": "filter,preference_item",
                "query": "processIdentifier:filter OR processIdentifier:preference_item",
                "type": "Case",
                "group": null,
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
        String leafItemId = apiCase.dataSet["menu_stringId"].value

        Case testFolder = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.${PREFERENCE_ITEM_FIELD_NODE_PATH}.textValue:\"/netgrif/test\"", PageRequest.of(0, 1))[0]
        String netgrifFolderId = testFolder.dataSet[PREFERENCE_ITEM_FIELD_PARENT_ID].value

        Case netgrifFolder = workflowService.findOne(netgrifFolderId)
        assert netgrifFolder.dataSet[PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS].options.containsKey(testFolder.stringId)
        assert workflowService.findOne(testFolder.stringId) != null
        assert workflowService.findOne(leafItemId) != null

        workflowService.deleteCase(testFolder)
        sleep(2000)
        netgrifFolder = workflowService.findOne(netgrifFolderId)
        assert !netgrifFolder.dataSet[PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS].options.containsKey(testFolder.stringId)
        assertThrows(IllegalArgumentException.class, () -> {
            workflowService.findOne(testFolder.stringId)
        })
        assertThrows(IllegalArgumentException.class, () -> {
            workflowService.findOne(leafItemId)
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
