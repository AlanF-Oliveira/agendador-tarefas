# Agendador de Tarefas

API REST para gerenciamento e agendamento de tarefas pessoais. Permite criar, consultar, atualizar e deletar tarefas, com autenticação via **JWT** e integração com o microsserviço de usuários via **OpenFeign**.

> Este microsserviço faz parte de um ecossistema maior. O ponto de entrada recomendado para o frontend é o **[BFF Agendador de Tarefas](https://github.com/AlanF-Oliveira/bff-agendador-tarefas)**.

---

## Tecnologias

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | Linguagem principal |
| Spring Boot | 4.0.3 | Framework base |
| Spring Data MongoDB | — | Persistência de dados |
| MongoDB | — | Banco de dados NoSQL |
| Spring Security | — | Autenticação e autorização |
| Spring Cloud OpenFeign | 2025.1.0 | Comunicação com o microsserviço usuario |
| JWT (jjwt) | 0.13.0 | Validação de tokens |
| MapStruct | 1.5.3 | Mapeamento DTO ↔ Entity |
| Springdoc OpenAPI | 3.0.2 | Documentação Swagger |
| Lombok | — | Redução de boilerplate |
| Gradle | — | Build |
| Docker | — | Containerização |

---

## Estrutura do Projeto

```
agendador-tarefas/
├── .github/
│   └── workflows/
│       └── gradle.yml
├── src/
│   └── main/
│       ├── java/com/alan/agendadortarefas/
│       │   ├── AgendadorTarefasApplication.java
│       │   ├── controller/
│       │   │   └── TarefasController.java
│       │   ├── business/
│       │   │   ├── TarefasService.java
│       │   │   ├── dto/
│       │   │   │   ├── TarefasDTORecord.java
│       │   │   │   └── UsuarioDTO.java
│       │   │   └── mapper/
│       │   │       ├── TarefasConverter.java
│       │   │       └── TarefaUpdateConverter.java
│       │   └── infrastructure/
│       │       ├── client/
│       │       │   └── UsuarioClient.java
│       │       ├── entity/
│       │       │   └── TarefasEntity.java
│       │       ├── enums/
│       │       │   └── StatusNotificacaoEnum.java
│       │       ├── exceptions/
│       │       │   └── ResourceNotFoundException.java
│       │       ├── repository/
│       │       │   └── TarefasRepository.java
│       │       └── security/
│       │           ├── JwtUtil.java
│       │           ├── JwtRequestFilter.java
│       │           ├── SecurityConfig.java
│       │           └── UserDetailsServiceImpl.java
│       └── resources/
│           └── application.properties
├── Dockerfile
└── build.gradle
```

---

## Endpoints

Base URL: `/tarefas`

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/tarefas` | Cria uma nova tarefa |
| `GET` | `/tarefas` | Lista todas as tarefas do usuário autenticado |
| `GET` | `/tarefas/eventos` | Busca tarefas em um intervalo de datas |
| `PUT` | `/tarefas?id={id}` | Atualiza uma tarefa |
| `PATCH` | `/tarefas?id={id}&status={status}` | Altera o status de notificação |
| `DELETE` | `/tarefas?id={id}` | Remove uma tarefa |

**Status de notificação disponíveis:** `PENDENTE` · `NOTIFICADO` · `CANCELADO`

### Exemplo — Criar tarefa

```json
// POST /tarefas
// Authorization: Bearer <token>
{
  "nomeTarefa": "Reunião de equipe",
  "descricao": "Discutir metas do trimestre",
  "dataEvento": "25-03-2026 14:00:00"
}

// Response 200
{
  "id": "abc123",
  "nomeTarefa": "Reunião de equipe",
  "descricao": "Discutir metas do trimestre",
  "dataCriacao": "07-03-2026 10:00:00",
  "dataEvento": "25-03-2026 14:00:00",
  "emailUsuario": "alan@email.com",
  "statusNotificacaoEnum": "PENDENTE"
}
```

---

## Modelo de Dados

### TarefasEntity (coleção: `tarefa`)

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | String | Identificador único (MongoDB) |
| `nomeTarefa` | String | Nome da tarefa |
| `descricao` | String | Descrição detalhada |
| `dataCriacao` | LocalDateTime | Data de criação automática |
| `dataEvento` | LocalDateTime | Data/hora do evento agendado |
| `emailUsuario` | String | E-mail do dono da tarefa (extraído do JWT) |
| `dataAlteracao` | LocalDateTime | Data da última alteração |
| `statusNotificacaoEnum` | Enum | `PENDENTE`, `NOTIFICADO`, `CANCELADO` |

---

## Autenticação

Todas as requisições (exceto `/swagger-ui/**` e `/v3/api-docs/**`) exigem um token JWT gerado pelo microsserviço **[usuario](https://github.com/AlanF-Oliveira/usuario)**:

```
Authorization: Bearer <token>
```

O e-mail do usuário é extraído diretamente do token para associar as tarefas ao dono correto. A cada requisição, o filtro `JwtRequestFilter` valida o token e consulta o microsserviço `usuario` via OpenFeign para carregar os dados do usuário autenticado.

---

## Integração com o microsserviço Usuario

```java
@FeignClient(name = "usuario", url = "${usuario.url}")
public interface UsuarioClient {
    @GetMapping("/usuario")
    UsuarioDTO buscaUsuarioPorEmail(@RequestParam("email") String email,
                                    @RequestHeader("Authorization") String token);
}
```

---

## Executando com Docker (recomendado)

> Para subir todo o ecossistema de uma vez (BFF + todos os microsserviços + bancos), use o `docker-compose` do repositório **[bff-agendador-tarefas](https://github.com/AlanF-Oliveira/bff-agendador-tarefas)**.

Para rodar apenas este serviço isoladamente:

```bash
git clone https://github.com/AlanF-Oliveira/agendador-tarefas.git
cd agendador-tarefas
docker build -t agendador-tarefas .
docker run -p 8081:8081 \
  -e SPRING_MONGODB_URI=mongodb://host.docker.internal:27017/db_agendador \
  -e USUARIO_URL=http://host.docker.internal:8080 \
  agendador-tarefas
```

| Serviço | Porta |
|---|---|
| `agendador-tarefas` | `8081` |

---

## Executando sem Docker

### Pré-requisitos

- Java 17+
- MongoDB rodando localmente
- Microsserviço **[usuario](https://github.com/AlanF-Oliveira/usuario)** rodando (necessário para validação do JWT)

### Configuração

Edite o `src/main/resources/application.properties`:

```properties
spring.mongodb.uri=mongodb://localhost:27017/db_agendador
usuario.url=localhost:8080
server.port=8081
```

### Executando

```bash
git clone https://github.com/AlanF-Oliveira/agendador-tarefas.git
cd agendador-tarefas
./gradlew bootRun
```

---

## Documentação da API (Swagger)

Com a aplicação rodando, acesse:

```
http://localhost:8081/swagger-ui.html
```

---

## CI/CD

O projeto utiliza **GitHub Actions** para integração contínua. O pipeline é acionado automaticamente em:

- Pull Requests abertos, sincronizados ou reabertos para a branch `master`

**Etapas do pipeline:**

1. Checkout do código
2. Configuração do JDK 17 (Temurin)
3. Cache das dependências Gradle
4. Permissão de execução para o `gradlew`
5. Build com Gradle (`./gradlew build`)
6. Execução dos testes (`./gradlew test`)

O arquivo de configuração está em `.github/workflows/gradle.yml`.

---

## Microsserviços Relacionados

| Serviço | Repositório | Papel |
|---|---|---|
| **BFF** | [bff-agendador-tarefas](https://github.com/AlanF-Oliveira/bff-agendador-tarefas) | Ponto de entrada — orquestra todas as chamadas |
| **usuario** | [usuario](https://github.com/AlanF-Oliveira/usuario) | Fornece autenticação JWT |
| **notificacao** | [notificacao](https://github.com/AlanF-Oliveira/notificacao) | Envia notificações sobre tarefas agendadas |