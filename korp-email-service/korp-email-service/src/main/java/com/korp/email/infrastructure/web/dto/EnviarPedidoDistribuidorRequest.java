package com.korp.email.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EnviarPedidoDistribuidorRequest(
        @NotBlank @Email String emailDistribuidor,
        @NotBlank String nomeDistribuidor,
        @NotBlank String codigoPedido,
        @NotBlank String nomeCliente,
        @NotBlank String pdfBase64
) {
}
