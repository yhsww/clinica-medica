import { useState } from "react";
import { URL_BASE_API, obterMensagemDeErro } from "../api";
import type { Usuario } from "../types";

type PropriedadesTelaLogin = {
  aoEntrar: (usuario: Usuario, token: string) => void;
  aoCadastrarSe: () => void;
};

export default function TelaLogin({
  aoEntrar,
  aoCadastrarSe,
}: PropriedadesTelaLogin) {
  const [email, definirEmail] = useState("");
  const [senha, definirSenha] = useState("");
  const [carregando, definirCarregando] = useState(false);
  const [errosDeValidacao, definirErrosDeValidacao] = useState<{
    email?: string;
    senha?: string;
  }>({});
  const [mensagemDeFeedback, definirMensagemDeFeedback] = useState({
    texto: "",
    tipo: null as "sucesso" | "erro" | null,
  });

  function limparValidacao() {
    definirErrosDeValidacao({});
    definirMensagemDeFeedback({ texto: "", tipo: null });
  }

  function validarFormulario(): boolean {
    const novosErrosDeValidacao: { email?: string; senha?: string } = {};
    let valido = true;

    if (!email.trim()) {
      novosErrosDeValidacao.email = "Informe o e-mail.";
      valido = false;
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      novosErrosDeValidacao.email = "E-mail inválido.";
      valido = false;
    }

    if (!senha) {
      novosErrosDeValidacao.senha = "Informe a senha.";
      valido = false;
    }

    definirErrosDeValidacao(novosErrosDeValidacao);
    return valido;
  }

  async function aoEnviarLogin(evento: React.FormEvent) {
    evento.preventDefault();
    limparValidacao();

    if (!validarFormulario()) {
      return;
    }

    definirCarregando(true);
    try {
      const resposta = await fetch(`${URL_BASE_API}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, senha }),
      });

      if (!resposta.ok) {
        definirMensagemDeFeedback({
          texto: await obterMensagemDeErro(
            resposta,
            "E-mail ou senha incorretos.",
          ),
          tipo: "erro",
        });
        return;
      }

      const dadosDaResposta = await resposta.json();
      if (!dadosDaResposta.token) {
        throw new Error("Token não recebido.");
      }

      const usuario: Usuario = {
        id: dadosDaResposta.id,
        nome: dadosDaResposta.nome,
        email: dadosDaResposta.email,
        papel: dadosDaResposta.papel.toLowerCase() as Usuario["papel"],
      };
      aoEntrar(usuario, dadosDaResposta.token);
    } catch {
      definirMensagemDeFeedback({
        texto: "Erro ao fazer login. Tente novamente.",
        tipo: "erro",
      });
    } finally {
      definirCarregando(false);
    }
  }

  return (
    <main>
      <h1>Login</h1>

      {mensagemDeFeedback.texto && (
        <div
          role="alert"
          aria-live="assertive"
          style={{ color: "red", marginBottom: "1rem" }}
        >
          {mensagemDeFeedback.texto}
        </div>
      )}

      <form onSubmit={aoEnviarLogin} noValidate>
        <div style={{ marginBottom: "1rem" }}>
          <label htmlFor="email">E-mail</label>
          <br />
          <input
            id="email"
            name="email"
            type="email"
            value={email}
            onChange={(evento) => definirEmail(evento.target.value)}
            required
            aria-invalid={!!errosDeValidacao.email}
            style={{ width: "100%", padding: "0.5rem", marginTop: "0.25rem" }}
          />
          {errosDeValidacao.email && (
            <span style={{ color: "red" }}>{errosDeValidacao.email}</span>
          )}
        </div>

        <div style={{ marginBottom: "1rem" }}>
          <label htmlFor="password">Senha</label>
          <br />
          <input
            id="password"
            name="password"
            type="password"
            value={senha}
            onChange={(evento) => definirSenha(evento.target.value)}
            required
            aria-invalid={!!errosDeValidacao.senha}
            style={{ width: "100%", padding: "0.5rem", marginTop: "0.25rem" }}
          />
          {errosDeValidacao.senha && (
            <span style={{ color: "red" }}>{errosDeValidacao.senha}</span>
          )}
        </div>

        <button
          type="submit"
          disabled={carregando}
          style={{ padding: "0.5rem 1rem", cursor: "pointer" }}
        >
          {carregando ? "Entrando..." : "Entrar"}
        </button>
      </form>

      <button
        type="button"
        onClick={aoCadastrarSe}
        style={{ marginTop: "1rem", padding: "0.5rem 1rem", cursor: "pointer" }}
      >
        Cadastrar-se
      </button>
    </main>
  );
}
