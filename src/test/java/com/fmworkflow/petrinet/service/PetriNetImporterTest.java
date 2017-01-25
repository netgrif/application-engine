package com.fmworkflow.petrinet.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class PetriNetImporterTest {
    File xmlFile;

    @Before
    public void setUp() throws Exception {
        xmlFile = new File("test1.xml");
    }

    @Test
    public void testImport() throws Exception {
        PetriNetImporter importer = new PetriNetImporter(xmlFile);
        importer.importPetriNet();

    }
}