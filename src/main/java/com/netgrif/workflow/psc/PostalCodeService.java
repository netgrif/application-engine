package com.netgrif.workflow.psc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostalCodeService implements IPostalCodeService {

    @Autowired
    private PostalCodeRepository repository;

    @Override
    public void createPostalCode(String code, String locality, String region, String regionCode) {
        repository.save(new PostalCode(code.replaceAll("\\s","").trim(),locality.trim(),region.trim(),regionCode.trim()));
    }

    @Override
    public void savePostalCode(PostalCode postalCode) {
        repository.save(postalCode);
    }

    @Override
    public List<PostalCode> findByCode(String code) {
        return repository.findByCode(code.replaceAll("\\s","").trim());
    }

    @Override
    public List<PostalCode> findByLocality(String locality) {
        return repository.findByLocality(locality.trim());
    }
}
