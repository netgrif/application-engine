package com.netgrif.application.engine.action

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.UriContentType
import com.netgrif.application.engine.petrinet.domain.UriNode
import com.netgrif.application.engine.petrinet.domain.dataset.ButtonField
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.startup.FilterRunner
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.domain.menu.MenuItemConstants
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
class MenuItemApiTest extends EngineTest {

    @BeforeEach
    void before() {
        super.before()
        importHelper.createNet("filter_api_test.xml")
    }

    @Test
    void testCreateFilterAndMenuItems() {
        Case caze = createMenuItem()
        Case item = getMenuItem(caze)
        Case filter = getFilter(caze)

        Thread.sleep(4000)
        UriNode leafNode = uriService.findByUri("/netgrif/test/new_menu_item")
        assert item.uriNodeId == uriService.findByUri("/netgrif/test").stringId
        assert item.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_MENU_ICON.attributeId).rawValue == "device_hub"
        assert item.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_MENU_NAME.attributeId).rawValue == new I18nString("FILTER")
        assert item.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_IDENTIFIER.attributeId).rawValue.toString() == "new_menu_item"
        assert (item.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_FILTER_CASE.attributeId).rawValue as List)[0] == filter.stringId
        assert item.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_ALLOWED_ROLES.attributeId).options.containsKey("role_1:filter_api_test")
        assert item.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_BANNED_ROLES.attributeId).options.containsKey("role_2:filter_api_test")
        assert item.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_DEFAULT_HEADERS.attributeId).rawValue == "meta-title,meta-title"
        assert item.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_DEFAULT_HEADERS.attributeId).rawValue == "meta-title,meta-title"

        assert filter.dataSet.get("filter").filterMetadata["filterType"] == "Case"
        assert filter.dataSet.get("filter").allowedNets == ["filter", "preference_item"]
        assert filter.dataSet.get("filter").rawValue == "processIdentifier:filter OR processIdentifier:preference_item"
        assert filter.dataSet.get("filter_type").rawValue == "Case"
        assert leafNode != null

        Case testFolder = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.${MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId}.textValue.keyword:\"/netgrif/test\"", PageRequest.of(0, 1))[0]
        Case netgrifFolder = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.${MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId}.textValue.keyword:\"/netgrif\"", PageRequest.of(0, 1))[0]
        UriNode testNode = uriService.findByUri("/netgrif")
        UriNode netgrifNode = uriService.getRoot()
        Case rootFolder = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.${MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId}.textValue.keyword:\"/\"", PageRequest.of(0, 1))[0]

        assert testFolder != null && testNode != null
        assert testFolder.uriNodeId == testNode.stringId
        assert testFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID.attributeId).rawValue == [netgrifFolder.stringId]
        assert (testFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId).rawValue as ArrayList).contains(item.stringId)
        assert item.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID.attributeId).rawValue == [testFolder.stringId]
        assert netgrifFolder.uriNodeId == netgrifNode.stringId
        assert netgrifFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID.attributeId).rawValue == [rootFolder.stringId]
        assert (netgrifFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId).rawValue as ArrayList).contains(testFolder.stringId)
        assert rootFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID.attributeId).rawValue == []
        assert (rootFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId).rawValue as ArrayList).contains(netgrifFolder.stringId)
    }

    @Test
    void testChangeFilterAndMenuItems() {
        Case caze = createMenuItem()
        Thread.sleep(3000)
        def newUri = uriService.getOrCreate("/netgrif/test_new", UriContentType.DEFAULT)
        caze = setData(caze, new DataSet([
                "uri"                   : newUri.uriPath,
                "title"                 : "CHANGED FILTER",
                "allowed_nets"          : "filter",
                "query"                 : "processIdentifier:filter",
                "type"                  : "Case",
                "icon"                  : "",
                "change_filter_and_menu": "0"
        ]))
        Case item = getMenuItem(caze)
        Case filter = getFilter(caze)

        assert item.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_MENU_NAME.attributeId).rawValue.toString() == "CHANGED FILTER"
        assert item.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_ALLOWED_ROLES.attributeId).options.entrySet()[0].key.contains("role_2")
        assert item.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_CASE_DEFAULT_HEADERS.attributeId).rawValue == "meta-title,meta-title,meta-title"
        assert item.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_TASK_DEFAULT_HEADERS.attributeId).rawValue == "meta-title,meta-title,meta-title"
        assert item.uriNodeId == newUri.stringId

        assert filter.dataSet.get("filter").allowedNets == ["filter"]
        assert filter.dataSet.get("filter").filterMetadata["defaultSearchCategories"] == false
        assert filter.dataSet.get("filter").rawValue == "processIdentifier:filter"
    }

    @Test
    void testFindFilter() {
        Case caze = createMenuItem()
        Case filter = getFilter(caze)

        caze = setData(caze, new DataSet([
                "find_filter": new ButtonField(rawValue: 0)
        ]))

        assert caze.dataSet.get("found_filter").rawValue == filter.stringId
    }

    @Test
    void testMoveMenuItem() {
        Case apiCase = createMenuItem("/netgrif/test")
        String viewId = apiCase.dataSet.get("menu_stringId").rawValue
        apiCase = createMenuItem("/netgrif2/test2", "new_menu_item2")
        String viewId2 = apiCase.dataSet.get("menu_stringId").rawValue


        // move view
        Thread.sleep(2000)
        apiCase = setData(apiCase, [
                "move_dest_uri"   : "/netgrif2",
                "move_item_id"    : viewId,
                "move_folder_path": null,
                "move_item"       : "0"
        ])

        Case viewCase = workflowService.findOne(viewId)
        Thread.sleep(2000)

        UriNode node = uriService.findByUri("/netgrif2")
        Case folderCase = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.${MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId}.textValue:\"/netgrif2\"", PageRequest.of(0, 1))[0]

        assert viewCase.uriNodeId == node.stringId
        ArrayList<String> childIds = folderCase.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId).rawValue as ArrayList<String>
        assert childIds.contains(viewId) && childIds.size() == 2


        // cyclic move
        assertThrows(IllegalArgumentException.class, () -> {
            setData(apiCase, [
                    "move_dest_uri"   : "/netgrif2/cyclic",
                    "move_item_id"    : null,
                    "move_folder_path": "/netgrif2",
                    "move_item"       : "0"
            ])
        })


        // move folder
        setData(apiCase, [
                "move_dest_uri"   : "/netgrif/test3",
                "move_item_id"    : null,
                "move_folder_path": "/netgrif2",
                "move_item"       : "0"
        ])
        Thread.sleep(2000)

        folderCase = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.${MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId}.textValue:\"/netgrif/test3\"", PageRequest.of(0, 1))[0]
        Case folderCase2 = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.${MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId}.textValue:\"/netgrif\"", PageRequest.of(0, 1))[0]
        assert folderCase != null && folderCase.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID.attributeId).rawValue == [folderCase2.stringId]

        folderCase = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.${MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId}.textValue:\"/netgrif/test3/netgrif2\"", PageRequest.of(0, 1))[0]
        assert folderCase != null
        node = uriService.findByUri("/netgrif/test3")
        assert node != null
        assert folderCase.uriNodeId == node.stringId
        assert folderCase.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId).rawValue == "/netgrif/test3/netgrif2"

        childIds = folderCase.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId).rawValue as ArrayList<String>
        assert childIds.size() == 2

        folderCase = workflowService.findOne(childIds[0])
        node = uriService.findByUri("/netgrif/test3/netgrif2")
        assert folderCase.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId).rawValue == "/netgrif/test3/netgrif2/test2"
        assert folderCase.uriNodeId == node.stringId

        viewCase = workflowService.findOne(viewId2)
        node = uriService.findByUri("/netgrif/test3/netgrif2/test2")
        assert viewCase.uriNodeId == node.stringId
    }

    @Test
    void testDuplicateMenuItem() {
        String starterUri = "/netgrif/test"
        Case apiCase = createMenuItem(starterUri, "new_menu_item")
        String itemId = apiCase.dataSet.get("menu_stringId").rawValue
        Case origin = workflowService.findOne(itemId)
        Case testFolder = workflowService.findOne((origin.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID.attributeId).rawValue as ArrayList<String>)[0])

        String newTitle = "New title"
        String newIdentifier = "new_identifier"

        String duplicateTaskId = testFolder.tasks.find { it.transition == "duplicate_item" }.task
        taskService.assignTask(duplicateTaskId)

        assertThrows(IllegalArgumentException.class, () -> {
            testFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_DUPLICATE_TITLE.attributeId).rawValue = new I18nString("")
            testFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_DUPLICATE_IDENTIFIER.attributeId).rawValue = newIdentifier
            testFolder = workflowService.save(testFolder)
            taskService.finishTask(duplicateTaskId)
        })

        assertThrows(IllegalArgumentException.class, () -> {
            testFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_DUPLICATE_TITLE.attributeId).rawValue = new I18nString(newTitle)
            testFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_DUPLICATE_IDENTIFIER.attributeId).rawValue = "new_menu_item"
            testFolder = workflowService.save(testFolder)
            taskService.finishTask(duplicateTaskId)
        })

        testFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_DUPLICATE_TITLE.attributeId).rawValue = new I18nString(newTitle)
        testFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_DUPLICATE_IDENTIFIER.attributeId).rawValue = newIdentifier
        testFolder = workflowService.save(testFolder)
        taskService.finishTask(duplicateTaskId)

        Case duplicated = workflowService.searchOne(QCase.case$.processIdentifier.eq("preference_item").and(QCase.case$.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_IDENTIFIER.attributeId).value.eq(newIdentifier)))
        assert duplicated != null

        UriNode leafNode = uriService.findByUri("/netgrif/" + newIdentifier)

        assert duplicated.uriNodeId == testFolder.uriNodeId
        assert leafNode != null
        assert duplicated.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_DUPLICATE_TITLE.attributeId).rawValue == null
        assert duplicated.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_DUPLICATE_IDENTIFIER.attributeId).rawValue == null
        assert duplicated.title == newTitle
        assert duplicated.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_MENU_NAME.attributeId).rawValue == new I18nString(newTitle)
        assert duplicated.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_IDENTIFIER.attributeId).rawValue == newIdentifier
        assert duplicated.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId).rawValue == "/netgrif/" + newIdentifier
        assert duplicated.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId).rawValue == []
        assert duplicated.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_HAS_CHILDREN.attributeId).rawValue == false
        assert duplicated.activePlaces["initialized"] == 1
    }

    List<Case> findCasesElastic(String query, Pageable pageable) {
        CaseSearchRequest request = new CaseSearchRequest()
        request.query = query
        List<Case> result = elasticCaseService.search([request], userService.system.transformToLoggedUser(), pageable, LocaleContextHolder.locale, false).content
        return result
    }

    Case createMenuItem(String uri = "/", String identifier = "new_menu_item") {
        Case caze = getCase()
        caze = setData(caze, new DataSet([
                "uri"                   : new TextField(rawValue: uri),
                "title"                 : new TextField(rawValue: "FILTER"),
                "allowed_nets"          : new TextField(rawValue: "filter,preference_item"),
                "query"                 : new TextField(rawValue: "processIdentifier:filter OR processIdentifier:preference_item"),
//                TODO: release/8.0.0 no type field?
//                "type"                  : new TextField(rawValue: "Case"),
                "identifier"            : new TextField(rawValue: identifier),
                "icon"                  : new TextField(rawValue: "device_hub"),
                "create_filter_and_menu": new ButtonField(rawValue: 0)
        ]))
        return caze
    }

    @Test
    void testRemoveMenuItem() {
        String starterUri = "/netgrif/test"
        Case apiCase = createMenuItem(starterUri, "new_menu_item")
        String leafItemId = apiCase.dataSet.get("menu_stringId").rawValue

        Case testFolder = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.${MenuItemConstants.PREFERENCE_ITEM_FIELD_NODE_PATH.attributeId}.textValue:\"/netgrif/test\"", PageRequest.of(0, 1))[0]
        String netgrifFolderId = (testFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_PARENT_ID.attributeId).rawValue as ArrayList<String>)[0]

        Case netgrifFolder = workflowService.findOne(netgrifFolderId)
        assert (netgrifFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId).rawValue as ArrayList).contains(testFolder.stringId)
        assert workflowService.findOne(testFolder.stringId) != null
        assert workflowService.findOne(leafItemId) != null

        workflowService.deleteCase(testFolder)
        sleep(2000)
        netgrifFolder = workflowService.findOne(netgrifFolderId)
        assert !(netgrifFolder.dataSet.get(MenuItemConstants.PREFERENCE_ITEM_FIELD_CHILD_ITEM_IDS.attributeId).rawValue as ArrayList).contains(testFolder.stringId)
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
        return workflowService.findOne(caze.dataSet.get("menu_stringId").rawValue as String)
    }

    Case getFilter(Case caze) {
        return workflowService.findOne(caze.dataSet.get("filter_stringId").rawValue as String)
    }

    def setData(Case caze, DataSet dataSet) {
        dataService.setData(caze.tasks["t1"].taskStringId, dataSet, superCreator.superUser)
        return workflowService.findOne(caze.stringId)
    }
}
