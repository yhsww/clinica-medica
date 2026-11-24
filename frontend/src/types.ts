export type Papel = 'paciente' | 'medico' | 'admin'

export type Usuario = {
  nome: string
  email: string
  papel: Papel
}
