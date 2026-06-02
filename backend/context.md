# Projeto

API REST de gerenciamento de usuários (`user-api`), artefato Maven `com.cnietsche:user-api:1.0.0`. O sistema expõe operações de CRUD parcial (criar, consultar, alterar tipo) e autenticação por credenciais (e-mail ou username + senha).

**Propósito observado:** backend de um monorepo (`simple-springboot-crud`) que serve um frontend React; em Docker, o backend fica na rede interna do Compose e é acessado via proxy Nginx do frontend em `/api`.

**Escopo funcional atual (evidência em controllers):**

| Método | Path | Comportamento |
|--------|------|---------------|
| `POST` | `/api/users` | Cria usuário |
| `GET` | `/api/users/{id}` | Retorna `name`, `email`, `type` (sem expor senha) |
| `PATCH` | `/api/users/{id}/type` | Altera `UserType` |
| `POST` | `/api/auth/login` | Valida credenciais e retorna dados do usuário autenticado |

Não há listagem de usuários, exclusão, atualização de perfil completa nem endpoints de refresh/token.

---

# Stack Tecnológica

| Tecnologia | Versão / detalhe | Evidência |
|------------|------------------|-----------|
| Java | 21 | `pom.xml` → `<java.version>21</java.version>` |
| Spring Boot | 3.4.5 | parent POM |
| Spring Web | starter-web | REST controllers |
| Spring Data JPA | starter-data-jpa | `UserJpaEntity`, `SpringDataUserRepository` |
| Bean Validation (Jakarta) | starter-validation | `@Valid`, `@NotBlank` nos DTOs |
| Hibernate | via JPA | `ddl-auto: update` |
| H2 Database | runtime (perfil padrão) | `application-h2.yml` |
| PostgreSQL | runtime (perfil `postgres`) | `application-postgres.yml`, driver no POM |
| Spring Security Crypto | apenas `BCryptPasswordEncoder` | `PasswordEncoderConfig`; **não** há `spring-boot-starter-security` |
| Maven | build + Docker multi-stage | `pom.xml`, `Dockerfile` |
| JUnit 5 + Mockito + AssertJ | testes unitários | `*ServiceTest.java` |
| Docker | JRE 21 Alpine | `backend/Dockerfile` |

**Não identificado no backend:** JWT, OAuth2, OpenAPI/Swagger, MapStruct, Lombok, filas/mensageria, cache, Actuator, Flyway/Liquibase.

---

# Arquitetura

**Padrão principal: Arquitetura Hexagonal (Ports & Adapters).**

Evidências:

- Descrição explícita no POM: `<description>User API - Hexagonal Architecture</description>`
- Pacotes `adapter.in` (entrada HTTP), `adapter.out` (persistência), `application` (casos de uso), `domain` (núcleo)
- Portas de entrada: interfaces `*UseCase` em `domain.port.in`
- Porta de saída: `UserRepositoryPort` em `domain.port.out`
- Adaptador de persistência: `UserPersistenceAdapter` implementa a porta sem expor JPA ao domínio

**Forma de deploy:** monólito Spring Boot (um JAR, um processo).

**Inversão de dependência:** controllers e services dependem de interfaces (`CreateUserUseCase`, `UserRepositoryPort`); Spring injeta implementações (`CreateUserService`, `UserPersistenceAdapter`).

```
[HTTP Client]
     │
     ▼
adapter.in.web (Controllers, DTOs, GlobalExceptionHandler)
     │  Commands / UseCase interfaces
     ▼
application.service (*Service implements *UseCase)
     │  domain.model.User, exceptions
     ▼
domain.port.out.UserRepositoryPort
     │
     ▼
adapter.out.persistence (UserPersistenceAdapter → Spring Data JPA)
     │
     ▼
H2 ou PostgreSQL
```

O domínio **não** importa Spring, JPA nem anotações web. Exceção aceitável na camada de aplicação: `PasswordEncoder` do Spring Security Crypto em `CreateUserService` e `ValidateLoginService`.

---

# Estrutura de Pastas

