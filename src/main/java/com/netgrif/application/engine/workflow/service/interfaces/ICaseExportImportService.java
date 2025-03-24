package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.CaseExportFiles;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

public interface ICaseExportImportService {

    void findAndExportCases(Set<String> caseIdsToExport, OutputStream exportFile) throws IOException;

    void findAndExportCasesWithFiles(Set<String> caseIdsToExport, OutputStream archiveFile) throws IOException;

    void exportCases(Set<Case> casesToExport, OutputStream exportFile) throws IOException;

    CaseExportFiles getFileNamesOfCases(Set<Case> casesToExport);

    List<Case> importCases(InputStream importFile);

    List<Case> importCasesWithFiles(InputStream importZipFile) throws IOException;
}
