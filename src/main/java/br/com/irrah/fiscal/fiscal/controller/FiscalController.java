package br.com.irrah.fiscal.fiscal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.irrah.fiscal.fiscal.dto.ValidacaoNotaResponse;
import br.com.irrah.fiscal.fiscal.dto.ValidarNotaRequest;
import br.com.irrah.fiscal.fiscal.service.ValidacaoFiscalService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fiscal")
public class FiscalController {

    private final ValidacaoFiscalService validacaoFiscalService;

    public FiscalController(ValidacaoFiscalService validacaoFiscalService) {
        this.validacaoFiscalService = validacaoFiscalService;
    }

    @PostMapping("/validar-nota")
    public ResponseEntity<ValidacaoNotaResponse> validarNota(
            @Valid @RequestBody ValidarNotaRequest request) {

        return ResponseEntity.ok(
                validacaoFiscalService.validar(request));
    }
}
