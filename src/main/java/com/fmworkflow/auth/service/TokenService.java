package com.fmworkflow.auth.service;

import com.fmworkflow.auth.domain.Token;
import com.fmworkflow.auth.domain.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TokenService implements ITokenService {

    private final Logger log = LoggerFactory.getLogger(TokenService.class);

    @Autowired
    private TokenRepository tokenRepository;

    @Scheduled(cron = "0 0 1 * * *")
    public void removeExpired() {
        log.info("Removing expired tokens");
        List<Token> removedTokens = tokenRepository.removeByExpirationDateBefore(LocalDateTime.now());
        log.info("Removed " + removedTokens.size() + " tokens");
    }

    @Override
    public boolean authorizeToken(String email, String token) {
        Token entity = tokenRepository.findByEmail(email);

        if (isValid(entity) && entity.getHashedToken().equals(token)) {
            tokenRepository.delete(entity);
            return true;
        }
        return false;
    }

    @Override
    public Token createToken(String email) {
        String hash = new BigInteger(260, new SecureRandom()).toString(32);
        Token token = new Token(email, hash);

        tokenRepository.deleteAllByEmail(email);
        tokenRepository.save(token);

        return token;
    }

    @Override
    public String getEmail(String token) {
        Token entity = tokenRepository.findByHashedToken(token);
        if (entity != null) return entity.getEmail();
        return null;
    }

    private boolean isValid(Token token) {
        return token != null && token.getExpirationDate().isAfter(LocalDateTime.now());
    }
}
