package com.netgrif.application.engine.configuration.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.configuration.properties.SecurityConfigurationProperties;
import com.netgrif.application.engine.objects.auth.domain.Attribute;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.AuthorityService;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
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
    private final SecurityConfigurationProperties.JwtProperties properties;
    private final ProcessRoleService roleService;
    private final AuthorityService authorityService;
    private ObjectMapper objectMapper;

    @PostConstruct
    private void resolveSecret() {
        configureObjectMapper();
        try {
            PrivateKeyReader reader = new PrivateKeyReader(properties.getAlgorithm());
            secret = reader.get(properties.getPrivateKey()).getEncoded();
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
                .serializeToJsonWith(new JacksonSerializer<>(objectMapper))
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

        LoggedUser user = new LoggedUserImpl();
        user.setId((String) userMap.get("stringId"));
        user.setUsername((String) userMap.get("username"));
        user.setFirstName((String) userMap.get("firstName"));
        user.setMiddleName((String) userMap.get("middleName"));
        user.setLastName((String) userMap.get("lastName"));
        user.setAuthoritySet(Collections.singleton(authorityService.getOrCreate(authority)));
        user.setProcessRoles(Collections.singleton(roleService.getAnonymousRole()));
        user.getAttributes().put("anonymous", new Attribute<>(true, false));

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

    private void configureObjectMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }
}
