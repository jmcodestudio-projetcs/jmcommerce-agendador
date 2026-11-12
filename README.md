# JM Commerce Agendador

Sistema de agendamento online para fotógrafos e filmadores. O fotógrafo cria seus serviços e abre a agenda; o cliente entra pelo link do fotógrafo, escolhe dia e horário e envia o pedido, sem precisar de conta.

## Estrutura

```
jmcommerce-agendador/
├── backend/             API em Java 21 + Spring Boot (Maven)
│   └── src/main/
│       ├── java/br/com/jmcodestudio/agendador/
│       │   ├── AgendadorApplication.java
│       │   └── domain/              entidades JPA e enums
│       └── resources/
│           ├── application.yml
│           └── db/migration/        scripts Flyway (V1__, V2__, ...)
├── frontend/            React + Vite
├── docs/
│   └── contratos-api.md             contratos de API (fonte para o desenvolvimento)
└── docker-compose.yml   PostgreSQL de desenvolvimento
```

## Pré-requisitos

- Java 21
- Maven 3.9+
- Node 20+
- Docker (para o banco de desenvolvimento)

## Como rodar

1. Banco de dados:

   ```bash
   docker compose up -d
   ```

2. Backend, em `http://localhost:8080`:

   ```bash
   cd backend
   mvn spring-boot:run
   ```

   Na primeira subida o Flyway cria as tabelas.

3. Frontend, em `http://localhost:5173`:

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

   Em desenvolvimento, as chamadas para `/api` são encaminhadas ao backend.

## Configuração

O backend lê o banco por variáveis de ambiente; os valores padrão são os do `docker-compose.yml`.

| Variável | Padrão |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/agendador` |
| `DB_USER` | `agendador` |
| `DB_PASSWORD` | `agendador` |

## Regras do projeto

- O schema pertence ao Flyway: toda mudança de banco é um novo script em `db/migration`, e o Hibernate apenas valida (`ddl-auto: validate`).
- O conflito de horário é resolvido pelo banco, com o índice único `ux_agendamento_horario_ativo`.
- Simplicidade primeiro: se uma solução ficar complexa, trocar por outra mais simples.
