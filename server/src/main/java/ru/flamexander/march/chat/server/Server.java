package ru.flamexander.march.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;
    private DatabaseAuthService authService;


    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("Сервер запущен на порту: %d, ожидаем подключения клиентов\n", port);
            while (true) {
                Socket socket = serverSocket.accept();
                subscribe(new ClientHandler(this, socket, UserRole.USER));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }

    public synchronized void sendPrivateMessage(String targetUsername, String message) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equalsIgnoreCase(targetUsername)) {
                client.sendMessage(message);
                break;
            }
        }
    }

    public synchronized void kickUser(String username) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equalsIgnoreCase(username)) {
                client.disconnect();
                System.out.println("Пользователь " + username + " был исключен из чата.");
                break;
            }
        }
    }

    public boolean authenticateUser(String username, String password) {
        return authService.authenticate(username, password);
    }
}