```
backend/
├── pom.xml
├── Dockerfile
├── context.md
└── src/
    ├── main/
    │   ├── java/com/cnietsche/
    │   │   ├── UserApiApplication.java      # Bootstrap Spring Boot
    │   │   ├── adapter/
    │   │   │   ├── in/web/                  # Adaptadores de entrada HTTP
    │   │   │   │   ├── *Controller.java
    │   │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   │   └── dto/                 # Request/Response records (API)
    │   │   │   └── out/persistence/         # Adaptador de saída JPA
    │   │   ├── application/service/         # Implementações dos casos de uso
    │   │   ├── config/                      # Beans de infraestrutura (PasswordEncoder)
    │   │   └── domain/
    │   │       ├── model/                   # Entidades e enums de domínio
    │   │       ├── exception/               # Exceções de negócio
    │   │       └── port/
    │   │           ├── in/                   # Use cases, commands, views
    │   │           └── out/                  # Portas para infraestrutura
    │   └── resources/
    │       ├── application.yml              # Config base + perfil default h2
    │       ├── application-h2.yml
    │       └── application-postgres.yml
    └── test/java/com/cnietsche/application/service/
        └── *ServiceTest.java                # Testes unitários dos services
```

| Diretório | Responsabilidade |
|-----------|------------------|
| `adapter.in.web` | Traduz HTTP ↔ comandos/casos de uso; validação de entrada via Bean Validation nos DTOs |
| `adapter.in.web.dto` | Contratos JSON da API (records); **não** vazam para o domínio como tipos principais |
| `adapter.out.persistence` | Mapeia `User` ↔ `UserJpaEntity`; implementa `UserRepositoryPort` |
| `application.service` | Orquestra regras de aplicação; `@Service` para registro no Spring |
| `domain.model` | `User` imutável (com `withType`), enum `UserType` |
| `domain.port.in` | Contratos de entrada: `*UseCase`, `*Command`, `UserView`, `AuthenticatedUser` |
| `domain.port.out` | Contrato de persistência |
| `domain.exception` | Hierarquia `DomainException` |
| `config` | Configuração mínima de beans cross-cutting |

---

# Convenções de Código

## Pacote base

`com.cnietsche` — alinhado ao `groupId` Maven.

## Nomenclatura

| Elemento | Padrão | Exemplo |
|----------|--------|---------|
| Caso de uso (porta) | `*UseCase` | `CreateUserUseCase` |
| Implementação | `*Service` | `CreateUserService` |
| Comando de entrada | `*Command` (record) | `CreateUserCommand` |
| DTO HTTP request | `*Request` (record) | `CreateUserRequest` |
| DTO HTTP response | `*Response` (record) | `UserResponse` |
| View de saída (domínio) | `*View` ou `AuthenticatedUser` (records) | `UserView` |
| Porta de saída | `*Port` | `UserRepositoryPort` |
| Adaptador persistência | `*Adapter` / `*PersistenceAdapter` | `UserPersistenceAdapter` |
| Entidade JPA | sufixo `JpaEntity` | `UserJpaEntity` |
| Repositório Spring Data | `SpringData*Repository` | `SpringDataUserRepository` |
| Exceções de domínio | sufixo descritivo + `Exception` | `DuplicateUserException` |
| Método de caso de uso | `execute(...)` | `createUserUseCase.execute(command)` |

## Classes de domínio

- `User`: classe com campos `final`, construtor explícito, getters; imutabilidade com `withType(UserType)` retornando nova instância.
- `UserType`: enum `ADMIN`, `USER`.
- IDs: `java.util.UUID`, gerados em `CreateUserService` via `UUID.randomUUID()`.

## DTOs da API (`adapter.in.web.dto`)

- Java **records** com validação Jakarta (`@NotBlank`, `@Email`, `@Size`, `@NotNull`).
- Campos opcionais de negócio no request: `UserType type` em `CreateUserRequest` (sem `@NotNull` — default tratado no service).

## Commands e views (`domain.port.in`)

- **Records** para transporte interno entre adapter e application.
- Commands não contêm validação Jakarta — validação ocorre nos DTOs HTTP ou na lógica do service.

## Entidade JPA

- Tabela `users`, coluna `password_hash`.
- Construtor protegido vazio para JPA; construtor público para criação via adapter.
- `UserType` persistido como `STRING` (`@Enumerated(EnumType.STRING)`).
- Domínio `User` é separado de `UserJpaEntity`; conversão apenas no `UserPersistenceAdapter`.

## Controllers

