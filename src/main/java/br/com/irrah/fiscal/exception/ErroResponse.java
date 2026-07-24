package br.com.irrah.fiscal.exception;

import java.time.Instant;

public record ErroResponse(
        Instant timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho
) {
}
