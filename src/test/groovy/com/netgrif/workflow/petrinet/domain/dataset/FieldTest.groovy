package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.auth.domain.repositories.UserRepository
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.ipc.TaskApiTest
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.startup.DefaultRoleRunner
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.startup.SystemUserRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class FieldTest {

    public static final String LIMITS_NET_FILE = "data_test.xml"
    public static final String LIMITS_NET_TITLE = "DATA TEST"
    public static final String LIMITS_NET_INITIALS = "DAT"

    @Autowired
    private Importer importer

    @Autowired
    private MongoTemplate template

    @Autowired
    private UserRepository userRepository

    @Autowired
    private UserProcessRoleRepository roleRepository

    @Autowired
    private SystemUserRunner systemUserRunner

    @Autowired
    private DefaultRoleRunner roleRunner

    @Autowired
    private SuperCreator superCreator

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    def limitsNetOptional
    PetriNet net

    @Test
    void testImport() {
        template.db.drop()
        userRepository.deleteAll()
        roleRepository.deleteAll()
        roleRunner.run()
        superCreator.run()
        systemUserRunner.run()

        limitsNetOptional = importer.importPetriNet(stream(LIMITS_NET_FILE))

        assertNet()
        assertNumberField()
        assertTextField()
        assertEnumerationField()
        assertMultichoiceField()
        assertBooleanField()
        assertDateField()
        assertFileField()
        assertUserField()
        assertDateTimeField()
    }

    private void assertNet() {
        assert limitsNetOptional.isPresent()
        net = limitsNetOptional.get()
        assert net.dataSet.size() == 10
    }

    private void assertNumberField() {
        NumberField field = net.dataSet["number"] as NumberField
        assert field.defaultValue == 10.0
        assert field.description.defaultValue == "Number field description"
        assert field.name.defaultValue == "Number"
        assert field.placeholder.defaultValue == "Number field placeholder"
        assert field.validations.get(0).validationRule == "inrange 0,inf"
        assert field.validations.get(1).validationMessage.defaultValue == "Number field validation message"
        assert field.validations.get(1).validationRule == "inrange 0,inf"
    }

    private void assertTextField() {
        TextField field = net.dataSet["text"] as TextField
        assert field.defaultValue == "text"
        assert field.description.defaultValue == "Text field description"
        assert field.name.defaultValue == "Text"
        assert field.placeholder.defaultValue == "Text field placeholder"
        assert field.validations.get(0).validationRule == "email"
        assert field.validations.get(1).validationMessage.defaultValue == "Mail validation message"
        assert field.validations.get(1).validationRule == "email"
    }

    private void assertEnumerationField() {
        EnumerationField field = net.dataSet["enumeration"] as EnumerationField
        assert field.defaultValue == "enumeration"
        assert field.description.defaultValue == "Enumeration field description"
        assert field.name.defaultValue == "Enumeration"
        assert field.placeholder.defaultValue == "Enumeration field placeholder"
        assert field.choices.size() == 3
        assert field.choices.find { it.defaultValue == "enumeration" }
        assert field.choices.find { it.defaultValue == "enumeration2" }
        assert field.choices.find { it.defaultValue == "enumeration3" }
    }

    private void assertMultichoiceField() {
        MultichoiceField field = net.dataSet["multichoice"] as MultichoiceField
        assert field.defaultValue.size() == 2
        assert field.defaultValue.find { it.defaultValue == "multichoice" }
        assert field.defaultValue.find { it.defaultValue == "multichoice2" }
        assert field.description.defaultValue == "Multichoice field description"
        assert field.name.defaultValue == "Multichoice"
        assert field.placeholder.defaultValue == "Multichoice field placeholder"
        assert field.choices.size() == 3
        assert field.choices.find { it.defaultValue == "multichoice" }
        assert field.choices.find { it.defaultValue == "multichoice2" }
        assert field.choices.find { it.defaultValue == "multichoice3" }
    }

    private void assertBooleanField() {
        BooleanField field = net.dataSet["boolean"] as BooleanField
        assert field.defaultValue == true
        assert field.description.defaultValue == "Boolean field description"
        assert field.name.defaultValue == "Boolean"
        assert field.placeholder.defaultValue == "Boolean field placeholder"
    }

    private void assertDateField() {
        DateField field = net.dataSet["date"] as DateField
        assert field.description.defaultValue == "Date field description"
        assert field.name.defaultValue == "Date"
        assert field.placeholder.defaultValue == "Date field placeholder"
        assert field.validations.get(0).validationRule == "between today,future"
        assert field.validations.get(1).validationMessage.defaultValue == "Date field validation message"
        assert field.validations.get(1).validationRule == "between today,future"
        assert field.validations.get(2).validationMessage.defaultValue == "Date field validation message 2"
        assert field.validations.get(2).validationRule == "between today,tommorow"
    }

    private void assertFileField() {
        FileField field = net.dataSet["file"] as FileField
        assert field.description.defaultValue == "File field description"
        assert field.name.defaultValue == "File"
        assert field.placeholder.defaultValue == "File field placeholder"
    }

    private void assertUserField() {
        UserField field = net.dataSet["user"] as UserField
        assert field.description.defaultValue == "User field description"
        assert field.name.defaultValue == "User"
        assert field.placeholder.defaultValue == "User field placeholder"
    }

    private void assertDateTimeField() {
        DateTimeField field = net.dataSet["dateTime"] as DateTimeField
        assert field.description.defaultValue == "DateTime field description"
        assert field.name.defaultValue == "DateTime"
        assert field.placeholder.defaultValue == "DateTime field placeholder"
    }

    private void assertCaseRef() {
        CaseField field = net.dataSet["caseRef"] as CaseField
        assert field.name.defaultValue == "CaseRef"
        assert field.allowedNets.size() == 2
        assert field.allowedNets.containsAll(["processId1", "processId2"])
    }
}