package br.com.irrah.fiscal.auth.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teste")
public class TestAuthController {

    @GetMapping("/autenticado")
    public Map<String, String> autenticado(
            Authentication authentication) {

        return Map.of(
                "mensagem", "Token válido.",
                "usuario", authentication.getName()
        );
    }
}