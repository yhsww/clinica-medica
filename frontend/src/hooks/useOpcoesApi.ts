import { useEffect, useState } from "react";
import { requisicaoApi } from "../api";

export function useOpcoesApi(caminho: string): string[] {
  const [opcoes, definirOpcoes] = useState<string[]>([]);

  useEffect(() => {
    let montado = true;

    async function carregarOpcoes() {
      try {
        const resposta = await requisicaoApi(caminho);

        if (!resposta.ok || !montado) return;

        const opcoesRecebidas: string[] = await resposta.json();

        if (montado) definirOpcoes(opcoesRecebidas);
      } catch {
        if (montado) definirOpcoes([]);
      }
    }

    carregarOpcoes();

    return () => {
      montado = false;
    };
  }, [caminho]);

  return opcoes;
}
