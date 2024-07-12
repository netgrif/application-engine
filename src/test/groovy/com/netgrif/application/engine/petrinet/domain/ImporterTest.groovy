package com.netgrif.application.engine.petrinet.domain

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.petrinet.domain.dataset.ChoiceField
import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceField
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRoleRepository
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.application.engine.petrinet.domain.version.Version
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.DataFieldBehavior
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import groovy.transform.CompileStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior.*
import static org.junit.jupiter.api.Assertions.assertThrows

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@CompileStatic
class ImporterTest {

    @Autowired
    private Importer importer
    @Autowired
    private IPetriNetService petriNetService
    @Autowired
    private SuperCreator superCreator
    @Autowired
    private ProcessRoleRepository processRoleRepository
    @Autowired
    private TestHelper testHelper
    @Autowired
    private ImportHelper importHelper
    @Autowired
    private IWorkflowService workflowService
    @Autowired
    private ITaskService taskService

    @Value("classpath:net_import_1.xml")
    private Resource firstVersionResource
    @Value("classpath:net_import_2.xml")
    private Resource secondVersionResource

    public static final String FILE_NAME = "importer_upsert.xml"
    public static final String IDENTIFIER = "importer_upsert"

    private static final String ENUMERATION_LIKE_MAP_FIELD = "enumeration_like_map"
    private static final String MULTICHOICE_LIKE_MAP_FIELD = "multichoice_like_map"
    private static final String ENUMERATION_FIELD = "enumeration"
    private static final String MULTICHOICE_FIELD = "multichoice"

