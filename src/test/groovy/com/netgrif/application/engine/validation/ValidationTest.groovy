package com.netgrif.application.engine.validation

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.BooleanField
import com.netgrif.application.engine.petrinet.domain.dataset.DateField
import com.netgrif.application.engine.petrinet.domain.dataset.NumberField
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["text01": new TextField(rawValue: "test@netgrif.com")]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["text01": new TextField(rawValue: "test@netgrif.co.com")]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["text01": new TextField(rawValue: "te.st@netgrif.co.com")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["text01": new TextField(rawValue: "test@@aaa.com")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["text01": new TextField(rawValue: "test@aaa.s")]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["text02": new TextField(rawValue: "+421 000 000 000")]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["text02": new TextField(rawValue: "+421-000-000-000")]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["text02": new TextField(rawValue: "0910-000-000")]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["text02": new TextField(rawValue: "0910000000")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["text02": new TextField(rawValue: "aaa 000 000 000")]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["regex01": new TextField(rawValue: "12345")]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["regex02": new TextField(rawValue: "AbC-012-Z9")]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["regex03": new TextField(rawValue: "TOTOK4EveR09")]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["regex04": new TextField(rawValue: "AA 09 bb")]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["regex05": new TextField(rawValue: "A(:?BB+ľščťžýáíééé===é/*-+12154 ô/[]??.!\\.-úaa<>4 MM adsa!!; ff @#&")]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["regex05": new TextField(rawValue: "    ")]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["regex06": new TextField(rawValue: "Toto00okJeTest012@netgrif.com")]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["regex06": new TextField(rawValue: "e-mail.totok@netgrif.com")]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["regex06": new TextField(rawValue: "totok@az.sk.so")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["regex01": new TextField(rawValue: "54544545454")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["regex01": new TextField(rawValue: "")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["regex01": new TextField(rawValue: "aav")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["regex02": new TextField(rawValue: "AAAAAAAAAAAAAaaaaaaaaaa")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["regex02": new TextField(rawValue: "AAAAAAA??a")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["regex02": new TextField(rawValue: "-")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["regex03": new TextField(rawValue: "aaaTOTOKaa1231")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["regex03": new TextField(rawValue: "TOTOK")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["regex03": new TextField(rawValue: "TOTOK4EveR0!")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["regex04": new TextField(rawValue: "5412122121212121")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["regex05": new TextField(rawValue: "")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["regex06": new TextField(rawValue: "aaa@@@aaa.ss")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["regex06": new TextField(rawValue: "aaa@aa")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["regex06": new TextField(rawValue: "@aa.sk")]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["regex06": new TextField(rawValue: "tot  ok@az.sk.so")]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["boolean_0": new BooleanField(rawValue:true)]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["boolean_0": new BooleanField(rawValue:false)]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["boolean_0": new BooleanField(rawValue: null)]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["date01": new DateField(rawValue: LocalDate.now())]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["date01": new DateField(rawValue: LocalDate.now().plusDays(1))]))
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
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), new DataSet(["date01": new DateField(rawValue: LocalDate.now().minusDays(1))]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["date02": new DateField(rawValue: LocalDate.now())]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["date02": new DateField(rawValue: LocalDate.now().minusDays(1))]))
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
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), new DataSet(["date02": new DateField(rawValue: LocalDate.now().plusDays(1))]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["date03": new DateField(rawValue: LocalDate.of(2020, 3, 3))]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["date03": new DateField(rawValue: LocalDate.now())]))
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
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), new DataSet(["date03": new DateField(rawValue: LocalDate.now().plusDays(1))]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["date04": new DateField(rawValue: LocalDate.of(2020, 3, 3))]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["date04": new DateField(rawValue: LocalDate.of(2020, 3, 3))]))
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
        // TODO: release/7.0.0 should fail with 3.3.2020? yes from 6.4.0
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), new DataSet(["date04": new DateField(rawValue: LocalDate.of(2020, 3, 4))]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["date05": new DateField(rawValue: LocalDate.of(2020, 1, 1))]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["date05": new DateField(rawValue: LocalDate.of(2022,3,3))]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["date06": new DateField(rawValue: LocalDate.of(1994,7,4))]))
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
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), new DataSet(["date06": new DateField(rawValue: LocalDate.of(1994,7,3))]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["date07": new DateField(rawValue: LocalDate.of(1994,7,3))]))
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
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            importHelper.setTaskData(task.getStringId(), new DataSet(["date07": new DateField(rawValue: LocalDate.of(1994,7,4))]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["number01": new NumberField(rawValue: 3)]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["number01": new NumberField(rawValue: 2)]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["number02": new NumberField(rawValue: 2)]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["number02": new NumberField(rawValue: 3)]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["number03": new NumberField(rawValue: 1.25624)]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["number03": new NumberField(rawValue: -1.1558)]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["number04": new NumberField(rawValue: -1.25624)]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["number04": new NumberField(rawValue: 1.1558)]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["number05": new NumberField(rawValue: 10)]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["number05": new NumberField(rawValue: 10.1558d)]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["number06": new NumberField(rawValue: 13.2452d)]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["number06": new NumberField(rawValue: 9.1558)]))
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
        importHelper.setTaskData(task.getStringId(), new DataSet(["number07": new NumberField(rawValue: 1)]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["number07": new NumberField(rawValue: 2)]))
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
            importHelper.setTaskData(task.getStringId(), new DataSet(["number07": new NumberField(rawValue: 7)]))
            Task taskFinish = importHelper.finishTaskAsSuper("Test", aCase.stringId).getTask()
            assert taskFinish != null
        })
        Assertions.assertEquals("error-number07-2", thrown.getMessage());
    }

}
