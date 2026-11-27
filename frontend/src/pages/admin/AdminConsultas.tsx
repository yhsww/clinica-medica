import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { obterMensagemDeErro, requisicaoApi } from "../../api";
import {
  estiloBotao,
  estiloCabecalho,
  estiloCelula,
  estiloEntrada,
  estiloErro,
  estiloTabela,
} from "./estilos";

type PropriedadesAdminConsultas = {
  aoAlterar: () => void;
};

type Medico = {
  id: number;
  nome: string;
};

type Paciente = {
  id: number;
  nome: string;
};

type Consulta = {
  id: number;
  medicoId: number | null;
  medicoNome: string | null;
  pacienteId: number | null;
  pacienteNome: string | null;
  dataHora: string;
  estado: "AGENDADA" | "CANCELADA" | "REALIZADA";
  valorCobrado: number | null;
};

type FormularioConsulta = {
  id: number | null;
  medicoId: string;
  pacienteId: string;
  dataHora: string;
};

const formularioConsultaVazio: FormularioConsulta = {
  id: null,
  medicoId: "",
  pacienteId: "",
  dataHora: "",
};

function dataHoraParaFormulario(dataHora: string): string {
  return dataHora.slice(0, 16);
}

function dataHoraParaApi(dataHora: string): string {
  return dataHora.length === 16 ? `${dataHora}:00` : dataHora;
}

