package br.com.irrah.fiscal.fiscal.dto;

import java.math.BigDecimal;

import br.com.irrah.fiscal.fiscal.domain.TipoImposto;

public record DivergenciaResponse(
        String codigoProduto,
        TipoImposto imposto,
        BigDecimal valorInformado,
        BigDecimal valorCorreto,
        String mensagem
) {
}
