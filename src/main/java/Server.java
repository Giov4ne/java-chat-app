import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class Server {
    private static final int PORT = 3000;

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        HashMap<String, ClientHandler> users = new HashMap<>();

        System.out.println("Servidor iniciado na porta " + PORT);

        while(true) {
            Socket socket = server.accept();
            new ClientHandler(socket, users).start();
            logClientConnection(socket);
        }
    }

    private static void logClientConnection(Socket socket) throws IOException {
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDate = now.format(formatter);

        String connectionStr = socket.getInetAddress().getHostAddress() + " - " + formattedDate;

        BufferedWriter writer = new BufferedWriter(new FileWriter("logs/client_connections_log.txt", true));
        writer.write(connectionStr);
        writer.newLine();
        writer.close();
    }
}
