package com.netgrif.application.engine.resource.domain;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NetgrifResource {

    public Resource getResource(String filePath) {
        try {
            File file = new File(filePath);
            InputStream in = new FileInputStream(file);
            return new InputStreamResource(in);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
