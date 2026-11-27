import { useState, useEffect } from "react";
import TelaLogin from "./pages/Login";
import TelaAdmin from "./pages/HomeAdmin";
import TelaPaciente from "./pages/HomePaciente";
import TelaMedico from "./pages/HomeMedico";
import TelaCadastrarSe from "./pages/CadastrarSe";
import {
  EVENTO_AUTENTICACAO_EXPIRADA,
  carregarAutenticacao,
  limparAutenticacao,
  salvarAutenticacao,
} from "./api";
import type { Usuario } from "./types";

type EstadoDaAutenticacao = {
  autenticado: boolean;
  usuario: Usuario | null;
};

export default function App() {
  const [autenticacao, definirAutenticacao] = useState<EstadoDaAutenticacao>(
    () => {
      const sessaoArmazenada = carregarAutenticacao();
      return {
        autenticado: sessaoArmazenada !== null,
        usuario: sessaoArmazenada?.usuario ?? null,
      };
    },
  );
  const [telaDeAcesso, definirTelaDeAcesso] = useState<"login" | "cadastrar">(
    "login",
  );

  useEffect(() => {
    function aoExpirarAutenticacao() {
      definirAutenticacao({ autenticado: false, usuario: null });
      definirTelaDeAcesso("login");
    }

    window.addEventListener(
      EVENTO_AUTENTICACAO_EXPIRADA,
      aoExpirarAutenticacao,
    );
    return () =>
      window.removeEventListener(
        EVENTO_AUTENTICACAO_EXPIRADA,
        aoExpirarAutenticacao,
      );
  }, []);

  function aoEntrar(usuario: Usuario, token: string) {
    salvarAutenticacao(usuario, token);
    definirAutenticacao({ autenticado: true, usuario });
    definirTelaDeAcesso("login");
  }

  function aoSair() {
    limparAutenticacao();
    definirAutenticacao({ autenticado: false, usuario: null });
    definirTelaDeAcesso("login");
  }

  if (autenticacao.autenticado && autenticacao.usuario) {
    if (autenticacao.usuario.papel === "paciente") {
      return <TelaPaciente usuario={autenticacao.usuario} aoSair={aoSair} />;
    }

    if (autenticacao.usuario.papel === "medico") {
      return <TelaMedico usuario={autenticacao.usuario} aoSair={aoSair} />;
    }

    return <TelaAdmin usuario={autenticacao.usuario} aoSair={aoSair} />;
  }

  if (telaDeAcesso === "cadastrar") {
    return <TelaCadastrarSe aoVoltar={() => definirTelaDeAcesso("login")} />;
  }

  return (
    <TelaLogin
      aoEntrar={aoEntrar}
      aoCadastrarSe={() => definirTelaDeAcesso("cadastrar")}
    />
  );
}
