package br.com.irrah.fiscal.fiscal.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.com.irrah.fiscal.fiscal.domain.CategoriaProduto;
import br.com.irrah.fiscal.fiscal.domain.TipoImposto;

public class CalculadoraPisTest {

    private final CalculadoraPis calculadora =
            new CalculadoraPis();

    @Test
    void deveCalcularPisParaEletronicos() {
        ContextoFiscal contexto = new ContextoFiscal(
                CategoriaProduto.ELETRONICOS,
                "PR",
                "PR",
                new BigDecimal("10.00")
        );

        CalculoImposto resultado =
                calculadora.calcular(contexto);

        assertEquals(TipoImposto.PIS, resultado.imposto());
        assertEquals(new BigDecimal("1.65"), resultado.aliquota());
        assertEquals(new BigDecimal("0.17"), resultado.valor());
    }

    @Test
    void deveCalcularPisParaCestaBasica() {
        ContextoFiscal contexto = new ContextoFiscal(
                CategoriaProduto.CESTA_BASICA,
                "PR",
                "PR",
                new BigDecimal("15.00")
        );

        CalculoImposto resultado =
                calculadora.calcular(contexto);

        assertEquals(TipoImposto.PIS, resultado.imposto());
        assertEquals(new BigDecimal("0.25"), resultado.valor());
    }

    @Test
    void deveCalcularPisZeroParaBebidasAlcoolicas() {
        ContextoFiscal contexto = new ContextoFiscal(
                CategoriaProduto.BEBIDAS_ALCOOLICAS,
                "PR",
                "PR",
                new BigDecimal("60.00")
        );

        CalculoImposto resultado =
                calculadora.calcular(contexto);

        assertEquals(TipoImposto.PIS, resultado.imposto());
        assertEquals(new BigDecimal("0.00"), resultado.aliquota());
        assertEquals(new BigDecimal("0.00"), resultado.valor());
    }
}
