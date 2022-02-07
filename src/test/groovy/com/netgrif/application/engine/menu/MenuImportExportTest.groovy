package com.netgrif.application.engine.menu

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.UserState
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.orgstructure.groups.NextGroupService
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.dataset.FileFieldValue
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.startup.*
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome
import com.netgrif.application.engine.workflow.domain.menu.MenuAndFilters
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.service.UserFilterSearchService
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.IMenuImportExportService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension


@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class MenuImportExportTest {

    public static final String DUMMY_USER_MAIL = "dummy@netgrif.com"
    public static final String DUMMY_USER_PASSWORD = "password"
    public static final String DUMMY_USER_GROUP_TITLE = "Dummy User"

    private static final String TEST_NET = "mortgage_net.xml"
    private static final String TEST_XML_FILE_PATH = "src/test/resources/menu_file_test.xml"

    private static final String GROUP_NAV_TASK = "navigationMenuConfig"
    private static final String IMPORT_FILE_FIELD = "import_menu_file"
    private static final String EXPORT_FILE_FIELD = "export_menu_file"

    private static final String IMPORT_BUTTON_FIELD = "import_menu_btn"
    private static final String EXPORT_BUTTON_FIELD = "export_menu_btn"
    private static final String IMPORT_RESULTS_FIELD = "import_results"
    private static final String EXPORT_MENUS_FIELD = "menus_for_export"
    private static final String IMPORTED_IDS_FIELD = "imported_menu_ids"
    private static final String MENU_NAME_FIELD = "menu_identifier"

    private static final String EXPECTED_RESULTS = "\n" +
            "IMPORTING MENU \"defaultMenu\":\n" +
            "\n" +
            "Menu entry \"My cases\": OK\n" +
            "\n" +
            "Menu entry \"All cases\": OK\n" +
            "\n" +
            "IMPORTING MENU \"defaultMenu\":\n" +
            "\n" +
            "Menu entry \"All tasks\": OK\n" +
            "\n" +
            "Menu entry \"My tasks\": OK\n"

    @Autowired
    IMenuImportExportService menuImportExportService

    @Autowired
    FilterRunner filterRunner

    @Autowired
    TestHelper testHelper

    @Autowired
    private CaseRepository repository;

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

    @Autowired
    private GroupRunner groupRunner

    @Autowired
    private UserService userService

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private NextGroupService nextGroupService

    @Autowired
    private SuperCreator superCreator

    private User dummyUser;

    private Authentication userAuth

    @BeforeEach
    void beforeTest() {
        this.testHelper.truncateDbs();
        this.defaultFiltersRunner.run()
        this.dummyUser = createDummyUser();
    }


    @Test
    @Disabled("Fix IllegalArgument")
    void testMenuImportExport() {
        userAuth = new UsernamePasswordAuthenticationToken(dummyUser.transformToLoggedUser(), DUMMY_USER_PASSWORD)
        SecurityContextHolder.getContext().setAuthentication(userAuth)

        def testNet = importHelper.createNet(TEST_NET)
        assert testNet.isPresent()

        Optional<Case> caseOptional = caseRepository.findOne(QCase.case$.title.eq(DUMMY_USER_GROUP_TITLE));
        assert caseOptional.isPresent()
        Case groupCase = caseOptional.get()

        File testXmlMenu = new File(TEST_XML_FILE_PATH);

        groupCase.dataSet[IMPORT_FILE_FIELD].value = FileFieldValue.fromString(testXmlMenu.getName() + ":" + testXmlMenu.getPath())
        workflowService.save(groupCase)

        QTask qTask = new QTask("task");
        Task task = taskService.searchOne(qTask.transitionId.eq(GROUP_NAV_TASK).and(qTask.caseId.eq(groupCase.stringId)));
        dataService.setData(task, ImportHelper.populateDataset([
                (IMPORT_BUTTON_FIELD): [
                        "value": "1",
                        "type" : "button"
                ]
        ]))
        Optional<Case> caseOpt = caseRepository.findOne(QCase.case$.title.eq(DUMMY_USER_GROUP_TITLE))
        assert caseOpt.isPresent()
        groupCase = caseOpt.get()

        String importResults = groupCase.getDataField(IMPORT_RESULTS_FIELD).getValue().toString()
        assert importResults <=> EXPECTED_RESULTS

        ArrayList<String> imported_ids_list = groupCase.getDataSet().get(IMPORTED_IDS_FIELD).getValue() as ArrayList<String>
        assert imported_ids_list.size() == 4

        Map<String, I18nString> menusForExportOptions = new LinkedHashMap<>()
        String[] split1 = imported_ids_list.get(0).split(",")
        String[] split2 = imported_ids_list.get(1).split(",")
        String[] split3 = imported_ids_list.get(2).split(",")
        String[] split4 = imported_ids_list.get(3).split(",")

        String menuName1 = workflowService.findOne(split1[0]).getDataSet().get(MENU_NAME_FIELD).getValue().toString()
        String menuName2 = workflowService.findOne(split3[0]).getDataSet().get(MENU_NAME_FIELD).getValue().toString()
        assert menuName1 == "defaultMenu"
        assert menuName2 == "newMenu"

        menusForExportOptions.put(split1[0] + "," + split2[0], new I18nString(menuName1))
        menusForExportOptions.put(split3[0] + "," + split4[0], new I18nString(menuName2))

        groupCase.dataSet[EXPORT_MENUS_FIELD].setOptions(menusForExportOptions)
        workflowService.save(groupCase)

        task = taskService.searchOne(qTask.transitionId.eq(GROUP_NAV_TASK).and(qTask.caseId.eq(groupCase.stringId)));
        setData(task, [(EXPORT_BUTTON_FIELD): ["type": "button", "value": "1"]])

        caseOpt = caseRepository.findOne(QCase.case$.title.eq(DUMMY_USER_GROUP_TITLE))
        assert caseOpt.isPresent()
        groupCase = caseOpt.get()

        FileFieldValue exportFileField = groupCase.getDataField(EXPORT_FILE_FIELD).getValue() as FileFieldValue
        File exportedFiltersFile = new File(exportFileField.getPath())
        assert exportedFiltersFile.exists()

        MenuAndFilters original = menuImportExportService.invokeMethod("loadFromXML", [FileFieldValue.fromString(testXmlMenu.getName() + ":" + testXmlMenu.getPath())] as Object[]) as MenuAndFilters
        MenuAndFilters exported = menuImportExportService.invokeMethod("loadFromXML", [exportFileField] as Object[]) as MenuAndFilters

        assert Objects.equals(original, exported);
    }

    private User createDummyUser() {
        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        return importHelper.createUser(new User(name: "Dummy", surname: "User", email: DUMMY_USER_MAIL, password: DUMMY_USER_PASSWORD, state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as ProcessRole[])
    }


    private SetDataEventOutcome setData(task, Map<String, Map<String, Object>> values) {
        return dataService.setData(task, ImportHelper.populateDataset(values))
    }


}