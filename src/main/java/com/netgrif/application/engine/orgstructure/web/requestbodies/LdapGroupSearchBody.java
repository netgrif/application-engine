package com.netgrif.application.engine.orgstructure.web.requestbodies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LdapGroupSearchBody {

    private String fulltext;

}
