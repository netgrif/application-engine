package com.netgrif.workflow.auth.domain.repositories;

import com.netgrif.workflow.auth.domain.UnactivatedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public interface UnactivatedUserRepository extends JpaRepository<UnactivatedUser, Long> {

    UnactivatedUser findByEmail(String email);

    UnactivatedUser findByToken(String token);

    List<UnactivatedUser> removeByExpirationDateBefore(LocalDateTime date);

    void deleteAllByEmail(String email);
}
