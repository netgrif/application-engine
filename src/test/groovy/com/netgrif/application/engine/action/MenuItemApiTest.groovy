package com.netgrif.application.engine.action


import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.application.engine.petrinet.domain.UriContentType
import com.netgrif.application.engine.petrinet.domain.UriNode
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService
import com.netgrif.application.engine.startup.FilterRunner
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
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
        assert item.uriNodeId == uriService.findByUri("/netgrif/test").id
        assert item.dataSet["icon"].value == "filter_alt"
        assert item.dataSet["type"].value.toString() == "view"
        assert item.dataSet["name"].value.toString() == "FILTER"
        assert item.dataSet["menu_item_identifier"].value.toString() == "new_menu_item"

        assert filter.dataSet["filter"].filterMetadata["filterType"] == "Case"
        assert filter.dataSet["filter"].allowedNets == ["filter", "preference_item"]
        assert filter.dataSet["filter"].value == "processIdentifier:filter OR processIdentifier:preference_item"
        assert filter.dataSet["filter_type"].value == "Case"
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

        assert item.dataSet["name"].value.toString() == "CHANGED FILTER"
        assert item.dataSet["allowed_roles"].options.entrySet()[0].key.contains("role_2")
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
        Case folderCase = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.nodePath.textValue:\"/netgrif2\"", PageRequest.of(0, 1))[0]

        assert viewCase.uriNodeId == node.id
        Set<String> childIds = folderCase.dataSet["childItemIds"].options.keySet()
        assert childIds.contains(viewId) && childIds.size() == 2

        setData(apiCase, [
            "move_dest_uri": "/netgrif/test3",
            "move_item_id": null,
            "move_folder_path": "/netgrif2",
            "move_item": "0"
        ])
        Thread.sleep(2000)

        folderCase = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.nodePath.textValue:\"/netgrif/test3\"", PageRequest.of(0, 1))[0]
        Case folderCase2 = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.nodePath.textValue:\"/netgrif\"", PageRequest.of(0, 1))[0]
        assert folderCase != null && folderCase.dataSet["parentId"].value == folderCase2.stringId

        folderCase = findCasesElastic("processIdentifier:$FilterRunner.PREFERRED_ITEM_NET_IDENTIFIER AND dataSet.nodePath.textValue:\"/netgrif/test3/netgrif2\"", PageRequest.of(0, 1))[0]
        assert folderCase != null
        folderCase = workflowService.populateUriNodeId(folderCase)
        node = uriService.findByUri("/netgrif/test3")
        assert node != null
        assert folderCase.uriNodeId == node.id
        assert folderCase.dataSet["nodePath"].value == "/netgrif/test3/netgrif2"

        childIds = folderCase.dataSet["childItemIds"].options.keySet()
        assert childIds.size() == 2

        folderCase = workflowService.findOne(childIds[0])
        folderCase = workflowService.populateUriNodeId(folderCase)
        node = uriService.findByUri("/netgrif/test3/netgrif2")
        assert folderCase.dataSet["nodePath"].value == "/netgrif/test3/netgrif2/test2"
        assert folderCase.uriNodeId == node.id

        viewCase = workflowService.findOne(viewId2)
        viewCase = workflowService.populateUriNodeId(viewCase)
        node = uriService.findByUri("/netgrif/test3/netgrif2/test2")
        assert viewCase.uriNodeId == node.id
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
