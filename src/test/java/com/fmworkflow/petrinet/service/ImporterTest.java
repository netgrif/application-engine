package com.fmworkflow.petrinet.service;

import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.domain.PetriNetRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ImporterTest {

    @Autowired
    private Importer importer;
    @Autowired
    private PetriNetRepository repository;
    @Autowired
    private IPetriNetService service;
    private File xmlFile;

    @Before
    public void setUp() throws Exception {
        xmlFile = new File("src/test/resources/test.xml");
    }

    @Test
    public void testImport() throws Exception {
        importer.importPetriNet(xmlFile, "dbref_test", "ref");
    }

    @Test
    public void loadImported() {
        PetriNet net = service.loadPetriNet("5895bee8b71c6d0eb0649416");

        assert net != null;
    }
}