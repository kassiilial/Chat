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



    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    public AutService getAutService() {
        return autService;
    }
}
