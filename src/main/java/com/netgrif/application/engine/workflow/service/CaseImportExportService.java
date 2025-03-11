package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.archive.interfaces.IArchiveService;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.FileStorageConfiguration;
import com.netgrif.application.engine.workflow.service.interfaces.ICaseExportImportService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaseImportExportService implements ICaseExportImportService {

    private final ObjectFactory<CaseImporter> caseImporterObjectFactory;
    private final ObjectFactory<CaseExporter> caseExporterObjectFactory;
    private final IArchiveService archiveService;
    private final FileStorageConfiguration fileStorageConfiguration;
    private final IWorkflowService workflowService;

    protected CaseImporter getCaseImporter() {
        return caseImporterObjectFactory.getObject();
    }

    protected CaseExporter getCaseExporter() {
        return caseExporterObjectFactory.getObject();
    }

    @Override
    public void findAndExportCases(Set<String> caseIdsToExport, OutputStream exportFile) {
        this.exportCases(caseIdsToExport.stream().map(caseId -> workflowService.findOne(caseId)).collect(Collectors.toSet()), exportFile);
    }

    @Override
    public void exportCases(Set<Case> casesToExport, OutputStream exportFile) {
        this.exportCasesToFile(casesToExport, exportFile);
    }

    @Override
    public Set<File> getFilesOfCases(Set<Case> casesToExport) {
        return Set.of();
    }

    @Override
    public ZipFile zipExportFileAndCaseFiles(OutputStream exportFile, Set<File> caseFiles) {
        return null;
    }


    @Override
    public File exportCasesWithFiles(Collection<Case> casesToExport, File exportFile) {
        try (FileOutputStream fos = new FileOutputStream(exportFile)) {
            this.exportCasesToFile(casesToExport, fos);
            String[] filePathsToZip = (String[]) casesToExport.stream()
                    .map(exportCase -> fileStorageConfiguration.getStoragePath() + "/" + exportCase.getStringId())
                    .filter(filePath -> Files.exists(Paths.get(filePath)))
                    .toArray();
//            todo archive name/path
            archiveService.pack(fileStorageConfiguration.getStoragePath() + "/exports", filePathsToZip);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Case> importCases(InputStream inputStream) {
        return caseImporterObjectFactory.getObject().importCases(inputStream);
    }

    private void exportCasesToFile(Collection<Case> casesToExport, OutputStream exportFile) {
        CaseExporter caseExporter = getCaseExporter();
        try {
            caseExporter.exportCases(casesToExport, exportFile);
        } catch (RuntimeException e) {
            log.error("Error exporting cases", e);
        }
    }
}
