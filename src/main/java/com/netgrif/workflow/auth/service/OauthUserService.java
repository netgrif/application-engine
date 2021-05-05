package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.event.events.user.UserRegistrationEvent;

public class OauthUserService extends UserService {

    @Override
    public User saveNew(User user) {
        addDefaultRole(user);
        addDefaultAuthorities(user);


        User savedUser = userRepository.save(user);
        groupService.createGroup(user);
        groupService.addUserToDefaultGroup(user);
        savedUser.setGroups(user.getGroups());
        upsertGroupMember(savedUser);
        publisher.publishEvent(new UserRegistrationEvent(savedUser));

        return savedUser;
    }
}
