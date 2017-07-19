package com.netgrif.workflow.psc;

import java.util.List;

public interface IPostalCodeService {

    void createPostalCode(String code, String locality, String regionCode);
    void savePostalCode(PostalCode postalCode);

    PostalCode findByCode(String code);

    List<PostalCode> findByLocality(String locality);

}
