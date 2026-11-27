export type Papel = "paciente" | "medico" | "admin";

export type Usuario = {
  id: number;
  nome: string;
  email: string;
  papel: Papel;
};
