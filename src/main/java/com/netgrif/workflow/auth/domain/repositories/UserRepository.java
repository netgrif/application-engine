package com.netgrif.workflow.auth.domain.repositories;

import com.netgrif.workflow.orgstructure.domain.Group;
import com.netgrif.workflow.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    User findBySurname(String surname);

//    List<User> findByOrganizationsIn(List<Group> orgs);

    List<User> findByUserProcessRoles_RoleIdIn(List<String> roleId);
}