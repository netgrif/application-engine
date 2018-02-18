package com.netgrif.workflow.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.PersistenceException;
import java.util.Collection;
import java.util.List;

@Service
public class PostalCodeService implements IPostalCodeService {

    @Autowired
    private PostalCodeRepository repository;

    @Override
    public void savePostalCodes(Collection<PostalCode> codes) {
        List<PostalCode> savedCodes = repository.save(codes);
        if (savedCodes == null || savedCodes.isEmpty()) {
            throw new PersistenceException("Could not save given postal codes");
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
        List<PostalCode> results = repository.findAllByCode(code.replaceAll("\\s", "").trim());
        //TODO: ošetriť requesty na poštu
//        if (results.isEmpty()) { }
        return results;
    }

    @Override
    public List<PostalCode> findAllByCity(String city) {
        List<PostalCode> results = repository.findAllByCity(city.trim());
        //TODO: ošetriť requesty na poštu
//        if (results.isEmpty()) { }
        return results;
    }
}