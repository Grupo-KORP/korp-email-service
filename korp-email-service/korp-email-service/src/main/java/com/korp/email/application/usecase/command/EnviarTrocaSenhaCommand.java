package com.korp.email.application.usecase.command;

public record EnviarTrocaSenhaCommand(
        String nome,
        String email,
        String urlRedefinicao
) {
}
