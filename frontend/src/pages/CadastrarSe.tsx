import { useState } from "react";
import type { CSSProperties, FormEvent } from "react";
import { URL_BASE_API, obterMensagemDeErro } from "../api";
import { useOpcoesApi } from "../hooks/useOpcoesApi";

type PropriedadesTelaCadastrarSe = {
  aoVoltar: () => void;
};

type TipoDeCadastro = "PACIENTE" | "MEDICO";

const estiloRotulo: CSSProperties = {
  display: "block",
  marginTop: "0.75rem",
  marginBottom: "0.25rem",
};

const estiloEntrada: CSSProperties = {
  padding: "0.4rem",
  border: "1px solid #ccc",
  borderRadius: "4px",
  minWidth: "260px",
};

const estiloBotao: CSSProperties = {
  cursor: "pointer",
  padding: "0.4rem 0.8rem",
  marginRight: "0.5rem",
  marginTop: "1rem",
};

export default function TelaCadastrarSe({
  aoVoltar,
}: PropriedadesTelaCadastrarSe) {
  const [tipoDeCadastro, definirTipoDeCadastro] =
    useState<TipoDeCadastro>("PACIENTE");
  const [nome, definirNome] = useState("");
  const [email, definirEmail] = useState("");
  const [senha, definirSenha] = useState("");
  const [idade, definirIdade] = useState("");
  const [planoSaude, definirPlanoSaude] = useState("");
  const [especialidade, definirEspecialidade] = useState("");
  const [valorConsultaParticular, definirValorConsultaParticular] =
    useState("");
  const [erro, definirErro] = useState<string | null>(null);
  const [sucesso, definirSucesso] = useState<string | null>(null);
  const [salvando, definirSalvando] = useState(false);

  const especialidades = useOpcoesApi("/especialidades");
  const planosDeSaude = useOpcoesApi("/planos");
  const ehMedico = tipoDeCadastro === "MEDICO";
  const especialidadeSelecionada = especialidade || especialidades[0] || "";

  function limparFormulario() {
    definirNome("");
    definirEmail("");
    definirSenha("");
    definirIdade("");
    definirPlanoSaude("");
    definirEspecialidade("");
    definirValorConsultaParticular("");
  }

  async function aoEnviarCadastro(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    definirErro(null);
    definirSucesso(null);
    definirSalvando(true);

    const corpoDaRequisicao = ehMedico
      ? {
          nome,
          email,
          senha,
          tipo: tipoDeCadastro,
          especialidade: especialidadeSelecionada,
          valorConsultaParticular: Number(valorConsultaParticular),
        }
      : {
          nome,
          email,
          senha,
          tipo: tipoDeCadastro,
          idade: Number(idade),
          planoSaude: planoSaude || null,
        };

    try {
      const resposta = await fetch(`${URL_BASE_API}/auth/cadastrar`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(corpoDaRequisicao),
      });

      if (!resposta.ok) {
        definirErro(await obterMensagemDeErro(resposta));
        return;
      }

      definirSucesso(
        ehMedico
          ? "Médico cadastrado com sucesso. Faça login para continuar."
          : "Cadastro realizado com sucesso. Faça login para continuar.",
      );
      limparFormulario();
    } catch {
      definirErro("Não foi possível cadastrar. Tente novamente.");
    } finally {
      definirSalvando(false);
    }
  }

  return (
    <main style={{ padding: "2rem", fontFamily: "Arial, sans-serif" }}>
      <h1>Cadastrar-se</h1>

      <button type="button" style={estiloBotao} onClick={aoVoltar}>
        Voltar para login
      </button>

      {erro && (
        <p
          style={{
            color: "red",
            backgroundColor: "#fff0f0",
            padding: "0.75rem",
          }}
        >
          {erro}
        </p>
      )}
      {sucesso && (
        <p
          style={{
            color: "green",
            backgroundColor: "#f0fff0",
            padding: "0.75rem",
          }}
        >
          {sucesso}
        </p>
      )}

      <form
        onSubmit={aoEnviarCadastro}
        noValidate
        style={{ maxWidth: "420px" }}
      >
        <label htmlFor="tipo" style={estiloRotulo}>
          Tipo de cadastro
        </label>
        <select
          id="tipo"
          style={estiloEntrada}
          value={tipoDeCadastro}
          onChange={(evento) =>
            definirTipoDeCadastro(evento.target.value as TipoDeCadastro)
          }
        >
          <option value="PACIENTE">Paciente</option>
          <option value="MEDICO">Médico</option>
        </select>

        <label htmlFor="nome" style={estiloRotulo}>
          Nome
        </label>
        <input
          id="nome"
          style={estiloEntrada}
          value={nome}
          onChange={(evento) => definirNome(evento.target.value)}
        />

        <label htmlFor="email" style={estiloRotulo}>
          E-mail
        </label>
        <input
          id="email"
          type="email"
          style={estiloEntrada}
          value={email}
          onChange={(evento) => definirEmail(evento.target.value)}
        />

        <label htmlFor="senha" style={estiloRotulo}>
          Senha
        </label>
        <input
          id="senha"
          type="password"
          style={estiloEntrada}
          value={senha}
          onChange={(evento) => definirSenha(evento.target.value)}
        />

        {ehMedico ? (
          <>
            <label htmlFor="especialidade" style={estiloRotulo}>
              Especialidade
            </label>
            <select
              id="especialidade"
              style={estiloEntrada}
              value={especialidadeSelecionada}
              onChange={(evento) => definirEspecialidade(evento.target.value)}
            >
              {especialidades.map((especialidade) => (
                <option key={especialidade} value={especialidade}>
                  {especialidade}
                </option>
              ))}
            </select>

            <label htmlFor="valor" style={estiloRotulo}>
              Valor da consulta
            </label>
            <input
              id="valor"
              type="number"
              step="0.01"
              min="0"
              style={estiloEntrada}
              value={valorConsultaParticular}
              onChange={(evento) =>
                definirValorConsultaParticular(evento.target.value)
              }
            />
          </>
        ) : (
          <>
            <label htmlFor="idade" style={estiloRotulo}>
              Idade
            </label>
            <input
              id="idade"
              type="number"
              min="1"
              style={estiloEntrada}
              value={idade}
              onChange={(evento) => definirIdade(evento.target.value)}
            />

            <label htmlFor="plano" style={estiloRotulo}>
              Plano de saúde
            </label>
            <select
              id="plano"
              style={estiloEntrada}
              value={planoSaude}
              onChange={(evento) => definirPlanoSaude(evento.target.value)}
            >
              <option value="">Sem plano</option>
              {planosDeSaude.map((planoDeSaude) => (
                <option key={planoDeSaude} value={planoDeSaude}>
                  {planoDeSaude}
                </option>
              ))}
            </select>
          </>
        )}

        <div>
          <button type="submit" style={estiloBotao} disabled={salvando}>
            {salvando ? "Cadastrando..." : "Cadastrar-se"}
          </button>
          <button type="button" style={estiloBotao} onClick={limparFormulario}>
            Limpar
          </button>
        </div>
      </form>
    </main>
  );
}
