package com.netgrif.workflow

import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.Organization
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository
import com.netgrif.workflow.auth.domain.repositories.OrganizationRepository
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository
import com.netgrif.workflow.auth.domain.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SuperCreator {

    @Autowired
    private OrganizationRepository organizationRepository
    @Autowired
    private AuthorityRepository authorityRepository
    @Autowired
    private UserProcessRoleRepository processRoleRepository
    @Autowired
    private UserRepository userRepository

    void run(String... strings) {
        Authority adminAuthority = authorityRepository.findByName(Authority.admin)
        if (adminAuthority == null)
            adminAuthority = authorityRepository.save(new Authority(Authority.admin))

        userRepository.save(new User(
                name: "Super",
                surname: "Trooper",
                email: "super@netgrif.com",
                password: "password",
                authorities: [adminAuthority] as Set<Authority>,
                organizations: organizationRepository.findAll() as Set<Organization>,
                userProcessRoles: processRoleRepository.findAll() as Set<UserProcessRole>))
    }
}
