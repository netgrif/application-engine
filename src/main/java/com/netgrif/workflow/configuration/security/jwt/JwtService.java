package com.netgrif.workflow.configuration.security.jwt;

import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.domain.LoggedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService implements IJwtService {

    @Value("${nae.security.jwt.expiration}")
    private final long EXPIRATION_TIME = 900000;

    @Value("${nae.security.jwt.private-key}")
    private String secretPath;

    private String secret = "";

    @PostConstruct
    private void resolveSecret(){
        try {
            secret = Base64.getEncoder().encodeToString(PrivateKeyReader.get(secretPath).getEncoded());
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public String tokenFrom(Map<String, Object> claims) {
        log.info("Generating new JWT token.");
        return Jwts.builder().addClaims(claims).setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    @Override
    public boolean isExpired(String token) {
        try {
            getExpirationDateFromToken(token);
        } catch (ExpiredJwtException e) {
            return true;
        }
        return false;
    }

    @Override
    public LoggedUser getLoggedUser(String token, Authority anonymousRole) {
        LinkedHashMap<String, Object> userMap = (LinkedHashMap<String, Object>)getAllClaimsFromToken(token).get("user");
        LoggedUser user = new LoggedUser(
                Long.parseLong(userMap.get("id").toString()),
                userMap.get("username").toString(),
                userMap.get("password").toString(),
                Collections.singleton(anonymousRole)
        );
        user.setFullName(userMap.get("fullName").toString());
        user.setAnonymous((boolean)userMap.get("anonymous"));
        return user;
    }

    private Date getExpirationDateFromToken(String token)  throws ExpiredJwtException {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) throws ExpiredJwtException {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) throws ExpiredJwtException {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }
}
