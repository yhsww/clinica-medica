import ConsultasDoUsuario from "../components/ConsultasDoUsuario";
import type { Usuario } from "../types";

type PropriedadesTelaMedico = {
  usuario: Usuario;
  aoSair: () => void;
};

export default function TelaMedico({
  usuario,
  aoSair,
}: PropriedadesTelaMedico) {
  return (
    <main style={{ padding: "2rem", fontFamily: "Arial, sans-serif" }}>
      <h1>Home - Médico</h1>
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
