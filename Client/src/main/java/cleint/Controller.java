package cleint;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public TextArea chatArea;
    @FXML
    public TextField inputArea;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox authPanel;
    @FXML
    public HBox msgPanel;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private boolean authenticated;
    private String nickName;

    private Stage stage;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);

        if (!authenticated){
            nickName = "";
        }
        serTitle(nickName);
        chatArea.clear();
    }

    @FXML
    public void functionNotFound(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Function is in processing");
        alert.showAndWait();
    }

    @FXML
    public void sendMessage(ActionEvent actionEvent) {
        try {
            out.writeUTF(inputArea.getText());
            inputArea.clear();
            inputArea.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(()->{
            stage = (Stage) chatArea.getScene().getWindow();
        });
        setAuthenticated(false);
    }

    private void connect(){
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(()->{
                try{
                    //цикл авторизации
                    while (true){
                        String str = in.readUTF();

                        if (str.startsWith("/")){
                            if (str.equals("/end")) {
                                System.out.println("Disconnect");
                                break;
                            }
                            if (str.startsWith("/auth_ok")){
                                nickName =str.split("\\s+")[1];
                                setAuthenticated(true);
                                break;
                            }
                        }
                        else { chatArea.appendText(str+"\n");}
                    }

                    //цикл работы
                    while (authenticated){
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            System.out.println("Disconnect");
                            break;
                        }
                        chatArea.appendText(str+"\n");
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    setAuthenticated(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void tryToAuth(ActionEvent actionEvent) {
        if (socket==null || socket.isClosed()){
            connect();
        }
        String msg = String.format("/auth %s %s",
                loginField.getText().trim(), passwordField.getText().trim());

        try {
            out.writeUTF(msg);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void serTitle(String nickName){
        Platform.runLater(()-> {
            if (nickName.equals("")) {
                stage.setTitle("Chatty");}
            else {
                stage.setTitle(String.format("Chatty: [ %s ]", nickName));
            }
                });
    }

}
