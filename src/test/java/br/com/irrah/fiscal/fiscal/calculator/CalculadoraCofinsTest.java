package br.com.irrah.fiscal.fiscal.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.com.irrah.fiscal.fiscal.domain.CategoriaProduto;
import br.com.irrah.fiscal.fiscal.domain.TipoImposto;

public class CalculadoraCofinsTest {

    private final CalculadoraCofins calculadora =
            new CalculadoraCofins();

    @Test
    void deveCalcularCofinsParaEletronicos() {
        ContextoFiscal contexto = new ContextoFiscal(
                CategoriaProduto.ELETRONICOS,
                "PR",
                "PR",
                new BigDecimal("10.00")
        );

        CalculoImposto resultado =
                calculadora.calcular(contexto);

        assertEquals(TipoImposto.COFINS, resultado.imposto());
        assertEquals(new BigDecimal("7.60"), resultado.aliquota());
        assertEquals(new BigDecimal("0.76"), resultado.valor());
    }

    @Test
    void deveCalcularCofinsParaCestaBasica() {
        ContextoFiscal contexto = new ContextoFiscal(
                CategoriaProduto.CESTA_BASICA,
                "PR",
                "PR",
                new BigDecimal("15.00")
        );

        CalculoImposto resultado =
                calculadora.calcular(contexto);

        assertEquals(TipoImposto.COFINS, resultado.imposto());
        assertEquals(new BigDecimal("1.14"), resultado.valor());
    }

    @Test
    void deveCalcularCofinsZeroParaBebidasAlcoolicas() {
        ContextoFiscal contexto = new ContextoFiscal(
                CategoriaProduto.BEBIDAS_ALCOOLICAS,
                "PR",
                "PR",
                new BigDecimal("60.00")
        );

        CalculoImposto resultado =
                calculadora.calcular(contexto);

        assertEquals(TipoImposto.COFINS, resultado.imposto());
        assertEquals(new BigDecimal("0.00"), resultado.aliquota());
        assertEquals(new BigDecimal("0.00"), resultado.valor());
    }
}
