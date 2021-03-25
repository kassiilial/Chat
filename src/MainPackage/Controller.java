package MainPackage;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class Controller {

    public TextArea chatArea;
    public TextField inputArea;

    public void functionNotFound(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Function is in processing");
        alert.showAndWait();
    }

    public void sendMessage(ActionEvent actionEvent) {
        String msg = inputArea.getText() + "\n";
        chatArea.appendText(msg);
        inputArea.clear();
        inputArea.requestFocus();
    }
}
