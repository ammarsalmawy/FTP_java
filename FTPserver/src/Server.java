import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    ServerSocket server = null;
    Socket client = null;

    public static void main(String[] arg) {
        Server server = new Server();
        // run the server
        server.establishConnection();
        
    }
    public void establishConnection() {
        try {
            server = new ServerSocket(1234);
            System.out.println("Server is running...");
            System.out.println("Waiting for clients");
            // keep the server running to handle clients
            while (true) {
                client = server.accept();
                // prints clients information
                SocketAddress clientAddress = client.getRemoteSocketAddress();
                System.out.println("Client connected from: " + clientAddress);
                // authenticate users and create a new thread
                if (authenticateClient(client)) {
                    ClientThread ct = new ClientThread(client);
                    ct.start();
                } else {
                    //  if clients are not authenticated deny access
                    System.out.println("Access denied");
                    client.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private boolean authenticateClient(Socket client) {
        try {
            DataInputStream in = new DataInputStream(client.getInputStream());
            DataOutputStream out = new DataOutputStream(client.getOutputStream());

            // ask clients for username and password and get the responses
            out.writeUTF("Please enter your username:");
            String username = in.readUTF();

            out.writeUTF("Please enter your password:");
            String password = in.readUTF();


            // validate the credentials
            if (isValidCredentials(username, password)) {
                return true;
            }
            else {
                out.writeUTF("user name or password are incorrect");
            }
        } catch (IOException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return false;
    }
    // check if username and password matches the data in the credentials file
    private boolean isValidCredentials(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader("credentials.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String storedUsername = parts[0];
                    String storedPassword = parts[1];
                    if (username.equals(storedUsername) && password.equals(storedPassword)) {

                        return true; // Username and password match
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading credentials file: " + e.getMessage());
        }
        return false;
    }
}
