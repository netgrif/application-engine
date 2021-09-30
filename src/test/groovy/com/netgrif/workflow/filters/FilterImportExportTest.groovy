package com.netgrif.workflow.filters

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.auth.domain.UserState
import com.netgrif.workflow.orgstructure.domain.Group
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.dataset.FileFieldValue
import com.netgrif.workflow.startup.DefaultFiltersRunner
import com.netgrif.workflow.startup.FilterRunner
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.domain.*
import com.netgrif.workflow.workflow.service.UserFilterSearchService
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.IFilterImportExportService
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator
import java.util.stream.Collectors

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class FilterImportExportTest {

    public static final String DUMMY_USER_MAIL = "dummy@netgrif.com"
    public static final String DUMMY_USER_PASSWORD = "password"

    private static final String EXPORT_NET_IDENTIFIER = "export_filters"
    private static final String IMPORT_NET_IDENTIFIER = "import_filters"

    private static final int DEFAULT_FILTERS_SIZE = 5
    private static final String[] FILTERS_TO_EXPORT = ["My cases", "My tasks", "Test filter"]
    private static final String[] FILTERS_TO_EXPORT_NEW = ["My cases new", "My tasks new", "Test filter new"]

    private static final String FILTER_VISIBILITY_PUBLIC = "public"
    private static final String FILTER_VISIBILITY_PRIVATE = "private"

    private static final String GERMAN_ISO_3166_CODE = "de"
    private static final String SLOVAK_ISO_3166_CODE = "sk"

    private static final String UPLOAD_FILE_FIELD = "upload_file"
    private static final String NEW_TITLE_FIELD = "new_title"
    private static final String VISIBILITY_FIELD = "visibility"
    private static final String IMPORTED_FILTERS_FIELD = "imported_filters"
    private static final String FILTER_FIELD = "filter"

    @Autowired
    IFilterImportExportService importExportService

    @Autowired
    FilterRunner filterRunner

    @Autowired
    TestHelper testHelper

    @Autowired
    IWorkflowService workflowService

    @Autowired
    ImportHelper importHelper

    @Autowired
    DefaultFiltersRunner defaultFiltersRunner

    @Autowired
    UserFilterSearchService userFilterSearchService

    @Autowired
    ITaskService taskService

    @Autowired
    private IDataService dataService

    private Authentication userAuth

    @Test
    void createImportExportFiltersNet() {
        this.testHelper.truncateDbs()
        this.defaultFiltersRunner.run()
        createTestFilter()
        User dummyUser = createDummyUser()
        userAuth = new UsernamePasswordAuthenticationToken(dummyUser.transformToLoggedUser(), DUMMY_USER_PASSWORD)
        SecurityContextHolder.getContext().setAuthentication(userAuth)

        Optional<PetriNet> importNet = this.filterRunner.createImportFiltersNet()
        Optional<PetriNet> exportNet = this.filterRunner.createExportFiltersNet()
        assert importNet.isPresent()
        assert exportNet.isPresent()

        Case importCase = this.workflowService.searchOne(
                QCase.case$.processIdentifier.eq(IMPORT_NET_IDENTIFIER).and(QCase.case$.author.email.eq(DUMMY_USER_MAIL))
        )
        Case exportCase = this.workflowService.searchOne(
                QCase.case$.processIdentifier.eq(EXPORT_NET_IDENTIFIER).and(QCase.case$.author.email.eq(DUMMY_USER_MAIL))
        )
        assert importCase != null
        assert exportCase != null

        List<Case> filterCases = this.userFilterSearchService.autocompleteFindFilters("")
        assert filterCases.size() == DEFAULT_FILTERS_SIZE

        Set<String> exportFiltersIds = filterCases.stream()
                .filter({ filterCase -> filterCase.title in FILTERS_TO_EXPORT })
                .map({filterCase -> filterCase.stringId})
                .collect(Collectors.toSet())
        FileFieldValue exportedFiltersField = this.importExportService.exportFilters(exportFiltersIds)
        File exportedFiltersFile = new File(exportedFiltersField.getPath())
        assert exportedFiltersFile.exists()

        importCase.dataSet.get(UPLOAD_FILE_FIELD).value = exportedFiltersField
        this.workflowService.save(importCase)
        List<String> importedTasksIds = this.importExportService.importFilters()
        assert importedTasksIds.size() == FILTERS_TO_EXPORT.size()

        validateFilterXML(new FileInputStream(exportedFiltersField.getPath()))
        importedTasksIds.forEach({taskId ->
            Task filterTask = this.taskService.findOne(taskId)
            this.dataService.setData(filterTask, ImportHelper.populateDataset([
                    (VISIBILITY_FIELD): [
                            "type": "enumeration_map",
                            "value": FILTER_VISIBILITY_PRIVATE
                    ],
                    (NEW_TITLE_FIELD): [
                            "type": "text",
                            "value": this.workflowService.findOne(filterTask.caseId).title + " new"
                    ]
            ]))
        })
        Task importTask = this.taskService.searchOne(QTask.task.caseId.eq(importCase.stringId).and(QTask.task.transitionId.eq("importFilter")))
        this.dataService.setData(importTask, ImportHelper.populateDataset([
                (IMPORTED_FILTERS_FIELD): [
                        "type": "taskRef",
                        "value": importedTasksIds
                ]
        ]))
        this.taskService.finishTask(importTask, dummyUser)
        Thread.sleep(1000)
        filterCases = this.userFilterSearchService.autocompleteFindFilters("")
        List<String> filterCasesNames = filterCases.stream().map({filterCase -> filterCase.title}).collect(Collectors.toList())
        assert filterCases.size() == DEFAULT_FILTERS_SIZE + FILTERS_TO_EXPORT.size()

        for (String filterName : FILTERS_TO_EXPORT_NEW) {
            assert filterName in filterCasesNames
        }
        for (Case filterCase : filterCases) {
            if (filterCase.title in FILTERS_TO_EXPORT_NEW) {
                assert filterCase.dataSet.get(VISIBILITY_FIELD).value == FILTER_VISIBILITY_PRIVATE
            }
        }
        for (int i = 0; i < FILTERS_TO_EXPORT.size(); i++) {
            Case filterCase1 = filterCases.get(filterCasesNames.indexOf(FILTERS_TO_EXPORT[i]))
            DataField filterField1 = filterCase1.dataSet.get(FILTER_FIELD)
            Case filterCase2 = filterCases.get(filterCasesNames.indexOf(FILTERS_TO_EXPORT_NEW[i]))
            DataField filterField2 = filterCase2.dataSet.get(FILTER_FIELD)
            assert filterCase1.icon == filterCase2.icon
            assert filterField1.value == filterField2.value
            assert filterField1.allowedNets == filterField2.allowedNets
            assert filterField1.filterMetadata == filterField2.filterMetadata
        }
    }

    User createDummyUser() {
        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        return importHelper.createUser(new User(name: "Dummy", surname: "User", email: DUMMY_USER_MAIL, password: DUMMY_USER_PASSWORD, state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as Group[],
                [] as UserProcessRole[])
    }

    private static void validateFilterXML(InputStream xml) throws IllegalFilterFileException {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
            Schema schema = factory.newSchema(FilterImportExportTest.class.getResource("/petriNets/filter_export_schema.xsd"))
            Validator validator = schema.newValidator()
            validator.validate(new StreamSource(xml))
        } catch (Exception ex) {
            throw new IllegalFilterFileException()
        }
    }

    void createTestFilter() {
        defaultFiltersRunner.createCaseFilter("Test filter", "filter_alt", "", FILTER_VISIBILITY_PUBLIC,
                "((((dataSet.number.numberValue:5) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.number.numberValue:[10 TO 100]) AND " +
                        "(processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.text.fulltextValue:*asdad*) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) " +
                        "OR ((dataSet.enumeration.fulltextValue:*asdasd*) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.enumeration_map.fulltextValue:*asdasd*) " +
                        "AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.multichoice.fulltextValue:*asdasd*) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) " +
                        "OR ((dataSet.boolean.booleanValue:true) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.boolean.booleanValue:false) AND " +
                        "(processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.date.timestampValue:[1631138400000 TO 1631224800000}) AND " +
                        "(processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.date.timestampValue:[1631138400000 TO 1631311200000}) AND " +
                        "(processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.file.fileNameValue:*asdasd*) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) OR " +
                        "((dataSet.fileList.fileNameValue:*asdasd*) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.user.userIdValue:<<me>>) AND " +
                        "(processIdentifier:6139e51308215f25b0a498c2_all_data)) OR ((dataSet.user.userIdValue:7) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) " +
                        "OR ((dataSet.datetime.timestampValue:[1631184300000 TO 1631184360000}) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) OR " +
                        "((dataSet.datetime.timestampValue:[1631184360000 TO 1631270820000}) AND (processIdentifier:6139e51308215f25b0a498c2_all_data))) AND (title:*asdasd*) AND " +
                        "((creationDateSortable:[1631138400000 TO 1631224800000}) OR (creationDateSortable:[1631138400000 TO 1631311200000})) AND " +
                        "((creationDateSortable:[1631184360000 TO 1631184420000}) OR (creationDateSortable:[1631184360000 TO 1631270820000})) AND " +
                        "(processIdentifier:6139e51308215f25b0a498c2_all_data) AND ((taskIds:1) AND (processIdentifier:6139e51308215f25b0a498c2_all_data)) AND " +
                        "((author:<<me>>) OR (!(author:7))) AND (visualId:*asdad*) AND (stringId:*asdasd*))", ["all_data", "test_net"],
                ["predicateMetadata": [
                        [[
                                 "category": "case_dataset",
                                 "configuration": [
                                         "operator": "equals",
                                         "datafield": "number#Number"
                                 ],
                                 "values": [5]
                         ], [
                                 "category": "case_dataset",
                                 "configuration": [
                                         "operator": "in_range",
                                         "datafield": "number#Number"
                                 ],
                                 "values": [10, 100]
                         ], [
                                 "category": "case_dataset",
                                 "configuration": [
                                         "operator": "substring",
                                         "datafield": "text#Text"
                                 ],
                                 "values": ["asdad"]
                         ], [
                                 "category": "case_dataset",
                                 "configuration": [
                                         "operator": "substring",
                                         "datafield": "enumeration#Enumeration"
                                 ],
                                 "values": ["asdasd"]
                         ], [
                                 "category": "case_dataset",
                                 "configuration": [
                                         "operator": "substring",
                                         "datafield": "enumeration_map#Enumeration Map"
                                 ],
                                 "values": ["asdasd"]
                         ], [
                                 "category": "case_dataset",
                                 "configuration": [
                                         "operator": "substring",
                                         "datafield": "multichoice#Multichoice"
                                 ],
                                 "values": ["asdasd"]
                         ], [
                                 "category": "case_dataset",
                                 "configuration": [
                                         "operator": "equals",
                                         "datafield": "boolean#Boolean"
                                 ],
                                 "values": [true]
                         ], [
                                 "category": "case_dataset",
                                 "configuration": [
                                         "operator": "equals",
                                         "datafield": "boolean#Boolean"
                                 ],
                                 "values": [false]
                         ], [
                                 "category": "case_dataset",
                                 "configuration": [
                                         "operator": "equals_date",
                                         "datafield": "date#Date"
                                 ],
                                 "values": [1631138400000]
                         ], [
                                 "category": "case_dataset",
                                 "configuration": [
                                         "operator": "in_range_date",
                                         "datafield": "date#Date"
                                 ],
                                 "values": [1631138400000, 1631224800000]
                         ], [
                                 "category": "case_dataset",
                                 "configuration": [
                                         "operator": "substring",
                                         "datafield": "file#File"
                                 ],
                                 "values": ["asdasd"]
                         ], [
                                 "category": "case_dataset",
                                 "configuration": [
                                         "operator": "substring",
                                         "datafield": "fileList#File List"
                                 ],
                                 "values": ["asdasd"]
                         ], [
                                 "category": "case_dataset",
                                 "configuration": [
                                         "operator": "equals",
                                         "datafield": "user#User"
                                 ],
                                 "values": [[
                                                    "text": "search.category.userMe",
                                                    "value": ["<<me>>"]
                                            ]]
                         ], [
                                 "category": "case_dataset",
                                 "configuration": [
                                         "operator": "equals",
                                         "datafield": "user#User"
                                 ],
                                 "values": [[
                                                    "text": "Admin Netgrif",
                                                    "value": [7]
                                            ]]
                         ], [
                                 "category": "case_dataset",
                                 "configuration": [
                                         "operator": "equals_date_time",
                                         "datafield": "dateTime#Datetime"
                                 ],
                                 "values": [1631184356623]
                         ], [
                                 "category": "case_dataset",
                                 "configuration": [
                                         "operator": "in_range_date_time",
                                         "datafield": "dateTime#Datetime"
                                 ],
                                 "values": [1631184364266, 1631270767000]
                         ]],
                        [[
                                 "category": "case_title",
                                 "configuration": [
                                         "operator": "substring"
                                 ],
                                 "values": ["asdasd"]
                         ]],
                        [[
                                 "category": "case_creation_date",
                                 "configuration": [
                                         "operator": "equals_date"
                                 ],
                                 "values": [1631138400000]
                         ], [
                                 "category": "case_creation_date",
                                 "configuration": [
                                         "operator": "in_range_date"
                                 ],
                                 "values": [1631138400000, 1631224800000]
                         ]],
                        [[
                                 "category": "case_creation_date_time",
                                 "configuration": [
                                         "operator": "equals_date_time"
                                 ],
                                 "values": [1631184402526]
                         ], [
                                 "category": "case_creation_date_time",
                                 "configuration": [
                                         "operator": "in_range_date_time"
                                 ],
                                 "values": [1631184408995, 1631270810000]
                         ]],
                        [[
                                 "category": "case_process",
                                 "configuration": [
                                         "operator": "equals"
                                 ],
                                 "values": ["All Data"]
                         ]],
                        [[
                                 "category": "case_task",
                                 "configuration": [
                                         "operator": "equals"
                                 ],
                                 "values": ["Task - editable"]
                         ]],
                        [[
                                 "category": "case_author",
                                 "configuration": [
                                         "operator": "equals"
                                 ],
                                 "values": [[
                                                    "text": "search.category.userMe",
                                                    "value": ["<<me>>"]
                                            ]]
                         ], [
                                 "category": "case_author",
                                 "configuration": [
                                         "operator": "not_equals"
                                 ],
                                 "values": [[
                                                    "text": "Admin Netgrif",
                                                    "value": [7]
                                            ]]
                         ]],
                        [[
                                 "category": "case_visual_id",
                                 "configuration": [
                                         "operator": "substring"
                                 ],
                                 "values": ["asdad"]
                         ]],
                        [[
                                 "category": "case_string_id",
                                 "configuration": [
                                         "operator": "substring"
                                 ],
                                 "values": ["asdasd"]
                         ]]
                ],
                 "searchCategories": ["case_dataset", "case_title", "case_creation_date", "case_creation_date_time", "case_process", "case_task", "case_author", "case_visual_id", "case_string_id"]],
                [
                        (GERMAN_ISO_3166_CODE): "Dig",
                        (SLOVAK_ISO_3166_CODE): "Dilino"
                ]
        )
    }

}
