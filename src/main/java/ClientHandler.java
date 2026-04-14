import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class ClientHandler extends Thread {
    private Socket socket;
    private HashMap<String, ClientHandler> users;
    private Scanner input;
    private PrintStream output;
    private String user;

    public ClientHandler(Socket socket, HashMap<String, ClientHandler> users) {
        this.socket = socket;
        this.users = users;
    }

    @Override
    public void run() {
        try {
            input = new Scanner(socket.getInputStream());
            output = new PrintStream(socket.getOutputStream());
            user = input.nextLine();

            synchronized(users) {
                if(users.containsKey(user)) {
                    output.println("Usuário com nome " + user + " já existe no servidor!");
                    return;
                }

                users.put(user, this);
            }

            output.println("Bem-vindo(a) ao servidor, " + user + "!");
            sendMessageForAllExceptSelf(user + " entrou no servidor!");

            while(input.hasNextLine()) {
                String message = input.nextLine();

                if(message.equalsIgnoreCase("/sair")) {
                    break;
                }

                if(message.equalsIgnoreCase("/users")) {
                    listUsers();
                    continue;
                }

                if(message.toLowerCase().startsWith("/send message")) {
                    sendMessage(message.substring(13).trim());
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void listUsers() {
        output.println("======= USUÁRIOS CONECTADOS =======");

        synchronized (users) {
            for (String user : users.keySet()) {
                output.println("- " + user);
            }
        }
    }

    private void sendMessage(String text) {
        int separatorSpaceIndex = text.indexOf(" ");

        if (separatorSpaceIndex == -1) {
            return;
        }

        String receiverName = text.substring(0, separatorSpaceIndex);
        ClientHandler receiver;

        synchronized (users) {
            receiver = users.get(receiverName);
        }

        if (receiver == null) {
            output.println("Usuário \"" + receiverName + "\" não encontrado no servidor!");
            return;
        }

        try {
            String message = text.substring(separatorSpaceIndex + 1);

            if (message.length() > 0) {
                receiver.getOutput().println(user + ": " + message);
            }
        } catch(StringIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageForAll(String message) {
        synchronized (users) {
            for(String user : users.keySet()) {
                ClientHandler receiver = users.get(user);
                receiver.getOutput().println(message);
            }
        }
    }

    private void sendMessageForAllExceptSelf(String message) {
        synchronized (users) {
            for(String user : users.keySet()) {
                if(!user.equals(this.user)) {
                    ClientHandler receiver = users.get(user);
                    receiver.getOutput().println(message);
                }
            }
        }
    }

    private void closeConnection() {
        output.println("Você saiu do servidor!");

        synchronized (users) {
            users.remove(user);
        }

        sendMessageForAll(user + " saiu do servidor!");

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PrintStream getOutput() {
        return output;
    }

}
