import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { obterMensagemDeErro, requisicaoApi } from "../../api";
import { useOpcoesApi } from "../../hooks/useOpcoesApi";
import {
  estiloBotao,
  estiloCabecalho,
  estiloCelula,
  estiloEntrada,
  estiloErro,
  estiloTabela,
} from "./estilos";

type Medico = {
  id: number;
  nome: string;
  email: string;
  especialidade: string | null;
  valorConsultaParticular: number | null;
};

type FormularioMedico = {
  id: number | null;
  nome: string;
  email: string;
  especialidade: string;
  valorConsultaParticular: string;
};

const formularioMedicoVazio: FormularioMedico = {
  id: null,
  nome: "",
  email: "",
  especialidade: "",
  valorConsultaParticular: "",
};

export default function AdminMedicos() {
  const especialidades = useOpcoesApi("/especialidades");
  const [medicos, definirMedicos] = useState<Medico[]>([]);
  const [carregando, definirCarregando] = useState(true);
  const [erro, definirErro] = useState<string | null>(null);
  const [formularioMedico, definirFormularioMedico] =
    useState<FormularioMedico>(formularioMedicoVazio);

  async function carregarMedicos() {
    definirErro(null);
    try {
      const resposta = await requisicaoApi("/medicos");

      if (!resposta.ok) {
        throw new Error("Erro ao carregar dados");
      }

      definirMedicos(await resposta.json());
    } catch {
      definirErro("Não foi possível carregar médicos.");
    }
  }

  useEffect(() => {
    async function carregarDadosIniciais() {
      definirCarregando(true);
      await carregarMedicos();
      definirCarregando(false);
    }

    carregarDadosIniciais();
  }, []);

  async function salvarMedico(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();

    if (formularioMedico.id == null) return;

    const resposta = await requisicaoApi(`/medicos/${formularioMedico.id}`, {
      method: "PUT",
      body: JSON.stringify({
        nome: formularioMedico.nome,
        email: formularioMedico.email,
        especialidade: formularioMedico.especialidade,
        valorConsultaParticular: Number(
          formularioMedico.valorConsultaParticular,
        ),
      }),
    });

    if (!resposta.ok) {
      definirErro(await obterMensagemDeErro(resposta));
      return;
    }

    definirFormularioMedico(formularioMedicoVazio);
    await carregarMedicos();
  }

  async function excluirMedico(id: number) {
    if (!window.confirm("Excluir este médico?")) return;

    const resposta = await requisicaoApi(`/medicos/${id}`, {
      method: "DELETE",
    });

    if (!resposta.ok) {
      definirErro(await obterMensagemDeErro(resposta));
      return;
    }

    if (formularioMedico.id === id)
      definirFormularioMedico(formularioMedicoVazio);
    await carregarMedicos();
  }

  return (
    <section style={{ marginTop: "2rem" }}>
      <h2>Médicos cadastrados ({medicos.length})</h2>

      {erro && <p style={estiloErro}>{erro}</p>}

      {formularioMedico.id != null && (
        <form onSubmit={salvarMedico} style={{ marginBottom: "1rem" }}>
          <input
            aria-label="Nome do médico"
            style={estiloEntrada}
            placeholder="Nome"
            value={formularioMedico.nome}
            onChange={(evento) =>
              definirFormularioMedico({
                ...formularioMedico,
                nome: evento.target.value,
              })
            }
          />
          <input
            aria-label="E-mail do médico"
            style={estiloEntrada}
            placeholder="E-mail"
            type="email"
            value={formularioMedico.email}
            onChange={(evento) =>
              definirFormularioMedico({
                ...formularioMedico,
                email: evento.target.value,
              })
            }
          />
          <select
            aria-label="Especialidade do médico"
            style={estiloEntrada}
            value={formularioMedico.especialidade}
            onChange={(evento) =>
              definirFormularioMedico({
                ...formularioMedico,
                especialidade: evento.target.value,
              })
            }
          >
            {especialidades.map((especialidade) => (
              <option key={especialidade} value={especialidade}>
                {especialidade}
              </option>
            ))}
          </select>
          <input
            aria-label="Valor da consulta do médico"
            style={estiloEntrada}
            placeholder="Valor da consulta"
            type="number"
            step="0.01"
            min="0"
            value={formularioMedico.valorConsultaParticular}
            onChange={(evento) =>
              definirFormularioMedico({
                ...formularioMedico,
                valorConsultaParticular: evento.target.value,
              })
            }
          />
          <button type="submit" style={estiloBotao}>
            Salvar alterações
          </button>
          <button
            type="button"
            style={estiloBotao}
            onClick={() => definirFormularioMedico(formularioMedicoVazio)}
          >
            Cancelar
          </button>
        </form>
      )}

      {carregando ? (
        <p>Carregando...</p>
      ) : medicos.length === 0 ? (
        <p>Nenhum médico cadastrado.</p>
      ) : (
        <table style={estiloTabela}>
          <thead>
            <tr style={estiloCabecalho}>
              <th style={estiloCelula}>Nome</th>
              <th style={estiloCelula}>E-mail</th>
              <th style={estiloCelula}>Especialidade</th>
              <th style={estiloCelula}>Valor consulta particular</th>
              <th style={estiloCelula}>Ações</th>
            </tr>
          </thead>
          <tbody>
            {medicos.map((medico) => (
              <tr key={medico.id}>
                <td style={estiloCelula}>{medico.nome}</td>
                <td style={estiloCelula}>{medico.email}</td>
                <td style={estiloCelula}>{medico.especialidade ?? "-"}</td>
                <td style={estiloCelula}>
                  {medico.valorConsultaParticular != null
                    ? "R$ " + medico.valorConsultaParticular.toFixed(2)
                    : "-"}
                </td>
                <td style={estiloCelula}>
                  <button
                    style={estiloBotao}
                    onClick={() =>
                      definirFormularioMedico({
                        id: medico.id,
                        nome: medico.nome,
                        email: medico.email,
                        especialidade:
                          medico.especialidade &&
                          especialidades.includes(medico.especialidade)
                            ? medico.especialidade
                            : (especialidades[0] ?? ""),
                        valorConsultaParticular:
                          medico.valorConsultaParticular?.toString() ?? "",
                      })
                    }
                  >
                    Editar
                  </button>
                  <button
                    style={estiloBotao}
                    onClick={() => excluirMedico(medico.id)}
                  >
                    Excluir
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
