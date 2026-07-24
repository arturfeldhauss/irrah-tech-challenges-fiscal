package br.com.irrah.fiscal.fiscal.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record ValidarNotaRequest(

        @NotBlank(message = "O número da nota é obrigatório.")
        String numeroNota,

        @NotBlank(message = "A UF de origem é obrigatória.")
        @Pattern(
                regexp = "^[A-Za-z]{2}$",
                message = "A UF de origem deve possuir duas letras."
        )
        String ufOrigem,

        @NotBlank(message = "A UF de destino é obrigatória.")
        @Pattern(
                regexp = "^[A-Za-z]{2}$",
                message = "A UF de destino deve possuir duas letras."
        )
        String ufDestino,

        @NotEmpty(message = "A nota deve possuir pelo menos um item.")
        List<@Valid ItemNotaRequest> itens
) {
}
