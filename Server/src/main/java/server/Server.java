package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static ServerSocket server;
    private static Socket socket;
    private static final int PORT = 8189;
    private List<ClientHandler> clients;
    private AutService autService;

    public Server() {

        clients = new CopyOnWriteArrayList<>();
        autService = new SimpleAuthService();

        try{
            server = new ServerSocket(PORT);
            System.out.println("Server started");

            while (true){
            socket = server.accept();
            System.out.println("Client connects" + socket.getRemoteSocketAddress());
            new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void broadCastMessage(ClientHandler sender,String msg){
        String message = String.format("%s: %s", sender.getNickName(), msg);
        for (ClientHandler c:
             clients) {
            c.sendMessage(message);
        }
    }

    public void privateMessage(ClientHandler sender, String msg){

        String message = String.format("%s: %s", sender.getNickName(), msg.split("\\s+", 3)[2]);
        sender.sendMessage(message);
        for (ClientHandler c:
                clients) {
            if (c.getNickName().equals(msg.split("\\s+",3)[1])) {
                if (sender.equals(c)) {return;}
                c.sendMessage(message);
                return;
            }
        }
        sender.sendMessage("User not found");
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public AutService getAutService() {
        return autService;
    }

    public boolean isLoginAuth(String login) {
        for (ClientHandler c:
             clients) {
            if (c.getLogin().equals(login)){
                return true;
            }
        }
        return false;
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clientlist");
        for (ClientHandler c:
                clients) {
            sb.append(" ").append(c.getNickName());
            }
        String mg = sb.toString();
        for (ClientHandler c:
                clients) {
            c.sendMessage(mg);
        }
    }
}
