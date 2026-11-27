import type { Usuario } from './types'

export const URL_BASE_API = 'http://localhost:8080/api'

const CHAVE_USUARIO = 'user'
const CHAVE_TOKEN_ACESSO = 'token'
export const EVENTO_AUTENTICACAO_EXPIRADA = 'autenticacao-expirada'

export function salvarAutenticacao(usuario: Usuario, token: string) {
  localStorage.setItem(CHAVE_USUARIO, JSON.stringify(usuario))
  localStorage.setItem(CHAVE_TOKEN_ACESSO, token)
}

export function limparAutenticacao() {
  localStorage.removeItem(CHAVE_USUARIO)
  localStorage.removeItem(CHAVE_TOKEN_ACESSO)
}

export function carregarAutenticacao(): { usuario: Usuario; token: string } | null {
  const usuarioArmazenado = localStorage.getItem(CHAVE_USUARIO)
  const token = localStorage.getItem(CHAVE_TOKEN_ACESSO)

  if (!usuarioArmazenado || !token) {
    limparAutenticacao()
    return null
  }

  try {
    const dadosUsuario = JSON.parse(usuarioArmazenado)
    const usuario: Usuario = {
      id: Number(dadosUsuario.id),
      nome: dadosUsuario.nome ?? dadosUsuario.name,
      email: dadosUsuario.email,
      papel: String(dadosUsuario.papel ?? dadosUsuario.role ?? '').toLowerCase() as Usuario['papel'],
    }

    if (!usuario.id || !usuario.nome || !usuario.email || !usuario.papel) {
      limparAutenticacao()
      return null
    }

    return { usuario, token }
  } catch {
    limparAutenticacao()
    return null
  }
}

export async function requisicaoApi(
  caminho: string,
  opcoes: RequestInit = {},
): Promise<Response> {
  const cabecalhos = new Headers(opcoes.headers)
  const token = localStorage.getItem(CHAVE_TOKEN_ACESSO)

  if (token) {
    cabecalhos.set('Authorization', `Bearer ${token}`)
  }
  if (opcoes.body != null && !cabecalhos.has('Content-Type')) {
    cabecalhos.set('Content-Type', 'application/json')
  }

  const resposta = await fetch(`${URL_BASE_API}${caminho}`, {
    ...opcoes,
    headers: cabecalhos,
  })

  if (resposta.status === 401) {
    limparAutenticacao()
    window.dispatchEvent(new Event(EVENTO_AUTENTICACAO_EXPIRADA))
  }

  return resposta
}

export async function obterMensagemDeErro(
  resposta: Response,
  mensagemPadrao = 'Não foi possível concluir a operação.',
): Promise<string> {
  try {
    const corpoDaResposta = await resposta.json()
    if (Array.isArray(corpoDaResposta.campos) && corpoDaResposta.campos.length > 0) {
      return corpoDaResposta.campos.map((erroDeCampo: { mensagem: string }) => erroDeCampo.mensagem).join(' ')
    }
    return corpoDaResposta.mensagem ?? mensagemPadrao
  } catch {
    return mensagemPadrao
  }
}
