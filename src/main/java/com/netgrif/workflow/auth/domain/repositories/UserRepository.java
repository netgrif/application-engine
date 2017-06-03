package com.netgrif.workflow.auth.domain.repositories;

import com.netgrif.workflow.auth.domain.Organization;
import com.netgrif.workflow.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    User findBySurname(String surname);

    List<User> findByOrganizationsIn(List<Organization> orgs);

    @Query("SELECT user FROM user LEFT OUTER JOIN user_process_roles\n" +
            "  ON user.id = user_process_roles.user_id\n" +
            "  LEFT OUTER JOIN  user_process_role ON user_process_roles.user_process_role_id = user_process_role.id\n" +
            "  WHERE role_id = ?1")
    List<User> findByUserProcessRolesId(String roleId);
}