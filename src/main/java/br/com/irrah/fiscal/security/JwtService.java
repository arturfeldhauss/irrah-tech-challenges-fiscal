package br.com.irrah.fiscal.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracao;

    public JwtService(
            @Value("${jwt.secret}") String segredo,
            @Value("${jwt.expiration}") long expiracao) {

        this.chave = Keys.hmacShaKeyFor(
                segredo.getBytes(StandardCharsets.UTF_8));

        this.expiracao = expiracao;
    }

    public String gerarToken(String email) {
        Date agora = new Date();
        Date vencimento = new Date(agora.getTime() + expiracao);

        return Jwts.builder()
                .subject(email)
                .issuedAt(agora)
                .expiration(vencimento)
                .signWith(chave)
                .compact();
    }

    public String extrairEmail(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean tokenValido(String token) {
        try {
            Jwts.parser()
                    .verifyWith(chave)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }
}