# Pluvial - Monitoramento de enchentes

Global Solution 2026/2 - Domain Driven Design Using Java (FIAP)

## Integrantes
RM 572560 - Gustavo Santos Viana
RM 573616 - Francisco Halison
RM 573299 - Pedro Vinicius 

## Sobre
API RESTful em Java com Spring Boot que apoia o monitoramento de enchentes: regioes monitoradas, sensores (pluviometros e sensores de nivel de rio) e as leituras enviadas por eles. O nivel de risco de cada regiao e calculado a partir das leituras.

Entidades (relacionamentos 1:N): `Regiao` 1---N `Sensor` 1---N `Leitura`.

Regras de negocio:
- Sensor INATIVO nao aceita novas leituras (HTTP 409).
- Cada leitura registrada recalcula o nivel de risco da regiao na mesma transacao: abaixo de 50% do limite do sensor = NORMAL; a partir de 50% = ATENCAO; a partir de 80% = ALERTA; a partir de 100% = EMERGENCIA.
- Nome de regiao unico por cidade; nao se remove regiao com sensores nem sensor com leituras (HTTP 409).

## Tecnologias
Java 17, Spring Boot 3, JDBC (DAO), Oracle, ModelMapper, Bean Validation, Docker, Render.

## Estrutura de pacotes
`model` (entidades), `dao` (JDBC), `dto` (entrada e saida), `controller` (rotas REST), `exception` (erros do dominio), `handler` (JSON de erro padronizado) e `config` (CORS e ModelMapper).

## Endpoints
| Metodo | Rota | Sucesso | Erros |
|---|---|---|---|
| POST | /api/regioes | 201 + Location | 400, 409 |
| GET | /api/regioes | 200 | - |
| GET | /api/regioes/{id} | 200 | 404 |
| PUT | /api/regioes/{id} | 200 | 400, 404, 409 |
| DELETE | /api/regioes/{id} | 204 | 404, 409 |
| GET | /api/regioes/{id}/sensores | 200 | 404 |
| POST | /api/sensores | 201 + Location | 400, 409 |
| GET | /api/sensores | 200 | - |
| GET | /api/sensores/{id} | 200 | 404 |
| PUT | /api/sensores/{id} | 200 | 400, 404, 409 |
| DELETE | /api/sensores/{id} | 204 | 404, 409 |
| GET | /api/sensores/{id}/leituras | 200 | 404 |
| POST | /api/sensores/{id}/leituras | 201 + Location | 400, 404, 409 |
| GET | /api/leituras | 200 | - |
| GET | /api/leituras/{id} | 200 | 404 |
| PUT | /api/leituras/{id} | 200 | 400, 404 |
| DELETE | /api/leituras/{id} | 204 | 404 |

## Como rodar
1. Rodar o script `sql/script_pluvial.sql` no Oracle da FIAP.
2. Definir as variaveis de ambiente `DB_USER` e `DB_PASSWORD` (e `DB_URL`, se for outra URL).
3. Executar a classe `PluvialApplication` ou usar Docker:

bash
docker build -t pluvial-api .
docker run -p 8080:8080 -e DB_USER=seu_usuario -e DB_PASSWORD=sua_senha pluvial-api


## API no Render
https://pluvial-api.onrender.com
https://pluvial-api.onrender.com/api/status
https://pluvial-api.onrender.com/api/regioes

## Uso de IA generativa
Foi utilizada IA generativa Claude para a implementação do projeto no render


