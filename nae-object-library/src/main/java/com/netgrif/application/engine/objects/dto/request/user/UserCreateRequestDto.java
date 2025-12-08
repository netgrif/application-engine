package com.netgrif.application.engine.objects.dto.request.user;

import java.io.Serializable;

public record UserCreateRequestDto(String username, String email, String firstName, String lastName, String password) implements Serializable {
}