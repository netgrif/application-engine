package com.netgrif.workflow.configuration.security;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import com.netgrif.workflow.configuration.security.interfaces.IAuthenticationService;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Service
public class AuthenticationService implements IAuthenticationService {


    @Value("${server.login.attempts:10}")
    private int maxLoginAttempts;

    @Value("${server.login.timeout:15}")
    private int loginTimeout;


    private ConcurrentMap<String, Attempt> cache;

    public AuthenticationService() {
        super();
        cache = new ConcurrentHashMap<>();
    }

    public static String getClientIP(HttpServletRequest request) {
        String xHeader = request.getHeader("X-Forwarded-For");
        if (xHeader == null)
            return request.getRemoteAddr();
        return xHeader.split(",")[0];

    }

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        loginFailed(((WebAuthenticationDetails) event.getAuthentication().getDetails()).getRemoteAddress());
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        loginSucceeded(((WebAuthenticationDetails) event.getAuthentication().getDetails()).getRemoteAddress());
    }

    @Override
    public void loginSucceeded(String key) {
        cache.remove(key);
    }

    @Override
    public void loginFailed(String key) {
        timeout(key);
        Attempt attempt = cache.getOrDefault(key, new Attempt());
        attempt.increase();
        if (attempt.getCount() >= maxLoginAttempts)
            attempt.setBlockTime(LocalDateTime.now());

        cache.put(key, attempt);
    }

    @Override
    public boolean isIPBlocked(String key) {
        timeout(key);
        return cache.get(key) != null && cache.get(key).getBlockTime() != null;
    }

    private void timeout(String key) {
        Attempt attempt = cache.get(key);
        if (attempt == null || attempt.getBlockTime() == null)
            return;
        if (ChronoUnit.SECONDS.between(attempt.getBlockTime(), LocalDateTime.now()) >= loginTimeout)
            cache.remove(key);

    }

    @Data
    public class Attempt {

        private Integer count;
        private LocalDateTime blockTime;

        public Attempt() {
            count = 0;
        }


        public void increase() {
            this.count++;
        }
    }

}