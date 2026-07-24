package br.com.irrah.fiscal.fiscal.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.com.irrah.fiscal.fiscal.domain.CategoriaProduto;
import br.com.irrah.fiscal.fiscal.domain.TipoImposto;

public class CalculadoraIcmsTest {

    private final CalculadoraIcms calculadora =
            new CalculadoraIcms();

    @Test
    void deveCalcularIcmsZeroParaCestaBasica() {
        ContextoFiscal contexto = new ContextoFiscal(
                CategoriaProduto.CESTA_BASICA,
                "PR",
                "PR",
                new BigDecimal("100.00")
        );

        CalculoImposto resultado =
                calculadora.calcular(contexto);

        assertEquals(TipoImposto.ICMS, resultado.imposto());
        assertEquals(new BigDecimal("0.00"), resultado.aliquota());
        assertEquals(new BigDecimal("0.00"), resultado.valor());
    }

    @Test
    void deveCalcularIcmsInterno() {
        ContextoFiscal contexto = new ContextoFiscal(
                CategoriaProduto.ELETRONICOS,
                "PR",
                "PR",
                new BigDecimal("100.00")
        );

        CalculoImposto resultado =
                calculadora.calcular(contexto);

        assertEquals(TipoImposto.ICMS, resultado.imposto());
        assertEquals(new BigDecimal("18.00"), resultado.aliquota());
        assertEquals(new BigDecimal("18.00"), resultado.valor());
    }

    @Test
    void deveCalcularIcmsInterestadual() {
        ContextoFiscal contexto = new ContextoFiscal(
                CategoriaProduto.ELETRONICOS,
                "PR",
                "RJ",
                new BigDecimal("90.00")
        );

        CalculoImposto resultado =
                calculadora.calcular(contexto);

        assertEquals(TipoImposto.ICMS, resultado.imposto());
        assertEquals(new BigDecimal("12.00"), resultado.aliquota());
        assertEquals(new BigDecimal("10.80"), resultado.valor());
    }

    @Test
    void deveIgnorarCaixaAoCompararUfs() {
        ContextoFiscal contexto = new ContextoFiscal(
                CategoriaProduto.ELETRONICOS,
                "pr",
                "PR",
                new BigDecimal("100.00")
        );

        CalculoImposto resultado =
                calculadora.calcular(contexto);

        assertEquals(TipoImposto.ICMS, resultado.imposto());
        assertEquals(new BigDecimal("18.00"), resultado.aliquota());
        assertEquals(new BigDecimal("18.00"), resultado.valor());
    }
}
