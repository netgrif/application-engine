package com.netgrif.workflow.psc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostalCodeService implements IPostalCodeService {

    @Autowired
    private PostalCodeRepository repository;
    @Autowired
    private PostalCodePostRequestService postRequestService;

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
        List<PostalCode> results = repository.findByCode(code.replaceAll("\\s","").trim());
        if(results.isEmpty()){
            //TODO: ošetriť requesty na poštu
//            results = postRequestService.getByCode(code);
        }
        return results;
    }

    @Override
    public List<PostalCode> findByLocality(String locality) {
        return repository.findByLocality(locality.trim());
    }
}
