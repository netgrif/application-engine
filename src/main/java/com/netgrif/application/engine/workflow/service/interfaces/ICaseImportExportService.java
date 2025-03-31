package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.files.throwable.StorageException;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.CaseExportFiles;
import com.netgrif.application.engine.workflow.exceptions.ImportXmlFileMissingException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

public interface ICaseImportExportService {

    void findAndExportCases(Set<String> caseIdsToExport, OutputStream exportStream) throws IOException;

    void findAndExportCasesWithFiles(Set<String> caseIdsToExport, OutputStream archiveStream) throws IOException;

    void exportCasesWithFiles(List<Case> caseIdsToExport, OutputStream archiveStream) throws IOException;

    void exportCases(List<Case> casesToExport, OutputStream exportStream) throws IOException;

    CaseExportFiles getFileNamesOfCases(List<Case> casesToExport);

    List<Case> importCases(InputStream importStream);

    List<Case> importCasesWithFiles(InputStream importZipStream) throws IOException, StorageException, ImportXmlFileMissingException;
}
