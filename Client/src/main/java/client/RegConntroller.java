package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RegConntroller {
    private Controller controller;

    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField nickNameField;
    @FXML
    private TextArea textArea;

    public void tryToGer(ActionEvent actionEvent) {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        String nickname = nickNameField.getText().trim();

        controller.registration(login, password, nickname);
    }

    public void showResult(String result) {
        if (result.startsWith("/reg_ok")){
            textArea.appendText("Успешная регистрация\n");
        }else {
            textArea.appendText("Неудачная регистрация " +
                    "\n(Логин или никнейм используются)\n");
        }
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}
