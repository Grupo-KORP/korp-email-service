package com.korp.email.infrastructure.web;

import com.korp.email.application.usecase.EnviarPedidoDistribuidorUseCase;
import com.korp.email.application.usecase.EnviarSenhaProvisoriaUseCase;
import com.korp.email.application.usecase.EnviarTrocaSenhaUseCase;
import com.korp.email.application.usecase.command.EnviarPedidoDistribuidorCommand;
import com.korp.email.application.usecase.command.EnviarSenhaProvisoriaCommand;
import com.korp.email.application.usecase.command.EnviarTrocaSenhaCommand;
import com.korp.email.infrastructure.web.dto.EmailAceitoResponse;
import com.korp.email.infrastructure.web.dto.EnviarPedidoDistribuidorRequest;
import com.korp.email.infrastructure.web.dto.EnviarSenhaProvisoriaRequest;
import com.korp.email.infrastructure.web.dto.EnviarTrocaSenhaRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints consumidos pelo serviço principal KORP via OpenFeign.
 * O pedido só retorna 202 Accepted depois de o RabbitMQ confirmar a
 * publicação. O envio SMTP acontece no consumidor da fila.
 */
@RestController
@RequestMapping("/emails")
public class EmailController {

    private final EnviarSenhaProvisoriaUseCase enviarSenhaProvisoriaUseCase;
    private final EnviarTrocaSenhaUseCase enviarTrocaSenhaUseCase;
    private final EnviarPedidoDistribuidorUseCase enviarPedidoDistribuidorUseCase;

    public EmailController(EnviarSenhaProvisoriaUseCase enviarSenhaProvisoriaUseCase,
                            EnviarTrocaSenhaUseCase enviarTrocaSenhaUseCase,
                            EnviarPedidoDistribuidorUseCase enviarPedidoDistribuidorUseCase) {
        this.enviarSenhaProvisoriaUseCase = enviarSenhaProvisoriaUseCase;
        this.enviarTrocaSenhaUseCase = enviarTrocaSenhaUseCase;
        this.enviarPedidoDistribuidorUseCase = enviarPedidoDistribuidorUseCase;
    }

    @PostMapping("/senha-provisoria")
    public ResponseEntity<EmailAceitoResponse> enviarSenhaProvisoria(@Valid @RequestBody EnviarSenhaProvisoriaRequest request) {
        Long emailLogId = enviarSenhaProvisoriaUseCase.executar(
                new EnviarSenhaProvisoriaCommand(request.nome(), request.email(), request.senhaProvisoria()));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(EmailAceitoResponse.pendente(emailLogId));
    }

    @PostMapping("/troca-senha")
    public ResponseEntity<EmailAceitoResponse> enviarTrocaSenha(@Valid @RequestBody EnviarTrocaSenhaRequest request) {
        Long emailLogId = enviarTrocaSenhaUseCase.executar(
                new EnviarTrocaSenhaCommand(request.nome(), request.email(), request.urlRedefinicao()));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(EmailAceitoResponse.pendente(emailLogId));
    }

    @PostMapping("/pedido-distribuidor")
    public ResponseEntity<EmailAceitoResponse> enviarPedidoDistribuidor(@Valid @RequestBody EnviarPedidoDistribuidorRequest request) {
        Long emailLogId = enviarPedidoDistribuidorUseCase.executar(new EnviarPedidoDistribuidorCommand(
                request.emailDistribuidor(), request.nomeDistribuidor(),
                request.codigoPedido(), request.nomeCliente(), request.pdfBase64()));

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(EmailAceitoResponse.pendente(emailLogId));
    }
}
