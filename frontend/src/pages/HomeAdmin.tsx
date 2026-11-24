import { useEffect, useState } from 'react'
import type { CSSProperties, FormEvent } from 'react'
import { BASE_API, lerErro } from '../api'
import { ESPECIALIDADES, PLANOS } from '../constants'
import type { Usuario } from '../types'

type Medico = {
  id: number
  nome: string
  email: string
  especialidade: string | null
  valorConsultaParticular: number | null
}

type Paciente = {
  id: number
  nome: string
  email: string
  idade: number | null
  planoSaude: string | null
}

type PropriedadesTelaAdmin = {
  usuario: Usuario
  aoSair: () => void
}

type FormularioMedico = {
  id: number | null
  nome: string
  email: string
  especialidade: string
  valorConsultaParticular: string
}

type FormularioPaciente = {
  id: number | null
  nome: string
  email: string
  idade: string
  planoSaude: string
}

const medicoVazio: FormularioMedico = {
  id: null,
  nome: '',
  email: '',
  especialidade: '',
  valorConsultaParticular: '',
}

const pacienteVazio: FormularioPaciente = {
  id: null,
  nome: '',
  email: '',
  idade: '',
  planoSaude: '',
}

const estiloTabela: CSSProperties = {
  borderCollapse: 'collapse',
  width: '100%',
  marginTop: '0.5rem',
}

const estiloCelula: CSSProperties = {
  border: '1px solid #ddd',
  padding: '0.75rem',
  textAlign: 'left',
}

const estiloCabecalho: CSSProperties = {
  backgroundColor: '#f5f5f5',
}

const estiloEntrada: CSSProperties = {
  padding: '0.4rem',
  border: '1px solid #ccc',
  borderRadius: '4px',
}

const estiloBotao: CSSProperties = {
  cursor: 'pointer',
  padding: '0.4rem 0.8rem',
  marginRight: '0.5rem',
}

