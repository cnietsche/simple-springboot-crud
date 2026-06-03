# Projeto

SPA (Single Page Application) React que serve como interface do monorepo `simple-springboot-crud`. Nome do pacote npm: `cnietsche-frontend` (`package.json`).

**Propósito observado:** autenticação local (login), cadastro de usuário, área autenticada com menu e modal de configurações para alterar o tipo do usuário (`ADMIN` / `USER`). Comunicação com o backend exclusivamente via REST em paths relativos `/api/*`.

**Rotas implementadas** (`App.tsx`):

| Rota | Acesso | Conteúdo |
|------|--------|----------|
| `/login` | Público | Login e cadastro (abas) |
| `/` | Protegido | Home: USER gera/lista overload; ADMIN vê dashboard com gráficos |
| `*` | — | Redireciona para `/` |

**Deploy:** build estático servido por Nginx; em Docker, proxy de `/api/` para o serviço `backend:8080`. Em desenvolvimento, Vite faz proxy de `/api` para `http://localhost:8080`.

**Idioma da UI:** português (labels, mensagens de sucesso/erro fallback, `lang="pt-BR"` em `index.html`).

---

# Stack Tecnológica

| Tecnologia | Versão / detalhe | Evidência |
|------------|------------------|-----------|
| React | ^18.3.1 | `package.json` |
| React DOM | ^18.3.1 | `main.tsx` |
| React Router | ^6.28.0 | `BrowserRouter`, `Routes`, `Navigate` |
| TypeScript | ~5.6.3 | `tsconfig.json`, arquivos `.ts`/`.tsx` |
| Vite | ^5.4.11 | `vite.config.ts`, scripts `dev`/`build` |
| Vitest | ^2.1.5 | `vitest.config.ts`, script `test` |
| Testing Library | React ^16, jest-dom ^6.6 | testes em `src/test/` |
| jsdom | ^25.0.1 | ambiente de teste Vitest |
| Nginx | Alpine (imagem Docker) | `Dockerfile`, `nginx.conf` |
| Node | 22 Alpine (build Docker) | `Dockerfile` |
| CSS Modules | nativo Vite | `*.module.css` importados nos componentes |
| Fetch API | nativo | `api/client.ts` → `fetch` |

**Charts:** Recharts (`PieChart`, `LineChart`) no `AdminOverloadDashboard`.

**Não identificado no frontend:** Redux, Zustand, TanStack Query, Axios, UI kit (MUI, Chakra), Tailwind, ESLint/Prettier configurados, i18n, PWA, SSR.

---

# Arquitetura

**Padrão principal: SPA React com separação em camadas leves (feature-oriented / layered frontend).**

Não é hexagonal nem microfrontend; é uma aplicação cliente única com:

1. **Camada de apresentação** — `pages/`, `components/`
2. **Camada de estado global** — `context/AuthContext.tsx`
3. **Camada de acesso a API** — `api/` (`client`, `auth`, `users`)
4. **Camada de tipos** — `types/user.ts`
5. **Hook de conveniência** — `hooks/useAuth.ts` (reexport)

```
[Browser]
    │
    ▼
main.tsx (StrictMode, BrowserRouter, AuthProvider)
    │
    ▼
App.tsx (rotas)
    │
    ├── LoginPage ──► api/auth, api/users ──► apiFetch ──► /api (proxy)
    │
    └── ProtectedRoute ──► AppLayout ──► Outlet ──► HomePage
              │                │
              │                └── SettingsModal ──► api/users
              │
              └── useAuth (sessionStorage)
```

**Roteamento:** React Router v6 com rotas aninhadas: layout protegido envolve `AppLayout`, filha `HomePage` via `<Outlet />`.

**Estado:** React Context para autenticação; estado local `useState` em páginas e modais. Sem biblioteca de estado global além do contexto.

**Estilo:** CSS Modules por componente/página + reset/global em `index.css`.

---

# Estrutura de Pastas

