package br.com.irrah.fiscal.fiscal.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class FiscalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void autenticar() throws Exception {
        String loginPayload = """
                {
                  "email": "caixa01@erpvarejo.com",
                  "senha": "User@123"
                }
                """;

        String resposta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        token = objectMapper.readTree(resposta).get("token").asString();
    }

    @Test
    void requisicaoSemTokenDeveRetornar403() throws Exception {
        String payload = """
                {
                  "numeroNota": "NF-1001",
                  "ufOrigem": "PR",
                  "ufDestino": "RJ",
                  "itens": []
                }
                """;

        mockMvc.perform(post("/api/fiscal/validar-nota")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void prod001DeveRetornar200ComStatusAprovada() throws Exception {
        String payload = """
                {
                  "numeroNota": "NF-1001",
                  "ufOrigem": "PR",
                  "ufDestino": "RJ",
                  "itens": [
                    {
                      "codigoProduto": "PROD-001",
                      "nome": "Mouse USB Optico",
                      "categoria": "ELETRONICOS",
                      "quantidade": 1,
                      "valorUnitario": 10.00,
                      "desconto": 0.00,
                      "impostosInformados": {
                        "icms": 1.20,
                        "pis": 0.17,
                        "cofins": 0.76
                      }
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/fiscal/validar-nota")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.divergencias").isEmpty());
    }

    @Test
    void prod004DeveRetornar200ComStatusDivergenteEUmaDivergenciaDeIcms() throws Exception {
        String payload = """
                {
                  "numeroNota": "NF-1002",
                  "ufOrigem": "PR",
                  "ufDestino": "RJ",
                  "itens": [
                    {
                      "codigoProduto": "PROD-004",
                      "nome": "Teclado Mecânico",
                      "categoria": "ELETRONICOS",
                      "quantidade": 1,
                      "valorUnitario": 100.00,
                      "desconto": 10.00,
                      "impostosInformados": {
                        "icms": 16.20,
                        "pis": 1.49,
                        "cofins": 6.84
                      }
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/fiscal/validar-nota")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DIVERGENTE"))
                .andExpect(jsonPath("$.divergencias.length()").value(1))
                .andExpect(jsonPath("$.divergencias[0].imposto").value("ICMS"));
    }

    @Test
    void payloadSemItensDeveRetornar400() throws Exception {
        String payload = """
                {
                  "numeroNota": "NF-1003",
                  "ufOrigem": "PR",
                  "ufDestino": "RJ",
                  "itens": []
                }
                """;

        mockMvc.perform(post("/api/fiscal/validar-nota")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void payloadComCategoriaInvalidaDeveRetornar400() throws Exception {
        String payload = """
                {
                  "numeroNota": "NF-1004",
                  "ufOrigem": "PR",
                  "ufDestino": "RJ",
                  "itens": [
                    {
                      "codigoProduto": "PROD-999",
                      "nome": "Produto Teste",
                      "categoria": "CATEGORIA_INEXISTENTE",
                      "quantidade": 1,
                      "valorUnitario": 10.00,
                      "desconto": 0.00,
                      "impostosInformados": {
                        "icms": 1.20,
                        "pis": 0.17,
                        "cofins": 0.76
                      }
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/fiscal/validar-nota")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
