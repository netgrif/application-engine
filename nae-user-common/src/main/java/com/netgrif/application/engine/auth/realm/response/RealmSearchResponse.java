package com.netgrif.application.engine.auth.realm.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.netgrif.application.engine.auth.realm.RealmDto;
import org.springframework.data.domain.Page;
//TODO!
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RealmSearchResponse(int found,
                                  long total,
                                  Page<RealmDto> content) {

}
