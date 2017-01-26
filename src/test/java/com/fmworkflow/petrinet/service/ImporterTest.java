package com.fmworkflow.petrinet.service;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class ImporterTest {
    private File xmlFile;

    @Before
    public void setUp() throws Exception {
        xmlFile = new File("src/test/resources/test.xml");
    }

    @Test
    public void testImport() throws Exception {
        Importer importer = new Importer(xmlFile);
        importer.importPetriNet();

    }
}