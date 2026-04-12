import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private static final int PORT = 3000;

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        Set<String> users = new HashSet<>();

        System.out.println("Servidor iniciado na porta " + PORT);

        while(true) {
            Socket socket = server.accept();
            new ClientHandler(socket, users).start();
        }
    }
}
