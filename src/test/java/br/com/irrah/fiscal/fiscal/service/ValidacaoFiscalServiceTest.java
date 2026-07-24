package br.com.irrah.fiscal.fiscal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.irrah.fiscal.fiscal.calculator.CalculadoraCofins;
import br.com.irrah.fiscal.fiscal.calculator.CalculadoraIcms;
import br.com.irrah.fiscal.fiscal.calculator.CalculadoraPis;
import br.com.irrah.fiscal.fiscal.domain.CategoriaProduto;
import br.com.irrah.fiscal.fiscal.domain.StatusValidacao;
import br.com.irrah.fiscal.fiscal.domain.TipoImposto;
import br.com.irrah.fiscal.fiscal.dto.DivergenciaResponse;
import br.com.irrah.fiscal.fiscal.dto.ImpostosInformadosRequest;
import br.com.irrah.fiscal.fiscal.dto.ItemNotaRequest;
import br.com.irrah.fiscal.fiscal.dto.ValidacaoNotaResponse;
import br.com.irrah.fiscal.fiscal.dto.ValidarNotaRequest;

public class ValidacaoFiscalServiceTest {

    private final ValidacaoFiscalService service = new ValidacaoFiscalService(
            List.of(
                    new CalculadoraIcms(),
                    new CalculadoraPis(),
                    new CalculadoraCofins()
            )
    );

    private ItemNotaRequest item(
            String codigo,
            CategoriaProduto categoria,
            BigDecimal quantidade,
            BigDecimal valorUnitario,
            BigDecimal desconto,
            BigDecimal icmsInformado,
            BigDecimal pisInformado,
            BigDecimal cofinsInformado) {

        return new ItemNotaRequest(
                codigo,
                codigo,
                categoria,
                quantidade,
                valorUnitario,
                desconto,
                new ImpostosInformadosRequest(icmsInformado, pisInformado, cofinsInformado)
        );
    }

    @Test
    void prod001DeveRetornarAprovadaSemDivergencias() {
        ValidarNotaRequest request = new ValidarNotaRequest(
                "NF-1001",
                "PR",
                "RJ",
                List.of(item(
                        "PROD-001",
                        CategoriaProduto.ELETRONICOS,
                        new BigDecimal("1"),
                        new BigDecimal("10.00"),
                        new BigDecimal("0.00"),
                        new BigDecimal("1.20"),
                        new BigDecimal("0.17"),
                        new BigDecimal("0.76")
                ))
        );

        ValidacaoNotaResponse resposta = service.validar(request);

        assertEquals(StatusValidacao.APROVADA, resposta.status());
        assertTrue(resposta.divergencias().isEmpty());
    }

    @Test
    void prod004DeveRetornarDivergenteComDivergenciaDeIcms() {
        ValidarNotaRequest request = new ValidarNotaRequest(
                "NF-1002",
                "PR",
                "RJ",
                List.of(item(
                        "PROD-004",
                        CategoriaProduto.ELETRONICOS,
                        new BigDecimal("1"),
                        new BigDecimal("100.00"),
                        new BigDecimal("10.00"),
                        new BigDecimal("16.20"),
                        new BigDecimal("1.49"),
                        new BigDecimal("6.84")
                ))
        );

        ValidacaoNotaResponse resposta = service.validar(request);

        assertEquals(StatusValidacao.DIVERGENTE, resposta.status());
        assertEquals(1, resposta.divergencias().size());

        DivergenciaResponse divergencia = resposta.divergencias().get(0);
        assertEquals("PROD-004", divergencia.codigoProduto());
        assertEquals(TipoImposto.ICMS, divergencia.imposto());
        assertEquals(new BigDecimal("16.20"), divergencia.valorInformado());
        assertEquals(new BigDecimal("10.80"), divergencia.valorCorreto());

        assertEquals(new BigDecimal("90.00"), resposta.valorTotalNota());
        assertEquals(new BigDecimal("19.13"), resposta.totalImpostosCalculados());
    }

    @Test
    void prod005DeveRetornarDivergenciasDeIcmsECofins() {
        ValidarNotaRequest request = new ValidarNotaRequest(
                "NF-1003",
                "PR",
                "PR",
                List.of(item(
                        "PROD-005",
                        CategoriaProduto.CESTA_BASICA,
                        new BigDecimal("1"),
                        new BigDecimal("20.00"),
                        new BigDecimal("0.00"),
                        new BigDecimal("3.60"),
                        new BigDecimal("0.33"),
                        new BigDecimal("0.00")
                ))
        );

        ValidacaoNotaResponse resposta = service.validar(request);

        assertEquals(StatusValidacao.DIVERGENTE, resposta.status());
        assertEquals(2, resposta.divergencias().size());

        List<TipoImposto> impostosDivergentes = resposta.divergencias().stream()
                .map(DivergenciaResponse::imposto)
                .toList();

        assertTrue(impostosDivergentes.contains(TipoImposto.ICMS));
        assertTrue(impostosDivergentes.contains(TipoImposto.COFINS));
    }

