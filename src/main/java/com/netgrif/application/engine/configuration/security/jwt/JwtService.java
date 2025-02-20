package com.netgrif.application.engine.configuration.security.jwt;

import com.netgrif.adapter.auth.domain.LoggedUserImpl;
import com.netgrif.core.auth.domain.Attribute;
import com.netgrif.core.auth.domain.LoggedUser;
import com.netgrif.adapter.auth.service.AuthorityService;
import com.netgrif.adapter.petrinet.service.ProcessRoleService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService implements IJwtService {
    private byte[] secret;
    private final JwtProperties properties;
    private final ProcessRoleService roleService;
    private final AuthorityService authorityService;

    @PostConstruct
    private void resolveSecret() {
        try {
            PrivateKeyReader reader = new PrivateKeyReader(properties.getAlgorithm());
            secret = reader.get(properties.getPrivateKey().getFile().getPath()).getEncoded();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error while resolving secret key: " + e.getMessage(), e);
        }
    }

    @Override
    public String tokenFrom(Map<String, Object> header, String subject, Map<String, Object> claims) {
        log.info("Generating new JWT token.");
        return Jwts
                .builder()
                .setHeader(header)
                .setSubject(subject)
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + properties.getExpiration()))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return false;
        }
    }

    @Override
    public LoggedUser getLoggedUser(String token, String authority) {
        LinkedHashMap<String, Object> userMap = (LinkedHashMap<String, Object>) extractAllClaims(token).get("user");
        LoggedUser user = new LoggedUserImpl(
                userMap.get("id").toString(),
                userMap.get("username").toString(),
                userMap.get("password").toString(),
                Collections.singleton(authorityService.getOrCreate(authority)),
                Collections.singleton(roleService.anonymousRole())
        );
        user.setFirstName(userMap.get("firstName").toString());
        user.getAttributes().put("anonymous", new Attribute<>(true, false));
        user.setProcessRoles(Collections.singleton(roleService.anonymousRole()));
        return user;
    }

    private Date extractExpiration(String token) throws ExpiredJwtException {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) throws ExpiredJwtException {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) throws ExpiredJwtException {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(secret);
    }
}
