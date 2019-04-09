package demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class TextHighlight extends Application {
    public static void main(String[] args) { Application.launch(args); }
    @Override public void start(Stage stage) {
        final TextArea text = new TextArea("Here is some textz to hypelink");
        text.setStyle("-fx-highlight-fill: lightgray; -fx-highlight-text-fill: firebrick; -fx-font-size: 20px;");
        //text.setEditable(false);

        stage.setScene(new Scene(text));
        stage.show();

        Platform.runLater(() -> text.selectRange(13, 18));
    }
}