```
frontend/
├── index.html                 # Shell HTML, lang pt-BR
├── package.json
├── vite.config.ts             # Dev server + proxy /api
├── vitest.config.ts           # Testes (jsdom, setup)
├── tsconfig.json              # Strict TS para src/
├── tsconfig.node.json         # Config para arquivos Vite
├── nginx.conf                 # SPA + reverse proxy /api
├── Dockerfile                 # npm test + build → Nginx
├── context.md
└── src/
    ├── main.tsx               # Entry point
    ├── App.tsx                # Definição de rotas
    ├── index.css              # Estilos globais
    ├── vite-env.d.ts
    ├── api/
    │   ├── client.ts          # apiFetch, ApiError
    │   ├── auth.ts            # login()
    │   └── users.ts           # createUser, getUser, changeUserType
    ├── types/
    │   └── user.ts            # AuthUser, UserView, UserType, payloads
    ├── context/
    │   └── AuthContext.tsx    # AuthProvider, useAuth
    ├── hooks/
    │   └── useAuth.ts         # Reexport de useAuth
    ├── pages/
    │   ├── LoginPage.tsx
    │   ├── HomePage.tsx
    │   └── *.module.css
    ├── components/
    │   ├── AppLayout.tsx
    │   ├── TopMenu.tsx
    │   ├── ProtectedRoute.tsx
    │   ├── SettingsModal.tsx
    │   └── *.module.css
    └── test/
        ├── setup.ts
        ├── LoginPage.test.tsx
        └── ProtectedRoute.test.tsx
```

| Diretório | Responsabilidade |
|-----------|------------------|
| `api/` | Funções assíncronas que chamam o backend; sem lógica de UI |
| `types/` | Contratos TypeScript alinhados ao JSON da API |
| `types/statistics.ts` | `StatisticsPeriod`, `RecordBatchCount` — compartilhados entre dashboards |
| `context/` | Estado de sessão do usuário autenticado |
| `hooks/` | Ponto único de import para `useAuth` |
| `pages/` | Telas ligadas a rotas |
| `components/` | UI reutilizável (layout, menu, guarda de rota, modal) |
| `test/` | Testes Vitest + Testing Library |

---

# Convenções de Código

## Nomenclatura

| Elemento | Padrão | Exemplo |
|----------|--------|---------|
| Componentes React | PascalCase, export nomeado | `export function LoginPage()` |
| Páginas | sufixo `Page` | `LoginPage`, `HomePage` |
| Hooks | prefixo `use` | `useAuth` |
| Funções de API | verbo em camelCase | `login`, `createUser`, `changeUserType` |
| Tipos/interfaces | PascalCase | `AuthUser`, `LoginPayload` |
| Payloads de API | sufixo `Payload` quando request | `CreateUserPayload`, `LoginPayload` |
| CSS Modules | `ComponentName.module.css` | `LoginPage.module.css` |
| Classes CSS | camelCase no module | `styles.tabActive` |
| Constantes | UPPER_SNAKE | `STORAGE_KEY = 'auth_user'` |

## Componentes

- Funções (não `React.FC` explícito).
- Props via interface quando há props (`TopMenuProps`, `SettingsModalProps`).
- `App` é **default export**; demais componentes e páginas usam **named export**.
- Import de estilos: `import styles from './X.module.css'`.
- Atributos de acessibilidade pontuais: `role="dialog"`, `aria-modal="true"` no modal; `htmlFor`/`id` em formulários.

## Tipos (`types/user.ts`)

- `UserType` como union literal `'ADMIN' | 'USER'` (espelha enum do backend).
- `AuthUser`: resposta de login/create com `id`.
- `UserView`: consulta sem `id` (name, email, type).
- `CreateUserPayload`: campos do cadastro; `type` opcional.

## DTOs / contratos API

- Tipos em `types/`; payloads específicos de um módulo podem ficar em `api/` (`LoginPayload` em `auth.ts`).
- Respostas tipadas no genérico de `apiFetch<T>`.

