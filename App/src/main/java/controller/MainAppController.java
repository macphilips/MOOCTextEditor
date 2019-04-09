package controller;

import application.LaunchClass;
import application.MainApp;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import spelling.AutoComplete;
import spelling.Dictionary;
import spelling.SpellingSuggest;
import view.CodeEditor;
import view.FileTreeView;
import view.PNTreeItemElement;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainAppController {
    public MenuItem file_open;
    public Button run_button;
    public ScrollPane content;
    public ScrollPane leftPane;
    public SplitPane mainContainer;
    private MainApp mainApp;
    private TabPane tabPane;
    private Map<String, Tab> closedTab = new HashMap<>();
    private Map<String, Tab> openTab = new HashMap<>();
    private AutoComplete autoComplete;
    private SpellingSuggest spellingSuggest;
    private Dictionary dic;


    private String projectRoot = "C:\\Users\\tmoro\\IdeaProjects\\MOOCTextEditor\\App\\src\\main\\java";

    @FXML
    public void initialize() {
        ImageView menuIcon = new ImageView(new Image("/img/ic_open_folder.png"));
        menuIcon.setFitHeight(20);
        menuIcon.setFitWidth(20);
        file_open.setGraphic(menuIcon);

        openProjectInExplorer(new File(projectRoot));


        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        content.setContent(tabPane);
        tabPane.prefWidthProperty().bind(content.widthProperty());
        tabPane.prefHeightProperty().bind(content.heightProperty());

        Tab b = new Tab();
        tabPane.getTabs().add(b);
        tabPane.getTabs().removeAll(b);
        LaunchClass launch = new LaunchClass();
        dic = launch.getDictionary();
        autoComplete = launch.getAutoComplete();
        spellingSuggest = launch.getSpellingSuggest(dic);
        textBox = new CodeEditor();

    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void createProject(ActionEvent actionEvent) {

    }

    // UI Controls
    private CodeEditor textBox;

    private void openFile(File file) {
        if (getTab(file) != null) return;
        Tab tab = createTab();
        tab.setText(file.getName());
        textBox = new CodeEditor();
        tab.setContent(textBox);
        tabPane.getSelectionModel().select(tab);
        openTab.put(file.getAbsolutePath(), tab);
        Single.fromCallable(() -> {
            //System.out.println("reading on "+Thread.currentThread().getName());
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[1024];
            int n;
            StringBuilder builder = new StringBuilder();
            while ((n = inputStream.read(buffer)) != -1) {
                builder.append(new String(buffer, 0, n));
            }
            return builder.toString();

        })
                .subscribeOn(Schedulers.computation())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(new SingleObserver<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(String value) {
                        //System.out.println("result "+Thread.currentThread().getName());
                        textBox.clear();
                        textBox.replaceText(0, 0, value);
                        textBox.setAutoComplete(true);
                        tab.setTooltip(new Tooltip(file.getAbsolutePath()));
                        textBox.setStyle("-fx-font-size: 14px");
                        textBox.setWrapText(false);
                        textBox.requestFocus();

                    }

                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                    }
                });


        System.out.println("Done File");

    }

    private Tab getTab(File file) {
        String key = file.getAbsolutePath();
        Tab position = null;
        if (openTab.containsKey(key)) {
            position = openTab.get(key);
            tabPane.getSelectionModel().select(position);
        } else if (closedTab.containsKey(key)) {
            position = closedTab.get(key);
            tabPane.getTabs().add(position);
            tabPane.getSelectionModel().select(position);
            openTab.put(key, closedTab.remove(key));
        }
        return position;
    }

    private Tab createTab() {

        System.out.println("Creating tab");
        Tab tab = new Tab();
        tab.setOnCloseRequest(event -> {
            Object source = event.getSource();
            if (source instanceof Tab) {
                Tab t = (Tab) source;
                String key = t.getTooltip().getText();
                closedTab.put(key, openTab.remove(key));
            }
        });
        tabPane.getTabs().add(tab);
        return tab;
    }

    private FileTreeView<PNTreeItemElement> treeView;

    public void openProject(ActionEvent actionEvent) {
        mainApp.showProjectChooserDialog();
    }

    public void openProjectInExplorer(File file) {
        System.out.println();
        File dir = file;
        if (file.isFile()) dir = file.getParentFile();
        PNTreeItemElement f = new PNTreeItemElement(dir);
        treeView = new FileTreeView<>(f);

        leftPane.setContent(treeView);
        treeView.prefWidthProperty().bind(leftPane.widthProperty());
        treeView.prefHeightProperty().bind(leftPane.heightProperty());
        System.out.println("Setting Double Click listener");
        treeView.setDoubleClickListener(doubleClickListener);

        Point2D boundsInScene = treeView.localToScene((Point2D.ZERO));
        System.out.printf("%f %f", boundsInScene.getX(), boundsInScene.getY());

        coord();


    }

    private void coord() {
        final Scene scene = run_button.getScene();
        if (scene == null) return;
        final Point2D windowCoord = new Point2D(scene.getWindow().getX(), scene.getWindow().getY());

        final Point2D sceneCoord = new Point2D(scene.getX(), scene.getY());
        final Point2D nodeCoord = run_button.localToScene(0.0, 0.0);
        final double clickX = Math.round(windowCoord.getX() + sceneCoord.getX() + nodeCoord.getX());

        final double clickY = Math.round(windowCoord.getY() + sceneCoord.getY() + nodeCoord.getY());
        System.out.printf("X => %f, Y => %f", clickX, clickY);
    }

    private FileTreeView.OnDoubleClickListener doubleClickListener = file -> {
        System.out.println("onDouble Click");
        if (file.isFile()) openFile(file);
    };

    public void createJavaFile(ActionEvent actionEvent) {
        File file = new File(projectRoot, "Test.java");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        openFile(file);
    }
}

