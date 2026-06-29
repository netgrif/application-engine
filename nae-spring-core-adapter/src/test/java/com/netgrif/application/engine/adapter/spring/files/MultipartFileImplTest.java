package com.netgrif.application.engine.adapter.spring.files;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultipartFileImplTest {

    @Test
    void rejectsNullOrEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> new MultipartFileImpl(null, "file.txt", "text/plain", new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> new MultipartFileImpl("", "file.txt", "text/plain", new byte[0]));
    }

    @Test
    void exposesDefensiveContentCopy() throws Exception {
        byte[] content = "hello".getBytes();
        MultipartFileImpl file = new MultipartFileImpl("file", "file.txt", "text/plain", content);

        content[0] = 'H';
        byte[] returned = file.getBytes();
        returned[1] = 'A';

        assertEquals("file", file.getName());
        assertEquals("file.txt", file.getOriginalFilename());
        assertEquals("text/plain", file.getContentType());
        assertEquals(5, file.getSize());
        assertFalse(file.isEmpty());
        assertArrayEquals("hello".getBytes(), file.getBytes());
        assertArrayEquals("hello".getBytes(), file.getInputStream().readAllBytes());
    }

    @Test
    void transfersContentToDestinationFile() throws Exception {
        MultipartFileImpl file = new MultipartFileImpl("file", "file.txt", "text/plain", "data".getBytes());
        File destination = File.createTempFile("multipart-file-impl-", ".txt");
        destination.deleteOnExit();

        file.transferTo(destination);

        assertTrue(destination.isFile());
        assertArrayEquals("data".getBytes(), Files.readAllBytes(destination.toPath()));
    }
}
