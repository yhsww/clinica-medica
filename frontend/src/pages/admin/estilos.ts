import type { CSSProperties } from "react";

export const estiloTabela: CSSProperties = {
  borderCollapse: "collapse",
  width: "100%",
  marginTop: "0.5rem",
};

export const estiloCelula: CSSProperties = {
  border: "1px solid #ddd",
  padding: "0.75rem",
  textAlign: "left",
};

export const estiloCabecalho: CSSProperties = {
  backgroundColor: "#f5f5f5",
};

export const estiloEntrada: CSSProperties = {
  padding: "0.4rem",
  border: "1px solid #ccc",
  borderRadius: "4px",
  marginRight: "0.5rem",
};

export const estiloBotao: CSSProperties = {
  cursor: "pointer",
  padding: "0.4rem 0.8rem",
  marginRight: "0.5rem",
};

export const estiloErro: CSSProperties = {
  color: "red",
  backgroundColor: "#fff0f0",
  padding: "0.75rem",
};
