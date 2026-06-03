# Visão Geral do Produto

Monorepo **CNietsche — User CRUD** (`simple-springboot-crud`): API Spring (`backend/`) + SPA React (`frontend/`).

**O que faz:** cadastro e login de usuários, área logada simples e alteração do tipo (`ADMIN` / `USER`). Persistência relacional (H2 por padrão; PostgreSQL opcional).

**Fora do escopo atual:** listagem, exclusão, edição completa de perfil, tokens/refresh, OAuth.

**Quem usa:** usuário final (telas em PT); desenvolvedor (Docker Compose ou dev com Maven + Vite). Em Docker, só o frontend expõe porta no host (`8080`).

**Detalhes por camada:** `backend/context.md`, `frontend/context.md`.

---

# Arquitetura Geral

```
Browser → [Nginx + SPA] → proxy /api → [Spring Boot] → H2 | PostgreSQL
```

| Aspecto | Resumo |
|---------|--------|
| Protocolo | HTTP, JSON (`application/json`) |
| API | Prefixo `/api`; paths relativos no frontend |
| Proxy dev | Vite: `/api` → `localhost:8080` |
| Proxy prod | Nginx → `backend:8080` |
| Backend | Monólito hexagonal (Java 21, Spring Boot 3.4) |
| Frontend | SPA React 18 + Vite + TypeScript |

**Autenticação no sistema**

- **Backend:** valida login e devolve `{ id, name, email, type }`. Sem JWT, sessão server-side ou `Authorization` nas demais rotas.
- **Frontend:** guarda usuário em `sessionStorage` + Context; `ProtectedRoute` só controla navegação.
- **Atenção:** rotas protegidas na UI **não** significam API protegida.

**Fluxo de dados (resumido):** UI → `apiFetch` → proxy → controller → caso de uso → JPA → banco → JSON (ou `{ "message" }` em erro).

---

# Módulos do Sistema

| Módulo | Papel |
|--------|--------|
| `backend/` | REST, regras de negócio, persistência (`User`) |
| `frontend/` | UI, rotas, cliente HTTP, sessão client-side |
| `docker-compose.yml` | Sobe FE + BE; volume H2 no backend |

Domínios de produto: **usuário** e **overload** (teste de resiliência). Infra: Dockerfiles + `nginx.conf` no frontend.

---

# Fluxos de Negócio

| Fluxo | UI | API | Notas |
|-------|-----|-----|--------|
| Cadastro | Aba em `/login` | `POST /api/users` | Sucesso → login manual (sem auto-login) |
| Login | `/login` | `POST /api/auth/login` | `identifier` = e-mail ou username |
| Área logada | `/` | — | Dados do Context; home não chama API |
| Alterar tipo | Modal Configurações | `PATCH /api/users/{id}/type` | Atualiza banco e Context |
| Gerar overload | Home (USER) | `POST /api/overloads/generate` | `userId` + `count` (50/100/400/1000) |
| Listar overload | Home (USER) | `GET /api/overloads` | Paginado por `userId`, 50/página, `date` DESC |
| Estatísticas overload | Home (ADMIN) | `GET /api/overloads/statistics` | `period` + `userId` opcional; refresh 30s na UI |
| Métricas login | Métricas (ADMIN) | `GET /api/metrics/login-attempts` | `period`; buckets `success`/`fail`; refresh 30s |
| Listar usuários | Dashboard ADMIN | `GET /api/users` | `id` + `name`, ordem alfabética |
| Consultar por ID | **Não usado** | `GET /api/users/{id}` | Cliente `getUser` existe; sem tela |

Regras transversais: tipos `ADMIN`/`USER`; senha nunca volta no JSON; duplicidade e-mail/username → 409; senha mín. 6 (BE + HTML5 no FE).

---

# Contratos Frontend ↔ Backend

### Endpoints em uso

| Método | Path | Body (request) | Sucesso |
|--------|------|----------------|---------|
| POST | `/api/auth/login` | `identifier`, `password` | `{ id, name, email, type }` |
| POST | `/api/users` | `name`, `email`, `username`, `password`, `type?` | `201` + mesmo shape com `id` |
| PATCH | `/api/users/{id}/type` | `{ type }` | `{ name, email, type }` |
| GET | `/api/users` | — | `[{ id, name }]` |
| GET | `/api/users/{id}` | — | `{ name, email, type }` (sem `id`; UI não consome) |
| POST | `/api/overloads/generate` | `{ userId, count }` | `{ created }` |
| GET | `/api/overloads` | query: `userId`, `page`, `size` | página com `content[]` (`id`, `date`, `userId`, `value`) |
| GET | `/api/overloads/statistics` | query: `period`, `userId?` | `{ topUsers[], timeSeries[] }` |

