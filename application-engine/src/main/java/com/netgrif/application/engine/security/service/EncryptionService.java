package com.netgrif.application.engine.security.service;

import com.netgrif.application.engine.configuration.properties.SecurityConfigurationProperties;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class EncryptionService implements IEncryptionService {

    private final String PREFIX = "#encrypted";

    @Autowired
    private StandardPBEStringEncryptor standardEncryptor;

    @Autowired
    private SecurityConfigurationProperties.EncryptionProperties encryptionProperties;

    private HashMap<String, StandardPBEStringEncryptor> encryptors = new HashMap<>();

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
        if (value != null && !value.contains(PREFIX)) {
            return PREFIX + encryptor.encrypt(value);
        }
        return value;
    }

    private String decrypt(String value, StandardPBEStringEncryptor encryptor) {
        if (value != null && value.contains(PREFIX)) {
            return encryptor.decrypt(value.substring(PREFIX.length()));
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
        encryptor.setPassword(encryptionProperties.getPassword());
        encryptor.setProvider(new BouncyCastleProvider());

        encryptors.put(algorithm, encryptor);

        return encryptor;
    }
}