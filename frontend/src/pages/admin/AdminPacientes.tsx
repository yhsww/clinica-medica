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

type Paciente = {
  id: number;
  nome: string;
  email: string;
  idade: number | null;
  planoSaude: string | null;
};

type FormularioPaciente = {
  id: number | null;
  nome: string;
  email: string;
  idade: string;
  planoSaude: string;
};

const formularioPacienteVazio: FormularioPaciente = {
  id: null,
  nome: "",
  email: "",
  idade: "",
  planoSaude: "",
};

export default function AdminPacientes() {
  const planosDeSaude = useOpcoesApi("/planosDeSaude");
  const [pacientes, definirPacientes] = useState<Paciente[]>([]);
  const [carregando, definirCarregando] = useState(true);
  const [erro, definirErro] = useState<string | null>(null);
  const [formularioPaciente, definirFormularioPaciente] =
    useState<FormularioPaciente>(formularioPacienteVazio);

  async function carregarPacientes() {
    definirErro(null);
    try {
      const resposta = await requisicaoApi("/pacientes");

      if (!resposta.ok) {
        throw new Error("Erro ao carregar dados");
      }

      definirPacientes(await resposta.json());
    } catch {
      definirErro("Não foi possível carregar pacientes.");
    }
  }

  useEffect(() => {
    async function carregarDadosIniciais() {
      definirCarregando(true);
      await carregarPacientes();
      definirCarregando(false);
    }

    carregarDadosIniciais();
  }, []);

  async function salvarPaciente(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();

    if (formularioPaciente.id == null) return;

    const resposta = await requisicaoApi(
      `/pacientes/${formularioPaciente.id}`,
      {
        method: "PUT",
        body: JSON.stringify({
          nome: formularioPaciente.nome,
          email: formularioPaciente.email,
          idade: Number(formularioPaciente.idade),
          planoSaude: formularioPaciente.planoSaude || null,
        }),
      },
    );

    if (!resposta.ok) {
      definirErro(await obterMensagemDeErro(resposta));
      return;
    }

    definirFormularioPaciente(formularioPacienteVazio);
    await carregarPacientes();
  }

  async function excluirPaciente(id: number) {
    if (!window.confirm("Excluir este paciente?")) return;

    const resposta = await requisicaoApi(`/pacientes/${id}`, {
      method: "DELETE",
    });

    if (!resposta.ok) {
      definirErro(await obterMensagemDeErro(resposta));
      return;
    }

    if (formularioPaciente.id === id)
      definirFormularioPaciente(formularioPacienteVazio);
    await carregarPacientes();
  }

  return (
    <section style={{ marginTop: "2rem" }}>
      <h2>Pacientes cadastrados ({pacientes.length})</h2>

      {erro && <p style={estiloErro}>{erro}</p>}

      {formularioPaciente.id != null && (
        <form onSubmit={salvarPaciente} style={{ marginBottom: "1rem" }}>
          <input
            aria-label="Nome do paciente"
            style={estiloEntrada}
            placeholder="Nome"
            value={formularioPaciente.nome}
            onChange={(evento) =>
              definirFormularioPaciente({
                ...formularioPaciente,
                nome: evento.target.value,
              })
            }
          />
          <input
            aria-label="E-mail do paciente"
            style={estiloEntrada}
            placeholder="E-mail"
            type="email"
            value={formularioPaciente.email}
            onChange={(evento) =>
              definirFormularioPaciente({
                ...formularioPaciente,
                email: evento.target.value,
              })
            }
          />
          <input
            aria-label="Idade do paciente"
            style={estiloEntrada}
            placeholder="Idade"
            type="number"
            min="1"
            value={formularioPaciente.idade}
            onChange={(evento) =>
              definirFormularioPaciente({
                ...formularioPaciente,
                idade: evento.target.value,
              })
            }
          />
          <select
            aria-label="Plano de saúde do paciente"
            style={estiloEntrada}
            value={formularioPaciente.planoSaude}
            onChange={(evento) =>
              definirFormularioPaciente({
                ...formularioPaciente,
                planoSaude: evento.target.value,
              })
            }
          >
            <option value="">Sem plano</option>
            {planosDeSaude.map((planoDeSaude) => (
              <option key={planoDeSaude} value={planoDeSaude}>
                {planoDeSaude}
              </option>
            ))}
          </select>
          <button type="submit" style={estiloBotao}>
            Salvar alterações
          </button>
          <button
            type="button"
            style={estiloBotao}
            onClick={() => definirFormularioPaciente(formularioPacienteVazio)}
          >
            Cancelar
          </button>
        </form>
      )}

      {carregando ? (
        <p>Carregando...</p>
      ) : pacientes.length === 0 ? (
        <p>Nenhum paciente cadastrado.</p>
      ) : (
        <table style={estiloTabela}>
          <thead>
            <tr style={estiloCabecalho}>
              <th style={estiloCelula}>Nome</th>
              <th style={estiloCelula}>E-mail</th>
              <th style={estiloCelula}>Idade</th>
              <th style={estiloCelula}>Plano de saúde</th>
              <th style={estiloCelula}>Ações</th>
            </tr>
          </thead>
          <tbody>
            {pacientes.map((paciente) => (
              <tr key={paciente.id}>
                <td style={estiloCelula}>{paciente.nome}</td>
                <td style={estiloCelula}>{paciente.email}</td>
                <td style={estiloCelula}>{paciente.idade ?? "-"}</td>
                <td style={estiloCelula}>{paciente.planoSaude ?? "-"}</td>
                <td style={estiloCelula}>
                  <button
                    style={estiloBotao}
                    onClick={() =>
                      definirFormularioPaciente({
                        id: paciente.id,
                        nome: paciente.nome,
                        email: paciente.email,
                        idade: paciente.idade?.toString() ?? "",
                        planoSaude:
                          paciente.planoSaude &&
                          planosDeSaude.includes(paciente.planoSaude)
                            ? paciente.planoSaude
                            : "",
                      })
                    }
                  >
                    Editar
                  </button>
                  <button
                    style={estiloBotao}
                    onClick={() => excluirPaciente(paciente.id)}
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
