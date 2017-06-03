package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.importer.Importer;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.repositories.TaskRepository;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.List;

@SpringBootTest
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
public class TaskServiceTest {

    @Autowired
    private ITaskService service;

    @Autowired
    private TaskRepository repository;

    @Autowired
    private Importer importer;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private PetriNetRepository petriNetRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Before
    public void setUp() {
        mongoTemplate.getDb().dropDatabase();
        taskRepository.deleteAll();

        importer.importPetriNet(new File("src/test/resources/prikladFM.xml"), "fm net", "fm");
        PetriNet net = petriNetRepository.findAll().get(0);
        workflowService.createCase(net.getStringId(), "Storage Unit", "color");
    }

//    @Test
//    public void getAll() throws Exception {
//    }
//
//    @Test
//    public void findByCaseId() throws Exception {
//    }
//
//    @Test
//    public void findById() throws Exception {
//    }

    @Test
    public void createTasks() throws Exception {
        Case useCase = caseRepository.findAll().get(0);

        service.createTasks(useCase);

        assert repository.findAll().size() > 0;
    }

//    @Test
//    public void findByUser() throws Exception {
//    }
//
//    @Test
//    public void findUserFinishedTasks() throws Exception {
//    }
//
//    @Test
//    public void finishTask() throws Exception {
//    }
//
//    @Test
//    public void assignTask() throws Exception {
//    }

    @Test
    public void getData() throws Exception {
        Task task = repository.findAll().stream().filter(t -> t.getTitle().equals("Data UJ")).findFirst().get();

        List<Field> fields = service.getData(task.getStringId());

        assert fields != null && !fields.isEmpty();
    }

//    @Test
//    public void setDataFieldsValues() throws Exception {
//    }
//
//    @Test
//    public void cancelTask() throws Exception {
//    }
//
//    @Test
//    public void saveFile() throws Exception {
//    }
//
//    @Test
//    public void getFile() throws Exception {
//    }
//
//    @Test
//    public void delegateTask() throws Exception {
//    }
}