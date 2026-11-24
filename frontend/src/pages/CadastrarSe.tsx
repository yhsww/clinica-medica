import { useState } from 'react'
import type { CSSProperties, FormEvent } from 'react'
import { BASE_API, lerErro } from '../api'
import { ESPECIALIDADES, PLANOS } from '../constants'

type PropriedadesTelaCadastrarSe = {
  aoVoltar: () => void
}

type Tipo = 'PACIENTE' | 'MEDICO'

const estiloRotulo: CSSProperties = {
  display: 'block',
  marginTop: '0.75rem',
  marginBottom: '0.25rem',
}

const estiloEntrada: CSSProperties = {
  padding: '0.4rem',
  border: '1px solid #ccc',
  borderRadius: '4px',
  minWidth: '260px',
}

const estiloBotao: CSSProperties = {
  cursor: 'pointer',
  padding: '0.4rem 0.8rem',
  marginRight: '0.5rem',
  marginTop: '1rem',
}

export default function TelaCadastrarSe({ aoVoltar }: PropriedadesTelaCadastrarSe) {
  const [tipo, definirTipo] = useState<Tipo>('PACIENTE')
  const [nome, definirNome] = useState('')
  const [email, definirEmail] = useState('')
  const [senha, definirSenha] = useState('')
  const [idade, definirIdade] = useState('')
  const [planoSaude, definirPlanoSaude] = useState('')
  const [especialidade, definirEspecialidade] = useState(ESPECIALIDADES[0])
  const [valorConsultaParticular, definirValorConsultaParticular] = useState('')
  const [erro, definirErro] = useState<string | null>(null)
  const [sucesso, definirSucesso] = useState<string | null>(null)
  const [salvando, definirSalvando] = useState(false)

  const ehMedico = tipo === 'MEDICO'

  function limpar() {
    definirNome('')
    definirEmail('')
    definirSenha('')
    definirIdade('')
    definirPlanoSaude('')
    definirEspecialidade(ESPECIALIDADES[0])
    definirValorConsultaParticular('')
  }

  async function aoEnviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    definirErro(null)
    definirSucesso(null)
    definirSalvando(true)

    const corpo = ehMedico
      ? {
          nome,
          email,
          senha,
          tipo,
          especialidade,
          valorConsultaParticular: Number(valorConsultaParticular),
        }
      : {
          nome,
          email,
          senha,
          tipo,
          idade: Number(idade),
          planoSaude: planoSaude || null,
        }

    try {
      const resposta = await fetch(`${BASE_API}/auth/cadastrar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(corpo),
      })

      if (!resposta.ok) {
        definirErro(await lerErro(resposta))
        return
      }

      definirSucesso(
        ehMedico
          ? 'Médico cadastrado com sucesso. Faça login para continuar.'
          : 'Cadastro realizado com sucesso. Faça login para continuar.',
      )
      limpar()
    } catch {
      definirErro('Não foi possível cadastrar. Tente novamente.')
    } finally {
      definirSalvando(false)
    }
  }

  return (
    <main style={{ padding: '2rem', fontFamily: 'Arial, sans-serif' }}>
      <h1>Cadastrar-se</h1>

      <button type="button" style={estiloBotao} onClick={aoVoltar}>
        Voltar para login
      </button>

      {erro && (
        <p style={{ color: 'red', backgroundColor: '#fff0f0', padding: '0.75rem' }}>{erro}</p>
      )}
      {sucesso && (
        <p style={{ color: 'green', backgroundColor: '#f0fff0', padding: '0.75rem' }}>{sucesso}</p>
      )}

      <form onSubmit={aoEnviar} noValidate style={{ maxWidth: '420px' }}>
        <label htmlFor="tipo" style={estiloRotulo}>
          Tipo de cadastro
        </label>
        <select
          id="tipo"
          style={estiloEntrada}
          value={tipo}
          onChange={(evento) => definirTipo(evento.target.value as Tipo)}
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
              value={especialidade}
              onChange={(evento) => definirEspecialidade(evento.target.value)}
            >
              {ESPECIALIDADES.map((opcao) => (
                <option key={opcao} value={opcao}>
                  {opcao}
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
              onChange={(evento) => definirValorConsultaParticular(evento.target.value)}
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
              {PLANOS.map((opcao) => (
                <option key={opcao} value={opcao}>
                  {opcao}
                </option>
              ))}
            </select>
          </>
        )}

        <div>
          <button type="submit" style={estiloBotao} disabled={salvando}>
            {salvando ? 'Cadastrando...' : 'Cadastrar-se'}
          </button>
          <button type="button" style={estiloBotao} onClick={limpar}>
            Limpar
          </button>
        </div>
      </form>
    </main>
  )
}
