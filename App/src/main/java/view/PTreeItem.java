package view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PTreeItem<T extends BaseElement> extends TreeItem<T> {
    private final Class clazz;
    private boolean isLeaf;
    private boolean isFirstTimeChildren = true;
    private boolean isFirstTimeLeaf = true;
    private TextField pathURL;

    public PTreeItem(T f) {
        super(f);
        if (!isLeaf()) {
            ImageView dirIcon = new ImageView(new Image("/img/ic_open_folder.png"));
            dirIcon.setFitHeight(20);
            dirIcon.setFitWidth(20);
            setGraphic(dirIcon);
        }
        this.clazz = f.getClass();
    }

    private PTreeItem<T> createNode(final T f) {
        return new PTreeItem<>(f);
    }

    @Override
    public ObservableList<TreeItem<T>> getChildren() {
        if (isFirstTimeChildren) {
            isFirstTimeChildren = false;

            super.getChildren().setAll(buildChildren(this));
        }
        return super.getChildren();
    }

    @Override
    public boolean isLeaf() {
        if (isFirstTimeLeaf) {
            isFirstTimeLeaf = false;
            T f = getValue();
            isLeaf = f.getFile().isFile();
        }

        return isLeaf;
    }

    private ObservableList<TreeItem<T>> buildChildren(TreeItem<T> TreeItem) {
        File f = TreeItem.getValue().getFile();
        if (f != null && f.isDirectory()) {
            // File[] files = f.listFiles();
            List<File> files = getFileList(f);
            if (files != null) {
                try {
                    ObservableList<TreeItem<T>> children = FXCollections.observableArrayList();
                    for (File childFile : files) {
                        Constructor<T> c = clazz.getDeclaredConstructor(File.class);
                        T f1 = c.newInstance(childFile);
                        f1.setFile(childFile);
                        children.add(createNode(f1));
                    }
                    return children;
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    // e.printStackTrace();
                    System.out.println(clazz.getCanonicalName() + " must have a constructor that take in only parameter of type " + File.class.getCanonicalName());
                }
            }
        }

        return FXCollections.emptyObservableList();
    }

    private List<File> getFileList(File path) {
        File[] dirs = (path).listFiles(File::isDirectory);
        File[] files = (path).listFiles(File::isFile);
        List<File> fileList = new ArrayList<>();
        if (dirs != null) {
            fileList.addAll(Arrays.asList(dirs));
        }
        if (files != null) {
            fileList.addAll(Arrays.asList(files));
        }
        return fileList;
    }

}