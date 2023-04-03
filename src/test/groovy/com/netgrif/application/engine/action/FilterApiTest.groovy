package com.netgrif.application.engine.action

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.application.engine.petrinet.domain.UriContentType
import com.netgrif.application.engine.petrinet.domain.dataset.*
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService
import com.netgrif.application.engine.startup.FilterRunner
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import groovy.transform.CompileStatic
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
@CompileStatic
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

    @Autowired
    private SuperCreator superCreator

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
        assert item.dataSet.get("icon_name").rawValue == "device_hub"
        assert item.dataSet.get("entry_name").rawValue.toString() == "FILTER"
        assert item.dataSet.get("menu_item_identifier").rawValue.toString() == "new_menu_item"
        assert item.dataSet.get("parentId").rawValue.toString() == defGroup.stringId

        assert ((FilterField) filter.dataSet.get("filter")).filterMetadata["filterType"] == "Case"
        assert ((FilterField) filter.dataSet.get("filter")).rawValue == "processIdentifier:filter OR processIdentifier:preference_filter_item"
        assert ((FilterField) filter.dataSet.get("filter")).allowedNets == ["filter", "preference_filter_item"]
        assert filter.dataSet.get("filter_type").rawValue == "Case"

        List<String> taskIds = (defGroup.dataSet.get("filter_tasks").rawValue ?: []) as List
        assert taskIds.contains(item.getTaskStringId("view"))
    }

    @Test
    @Disabled("Fix NullPointer")
    void testChangeFilterAndMenu() {
        Case caze = createMenuItem()
        def newUri = uriService.getOrCreate("netgrif/test_new", UriContentType.DEFAULT)
        DataSet dataSet = new DataSet([
                "uri"                   : new TextField(rawValue: newUri.uriPath),
                "title"                 : "CHANGED FILTER",
                "allowed_nets"          : new TextField(rawValue: "filter"),
                "query"                 : new TextField(rawValue: "processIdentifier:filter"),
                "type"                  : new TextField(rawValue: "Case"),
                "icon"                  : new TextField(rawValue: ""),
                "create_filter_and_menu": new ButtonField(rawValue: 0)
        ] as Map<String, Field<?>>)
        dataService.setData(caze.getTaskStringId("t1"), dataSet, superCreator.getLoggedSuper())
        caze = workflowService.findOne(caze.stringId)
        Case item = getMenuItem(caze)
        Case filter = getFilter(caze)

        assert item.dataSet.get("icon_name").rawValue == ""
        assert item.dataSet.get("entry_name").value.toString() == "CHANGED FILTER"
        assert ((MultichoiceMapField) item.dataSet.get("allowed_roles")).options.entrySet()[0].key.contains("role_2")
        assert item.uriNodeId == newUri.id

        assert ((FilterField) filter.dataSet.get("filter")).allowedNets == ["filter"]
        assert ((FilterField) filter.dataSet.get("filter")).filterMetadata["defaultSearchCategories"] == false
        assert ((FilterField) filter.dataSet.get("filter")).rawValue == "processIdentifier:filter"
    }

    @Test
    void testDeleteItemAndFilter() {
        Case caze = createMenuItem()

        Case item = getMenuItem(caze)
        Case filter = getFilter(caze)
        DataSet dataSet = new DataSet([
                "delete_filter_and_menu": new ButtonField(rawValue: 0)
        ] as Map<String, Field<?>>)
        dataService.setData(caze.getTaskStringId("t1"), dataSet, superCreator.getLoggedSuper())
        workflowService.findOne(caze.stringId)
        Case defGroup = nextGroupService.findDefaultGroup()
        List<String> taskIds = (defGroup.dataSet.get(ActionDelegate.ORG_GROUP_FIELD_FILTER_TASKS).value.value ?: []) as List
        assert !taskIds

        Thread.sleep(2000)

        assert workflowService.searchOne(QCase.case$.id.eq(new ObjectId(item.stringId))) == null
        assert workflowService.searchOne(QCase.case$.id.eq(new ObjectId(filter.stringId))) == null
    }


    @Test
    void testFindFilter() {
        Case caze = createMenuItem()
        Case filter = getFilter(caze)

        DataSet dataSet = new DataSet([
                "find_filter": new ButtonField(rawValue: 0)
        ] as Map<String, Field<?>>)
        dataService.setData(caze.getTaskStringId("t1"), dataSet, superCreator.getLoggedSuper())
        caze = workflowService.findOne(caze.stringId)
        assert caze.dataSet.get("found_filter").rawValue == filter.stringId
    }

    Case createMenuItem() {
        Case caze = getCase()
        DataSet dataSet = new DataSet([
                "uri"                   : new TextField(rawValue: "netgrif/test"),
                "title"                 : new TextField(rawValue: "FILTER"),
                "allowed_nets"          : new TextField(rawValue: "filter,preference_filter_item"),
                "query"                 : new TextField(rawValue: "processIdentifier:filter OR processIdentifier:preference_filter_item"),
                "group"                 : new TextField(rawValue: null),
                "identifier"            : new TextField(rawValue: "new_menu_item"),
                "icon"                  : new TextField(rawValue: "device_hub"),
                "create_filter_and_menu": new ButtonField(rawValue: 0)
        ] as Map<String, Field<?>>)
        dataService.setData(caze.getTaskStringId("t1"), dataSet, superCreator.getLoggedSuper())
        caze = workflowService.findOne(caze.stringId)
        return caze
    }

    Case getCase() {
        return workflowService.searchOne(QCase.case$.processIdentifier.eq("netgrif/test/filter_api_test"))
    }

    Case getMenuItem(Case caze) {
        return workflowService.findOne(caze.dataSet.get("menu_stringId").value as String)
    }

    Case getFilter(Case caze) {
        return workflowService.findOne(caze.dataSet.get("filter_stringId").value as String)
    }
}
