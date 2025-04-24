package com.netgrif.application.engine.business;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

public interface IPostalCodeService {

    void savePostalCodes(Collection<PostalCode> codes);

    void createPostalCode(String code, String city);

    void savePostalCode(PostalCode postalCode);

    List<PostalCode> findAllByCode(@NotNull String code);

    List<PostalCode> findAllByCity(@NotNull String city);
}