## Organização de imports

Ordem observada: React → router → api → context/hooks → types → styles.

## TypeScript

- `strict: true`, `noUnusedLocals`, `noUnusedParameters` (`tsconfig.json`).
- `jsx: react-jsx` (sem import React obrigatório em JSX).

---

# Fluxo da Aplicação

## Bootstrap (`main.tsx`)

1. `createRoot` monta árvore: `StrictMode` → `BrowserRouter` → `AuthProvider` → `App`.
2. `AuthProvider` hidrata `user` de `sessionStorage` (`loadUser`).

## Login (`LoginPage`)

1. Usuário submete formulário → `handleLogin`.
2. `login({ identifier, password })` → `POST /api/auth/login`.
3. `apiFetch` parseia JSON ou lança `ApiError` com `message` do backend.
4. `setUser(user)` persiste em `sessionStorage` e atualiza contexto.
5. `navigate('/', { replace: true })`.

Se já autenticado: `<Navigate to="/" />` imediato.

## Cadastro (aba na `LoginPage`)

1. `createUser(...)` → `POST /api/users`.
2. Sucesso: mensagem em português, volta para aba login (**não** faz auto-login).
3. Erro: exibe `ApiError.message` ou fallback PT.

## Rota protegida

1. `ProtectedRoute` lê `isAuthenticated` (`user !== null`).
2. Se falso → `<Navigate to="/login" replace />`.
3. Se verdadeiro → renderiza `children` (`AppLayout`) ou `<Outlet />`.

## Layout autenticado (`AppLayout`)

1. `TopMenu` + `<Outlet />` (renderiza `HomePage` em `/`).
2. Ícone de engrenagem (à esquerda do Logoff) abre `SettingsModal` (estado local `settingsOpen`).

## Alterar tipo (`SettingsModal`)

1. `changeUserType(user.id, type)` → `PATCH /api/users/{id}/type`.
2. Atualiza contexto: `setUser({ ...user, type })`.
3. Fecha modal.

## Cliente HTTP (`apiFetch`)

1. `fetch(path)` com `Content-Type: application/json`.
2. Se `!response.ok`: JSON `{ message }` → `ApiError`.
3. Se status `204`: retorna `undefined`.
4. Caso contrário: `response.json()` tipado como `T`.

**Não envia** header `Authorization` nem cookie customizado — alinhado ao backend sem JWT.

---

# Regras de Desenvolvimento

## Nova tela com rota

1. Criar `src/pages/NomePage.tsx` + `NomePage.module.css` se necessário.
2. Registrar em `App.tsx`:
   - Rota pública: irmã de `/login`.
   - Rota autenticada: filha do bloco `ProtectedRoute` + `AppLayout` (usa `<Outlet />`).
3. Link no `TopMenu` via `NavLink` se fizer parte do menu principal.

## Nova chamada à API

1. Adicionar tipo em `types/` se for entidade compartilhada.
2. Criar função em `api/auth.ts` ou `api/users.ts` (ou novo arquivo `api/recurso.ts`).
3. Usar sempre `apiFetch` de `client.ts` — não chamar `fetch` direto nos componentes.
4. Tratar erros com `err instanceof ApiError`.

## Autenticação em componente

- Importar `useAuth` de `../hooks/useAuth` (não instanciar contexto manualmente).
- Garantir que a árvore esteja sob `AuthProvider` (já em `main.tsx`).

## Validações

- **Formulários:** atributos HTML5 (`required`, `type="email"`, `minLength={6}`) — ex.: senha no cadastro.
- **Sem** biblioteca de schema (Zod/Yup) no projeto atual.
- Validação de negócio no backend; UI exibe `message` retornada.

## Tratamento de erros (padrão observado)

```typescript
try {
  // chamada api
} catch (err) {
  setError(err instanceof ApiError ? err.message : 'Mensagem fallback em PT');
} finally {
  setLoading(false);
}
```

