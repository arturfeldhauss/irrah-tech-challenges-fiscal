package br.com.irrah.fiscal.fiscal.dto;

import java.math.BigDecimal;
import java.util.List;

import br.com.irrah.fiscal.fiscal.domain.StatusValidacao;

public record ValidacaoNotaResponse(
        String numeroNota,
        StatusValidacao status,
        BigDecimal valorTotalNota,
        BigDecimal totalImpostosCalculados,
        List<DivergenciaResponse> divergencias
) {
}
