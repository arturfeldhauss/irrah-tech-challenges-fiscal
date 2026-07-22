# 🛒 Desafio Técnico: Módulo Fiscal & ERP Varejo

Bem-vindo(a) ao desafio técnico para a vaga de **Desenvolvedor(a) Java / Spring**!

Neste desafio, você irá construir a API principal de um **ERP de Varejo**, responsável pela **autenticação de usuários**, **recebimento de dados de notas fiscais** e **validação/cálculo automatizado dos impostos brasileiros** (ICMS, PIS e COFINS).

---
**Prazo:** você deve informar a entrevistadora (sugestão 3 dias úteis, mas se for antes, será considerado como diferencial)
**Entrega:** Repositório público no GitHub (envie o link por e-mail ou WhatsApp)

---

## 🎯 Objetivo
Construir uma aplicação **RESTful em Java + Spring Boot** que atue como um engine de validação fiscal para vendas no varejo. O sistema deve permitir login seguro (JWT) e dispor de um endpoint para processar uma Nota Fiscal (NFe/NFCe) com múltiplos itens, calculando e validando a alíquota e o valor dos impostos de acordo com o regime tributário e estado de origem/destino.

---

## 👥 Tabela de Usuários para Teste (Autenticação JWT)

A aplicação deve disponibilizar estes usuários previamente cadastrados (seja via script `data.sql`, `schema.sql` ou *seeder* no startup da aplicação).

| Nome | E-mail / Username | Senha em Texto Puro | Descrição |
| :--- | :--- | :--- | :--- |
| **Admin ERP** | `admin@erpvarejo.com` | `Admin@123`  | Acesso total ao sistema e gestão fiscal. |
| **Operador Caixa 01** | `caixa01@erpvarejo.com` | `User@123` | Operador do PDV responsável pelo envio e validação das notas. |
| **Operador Caixa 02** | `caixa02@erpvarejo.com` | `User@123` | Segundo operador para testes de autenticação/permissões. |

---

## 🏛️ Regras Fiscais Simplificadas do ERP

No varejo brasileiro, o cálculo de impostos depende da categoria do produto e do fluxo de origem/destino da mercadoria.

### 1. Base de Cálculo (BC):
* **Base de Cálculo por Item** = `(Quantidade * Valor Unitário) - Desconto`
* **Valor do Imposto** = `Base de Cálculo * (Alíquota / 100)`

### 2. ICMS (Imposto sobre Circulação de Mercadorias e Serviços):
* **Venda Interna** (Origem e Destino no mesmo Estado, ex: `PR` ➔ `PR`): Alíquota padrão de **18%**.
* **Venda Interestadual** (Origem e Destino em Estados diferentes, ex: `PR` ➔ `RJ`): Alíquota interestadual de **12%**.
* **Produtos da categoria `CESTA_BASICA`**: Isentos de ICMS (**0%**).

### 3. PIS e COFINS (Regime Não-Cumulativo Varejo):
* **PIS**: Alíquota padrão de **1,65%**.
* **COFINS**: Alíquota padrão de **7,60%**.
* **Produtos da categoria `BEBIDAS_ALCOOLICAS`** (Regime Monofásico no Varejo): Alíquotas de PIS e COFINS zeradas no PDV de saída (**0% PIS** e **0% COFINS**).

---

## 📦 Massa de Dados e Cenários de Teste (6 Produtos)

Abaixo estão os 6 produtos oficiais para testes de validação do endpoint.

### 🟢 Grupo 1: Produtos com Impostos Corretos (Esperado: Status `APROVADA`)

Estes produtos possuem a memória de cálculo exata e não devem apresentar nenhuma divergência.

| Código | Produto | Categoria | Origem ➔ Destino | Qtd x Val. Unit (Desc) | Base de Cálculo | ICMS Inf. | PIS Inf. | COFINS Inf. | Status Esperado | Memória de Cálculo |
| :---: | :--- | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **PROD-001** | Mouse USB Optico | `ELETRONICOS` | **PR ➔ RJ** | 1x R$ 10,00 (R$ 0,00) | **R$ 10,00** | R$ 1,20 | R$ 0,17 | R$ 0,76 | 🟢 OK | • ICMS (12% interestadual) = R$ 1,20<br>• PIS (1,65%) = R$ 0,165 ➔ R$ 0,17<br>• COFINS (7,60%) = R$ 0,76 |
| **PROD-002** | Feijão Carioca 1kg | `CESTA_BASICA` | **PR ➔ PR** | 2x R$ 8,00 (R$ 1,00) | **R$ 15,00** | R$ 0,00 | R$ 0,25 | R$ 1,14 | 🟢 OK | • ICMS (Cesta Básica) = R$ 0,00<br>• PIS (1,65% de R$ 15) = R$ 0,2475 ➔ R$ 0,25<br>• COFINS (7,60% de R$ 15) = R$ 1,14 |
| **PROD-003** | Cerveja IPA 500ml | `BEBIDAS_ALCOOLICAS` | **PR ➔ PR** | 5x R$ 12,00 (R$ 0,00) | **R$ 60,00** | R$ 10,80 | R$ 0,00 | R$ 0,00 | 🟢 OK | • ICMS (18% interno) = R$ 10,80<br>• PIS (Monofásico) = R$ 0,00<br>• COFINS (Monofásico) = R$ 0,00 |

