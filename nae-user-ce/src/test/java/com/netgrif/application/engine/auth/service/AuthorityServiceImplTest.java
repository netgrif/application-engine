package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl;
import com.netgrif.application.engine.auth.config.AuthCacheProperties;
import com.netgrif.application.engine.auth.repository.AuthorityRepository;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorityServiceImplTest {

    private static final String AUTHORITY_ID = "authority-id";

    @Mock
    private AuthorityRepository authorityRepository;

    private AuthorityServiceImpl authorityService;

    @BeforeEach
    void setUp() {
        authorityService = new AuthorityServiceImpl();
        authorityService.setAuthorityRepository(authorityRepository);
        // injected by Spring in the application; default properties keep the cache enabled
        authorityService.setAuthCacheProperties(new AuthCacheProperties());
    }

    @Test
    void getOneLoadsAuthorityOnlyOnceWithDefaultCache() {
        Authority authority = new AuthorityImpl("ROLE_TEST");
        when(authorityRepository.findById(AUTHORITY_ID)).thenReturn(Optional.of(authority));

        assertSame(authority, authorityService.getOne(AUTHORITY_ID));
        assertSame(authority, authorityService.getOne(AUTHORITY_ID));

        verify(authorityRepository, times(1)).findById(AUTHORITY_ID);
    }

    @Test
    void getOneLoadsAuthorityOnlyOnceWithConfiguredCache() {
        Authority authority = new AuthorityImpl("ROLE_TEST");
        when(authorityRepository.findById(AUTHORITY_ID)).thenReturn(Optional.of(authority));
        AuthCacheProperties properties = new AuthCacheProperties();
        properties.getAuthority().setTtl(Duration.ofMinutes(5));
        properties.getAuthority().setMaxSize(10);
        authorityService.setAuthCacheProperties(properties);

        authorityService.getOne(AUTHORITY_ID);
        authorityService.getOne(AUTHORITY_ID);

        verify(authorityRepository, times(1)).findById(AUTHORITY_ID);
    }

    @Test
    void getOneDoesNotCacheWhenTtlIsZero() {
        Authority authority = new AuthorityImpl("ROLE_TEST");
        when(authorityRepository.findById(AUTHORITY_ID)).thenReturn(Optional.of(authority));
        AuthCacheProperties properties = new AuthCacheProperties();
        properties.getAuthority().setTtl(Duration.ZERO);
        authorityService.setAuthCacheProperties(properties);

        authorityService.getOne(AUTHORITY_ID);
        authorityService.getOne(AUTHORITY_ID);

        verify(authorityRepository, times(2)).findById(AUTHORITY_ID);
    }

    @Test
    void getOneThrowsAndDoesNotCacheWhenAuthorityDoesNotExist() {
        when(authorityRepository.findById(AUTHORITY_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authorityService.getOne(AUTHORITY_ID));
        assertEquals("Authority with id " + AUTHORITY_ID + " not found", exception.getMessage());
        assertThrows(IllegalArgumentException.class, () -> authorityService.getOne(AUTHORITY_ID));

        verify(authorityRepository, times(2)).findById(AUTHORITY_ID);
    }
}