Estados locais: `error`, `loading` (e `success` onde aplicável).

## Estilos

- Preferir CSS Module co-localizado com o componente.
- Variáveis globais mínimas em `index.css` (reset, fonte system-ui, fundo `#f4f6f8`).

## Testes de componente

- `MemoryRouter` (ou rotas explícitas) + `AuthProvider` quando usar `useAuth`.
- `sessionStorage.clear()` em `beforeEach` se testar auth (`ProtectedRoute.test.tsx`).
- Queries por label/role (`getByLabelText`, `getByRole`).

---

# Integrações Externas

| Integração | Detalhe | Evidência |
|------------|---------|-----------|
| **Backend REST** | Única integração de dados | `api/*.ts`, paths `/api/...` |
| **Banco de dados** | Não há acesso direto no frontend | — |
| **Filas / cloud** | Não identificadas | — |
| **OAuth / SSO** | Não identificado | — |

### Endpoints consumidos

| Função | Método | Path | Uso no UI |
|--------|--------|------|-----------|
| `login` | POST | `/api/auth/login` | `LoginPage` |
| `createUser` | POST | `/api/users` | `LoginPage` (cadastro) |
| `getUser` | GET | `/api/users/{id}` | **Definido, não utilizado** em nenhum componente |
| `changeUserType` | PATCH | `/api/users/{id}/type` | `SettingsModal` |
| `listUsers` | GET | `/api/users` | `AdminOverloadDashboard` |
| `generateOverload` | POST | `/api/overloads/generate` | `UserOverloadPanel` |
| `listOverloads` | GET | `/api/overloads` | `UserOverloadPanel` |
| `getOverloadStatistics` | GET | `/api/overloads/statistics` | `AdminOverloadDashboard` (refresh 30s) |
| `getLoginAttemptsMetrics` | GET | `/api/metrics/login-attempts` | `LoginAttemptsLineChart` (refresh 30s) |

### Proxy

- **Dev:** Vite `server.proxy['/api']` → `http://localhost:8080`.
- **Prod (Docker):** Nginx `location /api/` → `http://backend:8080/api/`.

Paths relativos (`/api/...`) funcionam em ambos os ambientes sem URL absoluta no código.

---

# Segurança

## Autenticação (lado cliente)

- **Modelo:** presença de `AuthUser` no React Context + `sessionStorage` (chave `auth_user`).
- **Não há token JWT** armazenado ou enviado nas requisições.
- **Persistência:** `sessionStorage` (dados perdidos ao fechar aba/navegador; não `localStorage`).
- Login bem-sucedido grava JSON do usuário (`id`, `name`, `email`, `type`).

## Autorização (lado cliente)

- **Rota:** `ProtectedRoute` bloqueia apenas usuários **não autenticados** (`isAuthenticated`).
- **Não há** checagem de `user.type === 'ADMIN'` para rotas ou componentes.
- Qualquer usuário autenticado acessa Configurações e pode tentar alterar tipo (backend também não restringe por role).

## Filtros / middlewares / interceptadores

- **Não identificados** no frontend (sem axios interceptors, sem wrapper de fetch para auth header).

## Implicações

- Proteção de rotas é **apenas UX**; APIs permanecem acessíveis sem credenciais se o backend for exposto diretamente.
- Recarregar página em rota protegida mantém sessão se `sessionStorage` ainda tiver dados.

---

# Persistência

| Aspecto | Implementação |
|---------|---------------|
| Banco de dados | **Não aplicável** no frontend |
| ORM | **Não aplicável** |
| Estado servidor | Não (sem SSR/cache de dados) |
| Estado cliente | `sessionStorage` para usuário autenticado |
| Cache de API | **Não identificado** (sem React Query/SWR) |

**Convenção de persistência de auth:**

```typescript
const STORAGE_KEY = 'auth_user';
sessionStorage.setItem(STORAGE_KEY, JSON.stringify(user));
```

