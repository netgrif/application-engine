package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.archive.interfaces.IArchiveService;
import com.netgrif.application.engine.configuration.properties.CaseExportProperties;
import com.netgrif.application.engine.files.IStorageResolverService;
import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.files.throwable.StorageException;
import com.netgrif.application.engine.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.FileListField;
import com.netgrif.application.engine.petrinet.domain.dataset.FileListFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.StorageField;
import com.netgrif.application.engine.workflow.domain.*;
import com.netgrif.application.engine.workflow.exceptions.ImportXmlFileMissingException;
import com.netgrif.application.engine.workflow.service.interfaces.ICaseImportExportService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
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
public class CaseImportExportService implements ICaseImportExportService {

    private final ObjectFactory<CaseImporter> caseImporterObjectFactory;
    private final ObjectFactory<CaseExporter> caseExporterObjectFactory;
    private final IArchiveService archiveService;
    private final IWorkflowService workflowService;
    private final IStorageResolverService storageResolverService;
    private final CaseExportProperties properties;
    private final ITaskService taskService;

    protected CaseImporter getCaseImporter() {
        return caseImporterObjectFactory.getObject();
    }

    protected CaseExporter getCaseExporter() {
        return caseExporterObjectFactory.getObject();
    }

    @Override
    public void findAndExportCases(Set<String> caseIdsToExport, OutputStream exportStream) throws IOException {
        this.exportCases(workflowService.findAllById(new ArrayList<>(caseIdsToExport)), exportStream);
    }

    @Override
    public void findAndExportCasesWithFiles(Set<String> caseIdsToExport, OutputStream archiveStream) throws IOException {
        this.exportCasesWithFiles(workflowService.findAllById(new ArrayList<>(caseIdsToExport)), archiveStream);
    }

    @Override
    public void exportCasesWithFiles(List<Case> casesToExport, OutputStream archiveStream) throws IOException {
        File exportFile = new File(Files.createTempDirectory("case_export").toFile(), properties.getFileName());
        this.exportCases(casesToExport, new FileOutputStream(exportFile));
        CaseExportFiles caseFiles = this.getFileNamesOfCases(casesToExport);
        archiveService.pack(archiveStream, caseFiles, exportFile.getPath());
        FileUtils.deleteDirectory(exportFile.getParentFile());
    }

    @Override
    public void exportCases(List<Case> casesToExport, OutputStream exportStream) throws IOException {
        this.exportCasesToFile(casesToExport, exportStream);
        exportStream.close();
    }

    @Override
    public CaseExportFiles getFileNamesOfCases(List<Case> casesToExport) {
        CaseExportFiles filesToExport = new CaseExportFiles();
        for (Case exportCase : casesToExport) {
            exportCase.getPetriNet().getDataSet().values().stream()
                    .filter(field -> field instanceof StorageField<?>)
                    .forEach(field -> {
                        DataField dataField = exportCase.getDataField(field.getStringId());
                        if (dataField == null || dataField.getValue() == null) {
                            return;
                        }
                        HashSet<String> namesPaths = new HashSet<>();
                        if (field instanceof FileListField) {
                            ((FileListFieldValue) dataField.getValue()).getNamesPaths().forEach(value -> namesPaths.add(value.getName()));
                        } else {
                            namesPaths.add(((FileFieldValue) dataField.getValue()).getName());
                        }
                        filesToExport.addFieldFilenames(exportCase.getStringId(), new StorageFieldWithFileNames((StorageField<?>) field, namesPaths));
                    });
        }
        return filesToExport;
    }

    @Override
    public List<Case> importCases(InputStream inputStream) {
        CaseImporter importer = getCaseImporter();
        List<Case> importedCases = importer.importCases(inputStream);
        if (importedCases.isEmpty()) {
            return importedCases;
        }
        return saveImportedObjects(importedCases, importer.getImportedTasksMap()).values().stream().toList();
    }

    @Override
    public List<Case> importCasesWithFiles(InputStream importZipStream) throws IOException, StorageException, ImportXmlFileMissingException {
        String directoryPath = archiveService.unpack(importZipStream, Files.createTempDirectory(UUID.randomUUID().toString()).toString());
        importZipStream.close();
        File caseExportXmlFile = FileUtils.getFile(new File(directoryPath.concat(File.separator).concat(properties.getFileName())));
        if (!caseExportXmlFile.exists() || caseExportXmlFile.isDirectory()) {
            throw new ImportXmlFileMissingException("Xml import file with name [" + properties.getFileName() + "] not found in archive");
        }
        FileInputStream fis = new FileInputStream(caseExportXmlFile);
        CaseImporter importer = getCaseImporter();
        List<Case> importedCases = importer.importCases(fis);
        fis.close();
        if (importedCases.isEmpty()) {
            return importedCases;
        }
        Map<String, Case> importedCasesMap = saveImportedObjects(importedCases, importer.getImportedTasksMap());
        List<String> caseFilesDirectories = Arrays.stream(Objects.requireNonNull(new File(directoryPath).list(DirectoryFileFilter.DIRECTORY)))
                .map(caseDirectory -> directoryPath.concat(File.separator).concat(caseDirectory))
                .toList();
        Map<String, Case> idsToCaseMapping = new HashMap<>();
        importer.getImportedIdsMapping().forEach((oldCaseId, newCaseId) -> {
            idsToCaseMapping.put(oldCaseId, importedCasesMap.get(newCaseId));
        });
        saveFiles(caseFilesDirectories, idsToCaseMapping);
        FileUtils.forceDelete(caseExportXmlFile.getParentFile());
        return importedCases;
    }

    private void saveFiles(List<String> casesDirectories, Map<String, Case> importedIdsMapping) throws IOException, StorageException {
        for (String caseDirectory : casesDirectories) {
            Case importedCase = importedIdsMapping.get(Paths.get(caseDirectory).getFileName().toString());
            List<String> fieldsOfCaseDirectories = Arrays.stream(Objects.requireNonNull(new File(caseDirectory).list(DirectoryFileFilter.DIRECTORY))).toList();
            for (String fieldDirectory : fieldsOfCaseDirectories) {
                String fieldDirectoryPath = caseDirectory.concat(File.separator).concat(fieldDirectory);
                StorageField<?> field = (StorageField<?>) importedCase.getField(Paths.get(fieldDirectoryPath).getFileName().toString());
                IStorageService storageService = storageResolverService.resolve(field.getStorageType());
                for (String fileName : Objects.requireNonNull(new File(fieldDirectoryPath).list(FileFileFilter.FILE))) {
                    String filePath = fieldDirectoryPath.concat(File.separator).concat(fileName);
                    String path = storageService.getPath(importedCase.getStringId(), field.getStringId(), fileName);
                    FileInputStream fis = new FileInputStream(filePath);
                    storageService.save(field, path, fis);
                    fis.close();
                }
            }
        }
    }

    private Map<String, Case> saveImportedObjects(List<Case> importedCases, Map<String, List<Task>> importedTasks) {
        return importedCases.stream().map(importedCase -> {
            taskService.save(importedTasks.get(importedCase.getStringId()));
            return workflowService.save(importedCase);
        }).collect(Collectors.toMap(Case::getStringId, c -> c));
    }

    private void exportCasesToFile(Collection<Case> casesToExport, OutputStream exportFile) {
        CaseExporter caseExporter = getCaseExporter();
        caseExporter.exportCases(casesToExport, exportFile);
    }
}
