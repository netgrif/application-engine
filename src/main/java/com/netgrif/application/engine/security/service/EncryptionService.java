package com.netgrif.application.engine.security.service;

import com.netgrif.application.engine.configuration.properties.DatabaseProperties;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class EncryptionService implements IEncryptionService {

    private final StandardPBEStringEncryptor standardEncryptor;
    private final DatabaseProperties properties;

    private final HashMap<String, StandardPBEStringEncryptor> encryptors = new HashMap<>();

    public EncryptionService(StandardPBEStringEncryptor standardEncryptor, DatabaseProperties properties) {
        this.standardEncryptor = standardEncryptor;
        this.properties = properties;
    }

    @Override
    public String encrypt(String value) {
        return encrypt(value, standardEncryptor);
    }

    @Override
    public String encrypt(String value, String algorithm) {
        StandardPBEStringEncryptor encryptor = getEncryptor(algorithm);
        return encrypt(value, encryptor);
    }

    @Override
    public String decrypt(String value) {
        return decrypt(value, standardEncryptor);
    }

    @Override
    public String decrypt(String value, String algorithm) {
        StandardPBEStringEncryptor encryptor = getEncryptor(algorithm);
        return decrypt(value, encryptor);
    }

    private String encrypt(String value, StandardPBEStringEncryptor encryptor) {
        if (value != null && !value.contains(properties.getEncryptionPrefix())) {
            return properties.getEncryptionPrefix() + encryptor.encrypt(value);
        }
        return value;
    }

    private String decrypt(String value, StandardPBEStringEncryptor encryptor) {
        if (value != null && value.contains(properties.getEncryptionPrefix())) {
            return encryptor.decrypt(value.substring(properties.getEncryptionPrefix().length()));
        }
        return value;
    }

    private StandardPBEStringEncryptor getEncryptor(String algorithm) {
        StandardPBEStringEncryptor encryptor = encryptors.get(algorithm);

        if (encryptor != null)
            return encryptor;

        return createEncryptor(algorithm);
    }

    private StandardPBEStringEncryptor createEncryptor(String algorithm) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

        encryptor.setAlgorithm(algorithm);
        encryptor.setPassword(properties.getPassword());
        encryptor.setProvider(new BouncyCastleProvider());

        encryptors.put(algorithm, encryptor);

        return encryptor;
    }
}