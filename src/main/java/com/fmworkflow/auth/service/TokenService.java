package com.fmworkflow.auth.service;

import com.fmworkflow.auth.domain.Token;
import com.fmworkflow.auth.domain.TokenRepository;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenService implements ITokenService {

    private final Logger log = LoggerFactory.getLogger(TokenService.class);

    @Autowired
    private TokenRepository tokenRepository;

    @Scheduled(cron = "0 0 1 * * *")
    public void removeExpired() {
        log.info("Removing expired tokens");
        List<Token> removedTokens = tokenRepository.removeByExpirationDateBefore(DateTime.now().toDate());
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

    private boolean isValid(Token token) {
        return token != null && token.getExpirationDate().isAfterNow();
    }
}
