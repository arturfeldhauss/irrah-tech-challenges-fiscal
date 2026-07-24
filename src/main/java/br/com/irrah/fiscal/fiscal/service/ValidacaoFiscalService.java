package br.com.irrah.fiscal.fiscal.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import br.com.irrah.fiscal.fiscal.calculator.CalculadoraImposto;
import br.com.irrah.fiscal.fiscal.calculator.CalculoImposto;
import br.com.irrah.fiscal.fiscal.calculator.ContextoFiscal;
import br.com.irrah.fiscal.fiscal.domain.CategoriaProduto;
import br.com.irrah.fiscal.fiscal.domain.StatusValidacao;
import br.com.irrah.fiscal.fiscal.domain.TipoImposto;
import br.com.irrah.fiscal.fiscal.dto.DivergenciaResponse;
import br.com.irrah.fiscal.fiscal.dto.ImpostosInformadosRequest;
import br.com.irrah.fiscal.fiscal.dto.ItemNotaRequest;
import br.com.irrah.fiscal.fiscal.dto.ValidacaoNotaResponse;
import br.com.irrah.fiscal.fiscal.dto.ValidarNotaRequest;

@Service
public class ValidacaoFiscalService {

    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");

    private static final BigDecimal TOLERANCIA = new BigDecimal("0.02");

    private final List<CalculadoraImposto> calculadoras;

    public ValidacaoFiscalService(List<CalculadoraImposto> calculadoras) {
        this.calculadoras = calculadoras;
    }

    public ValidacaoNotaResponse validar(ValidarNotaRequest request) {
        List<DivergenciaResponse> divergencias = new ArrayList<>();

        BigDecimal valorTotalNota = BigDecimal.ZERO;
        BigDecimal totalImpostosCalculados = BigDecimal.ZERO;

        for (ItemNotaRequest item : request.itens()) {
            BigDecimal baseCalculo = calcularBaseCalculo(item);
            valorTotalNota = valorTotalNota.add(baseCalculo);

            ContextoFiscal contexto = new ContextoFiscal(
                    item.categoria(),
                    request.ufOrigem(),
                    request.ufDestino(),
                    baseCalculo
            );

            for (CalculadoraImposto calculadora : calculadoras) {
                CalculoImposto calculo = calculadora.calcular(contexto);
                totalImpostosCalculados = totalImpostosCalculados.add(calculo.valor());

                BigDecimal valorInformado = obterValorInformado(
                        calculo.imposto(),
                        item.impostosInformados()
                );

                BigDecimal diferenca = valorInformado.subtract(calculo.valor());

                if (!estaDentroDaTolerancia(diferenca)) {
                    divergencias.add(new DivergenciaResponse(
                            item.codigoProduto(),
                            calculo.imposto(),
                            valorInformado,
                            calculo.valor(),
                            criarMensagem(calculo.imposto(), contexto)
                    ));
                }
            }
        }

        StatusValidacao status = divergencias.isEmpty()
                ? StatusValidacao.APROVADA
                : StatusValidacao.DIVERGENTE;

        return new ValidacaoNotaResponse(
                request.numeroNota(),
                status,
                arredondar(valorTotalNota),
                arredondar(totalImpostosCalculados),
                divergencias
        );
    }

    private BigDecimal calcularBaseCalculo(ItemNotaRequest item) {
        BigDecimal valorBruto = item.quantidade().multiply(item.valorUnitario());
        BigDecimal desconto = item.desconto();

        if (desconto.compareTo(valorBruto) > 0) {
            throw new IllegalArgumentException(
                    "O desconto informado é maior que o valor bruto do item "
                            + item.codigoProduto() + "."
            );
        }

        return arredondar(valorBruto.subtract(desconto));
    }

    private BigDecimal obterValorInformado(
            TipoImposto tipo,
            ImpostosInformadosRequest impostosInformados) {

        return switch (tipo) {
            case ICMS -> impostosInformados.icms();
            case PIS -> impostosInformados.pis();
            case COFINS -> impostosInformados.cofins();
        };
    }

    private boolean estaDentroDaTolerancia(BigDecimal diferenca) {
        return diferenca.abs().compareTo(TOLERANCIA) <= 0;
    }

    private String criarMensagem(TipoImposto tipo, ContextoFiscal contexto) {
        String baseFormatada = formatarMoeda(contexto.baseCalculo());

        return switch (tipo) {
            case ICMS -> criarMensagemIcms(contexto, baseFormatada);
            case PIS -> criarMensagemPis(contexto, baseFormatada);
            case COFINS -> criarMensagemCofins(contexto, baseFormatada);
        };
    }

    private String criarMensagemIcms(ContextoFiscal contexto, String baseFormatada) {
        if (contexto.categoria() == CategoriaProduto.CESTA_BASICA) {
            return String.format(
                    LOCALE_PT_BR,
                    "Divergência de ICMS: categoria CESTA_BASICA é isenta de ICMS (0%%) sobre a base R$ %s.",
                    baseFormatada
            );
        }

        boolean operacaoInterna = contexto.ufOrigem()
                .equalsIgnoreCase(contexto.ufDestino());

        if (operacaoInterna) {
            return String.format(
                    LOCALE_PT_BR,
                    "Divergência de ICMS: Operação interna (%s -> %s) deve aplicar 18%% sobre a base R$ %s.",
                    contexto.ufOrigem(), contexto.ufDestino(), baseFormatada
            );
        }

        return String.format(
                LOCALE_PT_BR,
                "Divergência de ICMS: Operação interestadual (%s -> %s) deve aplicar 12%% sobre a base R$ %s.",
                contexto.ufOrigem(), contexto.ufDestino(), baseFormatada
        );
    }

    private String criarMensagemPis(ContextoFiscal contexto, String baseFormatada) {
        if (contexto.categoria() == CategoriaProduto.BEBIDAS_ALCOOLICAS) {
            return String.format(
                    LOCALE_PT_BR,
                    "Divergência de PIS: regime monofásico zera o PIS (0%%) sobre a base R$ %s.",
                    baseFormatada
            );
        }

        return String.format(
                LOCALE_PT_BR,
                "Divergência de PIS: a alíquota correta é 1,65%% sobre a base R$ %s.",
                baseFormatada
        );
    }

    private String criarMensagemCofins(ContextoFiscal contexto, String baseFormatada) {
        if (contexto.categoria() == CategoriaProduto.BEBIDAS_ALCOOLICAS) {
            return String.format(
                    LOCALE_PT_BR,
                    "Divergência de COFINS: regime monofásico zera a COFINS (0%%) sobre a base R$ %s.",
                    baseFormatada
            );
        }

        return String.format(
                LOCALE_PT_BR,
                "Divergência de COFINS: a alíquota correta é 7,60%% sobre a base R$ %s.",
                baseFormatada
        );
    }

    private String formatarMoeda(BigDecimal valor) {
        return String.format(LOCALE_PT_BR, "%.2f", valor);
    }

    private BigDecimal arredondar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
