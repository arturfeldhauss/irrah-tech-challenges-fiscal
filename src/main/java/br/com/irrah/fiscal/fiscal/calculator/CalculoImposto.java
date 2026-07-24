package br.com.irrah.fiscal.fiscal.calculator;

import java.math.BigDecimal;

import br.com.irrah.fiscal.fiscal.domain.TipoImposto;

public record CalculoImposto(
        TipoImposto imposto,
        BigDecimal aliquota,
        BigDecimal valor
) {
}
