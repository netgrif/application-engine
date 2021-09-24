//package com.netgrif.workflow.ldap.service;
//
//
//import com.netgrif.workflow.ldap.domain.LdapGroupRef;
//import com.netgrif.workflow.ldap.domain.repository.LdapGroupRefRepository;
//import com.netgrif.workflow.ldap.service.interfaces.ILdapGroupRefService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
//@Service
//public class LdapGroupRefService implements ILdapGroupRefService {
//
//    @Autowired
//    private LdapGroupRefRepository ldapGroupRefRepository;
//
//    @Override
//    public List<LdapGroupRef> getAllGroups() {
//        return ldapGroupRefRepository.findAll();
//    }
//
//
//    @Override
//    public List<String> getAllGroupsCn() {
//        List<String> groupsName = new ArrayList<>();
//        List<LdapGroupRef> groups = getAllGroups();
//        if (groups != null) {
//            groups.forEach(group -> {
//                groupsName.add(group.getCn());
//            });
//            return groupsName;
//        }
//        return null;
//    }
//
//
//    @Override
//    public List<String> getAllGroupsDn() {
//        List<String> groupsName = new ArrayList<>();
//        List<LdapGroupRef> groups = getAllGroups();
//        if (groups != null) {
//            groups.forEach(group -> {
//                groupsName.add(group.getDn().toString());
//            });
//            return groupsName;
//        }
//        return null;
//    }
//
//}