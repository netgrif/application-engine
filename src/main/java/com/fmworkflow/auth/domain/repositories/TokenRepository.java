package com.fmworkflow.auth.domain.repositories;


import com.fmworkflow.auth.domain.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findByEmail(String email);

    Token findByHashedToken(String token);

    List<Token> removeByExpirationDateBefore(LocalDateTime now);

    void deleteAllByEmail(String email);
}
