package controller;

import application.MainApp;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import view.FileTreeView;
import view.PTreeItemElement;

import java.io.File;

public class ProjectChooserController {
    public FileTreeView<PTreeItemElement> treeView;
    public TextField pathURL;
    public VBox treeViewPane;
    private MainApp mainApp;
    private Stage dialogStage;
    private static final String rootPath = "C:\\";

    private FileTreeView.OnSelectionModelUpdated updated = new FileTreeView.OnSelectionModelUpdated() {
        @Override
        public void updated(File file) {
            Platform.runLater(() -> {
                String curr = file.getAbsolutePath();
                pathURL.setText(curr);
            });
        }
    };

    private FileTreeView.OnDoubleClickListener doubleClickListener = file -> {
        if (file.isFile()) handleChosenFile(file);
    };

    @FXML
    public void initialize() {
        setPathBar();
        PTreeItemElement f = new PTreeItemElement(new File(rootPath));
        f.setName(rootPath);
        treeView = new FileTreeView<>(f);
        treeView.setDoubleClickListener(doubleClickListener);
        treeView.setSelectionModelUpdated(updated);
        treeViewPane.getChildren().add(treeView);
    }

    public void onCancelButtonClicked(ActionEvent actionEvent) {
        dialogStage.close();
    }


    private void setPathBar() {
        pathURL.setText(rootPath);
        ChangeListener<String> stringChangeListener = (observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                String text = pathURL.getText();
                File file = new File(text);
                if (file.exists() && file.isDirectory()) {
                    treeView.expandPathTree(text);
                }
            });
        };

        pathURL.setOnKeyPressed(event -> {
            pathURL.textProperty().addListener(stringChangeListener);
        });
        pathURL.setOnKeyReleased(t -> {
            pathURL.textProperty().removeListener(stringChangeListener);
            if (t.getCode() == KeyCode.ENTER) {
                Platform.runLater(() -> {
                    File file = new File(pathURL.getText());
                    if (file.exists() && file.isDirectory()) {
                        handleChosenFile(file);
                    }
                });
            }
        });
    }


    public void onOKButtonClicked(ActionEvent actionEvent) {
        File file = new File(pathURL.getText());
        handleChosenFile(file);
    }

    private void handleChosenFile(File file) {
        if (file.exists()) {
            dialogStage.close();
            mainApp.getController().openProjectInExplorer(file);
        }
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }


}
