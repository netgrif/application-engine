package com.netgrif.workflow.premiuminsurance;

import java.util.List;

public interface IPostalCodeService {

    void createPostalCode(String code, String locality, String region, String regionCode);

    void savePostalCode(PostalCode postalCode);

    List<PostalCode> findByCode(String code);

    List<PostalCode> findByLocality(String locality);
}