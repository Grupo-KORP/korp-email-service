package com.korp.email.application.template;

/**
 * Monta o corpo HTML de cada tipo de e-mail.
 */
public final class EmailTemplates {

    private EmailTemplates() {
    }

    public static String senhaProvisoria(String nome, String senhaAleatoria) {
        return base(
                "Olá, " + nome + " 👋",
                "<p style=\"font-size: 16px; color: #555;\">" +
                        "  Seu acesso ao <strong>Sistema de Comissões da TND</strong> foi criado com sucesso." +
                        "</p>" +
                        "<p style=\"font-size: 16px; color: #555;\">" +
                        "  Para realizar seu primeiro acesso, utilize a senha provisória abaixo:" +
                        "</p>" +
                        "<div style=\"background-color: #eef3ff; border: 2px dashed #2F5BFF; border-radius: 12px; padding: 20px; text-align: center; margin: 30px 0;\">" +
                        "  <span style=\"font-size: 28px; font-weight: bold; color: #132B7A; letter-spacing: 3px;\">" + senhaAleatoria + "</span>" +
                        "</div>" +
                        "<p style=\"font-size: 15px; color: #666;\">" +
                        "  Por segurança, recomendamos que você altere sua senha após acessar o sistema pela primeira vez." +
                        "</p>",
                "Caso você tenha recebido este e-mail por engano, apenas ignore esta mensagem."
        );
    }

    public static String trocaSenha(String nome, String url) {
        return base(
                "Olá, " + nome + " 👋",
                "<p style=\"font-size: 16px; color: #555;\">" +
                        "  Recebemos uma solicitação para redefinir a senha da sua conta no <strong>Sistema de Comissões da TND</strong>." +
                        "</p>" +
                        "<p style=\"font-size: 16px; color: #555;\">Clique no botão abaixo para criar uma nova senha:</p>" +
                        "<div style=\"text-align: center; margin: 35px 0;\">" +
                        "  <a href=\"" + url + "\" style=\"background: linear-gradient(90deg, #132B7A, #4D87C7); color: white; padding: 14px 32px;" +
                        "     text-decoration: none; font-size: 16px; font-weight: bold; border-radius: 10px; display: inline-block; letter-spacing: 0.5px;\">" +
                        "    Redefinir Senha" +
                        "  </a>" +
                        "</div>",
                "Se você não solicitou a troca de senha, apenas ignore este e-mail. O link expira automaticamente após algum tempo."
        );
    }

    public static String pedidoDistribuidor(String nomeDistribuidor, String codigoPedido, String nomeCliente) {
        return base(
                "Olá, " + nomeDistribuidor + " 👋",
                "<p style=\"font-size: 16px; color: #555;\">" +
                        "  Um novo pedido foi registrado no <strong>Sistema de Comissões da TND</strong> e está associado à sua distribuidora." +
                        "</p>" +
                        "<div style=\"background-color: #eef3ff; border: 2px dashed #2F5BFF; border-radius: 12px; padding: 20px; margin: 30px 0;\">" +
                        "  <p style=\"margin: 0 0 8px; font-size: 13px; color: #666; text-transform: uppercase; letter-spacing: 1px;\">Código do Pedido</p>" +
                        "  <span style=\"font-size: 26px; font-weight: bold; color: #132B7A; letter-spacing: 3px;\">" + codigoPedido + "</span>" +
                        "  <hr style=\"border: none; border-top: 1px solid #c8d6f5; margin: 16px 0;\">" +
                        "  <p style=\"margin: 0; font-size: 15px; color: #444;\">Cliente: <strong>" + nomeCliente + "</strong></p>" +
                        "</div>" +
                        "<p style=\"font-size: 15px; color: #555;\">O PDF completo do pedido está anexado a este e-mail para sua referência.</p>",
                "Caso tenha dúvidas sobre este pedido, entre em contato com a equipe responsável."
        );
    }

    private static String base(String titulo, String conteudoPrincipal, String avisoRodape) {
        return "<div style=\"font-family: Arial, sans-serif; background-color: #f4f4f7; padding: 40px 20px; color: #1b1b1b;\">" +
                "  <div style=\"max-width: 600px; margin: auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08);\">" +
                "    <div style=\"background: linear-gradient(90deg, #132B7A, #4D87C7); padding: 30px; text-align: center;\">" +
                "      <h1 style=\"color: white; margin: 0; font-size: 28px;\">TND Brasil</h1>" +
                "      <p style=\"color: #dfe9ff; margin-top: 8px; font-size: 14px; letter-spacing: 1px;\">Sistema de Comissões</p>" +
                "    </div>" +
                "    <div style=\"padding: 40px 35px;\">" +
                "      <h2 style=\"color: #132B7A; margin-top: 0; font-size: 24px;\">" + titulo + "</h2>" +
                conteudoPrincipal +
                "      <div style=\"margin-top: 20px; padding: 18px; background-color: #f8f9fc; border-left: 4px solid #2F5BFF; border-radius: 8px;\">" +
                "        <p style=\"margin: 0; color: #555; font-size: 14px;\">" + avisoRodape + "</p>" +
                "      </div>" +
                "      <p style=\"margin-top: 40px; font-size: 15px; color: #444;\">" +
                "        Atenciosamente,<br><strong style=\"color: #132B7A;\">Equipe TND Brasil</strong>" +
                "      </p>" +
                "    </div>" +
                "    <div style=\"background-color: #f1f4fa; text-align: center; padding: 18px; font-size: 12px; color: #888;\">" +
                "      © 2026 TND Brasil • Sistema de Comissões" +
                "    </div>" +
                "  </div>" +
                "</div>";
    }
}
