//package com.netgrif.workflow.ldap.domain;
//
//import lombok.Data;
//import org.springframework.ldap.odm.annotations.Attribute;
//import org.springframework.ldap.odm.annotations.Entry;
//import org.springframework.ldap.odm.annotations.Id;
//
//import javax.naming.Name;
//import java.util.List;
//
//
//@Entry(
//        objectClasses = {"groupOfNames"}
//)
//
//@Data
//public class LdapGroupRef {
//
//    @Id
//    private Name dn;
//
//    @Attribute(name = "cn")
//    private String cn;
//
//    @Attribute(name = "member")
//    private List<String> member;
//
//}
//