`loadUser` faz `JSON.parse` com try/catch — JSON inválido resulta em `null` (deslogado).

---

# Testes

## Frameworks

- Vitest (`describe`, `it`, `expect`)
- `@testing-library/react` (`render`, `screen`, `waitFor`)
- `@testing-library/jest-dom` (matchers como `toBeInTheDocument`)
- Ambiente `jsdom` (`vitest.config.ts`)

## Estratégia observada

- Testes de componente isolados, **sem** mock de `fetch` nos arquivos existentes.
- Providers mínimos: `MemoryRouter`, `AuthProvider`.
- Docker build executa `npm test` antes de `npm run build`.

## Cobertura observada

| Arquivo | Coberto |
|---------|---------|
| `LoginPage` | Sim — renderiza campos de login |
| `ProtectedRoute` | Sim — redireciona se não autenticado |
| `HomePage`, `AppLayout`, `TopMenu`, `SettingsModal` | Não |
| `api/*`, `AuthContext` (lógica persist/load) | Não |
| Fluxos async (login, register, change type) | Não |

**Não identificado:** E2E (Playwright/Cypress), testes de snapshot, coverage threshold.

---

# Dependências Importantes

| Pacote | Papel |
|--------|-------|
| `react` / `react-dom` | UI |
| `react-router-dom` | Rotas, navegação, guards |
| `vite` | Bundler e dev server |
| `@vitejs/plugin-react` | Fast Refresh, JSX |
| `typescript` | Tipagem estática |
| `vitest` | Runner de testes |
| `@testing-library/react` | Testes de componentes |

Runtime de produção no container: apenas assets estáticos + Nginx (Node não presente na imagem final).

---

# Decisões Arquiteturais

1. **Paths relativos `/api`** — mesmo código em dev (proxy Vite) e prod (proxy Nginx) sem variável de ambiente.

2. **`apiFetch` centralizado** — tratamento uniforme de erro JSON `{ message }` e classe `ApiError`.

3. **Context + sessionStorage** — sessão simples sem biblioteca de auth; adequado a backend sem token.

4. **`hooks/useAuth.ts` como reexport** — imports consistentes `from '../hooks/useAuth'` sem acoplar páginas ao path do context.

5. **CSS Modules, sem UI framework** — bundle leve, estilos encapsulados por componente.

6. **Cadastro não autentica automaticamente** — fluxo explícito: criar → mensagem → login manual.

7. **`ProtectedRoute` suporta `children` e `Outlet`** — layout wrapper (`AppLayout`) usa children; padrão flexível para rotas aninhadas.

8. **TypeScript strict** — contratos de API explícitos, menos erros em refactors.

9. **Testes no pipeline Docker** — qualidade mínima garantida no build da imagem.

10. **`getUser` exposto na API client mas não usado** — provável preparação para feature futura ou resíduo; Home usa apenas dados do contexto.

---

# Guia para Futuras Implementações

## Adicionar página autenticada

```tsx
// App.tsx — dentro do bloco ProtectedRoute existente
<Route path="/perfil" element={<ProfilePage />} />
```

```tsx
// pages/ProfilePage.tsx
export function ProfilePage() {
  const { user } = useAuth();
  return <section>...</section>;
}
```

## Adicionar função de API

```typescript
// api/users.ts
export function getUser(id: string): Promise<UserView> {
  return apiFetch<UserView>(`/api/users/${id}`);
}
```

Usar em página com padrão de loading/erro da `LoginPage` ou `SettingsModal`.

## Consumir dados com loading

Padrão existente em `LoginPage` / `SettingsModal`:

- `useState` para `loading`, `error`
- `disabled={loading}` no botão submit
- texto do botão alterna (`Entrando...` / `Salvando...`)

## Restringir UI por tipo ADMIN (não existe hoje)

Se necessário, padrão sugerido alinhado ao código:

