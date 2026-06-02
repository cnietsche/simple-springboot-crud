# CNietsche — User CRUD

Monorepo com **backend** (Spring Boot 21, arquitetura hexagonal) e **frontend** (React + TypeScript).

## Estrutura

```
├── backend/     # API REST (H2 por padrão, perfil postgres preparado)
├── frontend/    # SPA React com proxy Nginx → backend
└── docker-compose.yml
```

## Docker

Build (executa testes em cada imagem):

```bash
docker compose build
```

Subir:

```bash
docker compose up -d
```

Acesse: **http://localhost:8080**

- Apenas o **frontend** é exposto no host (`8080`).
- O **backend** fica acessível só na rede interna do Compose; o Nginx encaminha `/api` para ele.

## API (via frontend)

| Método | Path | Descrição |
|--------|------|-----------|
| POST | `/api/users` | Criar usuário |
| GET | `/api/users/{id}` | Ver usuário (name, email, type) |
| POST | `/api/auth/login` | Login (e-mail ou username + senha) |
| PATCH | `/api/users/{id}/type` | Alterar tipo (ADMIN / USER) |

## Banco de dados

**Padrão:** perfil `h2` — arquivo em volume Docker `backend-data`.

**Futuro (Supabase / PostgreSQL):** copie `.env.example` para `.env`, defina `SPRING_PROFILES_ACTIVE=postgres` e as variáveis `DATABASE_*`. Veja `backend/src/main/resources/application-postgres.yml`.

## Desenvolvimento local

**Backend** (requer Maven e Java 21):

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

**Frontend** (requer Node 20+):

```bash
cd frontend
npm install
npm run dev
```

O Vite faz proxy de `/api` para `http://localhost:8080`.
