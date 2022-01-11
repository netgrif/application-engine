package com.netgrif.workflow.orgstructure.domain.ldap;

import lombok.Data;

import javax.naming.Name;
import java.util.List;


//TODO: JOZIKE objectClass
//@Entry(
//        objectClasses = {"groupOfNames"}
//)

@Data
public class LdapGroupRef {

//    @Id
    private Name dn;

//    @Attribute(name = "cn")
    private String cn;

//    @Attribute(name = "member")
    private List<String> member;

}
