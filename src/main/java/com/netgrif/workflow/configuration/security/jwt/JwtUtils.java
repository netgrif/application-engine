package com.netgrif.workflow.configuration.security.jwt;

import com.netgrif.workflow.auth.domain.Authority;
import com.netgrif.workflow.auth.domain.LoggedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;
import java.util.function.Function;

@Slf4j
public class JwtUtils {
    private static final long EXPIRATION_TIME = 900000;
    private static final String SECRET = "SECRET_KEY";

    public static String tokenFrom(Map<String, Object> claims) {
        log.info("Generating new JWT token.");
        return Jwts.builder().addClaims(claims).setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET).compact();
    }

    public static boolean isExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        Date currentDate = new Date();
        return expiration.compareTo(currentDate) < 0;
    }

    public static <T> T getClaim(String token, String key, Class<T> tClass) {
        return getAllClaimsFromToken(token).get(key, tClass);
    }

    public static LoggedUser getLoggedUser(String token, Authority anonymousRole) {
        Map<String, Object> userMap = (LinkedHashMap)getAllClaimsFromToken(token).get("user");
        LoggedUser user = new LoggedUser(
                Long.getLong(userMap.get("id").toString()),
                userMap.get("username").toString(),
                userMap.get("password").toString(),
                Collections.singleton(anonymousRole)
        );
        return user;
    }

    private static Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private static <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private static Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
    }
}
