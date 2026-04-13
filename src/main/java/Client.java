import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Informe seu nome: ");
        String user = scanner.nextLine();

        Socket socket = new Socket("localhost", 3000);
        Scanner input = new Scanner(socket.getInputStream());
        PrintStream output = new PrintStream(socket.getOutputStream());

        output.println(user);

        Thread receiver = new Thread(() -> {
            while(input.hasNextLine()) {
                System.out.println(input.nextLine());
            }
        });

        receiver.start();

        while(true) {
            String message = scanner.nextLine();
            output.println(message);

            if(message.equalsIgnoreCase("/sair")) {
                break;
            }
        }

        socket.close();
        scanner.close();
    }
}
