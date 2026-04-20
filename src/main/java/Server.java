import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

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
        File logDir = new File("logs");

        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDate = now.format(formatter);
        String connectionStr = socket.getInetAddress().getHostAddress() + " - " + formattedDate;
        File logFile = new File(logDir, "client_connections_log.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));

        writer.write(connectionStr);
        writer.newLine();
        writer.close();
    }
}