```typescript
const { user } = useAuth();
if (user?.type !== 'ADMIN') return null; // ou <Navigate />
```

Documentar que isso **não** substitui autorização no backend.

## Novo módulo de API

Criar `src/api/orders.ts`, importar `apiFetch`, tipos em `src/types/`.

## Exemplo de integração com erro

```typescript
import { ApiError } from '../api/client';
import { login } from '../api/auth';

try {
  const user = await login({ identifier, password });
  setUser(user);
} catch (err) {
  setError(err instanceof ApiError ? err.message : 'Erro ao fazer login');
}
```

## Teste de nova página

```tsx
render(
  <MemoryRouter>
    <AuthProvider>
      <MinhaPage />
    </AuthProvider>
  </MemoryRouter>
);
```

---

# Anti-Padrões

Com base na arquitetura atual, **evitar:**

1. **`fetch` direto em componentes** — usar `apiFetch` e módulos `api/`.
2. **`localStorage` para auth** — o projeto usa `sessionStorage` e chave `auth_user`.
3. **Duplicar `useAuth` fora do `AuthProvider`** — lança erro em runtime.
4. **Assumir que rota protegida = API segura** — não há token nas requisições.
5. **Hardcodar URL do backend** (`http://localhost:8080`) — quebra proxy Nginx/Vite.
6. **CSS global excessivo** para estilos de um único componente — usar CSS Module.
7. **Default export em páginas/componentes** (exceto `App`) — inconsistência com o restante.
8. **Ignorar `ApiError`** — mensagens do backend perdem-se com `catch` genérico.
9. **Auto-login após register** sem requisito explícito — fluxo atual pede login manual.
10. **Biblioteca de estado global** para dados já cobertos por Context — aumenta complexidade sem precedente.
11. **Confiar em `user.type` só no frontend** para segurança — backend não valida roles hoje.
12. **Deixar funções API órfãs** sem uso ou teste — preferir integrar ou remover (`getUser` hoje).

---

# Resumo Executivo

1. SPA React 18 + TypeScript + Vite para gestão de usuários do monorepo CNietsche.
2. Arquitetura em camadas: `pages`, `components`, `context`, `api`, `types`.
3. React Router v6 com `/login` público e `/` protegido via `ProtectedRoute`.
4. Autenticação client-side: `AuthContext` + `sessionStorage` (`auth_user`), sem JWT.
5. Cliente HTTP único: `apiFetch` + `ApiError` para erros `{ message }`.
6. APIs: login, create user, change user type; `getUser` definido mas não usado na UI.
7. Proxy `/api` no Vite (dev) e Nginx (Docker/prod) para o backend Spring.
8. UI em português; mensagens de erro misturam backend (EN) e fallbacks PT.
9. Estilos: CSS Modules + `index.css` global minimalista.
10. Formulários validados com HTML5; estado async com `loading`/`error` local.
11. `AppLayout` + `TopMenu` + `Outlet` para área logada; modal para configurações.
12. Cadastro na `LoginPage` (abas login/register); sem auto-login após criar conta.
13. Sem autorização por role (`ADMIN`/`USER`) nas rotas — só autenticado vs anônimo.
14. Testes Vitest + Testing Library: `LoginPage` e `ProtectedRoute` apenas.
15. Build Docker: `npm test` + `npm run build`, serve com Nginx na porta 80.
16. TypeScript `strict` com `noUnusedLocals` / `noUnusedParameters`.
17. Named exports para componentes; `useAuth` importado via `hooks/useAuth`.
18. Nenhum Redux/React Query/Axios — manter simplicidade salvo decisão explícita.
19. Host Docker expõe frontend na 8080; backend só via proxy interno.
20. Evoluções devem seguir `api/` + tipos + Context + CSS Module + padrão de erro `ApiError`.

---

*Documento gerado a partir do código em `frontend/` em junho de 2026. Revisar após mudanças estruturais significativas.*
