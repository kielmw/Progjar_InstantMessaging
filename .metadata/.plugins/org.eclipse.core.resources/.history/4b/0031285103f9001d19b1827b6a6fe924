package main;

import java.io.*;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.*;

public class Server {
    private static final int PORT = 5000;
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try {
            // Set up SSL context
            char[] keystorePassword = "123456".toCharArray();
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("keystore.jks"), keystorePassword);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keystorePassword);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            // Create SSL server socket
            SSLServerSocketFactory socketFactory = sslContext.getServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) socketFactory.createServerSocket(PORT);

            System.out.println("Server started on port " + PORT);

            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(Message message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static List<String> getOnlineUsers() {
        List<String> onlineUsers = new ArrayList<>();
        for (ClientHandler client : clients) {
            onlineUsers.add(client.getUsername());
        }
        return onlineUsers;
    }

    public static void sendPrivateMessage(String sender, String receiver, Message message) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(receiver)) {
                client.sendMessage(message);
                break;
            }
        }
    }
}

class ClientHandler extends Thread {
    private SSLSocket clientSocket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private String username;

    public ClientHandler(SSLSocket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try {
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            username = (String) objectInputStream.readObject();
            System.out.println("User " + username + " joined the chat.");

            Message message;
            do {
                message = (Message) objectInputStream.readObject();

                if (message.getReceiver() == null) {
                    // Broadcast message
                    Server.broadcast(message);
                } else if (message.getReceiver().equals("server")) {
                    // Get online users
                    List<String> onlineUsers = Server.getOnlineUsers();
                    Message onlineUsersMessage = new Message("server", onlineUsers.toString());
                    sendMessage(onlineUsersMessage);
                } else {
                    // Private message
                    Server.sendPrivateMessage(username, message.getReceiver(), message);
                }

            } while (!message.getContent().equals("bye"));

            System.out.println("User " + username + " left the chat.");

            objectOutputStream.close();
            objectInputStream.close();
            clientSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Message message) {
        try {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }
}
