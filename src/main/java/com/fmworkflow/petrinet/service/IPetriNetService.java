package com.fmworkflow.petrinet.service;

import com.fmworkflow.petrinet.domain.PetriNet;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public interface IPetriNetService {

    void importPetriNet(File xmlFile);

    void savePetriNet(PetriNet petriNet);
}