**Erros:** sempre `{ "message": "..." }` (mensagens em inglês no backend; UI pode usar fallback em PT).

**Autenticação HTTP:** só no POST de login. Demais chamadas **sem** token/cookie.

**Paginação:** `GET /api/overloads` (page/size). **Filtros por query:** estatísticas de overload (`period`, `userId`). Sem upload/download nem versionamento `/api/v1`.

### Divergências FE ↔ BE

| Tema | Detalhe |
|------|---------|
| Segurança | UI “logada” vs API aberta após login |
| `id` na resposta | Presente em login/create; ausente no GET de perfil |
| Idioma | UI PT; erros da API EN |
| `getUser` | Implementado no cliente, sem uso na interface |

---

# Regras de Negócio Compartilhadas

- `UserType`: `ADMIN` | `USER` (mesmos valores nos dois lados).
- Login por e-mail (case-insensitive no BE) ou username.
- Cadastro não autentica automaticamente no frontend.
- Alteração de tipo: PATCH no BE + atualização local no Context.
- Sem autorização por role (nem API nem rotas React).

Regras só no backend (normalização, BCrypt, UUID, default `USER`): ver `backend/context.md`.  
Regras só no frontend (`sessionStorage`, redirects): ver `frontend/context.md`.

---

# Convenções Globais

- Monorepo; cada pasta tem seu `context.md` com convenções e anti-padrões locais.
- API sob `/api`; integração FE via `apiFetch` (não `fetch` solto em componentes).
- Versão `1.0.0` em ambos os artefatos; sem `/api/v1`.
- Testes: unitários nos services (BE) e poucos testes de componente (FE); sem E2E FE↔BE identificado. Docker roda testes no build das imagens.

---

# Dependências Críticas

Spring Boot + JPA + H2/PostgreSQL + BCrypt · React + Router + Vite · Nginx (proxy `/api` em prod) · Docker Compose (deploy integrado opcional).

---

# Guia para Novas Funcionalidades

1. **Contrato primeiro** — método, path, JSON, status; alinhar tipos Java e TS; manter `{ "message" }` em erros.
2. **Backend** — UseCase → Service → Port; DTO + validação; handler de exceção. Detalhes: `backend/context.md`.
3. **Frontend** — `api/*` + tipos + página/rota + `ApiError`. Detalhes: `frontend/context.md`.
4. **Testes** — service test (BE); component test se tocar auth/rotas (FE).
5. **Segurança** — se a feature precisar de API autenticada, planejar mudança **coordenada** (token, header, filtro BE, `apiFetch`); o padrão atual não oferece isso.

Atualizar `backend/context.md` / `frontend/context.md` (e este arquivo) se o contrato ou a arquitetura mudar.

---

# Checklist para IA

- [ ] Li este arquivo e o `context.md` da(s) pasta(s) alterada(s).
- [ ] Confirmei contrato HTTP e impacto em auth (hoje sem token).
- [ ] Não tratei `ProtectedRoute` como proteção de API.
- [ ] Reutilizei padrões existentes (`apiFetch`, UseCase, erros `ApiError` / `DomainException`).
- [ ] Adicionei testes no estilo do projeto; builds Docker continuam passando se aplicável.
- [ ] Documentei divergências novas entre FE e BE.

---

# Resumo Executivo

1. Monorepo: API Spring hexagonal + SPA React, integrados por **JSON em `/api/*`**.
2. Fluxos de produto: **cadastro**, **login**, **home**, **alterar tipo**.
3. Auth **só na UX** (`sessionStorage`); backend **stateless e aberto** após login.
4. Erros: `{ "message" }`; UI em PT, mensagens da API em EN.
5. `GET /api/users/{id}` no BE; **não usado** na UI.
6. Paginação em overload; sem OAuth, filas ou proteção por `ADMIN` na API.
7. Detalhes de implementação: **`backend/context.md`** e **`frontend/context.md`**.

---

*Visão de sistema — junho de 2026. Complementa, não substitui, os contextos parciais.*
