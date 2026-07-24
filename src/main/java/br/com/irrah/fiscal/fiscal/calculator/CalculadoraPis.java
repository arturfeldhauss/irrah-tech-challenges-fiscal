package br.com.irrah.fiscal.fiscal.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import br.com.irrah.fiscal.fiscal.domain.CategoriaProduto;
import br.com.irrah.fiscal.fiscal.domain.TipoImposto;

@Component
public class CalculadoraPis implements CalculadoraImposto {

    private static final BigDecimal ALIQUOTA_PADRAO =
            new BigDecimal("1.65");

    @Override
    public TipoImposto tipo() {
        return TipoImposto.PIS;
    }

    @Override
    public CalculoImposto calcular(ContextoFiscal contexto) {
        BigDecimal aliquota =
                contexto.categoria()
                        == CategoriaProduto.BEBIDAS_ALCOOLICAS
                        ? BigDecimal.ZERO.setScale(2)
                        : ALIQUOTA_PADRAO;

        BigDecimal valor = calcularPercentual(
                contexto.baseCalculo(),
                aliquota
        );

        return new CalculoImposto(
                TipoImposto.PIS,
                aliquota,
                valor
        );
    }

    private BigDecimal calcularPercentual(
            BigDecimal baseCalculo,
            BigDecimal aliquota) {

        return baseCalculo
                .multiply(aliquota)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