- `@RestController` + `@RequestMapping` com prefixo `/api/...`.
- Injeção por construtor (sem `@Autowired` em campos).
- `@Valid @RequestBody` em POST/PATCH com body.
- Retornos: `ResponseEntity` com status explícito apenas em criação (`201 CREATED`); demais endpoints retornam o record diretamente (200 implícito).

## Services

- `@Service`, implementam interface `*UseCase`.
- Dependem de `UserRepositoryPort` e, quando necessário, `PasswordEncoder`.

---

# Fluxo da Aplicação

## Exemplo: criar usuário (`POST /api/users`)

1. `UserController.create` recebe `CreateUserRequest` validado.
2. Monta `CreateUserCommand` (name, email, username, password, type).
3. `CreateUserService.execute`:
   - Verifica duplicidade de e-mail e username via `UserRepositoryPort`.
   - Normaliza: `name`/`username` com `trim()`, `email` com `trim().toLowerCase()`.
   - Define `UserType.USER` se `type == null`.
   - Gera hash BCrypt da senha.
   - Instancia `User` com novo UUID.
   - Persiste via `userRepository.save`.
4. Controller mapeia `User` → `CreateUserResponse` (inclui `id`; **não** retorna senha).
5. Resposta HTTP `201` com JSON.

## Exemplo: login (`POST /api/auth/login`)

1. `AuthController.login` → `ValidateLoginCommand`.
2. `ValidateLoginService.execute`:
   - `identifier.trim()`; busca por e-mail (`toLowerCase()`) e, se vazio, por username.
   - Compara senha com `passwordEncoder.matches`.
   - Retorna `AuthenticatedUser` ou lança `InvalidCredentialsException`.
3. Controller → `LoginResponse`.

## Exemplo: consultar usuário (`GET /api/users/{id}`)

1. `UserController.getById` converte path para `UUID`.
2. `GetUserService` busca no repositório ou lança `UserNotFoundException`.
3. Retorna `UserView` mapeado para `UserResponse` (sem `id` na resposta).

## Exemplo: alterar tipo (`PATCH /api/users/{id}/type`)

1. `ChangeUserTypeService` carrega usuário, aplica `user.withType`, salva, retorna `UserView` via `GetUserService.toView`.

## Tratamento de erros

`GlobalExceptionHandler` (`@RestControllerAdvice`) centraliza:

| Exceção | HTTP | Corpo |
|---------|------|-------|
| `UserNotFoundException` | 404 | `{"message": "User not found"}` |
| `InvalidCredentialsException` | 401 | `{"message": "Invalid credentials"}` |
| `DomainException` (ex.: `DuplicateUserException`) | 409 | `{"message": "<texto>"}` |
| `MethodArgumentNotValidException` | 400 | primeira mensagem de campo |

Formato único: `Map<String, String>` com chave `"message"`.

---

# Regras de Desenvolvimento

## Criar novo endpoint

1. Definir interface `*UseCase` e record `*Command` (se houver entrada) em `domain.port.in`.
2. Implementar `*Service` em `application.service` com `@Service`.
3. Criar records `*Request`/`*Response` em `adapter.in.web.dto` com Bean Validation.
4. Adicionar método no controller adequado (`UserController` ou `AuthController`, ou novo controller sob `/api/...`).
5. Mapear DTO → Command no controller; **não** passar DTOs para services.
6. Registrar exceções novas em `GlobalExceptionHandler` se não forem subtipo de `DomainException` com status adequado.

## Criar novo serviço / caso de uso

1. Porta em `domain.port.in`.
2. Implementação em `application.service` dependendo apenas de `domain.*` e portas `domain.port.out` (evitar dependências web/JPA).
3. Teste unitário em `src/test/.../application/service/` com Mockito (`@ExtendWith(MockitoExtension.class)`).

## Criar nova integração de persistência

1. Estender ou criar porta em `domain.port.out`.
2. Implementar adaptador em `adapter.out.persistence` com `@Component`.
3. Se necessário, nova entidade JPA + interface `JpaRepository` com naming query methods (`findBy*`, `existsBy*`).

## Validações

- **Entrada HTTP:** Jakarta Validation nos DTOs (`@Valid` no controller).
- **Regras de negócio:** dentro dos `*Service` (ex.: duplicidade de e-mail em `CreateUserService`).
- Commands do domínio **não** usam anotações de validação.

## Tratar exceções

- Lançar subclasses de `DomainException` no domínio/aplicação.
- Mensagens em inglês (padrão observado).
- Handler global já cobre `DomainException` como 409; criar handler específico se precisar de outro status (como `UserNotFoundException` → 404).

