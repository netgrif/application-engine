package com.netgrif.application.engine.archive.interfaces;

import com.netgrif.application.engine.workflow.domain.CaseExportFiles;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IArchiveService {

    void pack(String archivePath, CaseExportFiles caseExportFiles, String... additionalFiles) throws IOException;

    void pack(OutputStream archiveStream, CaseExportFiles caseExportFiles, String... additionalFiles) throws IOException;

    OutputStream createArchive(CaseExportFiles caseExportFiles) throws IOException;

    OutputStream createArchive(String archivePath, CaseExportFiles caseExportFiles) throws IOException;

    void append(OutputStream archiveStream, String... filePaths) throws IOException;

    String unpack(String archivePath, String outputPath) throws IOException;

    String unpack(InputStream archiveStream, String outputPath) throws IOException;
}
