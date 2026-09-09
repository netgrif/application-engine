package com.netgrif.application.engine.auth.web;

import com.netgrif.application.engine.adapter.spring.common.web.responsebodies.ResponseMessage;
import com.netgrif.application.engine.auth.service.AuthorityService;
import com.netgrif.application.engine.auth.web.requestbodies.NewAuthorityRequest;
import com.netgrif.application.engine.auth.web.responsebodies.AuthorityDto;
import com.netgrif.application.engine.objects.annotations.Authorize;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/authority")
@ConditionalOnProperty(
        value = "nae.user.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Authority")
public class AuthorityController {

    @Autowired
    private AuthorityService authorityService;

    @Authorize(authority = {"AUTHORITY_DELETE", "ADMIN"})
    @Operation(description = "Delete authority", security = {@SecurityRequirement(name = "BasicAuth")})
    @DeleteMapping(value = "/delete/{name}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource delete(@PathVariable String name, Authentication auth) {
        try {
            authorityService.delete(name);
            log.info("Authority [{}] has been deleted successfully.", name);
            return new MessageResource(ResponseMessage.createSuccessMessage("Authority [" + name + "] has been deleted successfully."));
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            log.error("Failed to delete authority [{}].", name, e);
            return new MessageResource(ResponseMessage.createErrorMessage("Failed to delete authority."));
        }
    }

    @Authorize(authority = {"AUTHORITY_CREATE", "ADMIN"})
    @Operation(description = "Delete authority", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<AuthorityDto> create(@RequestBody NewAuthorityRequest request) {
        try {
            Authority authority = authorityService.getOrCreate(request.name);
            log.info("Authority [{}] has been created successfully.", authority);
            return ResponseEntity.ok(new AuthorityDto(authority));
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            log.error("Failed to create authority [{}].", request.name, e);
            return null;
        }
    }

    @Authorize(authority = {"AUTHORITY_GET_ALL", "ADMIN"})
    @Operation(description = "Delete authority", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/all", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<Page<AuthorityDto>> getAll(Pageable pageable) {
        Page<Authority> authorities = authorityService.findAll(pageable);
        List<AuthorityDto> authorityDtoList = authorities.stream().map(AuthorityDto::new).toList();
        return ResponseEntity.ok(new PageImpl<>(authorityDtoList, pageable, authorities.getTotalElements()));
    }

    @Authorize(authority = {"AUTHORITY_VIEW", "ADMIN"})
    @Operation(description = "Delete authority", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{name}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<AuthorityDto> getOne(@PathVariable("name") String name) {
        Optional<Authority> authority = authorityService.findOptionalByName(name);
        if (authority.isPresent()) {
            return ResponseEntity.ok(new AuthorityDto(authority.get()));
        } else {
            log.error("Cannot find authority with name [{}].", name);
            return null;
        }
    }

    @Authorize(authority = {"AUTHORITY_VIEW", "ADMIN"})
    @Operation(description = "Delete authority", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/scope/{scope}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<List<AuthorityDto>> getAllByScope(@PathVariable("scope") String scope, Authentication auth) {
        List<Authority> authorities = authorityService.findByScope(scope);
        List<AuthorityDto> authorityDtoList = authorities.stream().map(AuthorityDto::new).toList();
        return ResponseEntity.ok(authorityDtoList);
    }
}
