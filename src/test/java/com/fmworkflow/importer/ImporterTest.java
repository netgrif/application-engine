package com.fmworkflow.importer;

import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.domain.PetriNetRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ImporterTest {

    @Autowired
    private Importer importer;

    @Autowired
    private PetriNetRepository repository;

    @Test
//    @Ignore
    public void importPetriNet() throws Exception {
        importer.importPetriNet(new File("src/test/resources/prikladFM.xml"), "jaxb_test", "initials");
    }

    @Test
    @Ignore
    public void loadImportedPetriNet() {
        PetriNet net = repository.findOne("58a8bbe528f7351b3c8e2cea");
        System.out.println(net);
    }
}