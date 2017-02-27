package com.fmworkflow.auth.service;

import com.fmworkflow.auth.domain.Token;
import com.fmworkflow.auth.domain.TokenRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TokenServiceTest {

    @Autowired
    ITokenService service;

    @Autowired
    TokenRepository repository;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
    }

    @Test
    public void removeExpired() throws Exception {
        Token expiredToken = new Token();
        expiredToken.setEmail("test@test.com");
        expiredToken.setHashedToken("hashedtoken");
        expiredToken.setExpirationDate(LocalDateTime.now().minusDays(10));
        repository.save(expiredToken);
        Token newToken = new Token();
        newToken.setEmail("test@test.com");
        newToken.setHashedToken("hashedtoken");
        repository.save(newToken);

        service.removeExpired();

        assertContains(outContent, "Removed 1 tokens");
    }

    @Test
    public void authorizeToken() throws Exception {
        Token newToken = new Token();
        newToken.setEmail("test@test.com");
        newToken.setHashedToken("hashedtoken");
        repository.save(newToken);

        boolean authorized = service.authorizeToken("test@test.com", "hashedtoken");
        Token token = repository.findByEmail("test@test.com");

        assertTokenRemoved(authorized, token);
    }

    private void assertTokenRemoved(boolean authorized, Token token) {
        assert authorized;
        assert token == null;
    }


    private void assertContains(ByteArrayOutputStream haystack, String needle) {
        assert haystack.toString().contains(needle);
    }
}