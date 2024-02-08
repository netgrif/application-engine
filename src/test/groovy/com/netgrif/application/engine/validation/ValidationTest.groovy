package com.netgrif.application.engine.validation

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class ValidationTest {

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreator superCreator;

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
    }

    private PetriNet importTextNet() {
        PetriNet testNet = importHelper.createNet("validation/valid_text.xml", VersionType.MAJOR).get()
        assert testNet != null
        return testNet
    }

    private PetriNet importRegexNet() {
        PetriNet testNet = importHelper.createNet("validation/valid_regex.xml", VersionType.MAJOR).get()
        assert testNet != null
        return testNet
    }

    private PetriNet importBooleanNet() {
        PetriNet testNet = importHelper.createNet("validation/valid_boolean.xml", VersionType.MAJOR).get()
        assert testNet != null
        return testNet
    }

    private PetriNet importDateNet() {
        PetriNet testNet = importHelper.createNet("validation/valid_date.xml", VersionType.MAJOR).get()
        assert testNet != null
        return testNet
    }

    private PetriNet importNumberNet() {
        PetriNet testNet = importHelper.createNet("validation/valid_number.xml", VersionType.MAJOR).get()
        assert testNet != null
        return testNet
    }

    // TEXT FIELD
    @Test
    void textValid_email() {
        PetriNet testNet = importTextNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["text01": ["type": "text", "value": "test@netgrif.com"]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void textValid_email2() {
        PetriNet testNet = importTextNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["text01": ["type": "text", "value": "test@netgrif.co.com"]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void textValid_email3() {
        PetriNet testNet = importTextNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["text01": ["type": "text", "value": "te.st@netgrif.co.com"]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void textValid_email_Exception() {
        PetriNet testNet = importTextNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["text01": ["type": "text", "value": "test@@aaa.com"]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-text01", thrown.getMessage());
    }

    @Test
    void textValid_email_Exception2() {
        PetriNet testNet = importTextNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["text01": ["type": "text", "value": "test@aaa.s"]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-text01", thrown.getMessage());
    }

    @Test
    void textValid_telnumber() {
        PetriNet testNet = importTextNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["text02": ["type": "text", "value": "+421 000 000 000"]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void textValid_telnumber2() {
        PetriNet testNet = importTextNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["text02": ["type": "text", "value": "+421-000-000-000"]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void textValid_telnumber3() {
        PetriNet testNet = importTextNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["text02": ["type": "text", "value": "0910-000-000"]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void textValid_telnumber4() {
        PetriNet testNet = importTextNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["text02": ["type": "text", "value": "0910000000"]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void textValid_telnumber_Exception() {
        PetriNet testNet = importTextNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["text02": ["type": "text", "value": "aaa 000 000 000"]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-text02", thrown.getMessage());
    }

    @Test
    void regexValid_regex01() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["regex01": ["type": "text", "value": "12345"]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void regexValid_regex02() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["regex02": ["type": "text", "value": "AbC-012-Z9"]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void regexValid_regex03() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["regex03": ["type": "text", "value": "TOTOK4EveR09"]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void regexValid_regex04() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["regex04": ["type": "text", "value": "AA 09 bb"]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void regexValid_regex05() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["regex05": ["type": "text", "value": "A(:?BB+ľščťžýáíééé===é/*-+12154 ô/[]??.!\\.-úaa<>4 MM adsa!!; ff @#&"]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void regexValid_regex05_2() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["regex05": ["type": "text", "value": "    "]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void regexValid_regex06() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["regex06": ["type": "text", "value": "Toto00okJeTest012@netgrif.com"]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void regexValid_regex06_2() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["regex06": ["type": "text", "value": "e-mail.totok@netgrif.com"]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void regexValid_regex06_3() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["regex06": ["type": "text", "value": "totok@az.sk.so"]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void regexValid_regex01_Exception() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["regex01": ["type": "text", "value": "54544545454"]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-regex01", thrown.getMessage());
    }

    @Test
    void regexValid_regex01_Exception2() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["regex01": ["type": "text", "value": ""]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-regex01", thrown.getMessage());
    }

    @Test
    void regexValid_regex01_Exception3() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["regex01": ["type": "text", "value": "aav"]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-regex01", thrown.getMessage());
    }

    @Test
    void regexValid_regex02_Exception() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["regex02": ["type": "text", "value": "AAAAAAAAAAAAAaaaaaaaaaa"]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-regex02", thrown.getMessage());
    }

    @Test
    void regexValid_regex02_Exception2() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["regex02": ["type": "text", "value": "AAAAAAA??a"]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-regex02", thrown.getMessage());
    }

    @Test
    void regexValid_regex02_Exception3() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["regex02": ["type": "text", "value": "-"]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-regex02", thrown.getMessage());
    }

    @Test
    void regexValid_regex03_Exception() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["regex03": ["type": "text", "value": "aaaTOTOKaa1231"]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-regex03", thrown.getMessage());
    }

    @Test
    void regexValid_regex03_Exception2() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["regex03": ["type": "text", "value": "TOTOK"]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-regex03", thrown.getMessage());
    }

    @Test
    void regexValid_regex03_Exception3() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["regex03": ["type": "text", "value": "TOTOK4EveR0!"]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-regex03", thrown.getMessage());
    }

    @Test
    void regexValid_regex04_Exception() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["regex04": ["type": "text", "value": "5412122121212121"]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-regex04", thrown.getMessage());
    }

    @Test
    void regexValid_regex05_Exception() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["regex05": ["type": "text", "value": ""]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-regex05", thrown.getMessage());
    }

    @Test
    void regexValid_regex06_Exception() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["regex06": ["type": "text", "value": "aaa@@@aaa.ss"]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-regex06", thrown.getMessage());
    }

    @Test
    void regexValid_regex06_Exception2() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["regex06": ["type": "text", "value": "aaa@aa"]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-regex06", thrown.getMessage());
    }

    @Test
    void regexValid_regex06_Exception3() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["regex06": ["type": "text", "value": "@aa.sk"]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-regex06", thrown.getMessage());
    }

    @Test
    void regexValid_regex06_Exception4() {
        PetriNet testNet = importRegexNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["regex06": ["type": "text", "value": "tot  ok@az.sk.so"]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-regex06", thrown.getMessage());
    }

    // BOOLEAN FIELD
    @Test
    void booleanValid_requiredTrue() {
        PetriNet testNet = importBooleanNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["boolean_0": ["type": "boolean", "value": "true"]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void booleanValid_requiredTrue_Exception() {
        PetriNet testNet = importBooleanNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["boolean_0": ["type": "boolean", "value": "false"]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-boolean", thrown.getMessage());
    }

    @Test
    void booleanValid_requiredTrue_Exception2() {
        PetriNet testNet = importBooleanNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["boolean_0": ["type": "boolean", "value": null]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-boolean", thrown.getMessage());
    }

    // DATE FIELD
    @Test
    void dateValid_between_today() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.now()
        importHelper.setTaskData(task.getStringId(), ["date01": ["type": "date", "value": today.toDate()]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void dateValid_between_today_plusDay() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.now()
        today = today.plusDays(1)
        importHelper.setTaskData(task.getStringId(), ["date01": ["type": "date", "value": today.toDate()]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void dateValid_between_today_Exception() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.now()
        today = today.minusDays(1)
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        String text = today.format(formatters)
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["date01": ["type": "date", "value": text]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-date01", thrown.getMessage());
    }

    @Test
    void dateValid_between_past() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.now()
        importHelper.setTaskData(task.getStringId(), ["date02": ["type": "date", "value": today.toDate()]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void dateValid_between_past_minusDay() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.now()
        today = today.minusDays(1)
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        String text = today.format(formatters)
        importHelper.setTaskData(task.getStringId(), ["date02": ["type": "date", "value": text]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void dateValid_between_past_Exception() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.now()
        today = today.plusDays(1)
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        String text = today.format(formatters)
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["date02": ["type": "date", "value": text]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-date02", thrown.getMessage());
    }

    @Test
    void dateValid_between_fromDate() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.of(2020, 3, 3)
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        String text = today.format(formatters)
        importHelper.setTaskData(task.getStringId(), ["date03": ["type": "date", "value": text]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void dateValid_between_fromDate_today() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.now()
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        String text = today.format(formatters)
        importHelper.setTaskData(task.getStringId(), ["date03": ["type": "date", "value": text]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void dateValid_between_fromDate_Exception() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.now()
        today = today.plusDays(1)
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        String text = today.format(formatters)
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["date03": ["type": "date", "value": text]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-date03", thrown.getMessage());
    }

    @Test
    void dateValid_between_fromDate_past() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.of(2020, 3, 3)
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        String text = today.format(formatters)
        importHelper.setTaskData(task.getStringId(), ["date04": ["type": "date", "value": text]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void dateValid_between_fromDate_past_minusDay() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.of(2020, 3, 3)
        today = today.minusDays(1)
        importHelper.setTaskData(task.getStringId(), ["date04": ["type": "date", "value": today.toDate()]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void dateValid_between_fromDate_past_Exception() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.of(2020, 3, 3)
        today = today.plusDays(1)
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        String text = today.format(formatters)
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["date04": ["type": "date", "value": text]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-date04", thrown.getMessage());
    }

    @Test
    void dateValid_between_fromDate_toDate() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.of(2020, 1, 1)
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        String text = today.format(formatters)
        importHelper.setTaskData(task.getStringId(), ["date05": ["type": "date", "value": text]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void dateValid_between_fromDate_toDate2() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.of(2022, 3, 3)
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        String text = today.format(formatters)
        importHelper.setTaskData(task.getStringId(), ["date05": ["type": "date", "value": text]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void dateValid_workday() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.of(1994, 7, 4)
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        String text = today.format(formatters)
        importHelper.setTaskData(task.getStringId(), ["date06": ["type": "date", "value": text]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void dateValid_workday_Exception() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.of(1994, 7, 3)
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        String text = today.format(formatters)
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["date06": ["type": "date", "value": text]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-date06", thrown.getMessage());
    }

    @Test
    void dateValid_weekend() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.of(1994, 7, 3)
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        String text = today.format(formatters)
        importHelper.setTaskData(task.getStringId(), ["date07": ["type": "date", "value": text]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void dateValid_weekend_Exception() {
        PetriNet testNet = importDateNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        LocalDate today = LocalDate.of(1994, 7, 4)
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        String text = today.format(formatters)
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["date07": ["type": "date", "value": text]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-date07", thrown.getMessage());
    }

    // Number Field
    @Test
    void numberValid_odd() {
        PetriNet testNet = importNumberNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["number01": ["type": "number", "value": 3]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void numberValid_odd_Exception() {
        PetriNet testNet = importNumberNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["number01": ["type": "number", "value": 2]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-number01", thrown.getMessage());
    }

    @Test
    void numberValid_even() {
        PetriNet testNet = importNumberNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["number02": ["type": "number", "value": 2]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void numberValid_even_Exception() {
        PetriNet testNet = importNumberNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["number02": ["type": "number", "value": 3]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-number02", thrown.getMessage());
    }

    @Test
    void numberValid_positive() {
        PetriNet testNet = importNumberNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["number03": ["type": "number", "value": 1.25624]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void numberValid_positive_Exception() {
        PetriNet testNet = importNumberNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["number03": ["type": "number", "value": -1.1558]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-number03", thrown.getMessage());
    }

    @Test
    void numberValid_negative() {
        PetriNet testNet = importNumberNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["number04": ["type": "number", "value": -1.25624]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void numberValid_negative_Exception() {
        PetriNet testNet = importNumberNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["number04": ["type": "number", "value": 1.1558]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-number04", thrown.getMessage());
    }

    @Test
    void numberValid_decimal() {
        PetriNet testNet = importNumberNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["number05": ["type": "number", "value": 10]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void numberValid_decimal_Exception() {
        PetriNet testNet = importNumberNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["number05": ["type": "number", "value": 10.1558]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-number05", thrown.getMessage());
    }

    @Test
    void numberValid_inRange() {
        PetriNet testNet = importNumberNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["number06": ["type": "number", "value": 13.2452]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void numberValid_inRange_Exception() {
        PetriNet testNet = importNumberNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["number06": ["type": "number", "value": 9.1558]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-number06", thrown.getMessage());
    }


    @Test
    void numberValid_inRange_odd() {
        PetriNet testNet = importNumberNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null
        importHelper.setTaskData(task.getStringId(), ["number07": ["type": "number", "value": 1]])
        Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
        assert taskFinish != null
    }

    @Test
    void numberValid_inRange_odd_Exception() {
        PetriNet testNet = importNumberNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["number07": ["type": "number", "value": 2]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-number07-1", thrown.getMessage());
    }

    @Test
    void numberValid_inRange_odd_Exception2() {
        PetriNet testNet = importNumberNet()
        Case aCase = importHelper.createCase("TestCase", testNet)
        assert aCase != null
        Task task = importHelper.assignTaskToSuper("Test", aCase.stringId).getTask()
        assert task != null

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), ["number07": ["type": "number", "value": 7]])
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-number07-2", thrown.getMessage());
    }

}
