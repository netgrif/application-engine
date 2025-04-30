package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl;
import com.netgrif.application.engine.auth.repository.AuthorityRepository;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class AuthorityServiceImpl implements AuthorityService {

    private AuthorityRepository authorityRepository;

    @Autowired
    public void setAuthorityRepository(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    @Override
    public List<Authority> findAll() {
        return authorityRepository.findAll();
    }

    @Override
    @Transactional
    public Authority getOrCreate(String s) {
        Optional<Authority> authority = authorityRepository.findById(s);
        return authority.orElseGet(() -> authorityRepository.save(new AuthorityImpl(s)));
    }

    @Override
    @Transactional
    public Authority getOrCreatePermission(String s) {
        return getOrCreate(s);
    }

    @Override
    @Transactional
    public Authority getOrCreateRole(String s) {
        return getOrCreate(s);
    }

    //TODO: this was never used
    @Override
    public List<Authority> getAllPermissions() {
        return List.of();
    }

    //TODO: this was never used
    @Override
    public List<Authority> getAllRoles() {
        return List.of();
    }

    @Override
    public Authority getOne(String s) {
        return authorityRepository.findById(s).orElseThrow(() -> new IllegalArgumentException("Authority with id " + s + " not found"));
    }

    @Override
    public List<Authority> findAllByIds(List<String> ids) {
        return authorityRepository.findAllBy_idIn(ids.stream().map(ObjectId::new).collect(Collectors.toList()));
    }
}
