package br.com.irrah.fiscal.fiscal.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record ImpostosInformadosRequest(

        @NotNull(message = "O valor informado de ICMS é obrigatório.")
        @DecimalMin(value = "0.00", message = "O ICMS não pode ser negativo.")
        BigDecimal icms,

        @NotNull(message = "O valor informado de PIS é obrigatório.")
        @DecimalMin(value = "0.00", message = "O PIS não pode ser negativo.")
        BigDecimal pis,

        @NotNull(message = "O valor informado de COFINS é obrigatório.")
        @DecimalMin(value = "0.00", message = "O COFINS não pode ser negativo.")
        BigDecimal cofins
) {
}
