package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
    @FXML
    public HBox listAndFilesPanel;
    @FXML
    public ListView<String> clientList;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private boolean authenticated;
    private String nickName;

    private Stage stage;
    private Stage regStage;
    private RegConntroller regConntroller;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);

        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);

        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);

        listAndFilesPanel.setVisible(authenticated);
        listAndFilesPanel.setManaged(authenticated);

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
            stage.setOnCloseRequest(event -> {
                System.out.println("Bye");
                if (socket!=null && !socket.isClosed()){
                    try {
                        out.writeUTF("/end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
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
                            if (str.startsWith("/reg_ok")){
                                regConntroller.showResult("/reg_ok");
                            }
                            if (str.startsWith("/reg_no")){
                                regConntroller.showResult("/reg_no");
                            }
                        }
                        else { chatArea.appendText(str+"\n");}
                    }

                    //цикл работы
                    while (authenticated){
                        String str = in.readUTF();
                       if (str.startsWith("/")){
                            if (str.equals("/end")) {
                                System.out.println("Disconnect");
                                break;
                            }
                            /// добавление клиентов в список пользователей
                            if (str.startsWith("/clientlist")) {
                                String[] token = str.split("\\s+");
                                 Platform.runLater(()->{
                                     clientList.getItems().clear();
                                     for (int i = 1; i < token.length; i++) {
                                         clientList.getItems().add(token[i]);
                                     }
                                 });
                            }
                        }
                        else {
                            chatArea.appendText(str+"\n");}
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

    public void clickClients(MouseEvent mouseEvent) {
        String receiver = clientList.getSelectionModel().getSelectedItem();
        inputArea.setText("/w " + receiver + " ");
    }

    private void createRegWindows() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Windows/RegWindow.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("Chatty Registration form");
            regStage.setScene(new Scene(root, 400, 320));

            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.initStyle(StageStyle.UTILITY);

            regConntroller = fxmlLoader.getController();
            regConntroller.setController(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg(ActionEvent actionEvent) {
        if (regStage==null) {
            createRegWindows();
        }
        Platform.runLater(()->{
            regStage.show();
        });
    }

    public void registration(String login, String password, String nickname){
        if (socket==null || socket.isClosed()){
            connect();
        }
        String msg = String.format("/reg %s %s %s", login, password, nickname);
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
