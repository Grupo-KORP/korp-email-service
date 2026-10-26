# korp-email-service

Microsserviço responsável pelo envio de e-mails do Sistema de Comissões KORP/TND Brasil,
extraído do serviço principal e chamado por ele via OpenFeign.

## Estrutura (Clean Architecture)

```
domain/          -> regras e contratos puros, sem Spring
  model/           EmailMessage, Attachment, EmailLog, EmailType, EmailStatus
  gateway/         EmailLogRepository, EmailDispatcherGateway (portas/interfaces)
  exception/       EmailSendException

application/     -> orquestra o domínio, também sem depender de Spring
  usecase/         EnviarSenhaProvisoriaUseCase, EnviarTrocaSenhaUseCase, EnviarPedidoDistribuidorUseCase
  usecase/command/ inputs de cada usecase
  template/        EmailTemplates (HTML de cada tipo de e-mail)

infrastructure/  -> tudo que é detalhe técnico/framework
  web/             EmailController + DTOs (o que o OpenFeign do KORP chama)
  persistence/     JPA entity + Spring Data repository + adapter da porta EmailLogRepository
  mail/            EmailSenderService (JavaMailSender) — envia mensagens recebidas da fila
  messaging/       RabbitEmailDispatcherGateway, RabbitEmailConsumer e contrato interno JSON da fila
  config/          RabbitMqConfig (exchange, filas, retry e DLQ), UseCaseConfig
```

A regra de dependência é sempre pra dentro: `infrastructure` conhece `application` e `domain`;
`application` conhece só `domain`; `domain` não conhece ninguém.

## Fluxo de um envio

1. KORP (serviço principal) chama, via Feign, um dos endpoints em `EmailController`.
2. O controller monta um `Command` e chama o usecase correspondente.
3. O usecase monta a `EmailMessage`, cria um `EmailLog` com status `PENDENTE` e salva (rápido, síncrono).
4. O usecase chama `EmailDispatcherGateway.despachar(...)`, que aguarda a confirmação de publicação,
   e então o controller responde **202 Accepted** com o `emailLogId`.
5. `RabbitEmailConsumer` recebe a mensagem persistente e `EmailSenderService` envia via `JavaMailSender`.
   O log passa para `ENVIADO`; depois de três tentativas sem sucesso, passa para `FALHOU` e a mensagem
   segue para a DLQ.

A resposta `202 Accepted` só é enviada depois da confirmação do RabbitMQ. Se a publicação falhar, o log
passa para `FALHOU` e a API responde `503 Service Unavailable`.

## Endpoints

| Método | Rota                          | Body                                                                 |
|--------|-------------------------------|-----------------------------------------------------------------------|
| POST   | `/emails/senha-provisoria`    | `{ nome, email, senhaProvisoria }`                                    |
| POST   | `/emails/troca-senha`         | `{ nome, email, urlRedefinicao }`                                     |
| POST   | `/emails/pedido-distribuidor` | `{ emailDistribuidor, nomeDistribuidor, codigoPedido, nomeCliente, pdfBase64 }` |

Todas respondem `202 Accepted` com `{ emailLogId, status: "PENDENTE" }`.

## Exemplo do lado do KORP (client OpenFeign)

Isso NÃO faz parte deste repositório — é o que você vai criar no serviço principal (KORP) pra chamar este aqui:

```java
@FeignClient(name = "korp-email-service", url = "${korp.email-service.url}")
public interface EmailServiceClient {

    @PostMapping("/emails/senha-provisoria")
    EmailAceitoResponse enviarSenhaProvisoria(@RequestBody EnviarSenhaProvisoriaRequest request);

    @PostMapping("/emails/troca-senha")
    EmailAceitoResponse enviarTrocaSenha(@RequestBody EnviarTrocaSenhaRequest request);

    @PostMapping("/emails/pedido-distribuidor")
    EmailAceitoResponse enviarPedidoDistribuidor(@RequestBody EnviarPedidoDistribuidorRequest request);
}
```

Os records `EnviarSenhaProvisoriaRequest`, `EnviarTrocaSenhaRequest`, `EnviarPedidoDistribuidorRequest`
e `EmailAceitoResponse` precisam existir também do lado do KORP (mesma forma, é o contrato entre os dois
serviços) — pode copiar os campos dos DTOs deste projeto em `infrastructure/web/dto`.

Pra converter o `byte[]` do PDF (que hoje é gerado com jsPDF ou montado no backend) em `pdfBase64`:

```java
String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);
```

## Variáveis de ambiente

- `DB_USERNAME`, `DB_PASSWORD` — Postgres
- `MAIL_USERNAME`, `MAIL_PASSWORD` — conta do Gmail usada pra enviar (senha de app, não a senha normal)
- `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` — conexão com o broker
- `RABBITMQ_EMAIL_EXCHANGE`, `RABBITMQ_EMAIL_QUEUE`, `RABBITMQ_EMAIL_DLX`, `RABBITMQ_EMAIL_DLQ` — nomes de topologia
- `RABBITMQ_PUBLISHER_CONFIRM_TIMEOUT_MS` — limite de espera pela confirmação de publicação (padrão: 5000 ms)

Para desenvolvimento local, o broker pode ser iniciado com `docker compose up -d` dentro de `rabbit-mq/`.
Configure `RABBITMQ_USERNAME` e `RABBITMQ_PASSWORD` com os valores definidos no `compose.yaml` desse diretório.

As mensagens persistentes contêm o corpo do e-mail e, no caso do pedido de distribuidor, o PDF em Base64.
Proteja o acesso e as credenciais do RabbitMQ e evite inspecionar ou registrar o payload.

## Próximos passos sugeridos

- Endpoint `GET /emails/{id}` pra consultar o status de um envio (útil pro KORP checar se deu certo).
- Testes unitários dos usecases com mocks das portas (`EmailLogRepository`, `EmailDispatcherGateway`) —
  como eles não dependem de Spring, é só JUnit + Mockito puro, sem subir contexto.
