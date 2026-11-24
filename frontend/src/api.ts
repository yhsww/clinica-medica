export const BASE_API = 'http://localhost:8080/api'

export async function lerErro(
  resposta: Response,
  padrao = 'Não foi possível concluir a operação.',
): Promise<string> {
  try {
    const corpo = await resposta.json()
    if (Array.isArray(corpo.campos) && corpo.campos.length > 0) {
      return corpo.campos.map((campo: { mensagem: string }) => campo.mensagem).join(' ')
    }
    return corpo.mensagem ?? padrao
  } catch {
    return padrao
  }
}
