import { useState, useEffect } from 'react'
import TelaLogin from './pages/Login'
import TelaAdmin from './pages/HomeAdmin'
import TelaPaciente from './pages/HomePaciente'
import TelaMedico from './pages/HomeMedico'
import TelaCadastrarSe from './pages/CadastrarSe'
import type { Usuario } from './types'

type EstadoAutenticacao = {
  autenticado: boolean
  usuario: Usuario | null
}

function carregarAutenticacao(): EstadoAutenticacao {
  const dadosBrutos = localStorage.getItem('user')

  if (!dadosBrutos) {
    return { autenticado: false, usuario: null }
  }

  const dados = JSON.parse(dadosBrutos)
  const usuario: Usuario = {
    nome: dados.nome ?? dados.name,
    email: dados.email,
    papel: (dados.papel ?? dados.role) as Usuario['papel'],
  }

  return { autenticado: true, usuario }
}

function limparAutenticacao() {
  localStorage.removeItem('user')
}

function App() {
  const [autenticacao, definirAutenticacao] = useState<EstadoAutenticacao>({
    autenticado: false,
    usuario: null,
  })
  const [inicializado, definirInicializado] = useState(false)
  const [tela, definirTela] = useState<'login' | 'cadastrar'>('login')

  useEffect(() => {
    definirAutenticacao(carregarAutenticacao())
    definirInicializado(true)
  }, [])

  function aoEntrar(usuario: Usuario) {
    localStorage.setItem('user', JSON.stringify(usuario))
    definirAutenticacao({ autenticado: true, usuario })
    definirTela('login')
  }

  function aoSair() {
    limparAutenticacao()
    definirAutenticacao({ autenticado: false, usuario: null })
    definirTela('login')
  }

  if (!inicializado) {
    return <main>Carregando...</main>
  }

  if (autenticacao.autenticado && autenticacao.usuario) {
    if (autenticacao.usuario.papel === 'paciente') {
      return <TelaPaciente usuario={autenticacao.usuario} aoSair={aoSair} />
    }

    if (autenticacao.usuario.papel === 'medico') {
      return <TelaMedico usuario={autenticacao.usuario} aoSair={aoSair} />
    }

    return <TelaAdmin usuario={autenticacao.usuario} aoSair={aoSair} />
  }

  if (tela === 'cadastrar') {
    return <TelaCadastrarSe aoVoltar={() => definirTela('login')} />
  }

  return <TelaLogin aoEntrar={aoEntrar} aoCadastrarSe={() => definirTela('cadastrar')} />
}

export default App
