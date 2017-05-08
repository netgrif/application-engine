package com.fmworkflow.importer;

import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.domain.repositories.PetriNetRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@SpringBootTest
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
public class ImporterTest {

    @Autowired
    private Importer importer;

    @Autowired
    private PetriNetRepository repository;

    private static final String NET_TITLE = "jaxb_test";
    private static final String NET_INITIALS = "TST";
    private static final Integer NET_PLACES = 19;
    private static final Integer NET_TRANSITIONS = 24;
    private static final Integer NET_ARCS = 23;
    private static final Integer NET_FIELDS = 34;
    private static final Integer NET_ROLES = 3;

    @Before
    public void before() {
        repository.deleteAll();
    }

    @Test
    public void importPetriNet() throws Exception {
        importer.importPetriNet(new File("src/test/resources/prikladFM.xml"), NET_TITLE, NET_INITIALS);

        assertNetProperlyImported();
    }

    private void assertNetProperlyImported() {
        assert repository.count() == 1;
        PetriNet net = repository.findAll().get(0);
        assert net.getTitle().equals(NET_TITLE);
        assert net.getInitials().equals(NET_INITIALS);
        assert net.getPlaces().size() == NET_PLACES;
        assert net.getTransitions().size() == NET_TRANSITIONS;
        assert net.getArcs().size() == NET_ARCS;
        assert net.getDataSet().size() == NET_FIELDS;
        assert net.getRoles().size() == NET_ROLES;
    }
}