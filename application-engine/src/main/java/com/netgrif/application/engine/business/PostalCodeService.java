package com.netgrif.application.engine.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Service
public class PostalCodeService implements IPostalCodeService {

    @Autowired
    private PostalCodeRepository repository;

    @Override
    public void savePostalCodes(Collection<PostalCode> codes) {
        List<PostalCode> savedCodes = repository.saveAll(codes);
        if (savedCodes.isEmpty()) {
            throw new IllegalArgumentException("Could not save given postal codes");
        }
    }

    @Override
    public void createPostalCode(String code, String city) {
        repository.save(new PostalCode(code.replaceAll("\\s", "").trim(), city.trim()));
    }

    @Override
    public void savePostalCode(PostalCode postalCode) {
        repository.save(postalCode);
    }

    @Override
    public List<PostalCode> findAllByCode(String code) {
        if (code == null)
            return new LinkedList<>();
        return repository.findAllByCode(code.replaceAll("\\s", "").trim());
    }

    @Override
    public List<PostalCode> findAllByCity(String city) {
        if (city == null)
            return new LinkedList<>();
        return repository.findAllByCity(city.trim());
    }
}