export default function TelaAdmin({ usuario, aoSair }: PropriedadesTelaAdmin) {
  const [medicos, definirMedicos] = useState<Medico[]>([])
  const [pacientes, definirPacientes] = useState<Paciente[]>([])
  const [carregando, definirCarregando] = useState(true)
  const [erro, definirErro] = useState<string | null>(null)
  const [formularioMedico, definirFormularioMedico] = useState<FormularioMedico>(medicoVazio)
  const [formularioPaciente, definirFormularioPaciente] = useState<FormularioPaciente>(pacienteVazio)

  async function carregar() {
    definirErro(null)
    try {
      const [respostaMedicos, respostaPacientes] = await Promise.all([
        fetch(`${BASE_API}/medicos`),
        fetch(`${BASE_API}/pacientes`),
      ])

      if (!respostaMedicos.ok || !respostaPacientes.ok) {
        throw new Error('Erro ao carregar dados')
      }

      definirMedicos(await respostaMedicos.json())
      definirPacientes(await respostaPacientes.json())
    } catch {
      definirErro('Não foi possível carregar médicos e pacientes.')
    }
  }

  useEffect(() => {
    async function inicializar() {
      definirCarregando(true)
      await carregar()
      definirCarregando(false)
    }

    inicializar()
  }, [])

  async function salvarMedico(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()

    if (formularioMedico.id == null) return

    const resposta = await fetch(`${BASE_API}/medicos/${formularioMedico.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nome: formularioMedico.nome,
        email: formularioMedico.email,
        especialidade: formularioMedico.especialidade,
        valorConsultaParticular: Number(formularioMedico.valorConsultaParticular),
      }),
    })

    if (!resposta.ok) {
      definirErro(await lerErro(resposta))
      return
    }

    definirFormularioMedico(medicoVazio)
    await carregar()
  }

  async function salvarPaciente(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()

    if (formularioPaciente.id == null) return

    const resposta = await fetch(`${BASE_API}/pacientes/${formularioPaciente.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nome: formularioPaciente.nome,
        email: formularioPaciente.email,
        idade: Number(formularioPaciente.idade),
        planoSaude: formularioPaciente.planoSaude || null,
      }),
    })

    if (!resposta.ok) {
      definirErro(await lerErro(resposta))
      return
    }

    definirFormularioPaciente(pacienteVazio)
    await carregar()
  }

  async function excluirMedico(id: number) {
    if (!window.confirm('Excluir este médico?')) return

    const resposta = await fetch(`${BASE_API}/medicos/${id}`, { method: 'DELETE' })

    if (!resposta.ok) {
      definirErro(await lerErro(resposta))
      return
    }

    if (formularioMedico.id === id) definirFormularioMedico(medicoVazio)
    await carregar()
  }

  async function excluirPaciente(id: number) {
    if (!window.confirm('Excluir este paciente?')) return

    const resposta = await fetch(`${BASE_API}/pacientes/${id}`, { method: 'DELETE' })

    if (!resposta.ok) {
      definirErro(await lerErro(resposta))
      return
    }

    if (formularioPaciente.id === id) definirFormularioPaciente(pacienteVazio)
    await carregar()
  }

  if (carregando) {
    return <main>Carregando...</main>
  }

  return (
    <main style={{ padding: '2rem', fontFamily: 'Arial, sans-serif' }}>
      <h1>Home - Admin</h1>
      <p>
        Bem-vindo, <strong>{usuario.nome}</strong> ({usuario.papel})
      </p>
      <button onClick={aoSair} style={{ ...estiloBotao, marginBottom: '1.5rem' }}>
        Sair
      </button>

      {erro && (
        <p style={{ color: 'red', backgroundColor: '#fff0f0', padding: '0.75rem' }}>{erro}</p>
      )}

      <section>
        <h2>Médicos cadastrados ({medicos.length})</h2>

        {formularioMedico.id != null && (
          <form onSubmit={salvarMedico} style={{ marginBottom: '1rem' }}>
            <input
              style={estiloEntrada}
              placeholder="Nome"
              value={formularioMedico.nome}
              onChange={(evento) =>
                definirFormularioMedico({ ...formularioMedico, nome: evento.target.value })
              }
            />
            <input
              style={estiloEntrada}
              placeholder="E-mail"
              type="email"
              value={formularioMedico.email}
              onChange={(evento) =>
                definirFormularioMedico({ ...formularioMedico, email: evento.target.value })
              }
            />
            <select
              style={estiloEntrada}
              value={formularioMedico.especialidade}
              onChange={(evento) =>
                definirFormularioMedico({ ...formularioMedico, especialidade: evento.target.value })
              }
            >
              {ESPECIALIDADES.map((opcao) => (
                <option key={opcao} value={opcao}>
                  {opcao}
                </option>
              ))}
            </select>
            <input
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
            <button type="button" style={estiloBotao} onClick={() => definirFormularioMedico(medicoVazio)}>
              Cancelar
            </button>
          </form>
        )}

        {medicos.length === 0 ? (
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
                <tr key={String(medico.id)}>
                  <td style={estiloCelula}>{medico.nome}</td>
                  <td style={estiloCelula}>{medico.email}</td>
                  <td style={estiloCelula}>{medico.especialidade ?? '-'}</td>
                  <td style={estiloCelula}>
                    {medico.valorConsultaParticular != null
                      ? 'R$ ' + medico.valorConsultaParticular.toFixed(2)
                      : '-'}
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
                            medico.especialidade && ESPECIALIDADES.includes(medico.especialidade)
                              ? medico.especialidade
                              : ESPECIALIDADES[0],
                          valorConsultaParticular: medico.valorConsultaParticular?.toString() ?? '',
                        })
                      }
                    >
                      Editar
                    </button>
                    <button style={estiloBotao} onClick={() => excluirMedico(medico.id)}>
                      Excluir
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <section style={{ marginTop: '2rem' }}>
        <h2>Pacientes cadastrados ({pacientes.length})</h2>

        {formularioPaciente.id != null && (
          <form onSubmit={salvarPaciente} style={{ marginBottom: '1rem' }}>
            <input
              style={estiloEntrada}
              placeholder="Nome"
              value={formularioPaciente.nome}
              onChange={(evento) =>
                definirFormularioPaciente({ ...formularioPaciente, nome: evento.target.value })
              }
            />
            <input
              style={estiloEntrada}
              placeholder="E-mail"
              type="email"
              value={formularioPaciente.email}
              onChange={(evento) =>
                definirFormularioPaciente({ ...formularioPaciente, email: evento.target.value })
              }
            />
            <input
              style={estiloEntrada}
              placeholder="Idade"
              type="number"
              min="1"
              value={formularioPaciente.idade}
              onChange={(evento) =>
                definirFormularioPaciente({ ...formularioPaciente, idade: evento.target.value })
              }
            />
            <select
              style={estiloEntrada}
              value={formularioPaciente.planoSaude}
              onChange={(evento) =>
                definirFormularioPaciente({ ...formularioPaciente, planoSaude: evento.target.value })
              }
            >
              <option value="">Sem plano</option>
              {PLANOS.map((opcao) => (
                <option key={opcao} value={opcao}>
                  {opcao}
                </option>
              ))}
            </select>
            <button type="submit" style={estiloBotao}>
              Salvar alterações
            </button>
            <button type="button" style={estiloBotao} onClick={() => definirFormularioPaciente(pacienteVazio)}>
              Cancelar
            </button>
          </form>
        )}

        {pacientes.length === 0 ? (
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
                <tr key={String(paciente.id)}>
                  <td style={estiloCelula}>{paciente.nome}</td>
                  <td style={estiloCelula}>{paciente.email}</td>
                  <td style={estiloCelula}>{paciente.idade ?? '-'}</td>
                  <td style={estiloCelula}>{paciente.planoSaude ?? '-'}</td>
                  <td style={estiloCelula}>
                    <button
                      style={estiloBotao}
                      onClick={() =>
                        definirFormularioPaciente({
                          id: paciente.id,
                          nome: paciente.nome,
                          email: paciente.email,
                          idade: paciente.idade?.toString() ?? '',
                          planoSaude:
                            paciente.planoSaude && PLANOS.includes(paciente.planoSaude)
                              ? paciente.planoSaude
                              : '',
                        })
                      }
                    >
                      Editar
                    </button>
                    <button style={estiloBotao} onClick={() => excluirPaciente(paciente.id)}>
                      Excluir
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </main>
  )
}