    private static final String NUMBER_FIELD = "number"
    private static final String TEXT_FIELD = "text"
    private static final String ENUMERATION_MAP_FIELD = "enumeration_map"
    private static final String MULTICHOICE_MAP_FIELD = "multichoice_map"
    private static final String BOOLEAN_FIELD = "boolean"
    private static final String DATE_FIELD = "date"
    private static final String FILE_FIELD = "file"
    private static final String FILE_LIST_FIELD = "fileList"
    private static final String USER_FIELD = "user"
    private static final String DATETIME_FIELD = "datetime"
    private static final String BUTTON_FIELD = "button"
    private static final String I18N_FIELD = "i18n"

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void importTest() {
        long beforeImportNet = processRoleRepository.count()
        def netOptional = petriNetService.importPetriNet(
                firstVersionResource.inputStream,
                VersionType.MAJOR,
                superCreator.loggedSuper
        )
        assert netOptional.getNet() != null
        assert processRoleRepository.count() == beforeImportNet + 2
        long statusImportRole = processRoleRepository.count()
        def net = netOptional.getNet()

        // ASSERT IMPORTED NET
        assert net.importId == "new_model"
        assert net.version.major == 1
        assert net.version.minor == 0
        assert net.version.patch == 0
        assert net.initials == "NEW"
        assert net.title.defaultValue == "New Model"
        assert net.icon == "home"
        assert net.roles.size() == 2
        2.times {
            assert net.roles.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("newRole_${it + 1}" as String)
            assert net.roles.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].name.defaultValue == ("newRole_${it + 1}" as String)
        }
        assert net.dataSet.size() == 5
        5.times {
            assert net.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("newVariable_${it + 1}" as String)
            assert net.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].name.defaultValue == ("newVariable_${it + 1}" as String)
        }
        assert net.transitions.size() == 3
        2.times {
            net.transitions.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("task${it + 1}" as String)
            net.transitions.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].title.defaultValue == ("task${it + 1}" as String)
        }
        assert net.places.size() == 0

        // ASSERT IMPORTED NET FROM REPO
        net = petriNetService.getNewestVersionByIdentifier("new_model")
        assert net != null
        assert net.importId == "new_model"
        assert net.version.major == 1
        assert net.version.minor == 0
        assert net.version.patch == 0
        assert net.initials == "NEW"
        assert net.title.defaultValue == "New Model"
        assert net.icon == "home"
        assert net.roles.size() == 2
        2.times {
            assert net.roles.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("newRole_${it + 1}" as String)
            assert net.roles.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].name.defaultValue == ("newRole_${it + 1}" as String)
        }
        assert net.dataSet.size() == 5
        5.times {
            assert net.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("newVariable_${it + 1}" as String)
            assert net.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].name.defaultValue == ("newVariable_${it + 1}" as String)
        }
        assert net.transitions.size() == 3
        2.times {
            net.transitions.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("task${it + 1}" as String)
            net.transitions.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].title.defaultValue == ("task${it + 1}" as String)
        }
        assert net.places.size() == 0

        def netOptional2 = petriNetService.importPetriNet(
                secondVersionResource.inputStream,
                VersionType.MAJOR,
                superCreator.loggedSuper
        )

        assert processRoleRepository.count() == statusImportRole + 1
        assert netOptional2.getNet() != null
        def net2 = netOptional2.getNet()

        // ASSERT NEW IMPORTED NET
        assert net2.importId == "new_model"
        assert net2.version.major == 2
        assert net2.version.minor == 0
        assert net2.version.patch == 0
        assert net2.initials == "NEW"
        assert net2.title.defaultValue == "New Model2"
        assert net2.icon == "home2"
        assert net2.roles.size() == 1
        assert net2.roles.values()[0].importId == "newRole_3"
        assert net2.roles.values()[0].name.defaultValue == "newRole_3"
        assert net2.dataSet.size() == 2
        2.times {
            assert net2.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("newVariable_${it + 6}" as String)
            assert net2.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].name.defaultValue == ("newVariable_${it + 6}" as String)
        }
        assert net2.transitions.size() == 2
        net2.transitions.values()[0].importId == "task3"
        net2.transitions.values()[0].title.defaultValue == "task3"
        assert net2.places.size() == 0

        // ASSERT NEW NET FROM REPO
        net2 = petriNetService.getNewestVersionByIdentifier("new_model")
        assert net2 != null
        assert net2.importId == "new_model"
        assert net2.version.major == 2
        assert net2.version.minor == 0
        assert net2.version.patch == 0
        assert net2.initials == "NEW"
        assert net2.title.defaultValue == "New Model2"
        assert net2.icon == "home2"
        assert net2.roles.size() == 1
        assert net2.roles.values()[0].importId == "newRole_3"
        assert net2.roles.values()[0].name.defaultValue == "newRole_3"
        assert net2.dataSet.size() == 2
        2.times {
            assert net2.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("newVariable_${it + 6}" as String)
            assert net2.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].name.defaultValue == ("newVariable_${it + 6}" as String)
        }
        assert net2.transitions.size() == 2
        net2.transitions.values()[0].importId == "task3"
        net2.transitions.values()[0].title.defaultValue == "task3"
        assert net2.places.size() == 0

        // ASSERT OLD NET FROM REPO
        net = petriNetService.getPetriNet(net.stringId)
        assert net != null
        assert net.importId == "new_model"
        assert net.version.major == 1
        assert net.version.minor == 0
        assert net.version.patch == 0
        assert net.initials == "NEW"
        assert net.title.defaultValue == "New Model"
        assert net.icon == "home"
        assert net.roles.size() == 2
        2.times {
            assert net.roles.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("newRole_${it + 1}" as String)
            assert net.roles.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].name.defaultValue == ("newRole_${it + 1}" as String)
        }
        assert net.dataSet.size() == 5
        5.times {
            assert net.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("newVariable_${it + 1}" as String)
            assert net.dataSet.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].name.defaultValue == ("newVariable_${it + 1}" as String)
        }
        assert net.transitions.size() == 3
        2.times {
            net.transitions.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].importId == ("task${it + 1}" as String)
            net.transitions.values().toSorted({ a, b ->
                return a.importId <=> b.importId
            })[it].title.defaultValue == ("task${it + 1}" as String)
        }
        assert net.places.size() == 0
    }

    @Test
    void upsertTest() {
        Optional<PetriNet> netOptional = importHelper.upsertNet(FILE_NAME, IDENTIFIER)
        assert netOptional.present
        PetriNet net = petriNetService.getNewestVersionByIdentifier(IDENTIFIER)

        def upserted = importHelper.upsertNet(FILE_NAME, IDENTIFIER)
        assert upserted.present

        // MongoDB does not store creationDate with the same precision as was created, net needs to be reloaded for
        // comparison to be true
        assert upserted.get().creationDate == net.creationDate
    }

    @Test
    void thisKeywordInDataEventsTest() {
        PetriNet net = petriNetService.importPetriNet(new ClassPathResource("/this_kw_test.xml").getInputStream(), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()

        assert net != null
        Case testCase = workflowService.createCase(net.stringId, "Test case", "", superCreator.loggedSuper).getCase()
        taskService.assignTask(testCase.getTaskStringId("t1"))
        testCase = workflowService.findOne(testCase.getStringId())
        assert testCase.getDataSet().get("text_field").getRawValue() == "Hello world!"
        assert testCase.getDataSet().get("tester_text_field").getRawValue() == "Hello world!"
    }

    @Test
    void initialBehaviorTest() {
        PetriNet net = petriNetService.importPetriNet(new ClassPathResource("/initial_behavior.xml").getInputStream(), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()

        assert net
        Case testCase = workflowService.createCase(net.stringId, "Test case", "", superCreator.loggedSuper).getCase()

        assertBehaviors(testCase.dataSet.get(NUMBER_FIELD).behaviors.get("1"), FORBIDDEN)
        assertBehaviors(testCase.dataSet.get(TEXT_FIELD).behaviors.get("1"), HIDDEN)
        assertBehaviors(testCase.dataSet.get(ENUMERATION_FIELD).behaviors.get("1"), VISIBLE)
        assertBehaviors(testCase.dataSet.get(ENUMERATION_MAP_FIELD).behaviors.get("1"), EDITABLE)
        assertBehaviors(testCase.dataSet.get(MULTICHOICE_FIELD).behaviors.get("1"), EDITABLE, true)
        assertBehaviors(testCase.dataSet.get(MULTICHOICE_MAP_FIELD).behaviors.get("1"), EDITABLE, false, true)
        assertBehaviors(testCase.dataSet.get(BOOLEAN_FIELD).behaviors.get("1"), EDITABLE)
        assertBehaviors(testCase.dataSet.get(DATE_FIELD).behaviors.get("1"), EDITABLE, true)
        assertBehaviors(testCase.dataSet.get(DATETIME_FIELD).behaviors.get("1"), EDITABLE, true, true)
        assertBehaviors(testCase.dataSet.get(FILE_FIELD).behaviors.get("1"), FORBIDDEN, false, true)
        assertBehaviors(testCase.dataSet.get(FILE_LIST_FIELD).behaviors.get("1"), HIDDEN)
        assertBehaviors(testCase.dataSet.get(USER_FIELD).behaviors.get("1"), HIDDEN, false, true)
        assertBehaviors(testCase.dataSet.get(BUTTON_FIELD).behaviors.get("1"), EDITABLE, true, true)
        assertBehaviors(testCase.dataSet.get(I18N_FIELD).behaviors.get("1"), HIDDEN, false, true)
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    private void assertBehaviors(DataFieldBehavior dataFieldBehavior, FieldBehavior behavior, boolean required = false, boolean immediate = false) {
        assert dataFieldBehavior.behavior == behavior
        assert dataFieldBehavior.required == required
        assert dataFieldBehavior.immediate == immediate
    }

    @Test
    void enumerationMultichoiceOptionsTest() throws IOException, MissingPetriNetMetaDataException {
        PetriNet net = petriNetService.importPetriNet(new ClassPathResource("/enumeration_multichoice_options.xml").getInputStream(), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()

        assert net != null

        MultichoiceField multichoice = (MultichoiceField) net.getDataSet().get(MULTICHOICE_FIELD)
        MultichoiceField multichoice_like_map = (MultichoiceField) net.getDataSet().get(MULTICHOICE_LIKE_MAP_FIELD)
        ChoiceField enumeration = (ChoiceField) net.getDataSet().get(ENUMERATION_FIELD)
        ChoiceField enumeration_like_map = (ChoiceField) net.getDataSet().get(ENUMERATION_LIKE_MAP_FIELD)

        assert equalSet(multichoice.getChoices(), multichoice_like_map.getChoices())
        assert equalSet(enumeration.getChoices(), enumeration_like_map.getChoices())

        assert multichoice.getRawValue() == multichoice_like_map.getRawValue()
        assert enumeration.getRawValue() == enumeration_like_map.getRawValue()

        assert equalSet(multichoice.getDefaultValue(), multichoice_like_map.getDefaultValue())
        assert enumeration.getDefaultValue() == enumeration_like_map.getDefaultValue()
    }

    @Test
    void testDataGroupImportWithoutId() {
        def netOutcome = petriNetService.importPetriNet(
                new FileInputStream("src/test/resources/datagroup_no_id_test.xml"),
                VersionType.MAJOR,
                superCreator.loggedSuper)

        assert netOutcome.getNet() != null

        def net = netOutcome.getNet()
        net.getTransition("test").getDataGroups().forEach((k, v) -> {
            assert v.getStringId() != null && v.getStringId().length() > 0
        })
    }

    @Test
    void createTransitionNoLabel() {
        PetriNet net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/importTest/NoLabel.xml"), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()
        assert net
        PetriNet importNet = petriNetService.findByImportId(net.getImportId()).get()
        assert importNet
        assert importNet.getTransition("1").getTitle()
        assert importNet.getTransition("layout").getTitle()
        assert importNet.getTransition("layout").getTitle().defaultValue == ""
    }

    @Test
    void importNetWithParent() {
        PetriNet superParentNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/importTest/super_parent_to_be_extended.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()
        assert superParentNet

        PetriNet parentNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/importTest/parent_to_be_extended.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()
        assert parentNet.version == new Version(1, 0, 0)

        parentNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/importTest/parent_to_be_extended.xml"),
                VersionType.MINOR, superCreator.getLoggedSuper()).getNet()
        assert parentNet.version == new Version(1, 1, 0)
        assert parentNet.parentIdentifiers.size() == 1

        PetriNet childNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/importTest/child_extending_parent.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()
        assert childNet.identifier == "child_extending_parent"
        assert childNet.title.defaultValue == "Child extending parent"
        assert childNet.initials == "CEP"
        assert childNet.creationDate != parentNet.creationDate

        assert childNet.parentIdentifiers.size() == 2
        assert childNet.parentIdentifiers.get(0).version == superParentNet.version
        assert childNet.parentIdentifiers.get(0).identifier == superParentNet.identifier
        assert childNet.parentIdentifiers.get(0).id == superParentNet.id
        assert childNet.parentIdentifiers.get(1).version == parentNet.version
        assert childNet.parentIdentifiers.get(1).identifier == parentNet.identifier
        assert childNet.parentIdentifiers.get(1).id == parentNet.id

        assert childNet.places.size() == 3
        assert childNet.places.containsKey("p0")
        assert childNet.places.containsKey("p1")
        assert childNet.places.containsKey("p2")

        assert childNet.transitions.size() == 4
        assert childNet.transitions.containsKey("t0")
        assert childNet.transitions.containsKey("t1")
        assert childNet.transitions.containsKey("t2")
        assert childNet.transitions.get("t2").dataSet.containsKey("variable1")
        assert childNet.transitions.get("t2").dataSet.get("variable1").field != null

        assert childNet.arcs.size() == 3
        assert childNet.arcs.containsKey("t0")
        assert childNet.arcs.get("t0").size() == 2
        assert childNet.arcs.containsKey("t1")
        assert childNet.arcs.get("t1").size() == 2
        assert childNet.arcs.containsKey("t2")
        assert childNet.arcs.get("t2").size() == 1

        assert childNet.dataSet.containsKey("variable0")
        assert childNet.dataSet.containsKey("variable1")
        assert childNet.dataSet.containsKey("variable2")
        assert childNet.dataSet.containsKey("taskref2")
        assert childNet.dataSet.get("taskref2").defaultValue == ["t0"]

        assert childNet.roles.size() == 3
        assert childNet.roles.values().find { processRole ->
            processRole.importId == superParentNet.roles.values().first().importId
        } != null

        assert childNet.tags.size() == 3
        assert childNet.tags.containsKey("tag0")
        assert childNet.tags.containsKey("tag1")
        assert childNet.tags.containsKey("tag2")
    }

    @Test
    void importNetWithNonExistingParent() {
        assertThrows(IllegalArgumentException.class, () -> {
            petriNetService.importPetriNet(new FileInputStream("src/test/resources/importTest/extending_non_existing_parent.xml"),
                    VersionType.MAJOR, superCreator.getLoggedSuper())
        })
    }

    @Test
    void importNetsWithInvalidExtension() {
        PetriNet superParentNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/importTest/super_parent_to_be_extended.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()
        assert superParentNet

        PetriNet parentNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/importTest/parent_to_be_extended.xml"),
                VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()
        assert parentNet

        assertThrows(IllegalArgumentException.class, () -> {
            petriNetService.importPetriNet(new FileInputStream("src/test/resources/importTest/extending_with_invalid_extension_1.xml"),
                    VersionType.MAJOR, superCreator.getLoggedSuper())
        })

        assertThrows(IllegalArgumentException.class, () -> {
            petriNetService.importPetriNet(new FileInputStream("src/test/resources/importTest/extending_with_invalid_extension_2.xml"),
                    VersionType.MAJOR, superCreator.getLoggedSuper())
        })

        assertThrows(IllegalArgumentException.class, () -> {
            petriNetService.importPetriNet(new FileInputStream("src/test/resources/importTest/extending_with_invalid_extension_3.xml"),
                    VersionType.MAJOR, superCreator.getLoggedSuper())
        })
    }

    private static boolean equalSet(Set<I18nString> first, Set<I18nString> second) {
        return first.every {
            second.any { that -> it.defaultValue == that.defaultValue }
        }
    }
}
