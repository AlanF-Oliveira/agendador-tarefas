# 📋 Agendador de Tarefas

API REST para gerenciamento e agendamento de tarefas pessoais, desenvolvida com **Java 17** e **Spring Boot 4**. O serviço permite criar, consultar, atualizar e deletar tarefas, com autenticação via **JWT** e integração com o microsserviço [usuario](https://github.com/AlanF-Oliveira/usuario) via **OpenFeign**.

---

## 🚀 Tecnologias Utilizadas

| Tecnologia | Descrição |
|---|---|
| Java 17 | Linguagem principal |
| Spring Boot 4.0.3 | Framework principal |
| Spring Data MongoDB | Persistência de dados |
| Spring Security | Autenticação e autorização |
| Spring Cloud OpenFeign | Comunicação com microsserviços |
| JWT (jjwt 0.13.0) | Geração e validação de tokens |
| MapStruct 1.5.3 | Mapeamento entre DTOs e Entidades |
| Lombok | Redução de boilerplate |
| Gradle | Gerenciamento de build |

---

## 📁 Estrutura do Projeto

```
src/main/java/com/alan/agendadortarefas/
├── AgendadorTarefasApplication.java       # Classe principal
├── controller/
│   └── TarefasController.java             # Endpoints REST
├── business/
│   ├── TarefasService.java                # Regras de negócio
│   ├── dto/
│   │   ├── TarefasDTO.java                # DTO de tarefas
│   │   └── UsuarioDTO.java                # DTO de usuários
│   └── mapper/
│       ├── TarefasConverter.java          # Mapper DTO <-> Entity
│       └── TarefaUpdateConverter.java     # Mapper para atualização parcial
└── infrastructure/
    ├── client/
    │   └── UsuarioClient.java             # Feign Client para serviço de usuários
    ├── entity/
    │   └── TarefasEntity.java             # Entidade MongoDB
    ├── enums/
    │   └── StatusNotificacaoEnum.java     # Status da notificação
    ├── exceptions/
    │   └── ResourceNotFoundException.java # Exceção customizada
    ├── repository/
    │   └── TarefasRepository.java         # Repositório MongoDB
    └── security/
        ├── JwtUtil.java                   # Utilitários JWT
        ├── JwtRequestFilter.java          # Filtro de autenticação JWT
        ├── SecurityConfig.java            # Configuração do Spring Security
        └── UserDetailsServiceImpl.java    # Carregamento de detalhes do usuário
```

---

## 🔐 Autenticação

A API utiliza autenticação **stateless** baseada em **JWT**. Todas as requisições devem incluir o token no header:

```
Authorization: Bearer <seu_token_jwt>
```

> 💡 O token JWT é gerado pelo microsserviço **[usuario](https://github.com/AlanF-Oliveira/usuario)** no endpoint `POST /usuario/login`. Obtenha o token lá antes de consumir esta API.

O token é validado pelo `JwtRequestFilter` antes de cada requisição. O e-mail do usuário é extraído diretamente do token para associar as tarefas ao usuário correto.

---

## 🌐 Endpoints

Base URL: `/tarefas`

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/tarefas` | Cria uma nova tarefa |
| `GET` | `/tarefas` | Lista todas as tarefas do usuário autenticado |
| `GET` | `/tarefas/eventos` | Busca tarefas agendadas em um período |
| `PUT` | `/tarefas` | Atualiza uma tarefa existente |
| `PATCH` | `/tarefas` | Altera o status de notificação de uma tarefa |
| `DELETE` | `/tarefas` | Deleta uma tarefa por ID |

### Detalhamento dos Endpoints

#### `POST /tarefas`
Cria uma nova tarefa para o usuário autenticado.

**Header:** `Authorization: Bearer <token>`

**Body:**
```json
{
  "nomeTarefa": "Reunião de equipe",
  "descricao": "Discutir metas do trimestre",
  "dataEvento": "25-03-2026 14:00:00"
}
```

**Resposta:**
```json
{
  "id": "abc123",
  "nomeTarefa": "Reunião de equipe",
  "descricao": "Discutir metas do trimestre",
  "dataCriacao": "07-03-2026 10:00:00",
  "dataEvento": "25-03-2026 14:00:00",
  "emailUsuario": "usuario@email.com",
  "dataAlteracao": null,
  "statusNotificacaoEnum": "PENDENTE"
}
```

#### `GET /tarefas`
Lista todas as tarefas do usuário autenticado.

**Header:** `Authorization: Bearer <token>`

#### `GET /tarefas/eventos`
Busca tarefas agendadas dentro de um intervalo de datas.

**Query Params:**
- `dataInicial` (ISO DateTime): ex. `2026-03-01T00:00:00`
- `dataFinal` (ISO DateTime): ex. `2026-03-31T23:59:59`

#### `PUT /tarefas?id={id}`
Atualiza os dados de uma tarefa existente.

#### `PATCH /tarefas?id={id}&status={status}`
Altera o status de notificação de uma tarefa.

**Status disponíveis:** `PENDENTE`, `NOTIFICADO`, `CANCELADO`

#### `DELETE /tarefas?id={id}`
Remove uma tarefa pelo seu ID.

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
| `emailUsuario` | String | E-mail do dono da tarefa |
| `dataAlteracao` | LocalDateTime | Data da última alteração |
| `statusNotificacaoEnum` | Enum | Status: `PENDENTE`, `NOTIFICADO`, `CANCELADO` |

---

## 🔗 Integração com o Microsserviço de Usuários

Este serviço depende do microsserviço **[usuario](https://github.com/AlanF-Oliveira/usuario)** para funcionar. A integração acontece em dois momentos:

### 1. Obtenção do Token JWT

O token deve ser gerado pelo serviço `usuario` antes de qualquer chamada a esta API:

```
POST http://localhost:8081/usuario/login
Content-Type: application/json

{
  "email": "alan@email.com",
  "senha": "minhasenha"
}
```

Resposta: `Bearer eyJhbGciOiJIUzI1NiJ9...`

### 2. Validação do Usuário por Token

A cada requisição autenticada, o `agendador-tarefas` consulta o serviço `usuario` para validar o token e buscar os dados do usuário via **OpenFeign**:

```java
@FeignClient(name = "usuario", url = "${usuario.url}")
public interface UsuarioClient {
    @GetMapping("/usuario")
    UsuarioDTO buscaUsuarioPorEmail(@RequestParam("email") String email,
                                    @RequestHeader("Authorization") String token);
}
```

> 📌 O endpoint consumido é `GET /usuario?email={email}` do serviço [usuario](https://github.com/AlanF-Oliveira/usuario).

Configure a URL do serviço de usuários no `application.properties`:
```properties
usuario.url=http://localhost:8081
```

---

## ⚙️ Como Executar

### Pré-requisitos

- Java 17+
- MongoDB rodando localmente ou em um container
- Microsserviço **[usuario](https://github.com/AlanF-Oliveira/usuario)** rodando (necessário para autenticação)

### Configuração

Crie/edite o arquivo `src/main/resources/application.properties`:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/agendador-tarefas
usuario.url=http://localhost:8081
```

### Rodando com Gradle

```bash
# Clonar o repositório
git clone https://github.com/AlanF-Oliveira/agendador-tarefas.git
cd agendador-tarefas

# Executar
./gradlew bootRun
```

### Build

```bash
./gradlew build
```

### Testes

```bash
./gradlew test
```

---

## 🧩 Microsserviços Relacionados

| Serviço | Repositório | Responsabilidade |
|---|---|---|
| **usuario** | [AlanF-Oliveira/usuario](https://github.com/AlanF-Oliveira/usuario) | Cadastro, login e gestão de usuários |
| **agendador-tarefas** | [AlanF-Oliveira/agendador-tarefas](https://github.com/AlanF-Oliveira/agendador-tarefas) | Agendamento e gestão de tarefas |

---

## 👤 Autor

Desenvolvido por **[AlanF-Oliveira](https://github.com/AlanF-Oliveira)**
