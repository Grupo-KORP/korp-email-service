package com.korp.email.application.usecase.command;

public record EnviarPedidoDistribuidorCommand(
        String emailDistribuidor,
        String nomeDistribuidor,
        String codigoPedido,
        String nomeCliente,
        String pdfBase64
) {
}
