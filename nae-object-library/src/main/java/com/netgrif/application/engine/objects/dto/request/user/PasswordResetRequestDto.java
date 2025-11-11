package com.netgrif.application.engine.objects.dto.request.user;

import jakarta.annotation.Nullable;

public record PasswordResetRequestDto(String username,
                                      @Nullable
                                      String oldPassword,
                                      String newPassword,
                                      String realmId) {
}