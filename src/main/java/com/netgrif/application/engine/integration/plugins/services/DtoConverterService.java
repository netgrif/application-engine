//package com.netgrif.application.engine.integration.plugins.services;
//
//import com.netgrif.application.engine.integration.plugins.services.interfaces.IDtoConverterService;
//import com.netgrif.application.engine.petrinet.domain.DataGroup;
//import com.netgrif.adapter.petrinet.domain.PetriNet;
//import com.netgrif.adapter.workflow.domain.Case;
//import com.netgrif.adapter.workflow.domain.Task;
//import org.modelmapper.ModelMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import petrinet.domain.DataGroupDto;
//import petrinet.domain.PetriNetDto;
//import workflow.domain.CaseDto;
//import workflow.domain.TaskDto;
//
///**
// * Converter service to turn domain objects of NAE into DTO objects of NAE-API library
// * */
//@Service
//public class DtoConverterService implements IDtoConverterService {
//
//    @Autowired
//    private ModelMapper modelMapper;
//
//    /**
//     * Converts Petri Net into its DTO object
//     * @param net the input Petri Net to be converted into DTO
//     * @return Petri Net DTO object
//     * */
//    @Override
//    public PetriNetDto convertPetriNet(PetriNet net) {
//        return modelMapper.map(net, PetriNetDto.class);
//    }
//
//    /**
//     * Converts case into its DTO object
//     * @param aCase the input case to be converted into DTO
//     * @return case DTO object
//     * */
//    @Override
//    public CaseDto convertCase(Case aCase) {
//        return modelMapper.map(aCase, CaseDto.class);
//    }
//
//    /**
//     * Converts data task its DTO object
//     * @param task the input task to be converted into DTO
//     * @return task DTO object
//     * */
//    @Override
//    public TaskDto convertTask(Task task) {
//        return modelMapper.map(task, TaskDto.class);
//    }
//
//    /**
//     * Converts data group into its DTO object
//     * @param dataGroup the input data group to be converted into DTO
//     * @return data group DTO object
//     * */
//    @Override
//    public DataGroupDto convertDataGroup(DataGroup dataGroup) {
//        return modelMapper.map(dataGroup, DataGroupDto.class);
//    }
//}
