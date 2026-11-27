import { useEffect, useState } from "react";
import { obterMensagemDeErro, requisicaoApi } from "../../api";
import {
  estiloBotao,
  estiloCabecalho,
  estiloCelula,
  estiloErro,
  estiloTabela,
} from "./estilos";

type PropriedadesAdminContas = {
  contadorAlteracoesConsultas: number;
};

type Consulta = {
  id: number;
  medicoNome: string | null;
  pacienteNome: string | null;
  dataHora: string;
};

type Conta = {
  id: number;
  consultaId: number | null;
  valor: number | null;
  paga: boolean;
};

function descreverConsulta(consulta: Consulta | undefined, id: number): string {
  if (!consulta) return `Consulta ${id}`;

  const nomesParticipantes = [consulta.medicoNome, consulta.pacienteNome]
    .filter(Boolean)
    .join(" / ");
  const dataHoraFormatada = consulta.dataHora.replace("T", " ").slice(0, 16);

  return nomesParticipantes
    ? `${nomesParticipantes} - ${dataHoraFormatada}`
    : `Consulta ${id} - ${dataHoraFormatada}`;
}

function formatarValorEmReais(valor: number | null): string {
  return valor == null ? "-" : "R$ " + valor.toFixed(2);
}

export default function AdminContas({
  contadorAlteracoesConsultas,
}: PropriedadesAdminContas) {
  const [contas, definirContas] = useState<Conta[]>([]);
  const [consultas, definirConsultas] = useState<Consulta[]>([]);
  const [erro, definirErro] = useState<string | null>(null);
  const [carregando, definirCarregando] = useState(true);

  async function carregarDados() {
    definirErro(null);
    try {
      const [respostaContas, respostaConsultas] = await Promise.all([
        requisicaoApi("/contas"),
        requisicaoApi("/consultas"),
      ]);

      if (!respostaContas.ok || !respostaConsultas.ok) {
        throw new Error("Erro ao carregar dados");
      }

      definirContas(await respostaContas.json());
      definirConsultas(await respostaConsultas.json());
    } catch {
      definirErro("Não foi possível carregar contas e consultas.");
    }
  }

  useEffect(() => {
    async function carregarDadosIniciais() {
      definirCarregando(true);
      await carregarDados();
      definirCarregando(false);
    }

    carregarDadosIniciais();
  }, [contadorAlteracoesConsultas]);

  async function marcarContaComoPaga(id: number) {
    const resposta = await requisicaoApi(`/contas/${id}/pagar`, {
      method: "POST",
    });

    if (!resposta.ok) {
      definirErro(await obterMensagemDeErro(resposta));
      return;
    }

    await carregarDados();
  }

  async function excluirConta(id: number) {
    if (!window.confirm("Excluir esta conta?")) return;

    const resposta = await requisicaoApi(`/contas/${id}`, { method: "DELETE" });

    if (!resposta.ok) {
      definirErro(await obterMensagemDeErro(resposta));
      return;
    }

    await carregarDados();
  }

  return (
    <section style={{ marginTop: "2rem" }}>
      <h2>Contas</h2>
      <p>{contas.length} conta(s) gerada(s) automaticamente.</p>

      {erro && <p style={estiloErro}>{erro}</p>}

      {carregando ? (
        <p>Carregando...</p>
      ) : contas.length === 0 ? (
        <p>Nenhuma conta cadastrada.</p>
      ) : (
        <table style={estiloTabela}>
          <thead>
            <tr style={estiloCabecalho}>
              <th style={estiloCelula}>Consulta</th>
              <th style={estiloCelula}>Valor</th>
              <th style={estiloCelula}>Situação</th>
              <th style={estiloCelula}>Ações</th>
            </tr>
          </thead>
          <tbody>
            {contas.map((conta) => (
              <tr key={conta.id}>
                <td style={estiloCelula}>
                  {descreverConsulta(
                    consultas.find(
                      (consulta) => consulta.id === conta.consultaId,
                    ),
                    conta.consultaId ?? conta.id,
                  )}
                </td>
                <td style={estiloCelula}>
                  {formatarValorEmReais(conta.valor)}
                </td>
                <td style={estiloCelula}>{conta.paga ? "Paga" : "Pendente"}</td>
                <td style={estiloCelula}>
                  {!conta.paga && (
                    <button
                      style={estiloBotao}
                      onClick={() => marcarContaComoPaga(conta.id)}
                    >
                      Marcar como paga
                    </button>
                  )}
                  <button
                    style={estiloBotao}
                    onClick={() => excluirConta(conta.id)}
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
