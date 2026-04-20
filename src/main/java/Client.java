import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Informe seu nome: ");
        String user = scanner.nextLine();

        Socket socket = new Socket("localhost", 3000);
        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());

        output.writeUTF(user);

        Thread receiver = new Thread(() -> {
            while (true) {
                try {
                    String message = input.readUTF();

                    if (message.equalsIgnoreCase("/send file")) {
                        receiveFile(input);
                    } else {
                        System.out.println(message);
                    }
                } catch(EOFException e) {
                    System.out.println("Conexão encerrada pelo servidor.");
                    System.exit(0);
                } catch(IOException e) {
                    if(!socket.isClosed()) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        });

        receiver.start();

        while(true) {
            String message = scanner.nextLine();

            if(message.equalsIgnoreCase("/sair")) {
                output.writeUTF(message);
                System.out.println("Você saiu do servidor!");
                break;
            }

            handleCommand(message, output);
        }

        socket.close();
        scanner.close();
    }

    private static void sendFile(DataOutputStream output, String receiver, String path) throws IOException {
        File file = new File(path);

        if (!file.exists()) {
            System.out.println("Arquivo não encontrado!");
            return;
        }

        output.writeUTF("/send file");
        output.writeUTF(receiver);
        output.writeUTF(file.getName());
        output.writeLong(file.length());

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = fis.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }

        output.flush();
        fis.close();
    }

    private static void receiveFile(DataInputStream input) throws IOException {
        String sender = input.readUTF();
        String fileName = input.readUTF();
        long size = input.readLong();

        FileOutputStream fos = new FileOutputStream(fileName);

        byte[] buffer = new byte[4096];
        int bytesRead;
        long remaining = size;

        while (remaining > 0 && (bytesRead = input.read(buffer, 0, (int)Math.min(buffer.length, remaining))) != -1) {
            fos.write(buffer, 0, bytesRead);
            remaining -= bytesRead;
        }

        fos.close();

        System.out.println("Arquivo recebido de " + sender + ": " + fileName);
    }

    private static void handleCommand(String message, DataOutputStream output) throws IOException {
        if(message.toLowerCase().startsWith("/send file")) {
            String[] parts = message.split(" ", 4);

            if(parts.length < 4) {
                System.out.println("Para enviar arquivos, use o comando: /send file <destinatario> <caminho do arquivo>");
                return;
            }

            sendFile(output, parts[2], parts[3]);
        } else {
            output.writeUTF(message);
        }
    }
}