## Normalização de dados (padrão existente)

- E-mail: sempre minúsculas ao persistir/buscar.
- Strings de texto: `trim()` em name, email, username no create.
- Senha: nunca exposta em responses; apenas `password_hash` no banco.

## Registro Spring

- Services: `@Service`
- Adaptadores de saída: `@Component`
- Configuração: `@Configuration` + `@Bean` em `config`

Component scan: `@SpringBootApplication` em `com.cnietsche` cobre subpacotes automaticamente.

---

# Integrações Externas

| Integração | Uso | Configuração |
|------------|-----|----------------|
| **H2 (arquivo)** | Banco padrão (perfil `h2`) | `jdbc:h2:file:${H2_DATA_PATH:./data}/users`, usuário `sa`, senha vazia |
| **PostgreSQL** | Perfil opcional `postgres` (ex.: Supabase) | `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` |
| **APIs HTTP externas** | Não identificadas | — |
| **Filas / mensageria** | Não identificadas | — |
| **Serviços cloud** | Não no código; README menciona Supabase apenas como destino PostgreSQL | — |
| **Autenticação externa** | Não; login é validação local de credenciais | — |

**Docker Compose:** backend com `SPRING_PROFILES_ACTIVE=h2`, volume `backend-data` em `/app/data`, porta 8080 apenas `expose` (não publicada no host).

**Frontend:** não faz parte deste `context.md`; comunicação esperada via HTTP REST em `/api/*` (proxy Nginx/Vite).

---

# Segurança

## Autenticação

- **Credenciais:** POST `/api/auth/login` com `identifier` (e-mail ou username) e `password`.
- **Hash de senha:** BCrypt via `BCryptPasswordEncoder` (`PasswordEncoderConfig`).
- **Sem sessão server-side, sem JWT, sem cookies de autenticação** no backend analisado.
- Login bem-sucedido retorna dados do usuário em JSON; **não há token** — a persistência de sessão/autenticação em chamadas subsequentes é responsabilidade do cliente (evidência: ausência de filtros e de `spring-boot-starter-security`).

## Autorização

- **Não implementada** no backend: nenhum `@PreAuthorize`, filtro de segurança, verificação de `UserType` nos endpoints.
- `PATCH /api/users/{id}/type` e demais rotas são **públicas** (qualquer cliente que alcance a API pode invocá-las).

## Filtros / middlewares / interceptadores

- **Não identificados** (sem Spring Security filter chain, sem `HandlerInterceptor`, sem CORS configurado no backend).

## Exposição de dados sensíveis

- `GET /api/users/{id}` retorna apenas `name`, `email`, `type` (`UserResponse`).
- `CreateUserResponse` e `LoginResponse` incluem `id` mas nunca senha ou hash.

## H2 Console

- Desabilitado: `spring.h2.console.enabled: false` em `application-h2.yml`.

---

# Persistência

## Banco de dados

- **Padrão:** H2 em modo arquivo (`DB_CLOSE_DELAY=-1`).
- **Alternativo:** PostgreSQL (perfil `postgres`).

## Estratégia de acesso

- **Porta hexagonal** `UserRepositoryPort` abstrai persistência.
- **Adaptador** `UserPersistenceAdapter` traduz domínio ↔ JPA.
- **Spring Data JPA** `SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID>` com query methods derivados.

## ORM

- Hibernate via Spring Data JPA.
- `spring.jpa.hibernate.ddl-auto: update` (schema evolui automaticamente; **sem** migrations versionadas).
- `spring.jpa.open-in-view: false` (boa prática explícita no projeto).

## Convenções de consultas

- Métodos derivados: `findByEmail`, `findByUsername`, `existsByEmail`, `existsByUsername`.
- Unicidade: constraints `@Column(unique = true)` em `email` e `username` na entidade JPA.
- Tabela: `users`; PK: `UUID`.

## Mapeamento domínio ↔ JPA

Feito exclusivamente em `UserPersistenceAdapter.toEntity` / `toDomain`. O domínio nunca referencia `UserJpaEntity`.

---

# Testes

## Frameworks

- JUnit 5 (`@Test`, `@BeforeEach`, `@ExtendWith`)
- Mockito (`@Mock`, `when`, `verify`, `ArgumentCaptor`)
- AssertJ (`assertThat`, `assertThatThrownBy`)
- `spring-boot-starter-test` no POM (disponível; uso observado limitado a testes unitários manuais)

