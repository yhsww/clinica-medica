import { useState } from 'react'
import { BASE_API, lerErro } from '../api'
import type { Usuario } from '../types'

type PropriedadesTelaLogin = {
  aoEntrar: (usuario: Usuario) => void
  aoCadastrarSe: () => void
}

export default function TelaLogin({ aoEntrar, aoCadastrarSe }: PropriedadesTelaLogin) {
  const [email, definirEmail] = useState('')
  const [senha, definirSenha] = useState('')
  const [carregando, definirCarregando] = useState(false)
  const [erros, definirErros] = useState<{ email?: string; senha?: string }>({})
  const [mensagem, definirMensagem] = useState({
    texto: '',
    tipo: null as 'sucesso' | 'erro' | null,
  })

  function limparErros() {
    definirErros({})
    definirMensagem({ texto: '', tipo: null })
  }

  function validar(): boolean {
    const proximosErros: { email?: string; senha?: string } = {}
    let valido = true

    if (!email.trim()) {
      proximosErros.email = 'Informe o e-mail.'
      valido = false
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      proximosErros.email = 'E-mail inválido.'
      valido = false
    }

    if (!senha) {
      proximosErros.senha = 'Informe a senha.'
      valido = false
    }

    definirErros(proximosErros)
    return valido
  }

  async function aoEnviar(evento: React.FormEvent) {
    evento.preventDefault()
    limparErros()

    if (!validar()) {
      return
    }

    definirCarregando(true)
    try {
      const resposta = await fetch(`${BASE_API}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, senha }),
      })

      if (!resposta.ok) {
        definirMensagem({
          texto: await lerErro(resposta, 'E-mail ou senha incorretos.'),
          tipo: 'erro',
        })
        return
      }

      const dados = await resposta.json()
      const usuario: Usuario = {
        nome: dados.nome,
        email: dados.email,
        papel: dados.papel.toLowerCase() as Usuario['papel'],
      }
      localStorage.setItem('user', JSON.stringify(usuario))
      aoEntrar(usuario)
    } catch {
      definirMensagem({
        texto: 'Erro ao fazer login. Tente novamente.',
        tipo: 'erro',
      })
    } finally {
      definirCarregando(false)
    }
  }

  return (
    <main>
      <h1>Login</h1>

      {mensagem.texto && (
        <div role="alert" aria-live="assertive" style={{ color: 'red', marginBottom: '1rem' }}>
          {mensagem.texto}
        </div>
      )}

      <form onSubmit={aoEnviar} noValidate>
        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="email">E-mail</label>
          <br />
          <input
            id="email"
            name="email"
            type="email"
            value={email}
            onChange={(evento) => definirEmail(evento.target.value)}
            required
            aria-invalid={!!erros.email}
            style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
          />
          {erros.email && <span style={{ color: 'red' }}>{erros.email}</span>}
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="password">Senha</label>
          <br />
          <input
            id="password"
            name="password"
            type="password"
            value={senha}
            onChange={(evento) => definirSenha(evento.target.value)}
            required
            aria-invalid={!!erros.senha}
            style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
          />
          {erros.senha && <span style={{ color: 'red' }}>{erros.senha}</span>}
        </div>

        <button type="submit" disabled={carregando} style={{ padding: '0.5rem 1rem', cursor: 'pointer' }}>
          {carregando ? 'Entrando...' : 'Entrar'}
        </button>
      </form>

      <button
        type="button"
        onClick={aoCadastrarSe}
        style={{ marginTop: '1rem', padding: '0.5rem 1rem', cursor: 'pointer' }}
      >
        Cadastrar-se
      </button>
    </main>
  )
}
