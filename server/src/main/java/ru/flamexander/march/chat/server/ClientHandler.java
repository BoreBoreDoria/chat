package ru.flamexander.march.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;
    private UserRole role;

    private static int usersCounter = 0;

    private void generateUsername() {
        usersCounter++;
        this.username = "user" + usersCounter;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public ClientHandler(Server server, Socket socket, UserRole role) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.role = role;
        this.generateUsername();
        new Thread(() -> {
            try {
                // Аутентификация
                while (true) {
                    out.writeUTF("Введите имя пользователя:");
                    String username = in.readUTF();
                    out.writeUTF("Введите пароль:");
                    String password = in.readUTF();
                    if (server.authenticateUser(username, password)) {
                        this.username = username;
                        this.role = UserRole.USER; // Пример, нужно реализовать получение роли из БД
                        break;
                    } else {
                        out.writeUTF("Неверное имя пользователя или пароль. Попробуйте снова.");
                    }
                }
                System.out.println("Подключился новый клиент");
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/")) {
                        if (msg.startsWith("/exit")) {
                            disconnect();
                            break;
                        }
                        if (msg.startsWith("/w ")) {
                            String[] tokens = msg.split(" ", 3);
                            if (tokens.length == 3) {
                                String targetUsername = tokens[1];
                                String message = tokens[2];
                                server.sendPrivateMessage(targetUsername, username + ": " + message);
                            }
                            continue;
                        }
                        if (msg.startsWith("/kick ") && isAdmin()) {
                            String[] tokens = msg.split(" ", 2);
                            if (tokens.length == 2) {
                                String targetUsername = tokens[1];
                                server.kickUser(targetUsername);
                            }
                            continue;
                        }
                        continue;
                    }
                    server.broadcastMessage(username + ": " + msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