## Estratégia observada

- **Testes unitários** dos quatro services em `application.service`.
- **Mocks** de `UserRepositoryPort` e `PasswordEncoder` — sem contexto Spring (`@SpringBootTest` **não** utilizado).
- **Sem** testes de controller (`MockMvc` / `@WebMvcTest`).
- **Sem** testes de integração com banco (`@DataJpaTest`).
- **Sem** testes do `UserPersistenceAdapter` ou `GlobalExceptionHandler`.

## Cobertura observada

| Classe | Testada |
|--------|---------|
| `CreateUserService` | Sim (criação com tipo default, e-mail duplicado) |
| `GetUserService` | Sim (sucesso, not found) |
| `ValidateLoginService` | Sim (login e-mail, username, senha inválida) |
| `ChangeUserTypeService` | Sim (alteração de tipo) |
| Controllers, adapters, exception handler | Não |

**Docker build:** `mvn -B test package -DskipTests=false` no `Dockerfile` — testes rodam no build da imagem.

---

# Dependências Importantes

| Dependência | Papel crítico |
|-------------|---------------|
| `spring-boot-starter-web` | REST API, JSON, embedded Tomcat |
| `spring-boot-starter-data-jpa` | Repositórios, Hibernate, transações |
| `spring-boot-starter-validation` | Validação de DTOs |
| `spring-security-crypto` | BCrypt para senhas (**apenas** crypto, não security web) |
| `h2` | Banco em desenvolvimento/Docker default |
| `postgresql` | Banco em produção (perfil postgres) |
| `spring-boot-starter-test` | Stack de testes |

---

# Decisões Arquiteturais

1. **Hexagonal em vez de MVC tradicional** — domínio isolado de Spring Web e JPA; facilita testes unitários dos services e troca de adaptadores.

2. **Dois modelos de usuário (`User` vs `UserJpaEntity`)** — evita anotações JPA no núcleo e mantém entidade de domínio imutável.

3. **Records para commands, views e DTOs** — contratos imutáveis e concisos (Java 21).

4. **UUID como identificador** — gerado na aplicação, não pelo banco.

5. **Apenas `spring-security-crypto`** — hashing sem filter chain; API stateless do ponto de vista do servidor.

6. **Perfil Spring para banco** — `h2` default; `postgres` preparado para Supabase sem alterar código.

7. **`ddl-auto: update`** — simplicidade para demo/desenvolvimento; trade-off: sem histórico de schema versionado.

8. **`UserView` sem ID no GET** — resposta de consulta expõe menos metadados que create/login (decisão de contrato API).

9. **Exceções de domínio → HTTP via handler único** — respostas de erro homogêneas `{"message": "..."}`.

10. **Login por identifier flexível** — e-mail (case-insensitive) ou username na mesma operação.

---

# Guia para Futuras Implementações

## Adicionar campo ao usuário (ex.: `phone`)

1. Adicionar campo em `domain.model.User` (construtor, getter, atualizar `withType` ou criar `withPhone`).
2. Atualizar `UserJpaEntity` + migração implícita via Hibernate `update` (ou planejar Flyway se o projeto evoluir).
3. Estender `UserRepositoryPort` apenas se novas queries forem necessárias.
4. Atualizar `UserPersistenceAdapter.toEntity` / `toDomain`.
5. Incluir no DTO/command conforme camada: request HTTP → command → domínio.
6. Ajustar `UserView` / `UserResponse` se o campo for exposto na API.
7. Adicionar testes no `*Service` correspondente.

## Adicionar endpoint protegido por role

**Não existe precedente no código.** Opções alinhadas ao projeto:

- Introduzir mecanismo explícito (JWT + filter, ou `spring-boot-starter-security`) em `config` / novo pacote `adapter.in.web.security`.
- Manter regras de autorização nos services (ex.: verificar `UserType` no command) **e** documentar que hoje não há enforcement HTTP.

Sem evidência atual, qualquer abordagem deve ser decidida explicitamente — não inferir JWT só porque há login.

## Novo caso de uso — checklist

