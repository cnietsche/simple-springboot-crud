# CRUD de Usuários (Java + H2)

Aplicação de console simples para gerenciar a entidade **USER** (`id: UUID`, `name: String`) com persistência em banco H2 em arquivo.

## Requisitos

- Java 17+
- Maven 3.8+

## Executar

```bash
mvn -q compile exec:java -Dexec.mainClass="com.example.crud.App"
```

Ou gerar um JAR executável:

```bash
mvn -q package
java -jar target/user-crud-h2-1.0.0.jar
```

## Menu

| Opção | Ação                          |
|-------|-------------------------------|
| 1     | Inserir usuário (nome)        |
| 2     | Listar todos                  |
| 3     | Excluir por UUID              |
| 0     | Sair                          |

O banco fica em `./data/users.mv.db` (criado automaticamente na primeira execução).
