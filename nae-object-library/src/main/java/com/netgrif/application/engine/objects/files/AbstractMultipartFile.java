package com.netgrif.application.engine.objects.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractMultipartFile {

    public abstract String getName();

    public abstract String getOriginalFilename();

    public abstract String getContentType();

    public abstract boolean isEmpty();

    public abstract long getSize();

    public abstract byte[] getBytes() throws IOException;

    public abstract InputStream getInputStream() throws IOException;

    public abstract void transferTo(File dest) throws IOException, IllegalStateException;
}
