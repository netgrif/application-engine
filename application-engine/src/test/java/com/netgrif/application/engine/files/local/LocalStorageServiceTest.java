package com.netgrif.application.engine.files.local;

import com.netgrif.application.engine.files.minio.StorageConfigurationProperties;
import com.netgrif.application.engine.files.throwable.StorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void resolvesStorageAndPreviewPathsInsideConfiguredRoot() {
        LocalStorageService service = service();

        assertEquals(
                tempDir.resolve("case-1").resolve("field-file.txt").toString(),
                service.getPath("case-1", "field", "file.txt")
        );
        assertEquals(
                tempDir.resolve("file_preview").resolve("case-1").resolve("field-file.txt").toString(),
                service.getPreviewPath("case-1", "field", "file.txt")
        );
    }

    @Test
    void rejectsUnsafePathElements() {
        LocalStorageService service = service();

        assertThrows(IllegalArgumentException.class, () -> service.getPath("case-1", "../field", "file.txt"));
        assertThrows(IllegalArgumentException.class, () -> service.getPath("case-1", "field", "/tmp/file.txt"));
        assertThrows(IllegalArgumentException.class, () -> service.getPath("case-1", "field", " "));
    }

    @Test
    void savesReadsAndDeletesLocalFile() throws Exception {
        LocalStorageService service = service();
        String path = service.getPath("case-1", "field", "file.txt");

        assertTrue(service.save(null, path, new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8))));
        assertEquals("content", new String(service.get(null, path).readAllBytes(), StandardCharsets.UTF_8));

        service.delete(null, path);

        assertTrue(Files.notExists(Path.of(path)));
    }

    @Test
    void wrapsIoFailureAsStorageException() throws Exception {
        LocalStorageService service = service();
        Path parentFile = tempDir.resolve("parent-as-file");
        Files.writeString(parentFile, "not-a-directory");
        Path invalidPath = parentFile.resolve("file.txt");

        assertThrows(StorageException.class, () ->
                service.save(null, invalidPath.toString(), new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)))
        );
    }

    private LocalStorageService service() {
        StorageConfigurationProperties properties = new StorageConfigurationProperties();
        properties.setPath(tempDir.toString());
        LocalStorageService service = new LocalStorageService();
        service.setFileStorageConfiguration(properties);
        return service;
    }
}
