package br.com.irrah.fiscal.fiscal.calculator;

import java.math.BigDecimal;

import br.com.irrah.fiscal.fiscal.domain.CategoriaProduto;

public record ContextoFiscal(
        CategoriaProduto categoria,
        String ufOrigem,
        String ufDestino,
        BigDecimal baseCalculo
) {
}
