package com.fmworkflow.auth.domain.repositories;

import com.fmworkflow.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    User findBySurname(String surname);
}