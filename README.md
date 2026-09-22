# Customer Management Service

Desafio técnico Java Pleno: CRUD de clientes + integração HTTP de score.

## Stack
Java 17, Spring Boot 3.5, Maven, Spring Web, Spring Data JPA, JdbcTemplate, H2, Spring Security Basic Auth, Validation, JUnit/Mockito e WireMock.

## Executar
```bash
mvn clean test
mvn spring-boot:run
```
API: `http://localhost:8080`. H2 é em memória.

## Segurança
- `user / user123`: consultas (GET).
- `admin / admin123`: consultas + POST/PUT/DELETE.
Credenciais são apenas para o desafio e devem ser externalizadas em produção.

## Endpoints
- `POST /customers` (ADMIN)
- `PUT /customers/{id}` (ADMIN)
- `DELETE /customers/{id}` (ADMIN)
- `GET /customers/{id}`
- `GET /customers`
- `GET /customers?status=ACTIVE`
- `GET /customers/search?name=joao` (Native Query)
- `GET /customers/{id}/score`

O projeto também contém `CustomerJdbcRepository`, com consulta via `JdbcTemplate`, satisfazendo o requisito explícito do desafio.

## Exemplo
```bash
curl -u admin:admin123 -H 'Content-Type: application/json' -d '{"name":"Joao da Silva","cpf":"12345678901","email":"joao@email.com","status":"ACTIVE"}' http://localhost:8080/customers
curl -u user:user123 http://localhost:8080/customers
```

## Score externo
Por padrão a aplicação consulta `http://localhost:9090/scores/{cpf}`. Configure por `SCORE_SERVICE_URL`. Timeouts: `SCORE_CONNECT_TIMEOUT` e `SCORE_READ_TIMEOUT`.

Para simular com WireMock CLI/standalone, a pasta `wiremock/mappings` contém um stub. Exemplo, usando uma distribuição WireMock standalone disponível localmente:
```bash
java -jar wiremock-standalone.jar --port 9090 --root-dir wiremock
```
Depois: `GET /customers/{id}/score`. Falhas HTTP, timeout, indisponibilidade e resposta incompleta são convertidos em `502 Bad Gateway`.

## Respostas HTTP principais
201 criação; 204 exclusão; 400 validação; 401 não autenticado; 403 sem permissão; 404 cliente inexistente; 409 CPF duplicado; 502 erro no serviço de score.

## Decisões
Arquitetura em camadas (controller/service/repository/integration), DTOs para não expor entidade JPA, transações no service, CPF único também no banco, Native Query para busca por nome, JdbcTemplate isolado em repository, configuração do score externalizada e timeouts explícitos.
