package com.netgrif.application.engine.configuration.security.jwt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public PrivateKey get(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(spec);
    }
}