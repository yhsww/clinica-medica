import { useState } from "react";
import type { Usuario } from "../types";
import AdminConsultas from "./admin/AdminConsultas";
import AdminContas from "./admin/AdminContas";
import AdminMedicos from "./admin/AdminMedicos";
import AdminPacientes from "./admin/AdminPacientes";
import { estiloBotao } from "./admin/estilos";

type PropriedadesTelaAdmin = {
  usuario: Usuario;
  aoSair: () => void;
};

export default function TelaAdmin({ usuario, aoSair }: PropriedadesTelaAdmin) {
  const [contadorAlteracoesConsultas, definirContadorAlteracoesConsultas] =
    useState(0);

  return (
    <main style={{ padding: "2rem", fontFamily: "Arial, sans-serif" }}>
      <h1>Home - Admin</h1>
      <p>
        Bem-vindo, <strong>{usuario.nome}</strong> ({usuario.papel})
      </p>
      <button
        onClick={aoSair}
        style={{ ...estiloBotao, marginBottom: "1.5rem" }}
      >
        Sair
      </button>

      <AdminMedicos />
      <AdminPacientes />
      <AdminConsultas
        aoAlterar={() =>
          definirContadorAlteracoesConsultas((valorAtual) => valorAtual + 1)
        }
      />
      <AdminContas contadorAlteracoesConsultas={contadorAlteracoesConsultas} />
    </main>
  );
}
