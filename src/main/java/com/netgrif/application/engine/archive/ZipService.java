package com.netgrif.application.engine.archive;

import com.netgrif.application.engine.archive.interfaces.IArchiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;

@Slf4j
@Service
public class ZipService implements IArchiveService {

    @Override
    public void pack(String archivePath, String... filePaths) throws FileNotFoundException {
        this.pack(new FileOutputStream(archivePath), filePaths);
    }

    @Override
    public void pack(OutputStream archiveStream, String... filePaths) {
    }

    @Override
    public void unpack(String archivePath, String outputPath) {

    }

    @Override
    public void unpack(InputStream archiveStream, String outputPath) {

    }
}
