package kyj.poo.clinica_medica.security;

import kyj.poo.clinica_medica.model.Papel;
import org.springframework.security.core.Authentication;

/** Utilitário para consultar o papel (role) do usuário autenticado. */
public final class UsuarioAutenticado {

    private UsuarioAutenticado() {}

    public static boolean temPapel(Authentication autenticacao, Papel papel) {
        if (autenticacao == null || papel == null) {
            return false;
        }

        return autenticacao
            .getAuthorities()
            .stream()
            .anyMatch(autoridade ->
                autoridade.getAuthority().equals("ROLE_" + papel.name())
            );
    }
}
