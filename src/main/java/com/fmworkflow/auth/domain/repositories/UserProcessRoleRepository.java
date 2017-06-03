package com.fmworkflow.auth.domain.repositories;

import com.fmworkflow.auth.domain.UserProcessRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserProcessRoleRepository extends JpaRepository<UserProcessRole, Long>{
    List<UserProcessRole> findByRoleIdIn(Iterable<String> roleIds);
}
