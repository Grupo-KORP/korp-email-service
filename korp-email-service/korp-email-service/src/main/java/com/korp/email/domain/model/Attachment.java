package com.korp.email.domain.model;

/**
 * Anexo de um e-mail. O conteúdo trafega em Base64 (é assim que o
 * serviço principal KORP envia via OpenFeign) e só é decodificado
 * no momento do envio, dentro da infraestrutura.
 */
public record Attachment(
        String nomeArquivo,
        String conteudoBase64,
        String contentType
) {
}
