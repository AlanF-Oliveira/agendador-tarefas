# 📋 Agendador de Tarefas

API REST para gerenciamento e agendamento de tarefas pessoais. Permite criar, consultar, atualizar e deletar tarefas, com autenticação via **JWT** e integração com o microsserviço de usuários via **OpenFeign**.

> Este microsserviço faz parte de um ecossistema maior. O ponto de entrada recomendado para o frontend é o **[BFF Agendador de Tarefas](https://github.com/AlanF-Oliveira/bff-agendador-tarefas)**.

---

## 🚀 Tecnologias

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
| Lombok | — | Redução de boilerplate |
| Gradle | — | Build |
| Docker | — | Containerização |

---

## 📁 Estrutura do Projeto

```
src/main/java/com/alan/agendadortarefas/
├── AgendadorTarefasApplication.java
├── controller/
│   └── TarefasController.java
├── business/
│   ├── TarefasService.java
│   ├── dto/
│   │   ├── TarefasDTO.java
│   │   └── UsuarioDTO.java
│   └── mapper/
│       ├── TarefasConverter.java
│       └── TarefaUpdateConverter.java
└── infrastructure/
    ├── client/
    │   └── UsuarioClient.java
    ├── entity/
    │   └── TarefasEntity.java
    ├── enums/
    │   └── StatusNotificacaoEnum.java
    ├── exceptions/
    │   └── ResourceNotFoundException.java
    ├── repository/
    │   └── TarefasRepository.java
    └── security/
        ├── JwtUtil.java
        ├── JwtRequestFilter.java
        ├── SecurityConfig.java
        └── UserDetailsServiceImpl.java
```

---

## 🐳 Executando com Docker (recomendado)

> Para subir todo o ecossistema de uma vez (BFF + todos os microsserviços + bancos), use o `docker-compose` do repositório **[bff-agendador-tarefas](https://github.com/AlanF-Oliveira/bff-agendador-tarefas)**.

Para rodar apenas este serviço isoladamente:

```bash
git clone https://github.com/AlanF-Oliveira/agendador-tarefas.git
cd agendador-tarefas
docker build -t agendador-tarefas .
docker run -p 8081:8081 \
  -e SPRING_DATA_MONGODB_URI=mongodb://host.docker.internal:27017/db_agendador \
  -e USUARIO_URL=http://host.docker.internal:8080 \
  agendador-tarefas
```

### Serviço e porta

| Serviço | Porta |
|---|---|
| `agendador-tarefas` | `8081` |

---

## 🔧 Dockerfile

Build multi-stage com Gradle:

```dockerfile
# Stage 1 — build
FROM gradle:8.14-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

# Stage 2 — runtime
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/agendador_tarefas.jar
EXPOSE 8083
CMD ["java", "-jar", "/app/agendador_tarefas.jar"]
```

---

## ▶️ Executando sem Docker

### Pré-requisitos

- Java 17+
- MongoDB rodando localmente
- Microsserviço **[usuario](https://github.com/AlanF-Oliveira/usuario)** rodando (necessário para validação do JWT)

### Configuração

Edite o `src/main/resources/application.properties`:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/db_agendador
usuario.url=http://localhost:8080
```

### Executando

```bash
./gradlew bootRun
```

---

## 🔐 Autenticação

Todas as requisições exigem um token JWT gerado pelo microsserviço **[usuario](https://github.com/AlanF-Oliveira/usuario)**:

```
Authorization: Bearer <token>
```

O e-mail do usuário é extraído diretamente do token para associar as tarefas ao dono correto.

---

## 🌐 Endpoints

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
// Request — POST /tarefas
// Header: Authorization: Bearer <token>
{
  "nomeTarefa": "Reunião de equipe",
  "descricao": "Discutir metas do trimestre",
  "dataEvento": "25-03-2026 14:00:00"
}

// Response
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

## 📦 Modelo de Dados

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

## 🔗 Integração com o microsserviço Usuario

A cada requisição autenticada, este serviço consulta o **[usuario](https://github.com/AlanF-Oliveira/usuario)** via OpenFeign para validar o token e recuperar os dados do usuário:

```java
@FeignClient(name = "usuario", url = "${usuario.url}")
public interface UsuarioClient {
    @GetMapping("/usuario")
    UsuarioDTO buscaUsuarioPorEmail(@RequestParam("email") String email,
                                    @RequestHeader("Authorization") String token);
}
```

---

## 🧩 Microsserviços Relacionados

| Serviço | Repositório | Papel |
|---|---|---|
| **BFF** | [bff-agendador-tarefas](https://github.com/AlanF-Oliveira/bff-agendador-tarefas) | Ponto de entrada — orquestra todas as chamadas |
| **usuario** | [usuario](https://github.com/AlanF-Oliveira/usuario) | Fornece autenticação JWT |
| **notificacao** | [notificacao](https://github.com/AlanF-Oliveira/notificacao) | Envia notificações sobre tarefas agendadas |

---

## 📖 Documentação da API (Swagger)

Com a aplicação rodando, acesse:

```
http://localhost:8081/swagger-ui.html
```

---

## 👤 Autor

**Alan F. Oliveira** — [github.com/AlanF-Oliveira](https://github.com/AlanF-Oliveira)                                    