```java
// 1. domain/port/in/DeleteUserUseCase.java
public interface DeleteUserUseCase {
    void execute(UUID id);
}

// 2. application/service/DeleteUserService.java
@Service
public class DeleteUserService implements DeleteUserUseCase { ... }

// 3. UserController — injetar DeleteUserUseCase no construtor

// 4. DeleteUserServiceTest — mock UserRepositoryPort
```

Seguir nomes e pacotes dos exemplos existentes: `CreateUserService`, `CreateUserUseCase`, `CreateUserCommand`.

## Reutilizar projeção de saída

`GetUserService.toView(User)` é `static` e reutilizado por `ChangeUserTypeService` — para novas operações que retornem os mesmos campos, reutilizar `UserView` e mapear para `UserResponse` no controller.

## Exemplo de validação em DTO (padrão existente)

```java
public record CreateUserRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 3, max = 100) String username,
        @NotBlank @Size(min = 6, max = 100) String password,
        UserType type
) {}
```

## Exemplo de exceção de negócio

```java
public class DuplicateUserException extends DomainException {
    public DuplicateUserException(String message) {
        super(message);
    }
}
```

Lançada em `CreateUserService` quando `existsByEmail` ou `existsByUsername` retorna true.

---

# Anti-Padrões

Com base na arquitetura atual, **evitar:**

1. **Anotações JPA ou Spring em `domain.model`** — quebra isolamento hexagonal.
2. **Controllers acessando `SpringDataUserRepository` ou `UserJpaEntity` diretamente** — bypass do `UserRepositoryPort`.
3. **Retornar entidade `User` com `passwordHash` em DTOs HTTP** — violação do padrão de exposição atual.
4. **Lógica de negócio pesada em controllers** — deve ficar em `application.service`.
5. **DTOs HTTP dentro de `domain.port.in`** — commands/views são contratos internos; DTOs ficam em `adapter.in.web.dto`.
6. **Adicionar `spring-boot-starter-security` sem desenho explícito** — hoje não há filter chain; adição parcial pode quebrar endpoints públicos esperados pelo frontend.
7. **Assumir autenticação em rotas subsequentes** — o backend não valida token/sessão hoje; não documentar endpoints como “protegidos” sem implementar mecanismo.
8. **Testes que sobem contexto Spring para regra simples** — padrão do projeto é unit test puro com mocks.
9. **Senha em texto plano no banco** — sempre `passwordEncoder.encode` no create.
10. **Expor `id` no `UserResponse` de GET** — inconsistente com o contrato atual (alterar só se for requisito explícito).
11. **Dependências do domínio em bibliotecas de infraestrutura** (exceto o uso já existente de crypto na application layer).
12. **Query nativa SQL sem necessidade** — preferir métodos derivados do Spring Data, como no repositório atual.

---

# Resumo Executivo

1. API REST Spring Boot 3.4 / Java 21 para CRUD parcial de usuários + login.
2. Arquitetura **hexagonal** com pacotes `adapter`, `application`, `domain`.
3. Monólito empacotado como JAR; Docker multi-stage com testes no build.
4. Quatro casos de uso: criar usuário, obter por ID, validar login, alterar tipo.
5. Portas de entrada: interfaces `*UseCase`; saída: `UserRepositoryPort`.
6. Domínio: `User` imutável, `UserType` enum, exceções `DomainException`.
7. Persistência: JPA/Hibernate, adaptador `UserPersistenceAdapter`, tabela `users`.
8. Banco default H2 arquivo; perfil `postgres` para PostgreSQL/Supabase.
9. Senhas com BCrypt; **sem** Spring Security web, **sem** JWT.
10. Endpoints **sem autorização** — API aberta após deploy.
11. Validação HTTP via Jakarta Validation; regras de negócio nos services.
12. Erros JSON uniformes via `GlobalExceptionHandler`.
13. DTOs HTTP em records em `adapter.in.web.dto`; commands em `domain.port.in`.
14. IDs UUID gerados na aplicação.
15. Normalização: e-mail lowercase, trim em strings.
16. Testes unitários Mockito apenas nos services; sem testes de API ou JPA.
17. `ddl-auto: update` — sem Flyway/Liquibase.
18. Prefixo API: `/api/users`, `/api/auth`.
19. `open-in-view: false` configurado.
20. Evolução futura deve preservar separação domínio/adapters e padrão UseCase → Service → Port.

---

*Documento gerado a partir do código em `backend/` em junho de 2026. Revisar após mudanças estruturais significativas.*
