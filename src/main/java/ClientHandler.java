import java.net.Socket;
import java.util.HashMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.EOFException;

public class ClientHandler extends Thread {
    private Socket socket;
    private HashMap<String, ClientHandler> users;
    private DataInputStream input;
    private DataOutputStream output;
    private String user;

    public ClientHandler(Socket socket, HashMap<String, ClientHandler> users) {
        this.socket = socket;
        this.users = users;
    }

    @Override
    public void run() {
        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            user = input.readUTF();

            synchronized(users) {
                if(users.containsKey(user)) {
                    output.writeUTF("Usuário com nome " + user + " já existe no servidor!");
                    return;
                }

                users.put(user, this);
            }

            output.writeUTF("Bem-vindo(a) ao servidor, " + user + "!");
            sendMessageForAllExceptSelf(user + " entrou no servidor!");

            while(true) {
                String message = input.readUTF();

                if(message.equalsIgnoreCase("/sair")) {
                    break;
                }

                if(message.equalsIgnoreCase("/users")) {
                    listUsers();
                    continue;
                }

                if(message.toLowerCase().startsWith("/send message")) {
                    sendMessage(message.substring(13).trim());
                    continue;
                }

                if(message.equalsIgnoreCase("/send file")) {
                    sendFile();
                }
            }
        } catch(EOFException ignore) {

        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void listUsers() throws IOException {
        output.writeUTF("======= USUÁRIOS CONECTADOS =======");

        synchronized (users) {
            for (String user : users.keySet()) {
                output.writeUTF("- " + user);
            }
        }
    }

    private void sendMessage(String text) throws IOException {
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
            output.writeUTF("Usuário \"" + receiverName + "\" não encontrado no servidor!");
            return;
        }

        try {
            String message = text.substring(separatorSpaceIndex + 1);

            if (message.length() > 0) {
                receiver.getOutput().writeUTF(user + ": " + message);
            }
        } catch(StringIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void sendFile() throws IOException {
        String receiverName = input.readUTF();
        String fileName = input.readUTF();
        long size = input.readLong();

        ClientHandler receiver;

        synchronized (users) {
            receiver = users.get(receiverName);
        }

        if (receiver == null) {
            output.writeUTF("Usuário \"" + receiverName + "\" não encontrado no servidor!");
            skipBytes(size);
            return;
        }

        DataOutputStream receiverOutput = receiver.getOutput();

        synchronized (receiverOutput) {
            receiverOutput.writeUTF("/send file");
            receiverOutput.writeUTF(this.user);
            receiverOutput.writeUTF(fileName);
            receiverOutput.writeLong(size);

            byte[] buffer = new byte[4096];
            int bytesRead;
            long remaining = size;

            while (remaining > 0 && (bytesRead = input.read(buffer, 0, (int)Math.min(buffer.length, remaining))) != -1) {
                receiverOutput.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
            receiverOutput.flush();
        }
    }

    private void skipBytes(long size) throws IOException {
        byte[] buffer = new byte[4096];
        long remaining = size;
        while (remaining > 0) {
            int toRead = (int) Math.min(buffer.length, remaining);
            int read = input.read(buffer, 0, toRead);
            if (read == -1) break;
            remaining -= read;
        }
    }

    private void sendMessageForAll(String message) throws IOException {
        synchronized (users) {
            for(ClientHandler receiver : users.values()) {
                receiver.getOutput().writeUTF(message);
            }
        }
    }

    private void sendMessageForAllExceptSelf(String message) throws IOException {
        synchronized (users) {
            for(ClientHandler receiver : users.values()) {
                if(receiver != this) {
                    receiver.getOutput().writeUTF(message);
                }
            }
        }
    }

    private void closeConnection() {
        try {
            synchronized (users) {
                if(users.get(user) == this) {
                    users.remove(user);
                    sendMessageForAll(user + " saiu do servidor!");
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DataOutputStream getOutput() {
        return output;
    }

}
