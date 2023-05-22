package Master;

import java.io.*;
import java.security.KeyStore;
import javax.net.ssl.*;


public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        try {
            // Set up SSL context
            char[] keystorePassword = "password".toCharArray();
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("keystore.jks"), keystorePassword);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            // Create SSL socket
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            SSLSocket socket = (SSLSocket) socketFactory.createSocket(SERVER_IP, SERVER_PORT);

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter your username: ");
            String username = reader.readLine();

            objectOutputStream.writeObject(username);
            objectOutputStream.flush();

            Thread inputThread = new Thread(() -> {
                try {
                    while (true) {
                        String input = reader.readLine();
                        String[] inputParts = input.split(" ", 2);
                        String command = inputParts[0].toLowerCase();

                        if (command.equals("list")) {
                            // Request online users
                            Message requestUsersMessage = new Message(username, "server");
                            objectOutputStream.writeObject(requestUsersMessage);
                            objectOutputStream.flush();
                        } else if (command.equals("private")) {
                            // Send private message
                            if (inputParts.length != 3) {
                                System.out.println("Invalid format. Usage: private <receiver> <message>");
                            } else {
                                String receiver = inputParts[1];
                                String content = inputParts[2];
                                Message privateMessage = new Message(username, receiver, content);
                                objectOutputStream.writeObject(privateMessage);
                                objectOutputStream.flush();
                            }
                        } else {
                            // Broadcast message
                            Message broadcastMessage = new Message(username, null, input);
                            objectOutputStream.writeObject(broadcastMessage);
                            objectOutputStream.flush();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            inputThread.start();

            Message message;
            while ((message = (Message) objectInputStream.readObject()) != null) {
                if (message.getSender().equals("server")) {
                    // Online users response
                    System.out.println("Online users: " + message.getContent());
                } else {
                    // Message from another user
                    System.out.println(message.getSender() + ": " + message.getContent());
                }
            }

            objectInputStream.close();
            objectOutputStream.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}