package br.com.irrah.fiscal.fiscal.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import br.com.irrah.fiscal.fiscal.domain.CategoriaProduto;
import br.com.irrah.fiscal.fiscal.domain.TipoImposto;

@Component
public class CalculadoraIcms implements CalculadoraImposto {

    private static final BigDecimal ALIQUOTA_INTERNA =
            new BigDecimal("18.00");

    private static final BigDecimal ALIQUOTA_INTERESTADUAL =
            new BigDecimal("12.00");

    @Override
    public TipoImposto tipo() {
        return TipoImposto.ICMS;
    }

    @Override
    public CalculoImposto calcular(ContextoFiscal contexto) {
        BigDecimal aliquota = determinarAliquota(contexto);

        BigDecimal valor = calcularPercentual(
                contexto.baseCalculo(),
                aliquota
        );

        return new CalculoImposto(
                TipoImposto.ICMS,
                aliquota,
                valor
        );
    }

    private BigDecimal determinarAliquota(ContextoFiscal contexto) {
        if (contexto.categoria() == CategoriaProduto.CESTA_BASICA) {
            return BigDecimal.ZERO.setScale(2);
        }

        boolean operacaoInterna = contexto.ufOrigem()
                .equalsIgnoreCase(contexto.ufDestino());

        return operacaoInterna
                ? ALIQUOTA_INTERNA
                : ALIQUOTA_INTERESTADUAL;
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
