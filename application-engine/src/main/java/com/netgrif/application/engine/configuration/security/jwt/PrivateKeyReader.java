package com.netgrif.application.engine.configuration.security.jwt;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class PrivateKeyReader {

    private final KeyFactory keyFactory;

    public PrivateKeyReader(String algorithm) throws NoSuchAlgorithmException {
        keyFactory = KeyFactory.getInstance(algorithm);
    }

    public PrivateKey get(Resource resource) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        FileInputStream fileInputStream = new FileInputStream(resource.getFile());
        byte[] keyBytes = IOUtils.toByteArray(fileInputStream);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(spec);
    }
}