package com.korp.email.application.usecase.command;

public record EnviarSenhaProvisoriaCommand(
        String nome,
        String email,
        String senhaProvisoria
) {
}