export default function AdminConsultas({
  aoAlterar,
}: PropriedadesAdminConsultas) {
  const [consultas, definirConsultas] = useState<Consulta[]>([]);
  const [medicos, definirMedicos] = useState<Medico[]>([]);
  const [pacientes, definirPacientes] = useState<Paciente[]>([]);
  const [formularioConsulta, definirFormularioConsulta] =
    useState<FormularioConsulta>(formularioConsultaVazio);
  const [erro, definirErro] = useState<string | null>(null);
  const [carregando, definirCarregando] = useState(true);

  async function carregarDados() {
    definirErro(null);
    try {
      const [respostaConsultas, respostaMedicos, respostaPacientes] =
        await Promise.all([
          requisicaoApi("/consultas"),
          requisicaoApi("/medicos"),
          requisicaoApi("/pacientes"),
        ]);

      if (
        !respostaConsultas.ok ||
        !respostaMedicos.ok ||
        !respostaPacientes.ok
      ) {
        throw new Error("Erro ao carregar dados");
      }

      definirConsultas(await respostaConsultas.json());
      definirMedicos(await respostaMedicos.json());
      definirPacientes(await respostaPacientes.json());
    } catch {
      definirErro("Não foi possível carregar consultas, médicos e pacientes.");
    }
  }

  useEffect(() => {
    async function carregarDadosIniciais() {
      definirCarregando(true);
      await carregarDados();
      definirCarregando(false);
    }

    carregarDadosIniciais();
  }, []);

  async function salvarConsulta(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();

    if (
      !formularioConsulta.medicoId ||
      !formularioConsulta.pacienteId ||
      !formularioConsulta.dataHora
    ) {
      definirErro("Informe médico, paciente e data da consulta.");
      return;
    }

    const corpoDaRequisicao = {
      medicoId: Number(formularioConsulta.medicoId),
      pacienteId: Number(formularioConsulta.pacienteId),
      dataHora: dataHoraParaApi(formularioConsulta.dataHora),
    };
    const metodoHttp = formularioConsulta.id == null ? "POST" : "PUT";
    const rota =
      formularioConsulta.id == null
        ? "/consultas"
        : `/consultas/${formularioConsulta.id}`;

    const resposta = await requisicaoApi(rota, {
      method: metodoHttp,
      body: JSON.stringify(corpoDaRequisicao),
    });

    if (!resposta.ok) {
      definirErro(await obterMensagemDeErro(resposta));
      return;
    }

    definirFormularioConsulta(formularioConsultaVazio);
    await carregarDados();
    aoAlterar();
  }

  async function alterarEstadoDaConsulta(
    id: number,
    acaoDaConsulta: "realizar" | "cancelar",
  ) {
    const resposta = await requisicaoApi(`/consultas/${id}/${acaoDaConsulta}`, {
      method: "POST",
    });

    if (!resposta.ok) {
      definirErro(await obterMensagemDeErro(resposta));
      return;
    }

    await carregarDados();
    aoAlterar();
  }

  async function excluirConsulta(id: number) {
    if (!window.confirm("Excluir esta consulta?")) return;

    const resposta = await requisicaoApi(`/consultas/${id}`, {
      method: "DELETE",
    });

    if (!resposta.ok) {
      definirErro(await obterMensagemDeErro(resposta));
      return;
    }

    if (formularioConsulta.id === id)
      definirFormularioConsulta(formularioConsultaVazio);
    await carregarDados();
    aoAlterar();
  }

  return (
    <section style={{ marginTop: "2rem" }}>
      <h2>Consultas</h2>
      <p>{consultas.length} consulta(s) cadastrada(s).</p>

      {erro && <p style={estiloErro}>{erro}</p>}

      <form onSubmit={salvarConsulta} style={{ marginBottom: "1rem" }}>
        <label htmlFor="consulta-medico">Médico</label>
        <select
          id="consulta-medico"
          style={estiloEntrada}
          value={formularioConsulta.medicoId}
          onChange={(evento) =>
            definirFormularioConsulta({
              ...formularioConsulta,
              medicoId: evento.target.value,
            })
          }
        >
          <option value="">Selecione...</option>
          {medicos.map((medico) => (
            <option key={medico.id} value={medico.id}>
              {medico.nome}
            </option>
          ))}
        </select>

        <label htmlFor="consulta-paciente">Paciente</label>
        <select
          id="consulta-paciente"
          style={estiloEntrada}
          value={formularioConsulta.pacienteId}
          onChange={(evento) =>
            definirFormularioConsulta({
              ...formularioConsulta,
              pacienteId: evento.target.value,
            })
          }
        >
          <option value="">Selecione...</option>
          {pacientes.map((paciente) => (
            <option key={paciente.id} value={paciente.id}>
              {paciente.nome}
            </option>
          ))}
        </select>

        <label htmlFor="consulta-data">Data e hora</label>
        <input
          id="consulta-data"
          type="datetime-local"
          style={estiloEntrada}
          value={formularioConsulta.dataHora}
          onChange={(evento) =>
            definirFormularioConsulta({
              ...formularioConsulta,
              dataHora: evento.target.value,
            })
          }
        />

        <button type="submit" style={estiloBotao}>
          Salvar consulta
        </button>
        {formularioConsulta.id != null && (
          <button
            type="button"
            style={estiloBotao}
            onClick={() => definirFormularioConsulta(formularioConsultaVazio)}
          >
            Cancelar
          </button>
        )}
      </form>

      {carregando ? (
        <p>Carregando...</p>
      ) : consultas.length === 0 ? (
        <p>Nenhuma consulta cadastrada.</p>
      ) : (
        <table style={estiloTabela}>
          <thead>
            <tr style={estiloCabecalho}>
              <th style={estiloCelula}>Médico</th>
              <th style={estiloCelula}>Paciente</th>
              <th style={estiloCelula}>Data e hora</th>
              <th style={estiloCelula}>Estado</th>
              <th style={estiloCelula}>Valor</th>
              <th style={estiloCelula}>Ações</th>
            </tr>
          </thead>
          <tbody>
            {consultas.map((consulta) => (
              <tr key={consulta.id}>
                <td style={estiloCelula}>{consulta.medicoNome ?? "-"}</td>
                <td style={estiloCelula}>{consulta.pacienteNome ?? "-"}</td>
                <td style={estiloCelula}>
                  {consulta.dataHora.replace("T", " ").slice(0, 16)}
                </td>
                <td style={estiloCelula}>{consulta.estado}</td>
                <td style={estiloCelula}>
                  {consulta.valorCobrado != null
                    ? "R$ " + consulta.valorCobrado.toFixed(2)
                    : "-"}
                </td>
                <td style={estiloCelula}>
                  <button
                    style={estiloBotao}
                    onClick={() =>
                      definirFormularioConsulta({
                        id: consulta.id,
                        medicoId: String(consulta.medicoId ?? ""),
                        pacienteId: String(consulta.pacienteId ?? ""),
                        dataHora: dataHoraParaFormulario(consulta.dataHora),
                      })
                    }
                  >
                    Editar
                  </button>
                  <button
                    style={estiloBotao}
                    onClick={() => excluirConsulta(consulta.id)}
                  >
                    Excluir
                  </button>
                  {consulta.estado === "AGENDADA" && (
                    <>
                      <button
                        style={estiloBotao}
                        onClick={() =>
                          alterarEstadoDaConsulta(consulta.id, "realizar")
                        }
                      >
                        Realizar
                      </button>
                      <button
                        style={estiloBotao}
                        onClick={() =>
                          alterarEstadoDaConsulta(consulta.id, "cancelar")
                        }
                      >
                        Cancelar consulta
                      </button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
