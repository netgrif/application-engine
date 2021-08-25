package com.netgrif.workflow.workflow.service;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.google.common.collect.Lists;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.workflow.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.workflow.petrinet.domain.dataset.FilterField;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.workflow.domain.*;
import com.netgrif.workflow.workflow.service.interfaces.IFilterImportExportService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.List;
import java.util.Set;


@Service
public class FilterImportExportService implements IFilterImportExportService {

    private static final Logger log = LoggerFactory.getLogger(FilterImportExportService.class);

    private static final String EXPORT_NET_IDENTIFIER = "export_filters";
    private static final String IMPORT_NET_IDENTIFIER = "import_filters";

    private static final String EXPORT_FILE_FIELD = "export_file";

    @Autowired
    IUserService userService;

    @Autowired
    IWorkflowService workflowService;

    @Autowired
    IPetriNetService petriNetService;

    @Autowired
    private FileStorageConfiguration fileStorageConfiguration;

    @Override
    public void createFilterImport(User author) {
        createFilterCase("IMP_" + author.getFullName(), IMPORT_NET_IDENTIFIER, author.transformToLoggedUser());
    }

    @Override
    public void createFilterExport(User author) {
        createFilterCase("EXP_" + author.getFullName(), EXPORT_NET_IDENTIFIER, author.transformToLoggedUser());
    }

    private void createFilterCase(String title, String netIdentifier, LoggedUser loggedUser) {
        PetriNet filterImportNet = petriNetService.getNewestVersionByIdentifier(netIdentifier);
        workflowService.createCase(filterImportNet.getStringId(), title, "", loggedUser);
    }

    @Override
    public FileFieldValue exportFilters(Set<String> filtersToExport) throws IOException {
        log.info("Exporting selected filters");
        List<Case> selectedFilterCases = this.workflowService.findAllById(Lists.newArrayList(filtersToExport));
        FilterImportExportList filterList = new FilterImportExportList();
        selectedFilterCases.forEach(filter -> {
            filterList.getFilters().add(createExportClass(filter));
        });
        return createXML(filterList);
    }

    @Override
    public void importFilters() {

    }

    @Transactional
    protected FileFieldValue createXML(FilterImportExportList filters) throws IOException {
        String filePath = fileStorageConfiguration.getStoragePath() + "/filterExport/filter_" + userService.getLoggedUser().getName() + ".xml";
        File f = new File(filePath);
        f.getParentFile().mkdirs();

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        xmlMapper.writeValue(baos, filters);

        FileOutputStream fos = new FileOutputStream(f);
        baos.writeTo(fos);

        return new FileFieldValue("filter_" + userService.getLoggedUser().getName() + ".xml", filePath);
    }

    private FilterImportExport createExportClass(Case filter) {
        FilterImportExport exportFilter = new FilterImportExport();
        exportFilter.setTitle(filter.getTitle());
        filter.getImmediateData().forEach(immediateData -> {
            if (immediateData.getClass().equals(FilterField.class) && immediateData.getName().getDefaultValue().equals("Filter")) {
                exportFilter.setFilterName(immediateData.getName());
                exportFilter.setFilterValue(((FilterField) immediateData).getValue());
                exportFilter.setFilterMetadata(((FilterField) immediateData).getFilterMetadata());
            } else if (immediateData.getClass().equals(EnumerationMapField.class)) {
                if (immediateData.getName().equals("Filter visibility")) {
                    exportFilter.setVisibility(immediateData.getValue().toString());
                } else if (immediateData.getName().equals("Filter type")) {
                    exportFilter.setType(immediateData.getValue().toString());
                }
            }
        });
        return exportFilter;
    }
}



