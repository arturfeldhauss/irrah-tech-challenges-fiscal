package br.com.irrah.fiscal.fiscal.dto;

import java.math.BigDecimal;

import br.com.irrah.fiscal.fiscal.domain.CategoriaProduto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemNotaRequest(

        @NotBlank(message = "O código do produto é obrigatório.")
        String codigoProduto,

        @NotBlank(message = "O nome do produto é obrigatório.")
        String nome,

        @NotNull(message = "A categoria do produto é obrigatória.")
        CategoriaProduto categoria,

        @NotNull(message = "A quantidade é obrigatória.")
        @DecimalMin(value = "0.01", message = "A quantidade deve ser maior que zero.")
        BigDecimal quantidade,

        @NotNull(message = "O valor unitário é obrigatório.")
        @DecimalMin(value = "0.00", message = "O valor unitário não pode ser negativo.")
        BigDecimal valorUnitario,

        @NotNull(message = "O desconto é obrigatório.")
        @DecimalMin(value = "0.00", message = "O desconto não pode ser negativo.")
        BigDecimal desconto,

        @NotNull(message = "Os impostos informados são obrigatórios.")
        @Valid
        ImpostosInformadosRequest impostosInformados
) {
}