    @Test
    void prod006DeveRetornarDivergenciasDePisECofins() {
        ValidarNotaRequest request = new ValidarNotaRequest(
                "NF-1004",
                "PR",
                "RJ",
                List.of(item(
                        "PROD-006",
                        CategoriaProduto.BEBIDAS_ALCOOLICAS,
                        new BigDecimal("1"),
                        new BigDecimal("50.00"),
                        new BigDecimal("0.00"),
                        new BigDecimal("6.00"),
                        new BigDecimal("0.83"),
                        new BigDecimal("3.80")
                ))
        );

        ValidacaoNotaResponse resposta = service.validar(request);

        assertEquals(StatusValidacao.DIVERGENTE, resposta.status());
        assertEquals(2, resposta.divergencias().size());

        List<TipoImposto> impostosDivergentes = resposta.divergencias().stream()
                .map(DivergenciaResponse::imposto)
                .toList();

        assertTrue(impostosDivergentes.contains(TipoImposto.PIS));
        assertTrue(impostosDivergentes.contains(TipoImposto.COFINS));
    }

    @Test
    void diferencaExataDe002DeveSerAceita() {
        ValidarNotaRequest request = new ValidarNotaRequest(
                "NF-1005",
                "PR",
                "PR",
                List.of(item(
                        "PROD-TOL-01",
                        CategoriaProduto.ELETRONICOS,
                        new BigDecimal("1"),
                        new BigDecimal("10.00"),
                        new BigDecimal("0.00"),
                        new BigDecimal("1.82"),
                        new BigDecimal("0.17"),
                        new BigDecimal("0.76")
                ))
        );

        ValidacaoNotaResponse resposta = service.validar(request);

        assertEquals(StatusValidacao.APROVADA, resposta.status());
        assertTrue(resposta.divergencias().isEmpty());
    }

    @Test
    void diferencaDe003DeveGerarDivergencia() {
        ValidarNotaRequest request = new ValidarNotaRequest(
                "NF-1006",
                "PR",
                "PR",
                List.of(item(
                        "PROD-TOL-02",
                        CategoriaProduto.ELETRONICOS,
                        new BigDecimal("1"),
                        new BigDecimal("10.00"),
                        new BigDecimal("0.00"),
                        new BigDecimal("1.83"),
                        new BigDecimal("0.17"),
                        new BigDecimal("0.76")
                ))
        );

        ValidacaoNotaResponse resposta = service.validar(request);

        assertEquals(StatusValidacao.DIVERGENTE, resposta.status());
        assertEquals(1, resposta.divergencias().size());
        assertEquals(TipoImposto.ICMS, resposta.divergencias().get(0).imposto());
    }

    @Test
    void descontoMaiorQueValorBrutoDeveLancarIllegalArgumentException() {
        ValidarNotaRequest request = new ValidarNotaRequest(
                "NF-1007",
                "PR",
                "PR",
                List.of(item(
                        "PROD-DESCONTO-INVALIDO",
                        CategoriaProduto.ELETRONICOS,
                        new BigDecimal("1"),
                        new BigDecimal("10.00"),
                        new BigDecimal("20.00"),
                        new BigDecimal("0.00"),
                        new BigDecimal("0.00"),
                        new BigDecimal("0.00")
                ))
        );

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> service.validar(request)
        );

        assertTrue(excecao.getMessage().contains("PROD-DESCONTO-INVALIDO"));
    }

    @Test
    void notaComMultiplosItensDeveSomarValorTotalEImpostosCorretamente() {
        ValidarNotaRequest request = new ValidarNotaRequest(
                "NF-1008",
                "PR",
                "RJ",
                List.of(
                        item(
                                "PROD-001",
                                CategoriaProduto.ELETRONICOS,
                                new BigDecimal("1"),
                                new BigDecimal("10.00"),
                                new BigDecimal("0.00"),
                                new BigDecimal("1.20"),
                                new BigDecimal("0.17"),
                                new BigDecimal("0.76")
                        ),
                        item(
                                "PROD-002",
                                CategoriaProduto.CESTA_BASICA,
                                new BigDecimal("2"),
                                new BigDecimal("8.00"),
                                new BigDecimal("1.00"),
                                new BigDecimal("0.00"),
                                new BigDecimal("0.25"),
                                new BigDecimal("1.14")
                        )
                )
        );

        ValidacaoNotaResponse resposta = service.validar(request);

        assertEquals(StatusValidacao.APROVADA, resposta.status());
        assertEquals(new BigDecimal("25.00"), resposta.valorTotalNota());
        assertEquals(new BigDecimal("3.52"), resposta.totalImpostosCalculados());
    }

    @Test
    void notaComQualquerDivergenciaDeveReceberStatusDivergente() {
        ValidarNotaRequest request = new ValidarNotaRequest(
                "NF-1009",
                "PR",
                "RJ",
                List.of(
                        item(
                                "PROD-001",
                                CategoriaProduto.ELETRONICOS,
                                new BigDecimal("1"),
                                new BigDecimal("10.00"),
                                new BigDecimal("0.00"),
                                new BigDecimal("1.20"),
                                new BigDecimal("0.17"),
                                new BigDecimal("0.76")
                        ),
                        item(
                                "PROD-004",
                                CategoriaProduto.ELETRONICOS,
                                new BigDecimal("1"),
                                new BigDecimal("100.00"),
                                new BigDecimal("10.00"),
                                new BigDecimal("16.20"),
                                new BigDecimal("1.49"),
                                new BigDecimal("6.84")
                        )
                )
        );

        ValidacaoNotaResponse resposta = service.validar(request);

        assertEquals(StatusValidacao.DIVERGENTE, resposta.status());
        assertEquals(1, resposta.divergencias().size());
    }
}
