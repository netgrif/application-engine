package com.netgrif.application.engine.elastic;


import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.domain.IndexAwareElasticSearchRequest;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticIndexService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.UriContentType;
import com.netgrif.application.engine.petrinet.domain.UriNode;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class ElasticIndexTest {


    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IUserService userService;

    @Autowired
    private IUriService uriService;

    @Autowired
    private IElasticCaseService elasticCaseService;

    @Autowired
    private IDataService dataService;

    @Autowired
    private ImportHelper helper;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IElasticIndexService elasticIndexService;

    private PetriNet aaaNet;
    private PetriNet bbbNet;
    private PetriNet cccNet;


    @BeforeEach
    public void before() {
        testHelper.truncateDbs();

        UriNode aaa = uriService.getOrCreate("aaa", UriContentType.PROCESS);
        UriNode bbb = uriService.getOrCreate("bbb", UriContentType.PROCESS);

        this.aaaNet = helper.createNet("elasticIndex/aaa.xml", VersionType.MAJOR, userService.getSystem().transformToLoggedUser(), aaa.getStringId()).get();
        this.bbbNet = helper.createNet("elasticIndex/bbb.xml", VersionType.MAJOR, userService.getSystem().transformToLoggedUser(), bbb.getStringId()).get();

        this.cccNet = helper.createNet("elasticIndex/ccc.xml", VersionType.MAJOR, userService.getSystem().transformToLoggedUser(), uriService.getRoot().getStringId()).get();

        Case aaaCase1 = helper.createCase("A1", aaaNet);
        Case aaaCase2 = helper.createCase("A2", aaaNet);
        Case aaaCase3 = helper.createCase("A3", aaaNet);
        Case aaaCase4 = helper.createCase("A4", aaaNet);

        Case bbbCase1 = helper.createCase("B1", bbbNet);
        Case bbbCase2 = helper.createCase("B2", bbbNet);
        Case bbbCase3 = helper.createCase("B3", bbbNet);
        Case bbbCase4 = helper.createCase("B4", bbbNet);

        Case cccCase1 = helper.createCase("C1", cccNet);
        Case cccCase2 = helper.createCase("C2", cccNet);
        Case cccCase3 = helper.createCase("C3", cccNet);
        Case cccCase4 = helper.createCase("C4", cccNet);

        Case case_ = null;

        if (!aaaCase1.getTasks().isEmpty()) {
            String task = aaaCase1.getTasks().stream().findFirst().orElse(null).getTask();
            Map<String, Map<String, String>> dataset = new HashMap<>();
            Map<String, String> textAaaData = new HashMap<>();
            textAaaData.put("value", "aaa");
            textAaaData.put("type", "text");
            dataset.put("text_aaa", textAaaData);

            case_ = dataService.setData(task, ImportHelper.populateDataset(dataset)).getCase();
            workflowService.save(case_);
        }

        if (!bbbCase1.getTasks().isEmpty()) {
            String task = bbbCase1.getTasks().stream().findFirst().orElse(null).getTask();
            Map<String, Map<String, String>> dataset = new HashMap<>();
            Map<String, String> textBbbData = new HashMap<>();
            textBbbData.put("value", "bbb");
            textBbbData.put("type", "text");
            dataset.put("text_bbb", textBbbData);

            case_ = dataService.setData(task, ImportHelper.populateDataset(dataset)).getCase();
            workflowService.save(case_);
        }

        if (!cccCase1.getTasks().isEmpty()) {
            String task = cccCase1.getTasks().stream().findFirst().orElse(null).getTask();
            Map<String, Map<String, String>> dataset = new HashMap<>();
            Map<String, String> textCccData = new HashMap<>();
            textCccData.put("value", "ccc");
            textCccData.put("type", "text");
            dataset.put("text_ccc", textCccData);

            case_ = dataService.setData(task, ImportHelper.populateDataset(dataset)).getCase();
            workflowService.save(case_);
        }
    }


    @Test
    public void elasticIndexTest() throws InterruptedException {
        List<String> indexList_aaa = new ArrayList<>(Arrays.asList("nae_test_case_aaa"));
        List<String> indexList_bbb = new ArrayList<>(Arrays.asList("nae_test_case_bbb"));
        List<String> indexList_ccc = new ArrayList<>(Arrays.asList("nae_test_case"));  //ROOT

        List<String> combinedList_aaa_bbb = Stream.concat(indexList_aaa.stream(), indexList_bbb.stream())
                .collect(Collectors.toList());

        sleep(15000);

        List<Case> results_aaa_bbb = findCasesElastic("dataSet.text_1.textValue:\"aaa\"", combinedList_aaa_bbb, PageRequest.of(0, 100));
        assertNotNull(results_aaa_bbb.get(0), "test0 should not be null");

        List<Case> results_aaa = findCasesElastic("dataSet.text_1.textValue:\"aaa\"", indexList_aaa, PageRequest.of(0, 100));
        assertNotNull(results_aaa.get(0), "test1 should not be null");

        List<Case> results_bbb = findCasesElastic("dataSet.text_1.textValue:\"aaa\"", indexList_bbb, PageRequest.of(0, 100));
        assertTrue(results_bbb.isEmpty(), "test2 should be null");

        List<Case> results_ccc = findCasesElastic("dataSet.text_1.textValue:\"aaa\"", indexList_ccc, PageRequest.of(0, 100));
        assertTrue(results_ccc.isEmpty(), "test3 should be null");
    }

    @Test
    @Order(1)
    public void moveIndexByProcessTest() throws InterruptedException {
        IntStream.range(0, 10).parallel().forEach(i -> {
            helper.createCase("A" + i, aaaNet);
        });

        List<String> indexList_aaa = new ArrayList<>(Arrays.asList("nae_test_case_aaa"));
        List<String> indexList_bbb = new ArrayList<>(Arrays.asList("nae_test_case_bbb"));
        sleep(10000);

        List<Case> results_aaa = findCasesElastic("dataSet.text_1.textValue:\"aaa\"", indexList_aaa, PageRequest.of(0, 100));
        assertNotNull(results_aaa.get(0), "test1 should not be null");


        List<Case> results_bbb = findCasesElastic("dataSet.text_1.textValue:\"aaa\"", indexList_bbb, PageRequest.of(0, 100));
        assertTrue(results_bbb.isEmpty(), "test2 should be null");

        CaseSearchRequest request = new CaseSearchRequest();
        request.process = Collections.singletonList(new CaseSearchRequest.PetriNet(aaaNet.getIdentifier()));
        IndexAwareElasticSearchRequest searchRequests = new IndexAwareElasticSearchRequest();
        searchRequests.add(request);

        elasticCaseService.moveElasticIndex(searchRequests, "nae_test_case_aaa", "nae_test_case_bbb");

        sleep(15000);

        List<Case> results_aaa2 = findCasesElastic("dataSet.text_1.textValue:\"aaa\"", indexList_aaa, PageRequest.of(0, 100));
        assertTrue(results_aaa2.isEmpty(), "test2 should be null");
        sleep(15000);

        List<Case> results_bbb2 = findCasesElastic("dataSet.text_1.textValue:\"aaa\"", indexList_bbb, PageRequest.of(0, 100));
        assertFalse(results_bbb2.isEmpty(), "test1 should not be null");
    }


    @Test
    public void moveIndexTest() throws InterruptedException {
        IntStream.range(0, 120).parallel().forEach(i -> {
            helper.createCase("A" + i, aaaNet);
        });

        List<String> indexList_aaa = new ArrayList<>(Arrays.asList("nae_test_case_aaa"));
        List<String> indexList_bbb = new ArrayList<>(Arrays.asList("nae_test_case_bbb"));
        sleep(20000);

        List<Case> results_aaa = findCasesElastic("dataSet.text_1.textValue:\"aaa\"", indexList_aaa, PageRequest.of(0, 100));
        assertNotNull(results_aaa.get(0), "test1 should not be null");


        List<Case> results_bbb = findCasesElastic("dataSet.text_1.textValue:\"aaa\"", indexList_bbb, PageRequest.of(0, 100));
        assertTrue(results_bbb.isEmpty(), "test2 should be null");


        elasticCaseService.moveElasticIndex("nae_test_case_aaa", "nae_test_case_bbb");

        sleep(15000);


        assertFalse(elasticIndexService.indexExists("nae_test_case_aaa"), "index remove!");

        List<Case> results_bbb2 = findCasesElastic("dataSet.text_1.textValue:\"aaa\"", indexList_bbb, PageRequest.of(0, 100));
        assertNotNull(results_bbb2.get(0), "test1 should not be null");
    }


    protected List<Case> findCasesElastic(String query, List<String> index, Pageable pageable) {
        CaseSearchRequest request = new CaseSearchRequest();
        request.query = query;
        IndexAwareElasticSearchRequest searchRequests = new IndexAwareElasticSearchRequest();
        searchRequests.setIndexNames(index);
        searchRequests.add(request);
        return elasticCaseService.search(searchRequests, userService.getSystem().transformToLoggedUser(), pageable, LocaleContextHolder.getLocale(), false).getContent();
    }
}
