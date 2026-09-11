package com.korp.email.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EnviarSenhaProvisoriaRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank String senhaProvisoria
) {
}
