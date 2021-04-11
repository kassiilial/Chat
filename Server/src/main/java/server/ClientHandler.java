package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLOutput;

public class ClientHandler {

    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String nickName;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(()->{
                try {
                    socket.setSoTimeout(120000);
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            out.writeUTF("/end");
                            throw new RuntimeException("Клиент отключается");
                        }
                        // Аутентификация
                        if (str.startsWith("/auth")) {
                            String[] token = str.split("\\s+", 3);
                            if (token.length < 3) {
                                continue;
                            }
                            String newNick = server
                                    .getAutService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            if (newNick != null) {
                                login = token[1];
                                if (!server.isLoginAuth(login)) {
                                    nickName = newNick;
                                    sendMessage("/auth_ok " + nickName);
                                    server.subscribe(this);
                                    System.out.println("Client authenticated. nick: " + nickName +
                                            "Address: " + socket.getRemoteSocketAddress());
                                    break;
                                } else {
                                    sendMessage("Авторизация этим пользователем уже произведена");
                                }

                            } else {
                                sendMessage("Неверный логин/пароль");
                            }
                        }
                        //Регистрация
                        if (str.startsWith("/reg")) {

                            String[] token = str.split("\\s+", 4);
                            if (token.length < 4) {
                                continue;
                            }
                            boolean b = server.getAutService().registration(token[1], token[2], token[3]);
                            if (b) {
                                sendMessage("/reg_ok");
                            } else {
                                sendMessage("/reg_no");
                            }
                        }

                    }
                    socket.setSoTimeout(0);
                    //цикл работы
                    while (true) {
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            out.writeUTF("/end");
                            break;
                        }
                        if (str.startsWith("/w")) {
                            server.privateMessage(this, str);

                        } else {
                            server.broadCastMessage(this, str);
                        }
                    }
                } catch (SocketTimeoutException e){
                    try {
                        out.writeUTF("/end");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                }catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    server.unsubscribe(this);
                    System.out.println("Client disconnect " +socket.getRemoteSocketAddress());
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

    public String getLogin() {
        return login;
    }

    public void sendMessage(String msg){
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