---

### 🔴 Grupo 2: Produtos com Impostos Incorretos (Esperado: Status `DIVERGENTE`)

Estes produtos possuem divergências intencionais que a sua engine fiscal deve capturar e detalhar.

| Código | Produto | Categoria | Origem ➔ Destino | Qtd x Val. Unit (Desc) | Base de Cálculo | ICMS Inf. | PIS Inf. | COFINS Inf. | Status Esperado | Divergência(s) que devem ser apontadas |
| :---: | :--- | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| **PROD-004** | Teclado Mecânico | `ELETRONICOS` | **PR ➔ RJ** | 1x R$ 100,00 (R$ 10,00) | **R$ 90,00** | **R$ 16,20** *(Erro)* | R$ 1,49 | R$ 6,84 | 🔴 ERRO | **Erro no ICMS:** Informou R$ 16,20 (18% interno), mas para PR ➔ RJ a alíquota correta é 12% (**R$ 10,80**). |
| **PROD-005** | Arroz Integral 1kg | `CESTA_BASICA` | **PR ➔ PR** | 1x R$ 20,00 (R$ 0,00) | **R$ 20,00** | **R$ 3,60** *(Erro)* | R$ 0,33 | **R$ 0,00** *(Erro)* | 🔴 ERRO | **1. Erro no ICMS:** Cobrou 18% (R$ 3,60) para item de Cesta Básica (correto = **R$ 0,00**).<br>**2. Erro no COFINS:** Informou R$ 0,00 (correto = 7,60% = **R$ 1,52**). |
| **PROD-006** | Vinho Tinto 750ml | `BEBIDAS_ALCOOLICAS` | **PR ➔ RJ** | 1x R$ 50,00 (R$ 0,00) | **R$ 50,00** | R$ 6,00 | **R$ 0,83** *(Erro)* | **R$ 3,80** *(Erro)* | 🔴 ERRO | **Erro em PIS/COFINS:** Bebidas alcoólicas no varejo são monofásicas (PIS e COFINS **devem ser R$ 0,00**), mas foram informadas alíquotas padrão. |

---

## 📋 Especificação dos Endpoints

### 1. Autenticação (`POST /api/auth/login`)

**Exemplo de Requisição:**
```json
{
  "email": "caixa01@erpvarejo.com",
  "senha": "User@123"
}
```

**Exemplo de Resposta (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "usuario": "caixa01@erpvarejo.com"
}
```

---

### 2. Validação Fiscal (`POST /api/fiscal/validar-nota`)
* Requer Header: `Authorization: Bearer <TOKEN_JWT>`

**Exemplo de Requisição (Com Itens do Grupo 2 - Divergentes):**
```json
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
```

**Exemplo de Resposta para Nota Divergente (200 OK):**
```json
{
  "numeroNota": "NF-1002",
  "status": "DIVERGENTE",
  "valorTotalNota": 90.00,
  "totalImpostosCalculados": 19.13,
  "divergencias": [
    {
      "codigoProduto": "PROD-004",
      "imposto": "ICMS",
      "valorInformado": 16.20,
      "valorCorreto": 10.80,
      "mensagem": "Divergência de ICMS: Operação interestadual (PR -> RJ) deve aplicar 12% sobre a base R$ 90,00."
    }
  ]
}
```

---

## 🛠️ Requisitos Técnicos

* **Linguagem:** Java 17 ou superior.
* **Framework:** Spring Boot 2.x ou superior (Spring Web, Spring Security, Spring Data JPA).
* **Banco de Dados:** H2 Database (em memória) ou PostgreSQL/MySQL via Docker.
* **Tipos de Dados:** Uso obrigatório de `BigDecimal` para cálculos financeiros/fiscais.
* **Margem de Tolerância:** Aceitar variações de arredondamento de até **R$ 0,02** por imposto.

---

## 🧪 Diferenciais Valorizados
* **Testes Unitários** 
* **Swagger/OpenAPI** 
* **Docker** 
* **Design Patterns**

---

## ⚖️ Critérios de Avaliação

1. **Arquitetura & Organização:** Separação clara de responsabilidades .
2. **Qualidade de Código:** Práticas de Clean Code, SOLID.
3. **Precisão Fiscal:** Cálculo exato de arredondamentos e aplicação das regras informadas.
4. **Segurança:** Proteção dos endpoints com JWT.
5. **Testabilidade:** Presença e clareza dos testes automatizados.

Boa sorte!
