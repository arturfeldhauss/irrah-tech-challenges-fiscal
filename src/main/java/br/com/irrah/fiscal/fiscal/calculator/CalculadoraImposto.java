package br.com.irrah.fiscal.fiscal.calculator;

import br.com.irrah.fiscal.fiscal.domain.TipoImposto;

public interface CalculadoraImposto {

    TipoImposto tipo();

    CalculoImposto calcular(ContextoFiscal contexto);
}
