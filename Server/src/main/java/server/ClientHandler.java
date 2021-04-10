package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String nickName;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(()->{
                try{

                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            out.writeUTF("/end");
                            break;
                        }

                        if (str.startsWith("/auth")){
                            String[] token = str.split("\\s+");
                            String newNick = server
                                    .getAutService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            if (newNick!=null){
                                nickName = newNick;
                                sendMessage("/auth_ok " + nickName);
                                server.subscribe(this);
                                System.out.println("Client authenticated. nick: " +nickName+
                                        "Address: "+socket.getRemoteSocketAddress());
                                break;
                            }else {
                                sendMessage("Неверный логин/пароль");
                            }
                        }

                    }

                    //цикл работы
                    while (true){
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            out.writeUTF("/end");
                            break;
                        }
                        if (str.startsWith("/w")) {
                            server.privateMessage(this, str);

                        } else {
                        server.broadCastMessage(this,str);}
                    }

                }catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    server.unsubscribe(this);
                    System.out.println("Client disconnect" +socket.getRemoteSocketAddress());
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickName() {
        return nickName;
    }

    public void sendMessage(String msg){
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
