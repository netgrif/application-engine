package com.netgrif.application.engine.archive;

import com.netgrif.application.engine.archive.interfaces.IArchiveService;
import com.netgrif.application.engine.files.IStorageResolverService;
import com.netgrif.application.engine.files.interfaces.IStorageService;
import com.netgrif.application.engine.petrinet.domain.dataset.StorageField;
import com.netgrif.application.engine.workflow.domain.CaseExportFiles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZipService implements IArchiveService {

    private final IStorageResolverService storageResolverService;

    @Override
    public void pack(String archivePath, CaseExportFiles caseExportFiles, String... additionalFiles) throws IOException {
        FileOutputStream fos = new FileOutputStream(archivePath);
        this.pack(fos, caseExportFiles, additionalFiles);
        fos.close();
    }

    @Override
    public void pack(OutputStream archiveStream, CaseExportFiles caseExportFiles, String... additionalFiles) throws IOException {
        ZipOutputStream zipStream = new ZipOutputStream(archiveStream);
        for (String caseId : caseExportFiles.getCaseIds()) {
            for (ImmutablePair<StorageField<?>, Set<String>> fields : caseExportFiles.getFieldsOfCase(caseId)) {
                StorageField<?> storageField = fields.left;
                IStorageService storageService = storageResolverService.resolve(storageField.getStorageType());
                for (String fileName : fields.right) {
                    String filePath = storageService.getPath(caseId, storageField.getStringId(), fileName);
                    InputStream fis = storageService.get(storageField, filePath);
                    String newFileName = caseId.concat(File.separator).concat(storageField.getStringId()).concat(File.separator).concat(fileName);
                    createAndWriteZipEntry(newFileName, zipStream, fis);
                    fis.close();
                }
            }
        }
        for (String filePath : additionalFiles) {
            InputStream fis = new FileInputStream(filePath);
            createAndWriteZipEntry(Paths.get(filePath).getFileName().toString(), zipStream, fis);
            fis.close();
        }
        zipStream.close();
    }

    @Override
    public OutputStream createArchive(CaseExportFiles caseExportFiles) throws IOException {
        return createArchive(Files.createTempFile(UUID.randomUUID().toString(), ".zip").toString(), caseExportFiles);
    }

    @Override
    public OutputStream createArchive(String archivePath, CaseExportFiles caseExportFiles) throws IOException {
        File zipFile = new File(archivePath);
        return zipFiles(zipFile, caseExportFiles);
    }

    private OutputStream zipFiles(File zipFile, CaseExportFiles caseExportFiles) throws IOException {
        FileOutputStream fos = new FileOutputStream(zipFile);
        this.pack(fos, caseExportFiles);
        return fos;
    }

    @Override
    public void append(OutputStream archiveStream, String... filePaths) throws IOException {
//        todo implement
    }

    private void createAndWriteZipEntry(String fileName, ZipOutputStream zipStream, InputStream fis) throws IOException {
//        source https://www.baeldung.com/java-compress-and-uncompress
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipStream.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipStream.write(bytes, 0, length);
        }
    }

    @Override
    public String unpack(String archivePath, String outputPath) throws IOException {
        return this.unpack(new FileInputStream(archivePath), outputPath);
    }

    @Override
    public String unpack(InputStream archiveStream, String outputPath) throws IOException {
//        source: https://www.baeldung.com/java-compress-and-uncompress
        File destDir = new File(outputPath);

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(archiveStream);
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
        return destDir.getPath();
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
