package com.economysim;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class StartController {

    @FXML private Button newGameBtn;
    @FXML private Button loadGameBtn;
    @FXML private Label messageLabel;

    private SaveManager saveManager = new SaveManager();

    @FXML
    public void initialize() {
        if (!saveManager.hasSave()) {
            loadGameBtn.setDisable(true);
            loadGameBtn.setOpacity(0.4);
        }
    }

    @FXML
    private void handleNewGame() {
        loadGame(false);
    }

    @FXML
    private void handleLoadGame() {
        loadGame(true);
    }

    private void loadGame(boolean loadSave) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game.fxml"));
            Scene scene = new Scene(loader.load(), 900, 620);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
//            GameController controller = loader.getController();
//            controller.initGame(loadSave);
            Stage stage = (Stage) newGameBtn.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            messageLabel.setText("Error loading game: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
