import { useCallback, useEffect, useState } from "react";
import type { FormEvent } from "react";
import { obterMensagemDeErro, requisicaoApi } from "../api";
import type { Usuario } from "../types";

type Medico = {
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

type PropriedadesConsultasDoUsuario = {
  usuario: Usuario;
};

export default function ConsultasDoUsuario({
  usuario,
}: PropriedadesConsultasDoUsuario) {
  const ehPaciente = usuario.papel === "paciente";
  const [consultas, definirConsultas] = useState<Consulta[]>([]);
  const [medicos, definirMedicos] = useState<Medico[]>([]);
  const [medicoSelecionadoId, definirMedicoSelecionadoId] = useState("");
  const [dataHoraSelecionada, definirDataHoraSelecionada] = useState("");
  const [erro, definirErro] = useState<string | null>(null);
  const [carregando, definirCarregando] = useState(true);

  const carregarDados = useCallback(async () => {
    definirErro(null);
    try {
      const [respostaConsultas, respostaMedicos] = await Promise.all([
        requisicaoApi("/consultas"),
        requisicaoApi("/medicos"),
      ]);

      if (!respostaConsultas.ok || !respostaMedicos.ok) {
        throw new Error("Erro ao carregar dados");
      }

      const todasAsConsultas: Consulta[] = await respostaConsultas.json();
      definirConsultas(
        todasAsConsultas.filter((consulta) =>
          ehPaciente
            ? consulta.pacienteId === usuario.id
            : consulta.medicoId === usuario.id,
        ),
      );
      definirMedicos(await respostaMedicos.json());
    } catch {
      definirErro("Não foi possível carregar as consultas.");
    }
  }, [ehPaciente, usuario.id]);

  useEffect(() => {
    async function carregarDadosIniciais() {
      definirCarregando(true);
      await carregarDados();
      definirCarregando(false);
    }

    carregarDadosIniciais();
  }, [carregarDados]);

  async function agendarConsulta(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();

    if (!medicoSelecionadoId || !dataHoraSelecionada) {
      definirErro("Informe o médico e a data da consulta.");
      return;
    }

    const resposta = await requisicaoApi("/consultas", {
      method: "POST",
      body: JSON.stringify({
        medicoId: Number(medicoSelecionadoId),
        pacienteId: usuario.id,
        dataHora:
          dataHoraSelecionada.length === 16
            ? `${dataHoraSelecionada}:00`
            : dataHoraSelecionada,
      }),
    });

    if (!resposta.ok) {
      definirErro(await obterMensagemDeErro(resposta));
      return;
    }

    definirMedicoSelecionadoId("");
    definirDataHoraSelecionada("");
    await carregarDados();
  }

  async function alterarEstadoDaConsulta(
    id: number,
    acaoDaConsulta: "cancelar" | "realizar",
  ) {
    const resposta = await requisicaoApi(`/consultas/${id}/${acaoDaConsulta}`, {
      method: "POST",
    });

    if (!resposta.ok) {
      definirErro(await obterMensagemDeErro(resposta));
      return;
    }

    await carregarDados();
  }

  return (
    <section style={{ marginTop: "2rem" }}>
      <h2>Minhas consultas</h2>

      {erro && <p style={{ color: "red" }}>{erro}</p>}

      {ehPaciente && (
        <form onSubmit={agendarConsulta} style={{ marginBottom: "1rem" }}>
          <div>
            <label htmlFor="agendar-medico">Médico</label>
            <select
              id="agendar-medico"
              value={medicoSelecionadoId}
              onChange={(evento) =>
                definirMedicoSelecionadoId(evento.target.value)
              }
            >
              <option value="">Selecione...</option>
              {medicos.map((medico) => (
                <option key={medico.id} value={medico.id}>
                  {medico.nome}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="agendar-data">Data e hora</label>
            <input
              id="agendar-data"
              type="datetime-local"
              value={dataHoraSelecionada}
              onChange={(evento) =>
                definirDataHoraSelecionada(evento.target.value)
              }
            />
          </div>
          <button type="submit" style={{ marginTop: "0.5rem" }}>
            Agendar consulta
          </button>
        </form>
      )}

      {carregando ? (
        <p>Carregando...</p>
      ) : consultas.length === 0 ? (
        <p>Nenhuma consulta cadastrada.</p>
      ) : (
        <table style={{ borderCollapse: "collapse", border: "1px solid #ccc" }}>
          <thead>
            <tr>
              <th style={{ border: "1px solid #ccc", padding: "0.4rem" }}>
                Médico
              </th>
              <th style={{ border: "1px solid #ccc", padding: "0.4rem" }}>
                Paciente
              </th>
              <th style={{ border: "1px solid #ccc", padding: "0.4rem" }}>
                Data e hora
              </th>
              <th style={{ border: "1px solid #ccc", padding: "0.4rem" }}>
                Estado
              </th>
              <th style={{ border: "1px solid #ccc", padding: "0.4rem" }}>
                Ações
              </th>
            </tr>
          </thead>
          <tbody>
            {consultas.map((consulta) => (
              <tr key={consulta.id}>
                <td style={{ border: "1px solid #ccc", padding: "0.4rem" }}>
                  {consulta.medicoNome ?? "-"}
                </td>
                <td style={{ border: "1px solid #ccc", padding: "0.4rem" }}>
                  {consulta.pacienteNome ?? "-"}
                </td>
                <td style={{ border: "1px solid #ccc", padding: "0.4rem" }}>
                  {consulta.dataHora.replace("T", " ").slice(0, 16)}
                </td>
                <td style={{ border: "1px solid #ccc", padding: "0.4rem" }}>
                  {consulta.estado}
                </td>
                <td style={{ border: "1px solid #ccc", padding: "0.4rem" }}>
                  {consulta.estado === "AGENDADA" && (
                    <>
                      <button
                        onClick={() =>
                          alterarEstadoDaConsulta(consulta.id, "cancelar")
                        }
                      >
                        Cancelar
                      </button>
                      {!ehPaciente && (
                        <button
                          onClick={() =>
                            alterarEstadoDaConsulta(consulta.id, "realizar")
                          }
                        >
                          Concluir
                        </button>
                      )}
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
