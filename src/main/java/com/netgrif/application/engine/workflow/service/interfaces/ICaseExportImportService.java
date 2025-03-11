package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.workflow.domain.Case;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ICaseExportImportService {

    void findAndExportCases(Set<String> caseIdsToExport, OutputStream exportFile);

    void exportCases(Set<Case> casesToExport, OutputStream exportFile);

    Set<File> getFilesOfCases(Set<Case> casesToExport);

    ZipFile zipExportFileAndCaseFiles(OutputStream exportFile, Set<File> caseFiles);

    File exportCasesWithFiles(Collection<Case> casesToExport, File exportFile);

    List<Case> importCases(InputStream importFile);
}
