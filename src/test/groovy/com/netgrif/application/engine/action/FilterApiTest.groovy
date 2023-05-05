package com.netgrif.application.engine.action


import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.application.engine.petrinet.domain.UriContentType
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService
import com.netgrif.application.engine.startup.FilterRunner
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.bson.types.ObjectId
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class FilterApiTest {

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

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
        helper.createNet("filter_api_test.xml")
    }

    @Test
    void testCreateFilterAndMenu() {
        Case caze = createMenuItem()
        Case item = getMenuItem(caze)
        Case filter = getFilter(caze)

        Case defGroup = nextGroupService.findDefaultGroup()

        assert item.uriNodeId == uriService.findByUri("netgrif/test").id
        assert item.dataSet["icon"].value == "filter_alt"
        assert item.dataSet["type"].value.toString() == "view"
        assert item.dataSet["name"].value.toString() == "FILTER"
        assert item.dataSet["menu_item_identifier"].value.toString() == "new_menu_item"
        assert item.dataSet["parentId"].value.toString() == defGroup.stringId

        assert filter.dataSet["filter"].filterMetadata["filterType"] == "Case"
        assert filter.dataSet["filter"].allowedNets == ["filter", "preference_item"]
        assert filter.dataSet["filter"].value == "processIdentifier:filter OR processIdentifier:preference_item"
        assert filter.dataSet["filter_type"].value == "Case"
    }


    @Test
    void testChangeFilterAndMenu() {
        Case caze = createMenuItem()
        def newUri = uriService.getOrCreate("netgrif/test_new", UriContentType.DEFAULT)
        Thread.sleep(5000)
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

    Case createMenuItem() {
        Case caze = getCase()
        caze = setData(caze, [
                "uri": "netgrif/test",
                "title": "FILTER",
                "allowed_nets": "filter,preference_item",
                "query": "processIdentifier:filter OR processIdentifier:preference_item",
                "type": "Case",
                "group": null,
                "identifier": "new_menu_item",
                "icon": "device_hub",
                "create_filter_and_menu": "0"
        ])
        return caze
    }

    Case getCase() {
        return workflowService.searchOne(QCase.case$.processIdentifier.eq("netgrif/test/filter_api_test"))
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
