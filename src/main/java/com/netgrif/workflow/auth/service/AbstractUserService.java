package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.*;
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.orgstructure.domain.Member;
import com.netgrif.workflow.orgstructure.groups.interfaces.INextGroupService;
import com.netgrif.workflow.orgstructure.service.IMemberService;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.service.interfaces.IProcessRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractUserService implements IUserService {

    @Autowired
    protected AuthorityRepository authorityRepository;

    @Autowired
    protected IProcessRoleService processRoleService;

    @Autowired
    protected IMemberService memberService;

    @Autowired
    protected INextGroupService groupService;

    @Override
    public Member upsertGroupMember(IUser user) {
        Member member = memberService.findByEmail(user.getEmail());
        if (member == null)
            member = new Member(user.getStringId(), user.getName(), user.getSurname(), user.getEmail());
        member.setGroups(user.getGroups());
        return memberService.save(member);
    }

    @Override
    public void addDefaultRole(IUser user) {
        user.addProcessRole(processRoleService.defaultRole());
    }

    @Override
    public void addDefaultAuthorities(IUser user) {
        if (user.getAuthorities().isEmpty()) {
            HashSet<Authority> authorities = new HashSet<>();
            authorities.add(authorityRepository.findByName(Authority.user));
            user.setAuthorities(authorities);
        }
    }

    @Override
    public void assignAuthority(String userId, String authorityId) {
        Optional<IUser> user = get(userId);
        Optional<Authority> authority = authorityRepository.findById(authorityId);

        if (!user.isPresent())
            throw new IllegalArgumentException("Could not find user with id ["+userId+"]");
        if (!authority.isPresent())
            throw new IllegalArgumentException("Could not find authority with id ["+authorityId+"]");

        user.get().addAuthority(authority.get());
        authority.get().addUser(user.get());

        save(user.get());
    }

    @Override
    public LoggedUser getAnonymousLogged() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals(UserProperties.ANONYMOUS_AUTH_KEY)) {
            getLoggedUser().transformToLoggedUser();
        }
        return (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public IUser addRole(IUser user, String roleStringId) {
        ProcessRole role = processRoleService.findById(roleStringId);
        user.addProcessRole(role);
        return save(user);
    }

    @Override
    public IUser removeRole(IUser user, String roleStringId) {
        ProcessRole role = processRoleService.findByImportId(roleStringId);
        user.removeProcessRole(role);
        return save(user);
    }

    public <T> Page<IUser> changeType(Page<T> users, Pageable pageable) {
        return new PageImpl<>(changeType(users.getContent()), pageable, users.getTotalElements());
    }

    public <T> List<IUser> changeType(List<T> users) {
        return users.stream().map(IUser.class::cast).collect(Collectors.toList());
    }

}