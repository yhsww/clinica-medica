import ConsultasDoUsuario from "../components/ConsultasDoUsuario";
import type { Usuario } from "../types";

type PropriedadesTelaPaciente = {
  usuario: Usuario;
  aoSair: () => void;
};

export default function TelaPaciente({
  usuario,
  aoSair,
}: PropriedadesTelaPaciente) {
  return (
    <main style={{ padding: "2rem", fontFamily: "Arial, sans-serif" }}>
      <h1>Home - Paciente</h1>
      <p>
        Bem-vindo, <strong>{usuario.nome}</strong>
      </p>
      <button
        onClick={aoSair}
        style={{ cursor: "pointer", padding: "0.4rem 0.8rem" }}
      >
        Sair
      </button>
      <ConsultasDoUsuario usuario={usuario} />
    </main>
  );
}
