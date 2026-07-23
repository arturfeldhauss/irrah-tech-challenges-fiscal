package br.com.irrah.fiscal.auth.dto;

public record LoginResponse(
        String token,
        String tipo,
        String usuario
) {
}