package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.archive.interfaces.IArchiveService;
import com.netgrif.application.engine.files.IStorageResolverService;
import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.files.throwable.StorageException;
import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.CaseExportFiles;
import com.netgrif.application.engine.workflow.domain.DataField;
import com.netgrif.application.engine.workflow.service.interfaces.ICaseExportImportService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaseExportImportService implements ICaseExportImportService {

    private final ObjectFactory<CaseImporter> caseImporterObjectFactory;
    private final ObjectFactory<CaseExporter> caseExporterObjectFactory;
    private final IArchiveService archiveService;
    private final IWorkflowService workflowService;
    private final IStorageResolverService storageResolverService;

    protected CaseImporter getCaseImporter() {
        return caseImporterObjectFactory.getObject();
    }

    protected CaseExporter getCaseExporter() {
        return caseExporterObjectFactory.getObject();
    }

    @Override
    public void findAndExportCases(Set<String> caseIdsToExport, OutputStream exportStream) throws IOException {
        this.exportCases(caseIdsToExport.stream().map(workflowService::findOne).collect(Collectors.toSet()), exportStream);
    }

    @Override
    public void findAndExportCasesWithFiles(Set<String> caseIdsToExport, OutputStream archiveStream) throws IOException {
        Set<Case> casesToExport = caseIdsToExport.stream().map(workflowService::findOne).collect(Collectors.toSet());
//        todo filename to property?
        File exportFile = new File(Files.createTempDirectory("case_export").toFile(), "case_export.xml");
        this.exportCases(casesToExport, new FileOutputStream(exportFile));
        CaseExportFiles caseFiles = this.getFileNamesOfCases(casesToExport);
        archiveService.pack(archiveStream, caseFiles, exportFile.getPath());
        FileUtils.deleteDirectory(exportFile.getParentFile());
    }

    @Override
    public void exportCases(Set<Case> casesToExport, OutputStream exportStream) throws IOException {
        this.exportCasesToFile(casesToExport, exportStream);
        exportStream.close();
    }

    @Override
    public CaseExportFiles getFileNamesOfCases(Set<Case> casesToExport) {
        CaseExportFiles filesToExport = new CaseExportFiles();
        for (Case exportCase : casesToExport) {
            List<String> fields = exportCase.getPetriNet().getDataSet().values().stream()
                    .filter(field -> field instanceof StorageField<?>)
                    .map(Field::getStringId).toList();
            fields.forEach(fieldId -> {
                DataField dataField = exportCase.getDataField(fieldId);
                if (dataField == null || dataField.getValue() == null) {
                    return;
                }
                Field<?> field = exportCase.getField(fieldId);
                HashSet<String> namesPaths = new HashSet<>();
                if (field instanceof FileListField) {
                    ((FileListFieldValue) dataField.getValue()).getNamesPaths().forEach(value -> namesPaths.add(value.getName()));
                } else {
                    namesPaths.add(((FileFieldValue) dataField.getValue()).getName());
                }
                filesToExport.addFieldFilenames(exportCase.getStringId(), (StorageField<?>) field, namesPaths);
            });
        }
        return filesToExport;
    }

    @Override
    public List<Case> importCases(InputStream inputStream) {
        return getCaseImporter().importCases(inputStream);
    }

    @Override
    public List<Case> importCasesWithFiles(InputStream importZipFile) throws IOException {
        String directoryPath = archiveService.unpack(importZipFile, Files.createTempDirectory(UUID.randomUUID().toString()).toString());
        importZipFile.close();
        File caseExportXmlFile = FileUtils.getFile(new File(directoryPath.concat(File.separator).concat("case_export.xml")));
        if (!caseExportXmlFile.exists() || caseExportXmlFile.isDirectory()) {
//            todo exception handling
            throw new RuntimeException("Could not find case export file");
        }
        FileInputStream fis = new FileInputStream(caseExportXmlFile);
        List<Case> importedCases = this.importCases(fis);
        fis.close();
        List<String> caseFilesDirectories = Arrays.stream(Objects.requireNonNull(new File(directoryPath).list(DirectoryFileFilter.DIRECTORY)))
                .map(caseDirectory -> directoryPath.concat(File.separator).concat(caseDirectory))
                .toList();
        saveFiles(caseFilesDirectories);
        FileUtils.deleteDirectory(caseExportXmlFile.getParentFile());
        return importedCases;
    }

    private void saveFiles(List<String> casesDirectories) {
        casesDirectories.forEach(caseDirectory -> {
            Case importedCase = workflowService.findOne(Paths.get(caseDirectory).getFileName().toString());
            List<String> fieldOfCaseDirectories = Arrays.stream(Objects.requireNonNull(new File(caseDirectory).list(DirectoryFileFilter.DIRECTORY))).toList();
            fieldOfCaseDirectories.forEach(fieldDirectory -> {
                String fieldDirectoryPath = caseDirectory.concat(File.separator).concat(fieldDirectory);
                StorageField<?> field = (StorageField<?>) importedCase.getField(Paths.get(fieldDirectoryPath).getFileName().toString());
                IStorageService storageService = storageResolverService.resolve(field.getStorageType());
                Arrays.stream(Objects.requireNonNull(new File(fieldDirectoryPath).list(FileFileFilter.FILE))).toList().forEach(fileName -> {
                    String filePath = fieldDirectoryPath.concat(File.separator).concat(fileName);
                    String path = storageService.getPath(importedCase.getStringId(), field.getStringId(), fileName);
                    try (FileInputStream fis = new FileInputStream(filePath)) {
                        storageService.save(field, path, fis);
//                        todo error handling
                    } catch (IOException | StorageException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        });
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
