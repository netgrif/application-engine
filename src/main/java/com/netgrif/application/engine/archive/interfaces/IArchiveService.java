package com.netgrif.application.engine.archive.interfaces;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IArchiveService {

    void pack(String archivePath, String... filePaths) throws FileNotFoundException;

    void pack(OutputStream archiveStream, String... filePaths);

    void unpack(String archivePath, String outputPath);

    void unpack(InputStream archiveStream, String outputPath);
}
