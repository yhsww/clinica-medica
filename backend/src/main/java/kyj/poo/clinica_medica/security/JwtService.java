package kyj.poo.clinica_medica.security;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import kyj.poo.clinica_medica.model.Credencial;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoMillis;

    public JwtService(
        @Value("${security.jwt.secret}") String segredo,
        @Value("${security.jwt.expiracao-millis}") long expiracaoMillis
    ) {
        this.chave = Keys.hmacShaKeyFor(
            segredo.getBytes(StandardCharsets.UTF_8)
        );
        this.expiracaoMillis = expiracaoMillis;
    }

    public String gerarToken(Credencial credencial) {
        Instant agora = Instant.now();

        return Jwts.builder()
            .subject(credencial.getUsuario().getEmail())
            .claim("nome", credencial.getUsuario().getNome())
            .claim("papel", credencial.getPapel().name())
            .issuedAt(Date.from(agora))
            .expiration(Date.from(agora.plusMillis(expiracaoMillis)))
            .signWith(chave)
            .compact();
    }

    public String extrairEmail(String token) {
        return parser().parseSignedClaims(token).getPayload().getSubject();
    }

    private JwtParser parser() {
        return Jwts.parser().verifyWith(chave).build();
    }
}
