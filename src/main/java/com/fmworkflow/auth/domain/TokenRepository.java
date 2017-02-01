package com.fmworkflow.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Transactional
public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findByEmail(String email);
    List<Token> removeByExpirationDateBefore(Date now);
}
