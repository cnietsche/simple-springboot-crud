package com.example.crud;

import com.example.crud.db.Database;
import com.example.crud.model.User;
import com.example.crud.repository.UserRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class App {

    private static final UserRepository repository = new UserRepository();

    public static void main(String[] args) {
        try {
            Database.initSchema();
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar banco: " + e.getMessage());
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                String option = scanner.nextLine().trim();
                switch (option) {
                    case "1" -> insertUser(scanner);
                    case "2" -> listUsers();
                    case "3" -> deleteUser(scanner);
                    case "0" -> running = false;
                    default -> System.out.println("Opção inválida.");
                }
                System.out.println();
            }
            System.out.println("Até logo.");
        }
    }

    private static void printMenu() {
        System.out.println("=== CRUD de Usuários (H2) ===");
        System.out.println("1 - Inserir");
        System.out.println("2 - Listar");
        System.out.println("3 - Excluir");
        System.out.println("0 - Sair");
        System.out.print("Escolha: ");
    }

    private static void insertUser(Scanner scanner) {
        System.out.print("Nome: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Nome não pode ser vazio.");
            return;
        }

        User user = new User(UUID.randomUUID(), name);
        try {
            repository.insert(user);
            System.out.println("Usuário criado: " + user);
        } catch (SQLException e) {
            System.out.println("Erro ao inserir: " + e.getMessage());
        }
    }

    private static void listUsers() {
        try {
            List<User> users = repository.findAll();
            if (users.isEmpty()) {
                System.out.println("Nenhum usuário cadastrado.");
                return;
            }
            System.out.println("ID                                   | NOME");
            System.out.println("-------------------------------------+------------------");
            for (User user : users) {
                System.out.printf("%-36s | %s%n", user.getId(), user.getName());
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar: " + e.getMessage());
        }
    }

    private static void deleteUser(Scanner scanner) {
        System.out.print("ID do usuário (UUID): ");
        String idText = scanner.nextLine().trim();
        UUID id;
        try {
            id = UUID.fromString(idText);
        } catch (IllegalArgumentException e) {
            System.out.println("UUID inválido.");
            return;
        }

        try {
            if (repository.deleteById(id)) {
                System.out.println("Usuário excluído.");
            } else {
                System.out.println("Usuário não encontrado.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao excluir: " + e.getMessage());
        }
    }
}
