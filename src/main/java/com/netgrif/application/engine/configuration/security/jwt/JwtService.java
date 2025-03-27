package com.netgrif.application.engine.configuration.security.jwt;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService implements IJwtService {
    // todo javadoc everywhere
    private String secret = "";

    private final JwtProperties properties;

    @PostConstruct
    private void resolveSecret() {
        try {
            PrivateKeyReader reader = new PrivateKeyReader(properties.getAlgorithm());
            secret = Base64.getEncoder().encodeToString(reader.get(properties.getPrivateKey().getFile().getPath()).getEncoded());
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error while resolving secret key: {}", e.getMessage(), e);
        }
    }

    @Override
    public String tokenFrom(Map<String, Object> claims) {
        log.info("Generating new JWT token.");
        return Jwts.builder().addClaims(claims).setExpiration(new Date(System.currentTimeMillis() + properties.getExpiration()))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    @Override
    public void isExpired(String token) throws ExpiredJwtException {
        getExpirationDateFromToken(token);
    }

    @Override
    @SuppressWarnings("unchecked")
    public LoggedIdentity getLoggedIdentity(String token) {
        LinkedHashMap<String, Object> loggedIdentityMap = (LinkedHashMap<String, Object>) getAllClaimsFromToken(token).get("identity");
        return LoggedIdentity.builder()
                .identityId(loggedIdentityMap.get("identityId").toString())
                .username(loggedIdentityMap.get("username").toString())
                .fullName(loggedIdentityMap.get("fullName").toString())
                .activeActorId(loggedIdentityMap.get("activeActorId").toString())
                .build();
    }

    private void getExpirationDateFromToken(String token) throws ExpiredJwtException {
        getClaimFromToken(token, Claims::getExpiration);
    }

    private <T> void getClaimFromToken(String token, Function<Claims, T> claimsResolver) throws ExpiredJwtException {
        final Claims claims = getAllClaimsFromToken(token);
        claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) throws ExpiredJwtException {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }
}
