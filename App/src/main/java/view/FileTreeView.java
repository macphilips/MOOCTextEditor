package view;

import com.sun.javafx.scene.control.skin.TreeViewSkin;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.io.File;

public class FileTreeView<T extends BaseElement> extends TreeView<T> {

    public void setRootElement(T rootElement) {
        this.rootElement = rootElement;
    }

    private T rootElement;

    private OnSelectionModelUpdated selectionModelUpdated;
    private OnClickListener<T> clickListener;
    private OnDoubleClickListener doubleClickListener;

    public FileTreeView(T rootElement) {
        this.rootElement = rootElement;
        init();
    }


    private void handleMouseClicked(MouseEvent event) {
        Node node = event.getPickResult().getIntersectedNode();
        if (node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null)) {
            final MultipleSelectionModel<TreeItem<T>> selectionModel = getSelectionModel();
            File file = selectionModel.getSelectedItem().getValue().getFile();
            FocusModel<TreeItem<T>> focusModel = getFocusModel();
            int selectedIndex = selectionModel.getSelectedIndex();
            scrollTo(selectedIndex);
            focusModel.focus(selectedIndex);
            if (selectionModelUpdated != null)
                selectionModelUpdated.updated(file);

            if (event.getClickCount() == 2 && doubleClickListener != null) {
                doubleClickListener.onDoubleClick(file);
            } else if (event.getClickCount() == 1 && clickListener != null) {
                clickListener.onClick(selectionModel.getSelectedItem());
            }

        }
    }

    private EventHandler<MouseEvent> eventHandler = this::handleMouseClicked;

    private void init() {

       // addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
        this.setSkin(new FolderTreeViewSkin<T>(this));
        this.setCellFactory(p -> new FileTreeCell(eventHandler));
        this.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                SelectionModel<TreeItem<T>> selectionMode = getSelectionModel();
                TreeItem<T> item = selectionMode.getSelectedItem();
                item.setExpanded(!item.isExpanded());
            }
        });
        buildFileSystemFromRoot();
    }

    private PTreeItem<T> root;

    private void buildFileSystemFromRoot() {
        root = new PTreeItem<>(rootElement);
        root.setExpanded(true);
        setRoot(root);
    }

    public void expandPathTree(String path) {
        boolean shouldExpand = true;//path.charAt(path.length() - 1) == '\\';
        String[] split = path.split("\\\\");
        if (split.length == 1) {
            return;
        }
        TreeItem<T> tmproot = root;
        for (int i = 1, j = 0; i < split.length; i++) {
            String dir = split[i];
            for (TreeItem<T> ff : tmproot.getChildren()) {
                if (ff.getValue().getFile().getName().equalsIgnoreCase(dir)) {
                    MultipleSelectionModel<TreeItem<T>> selectionModel = getSelectionModel();
                    FocusModel<TreeItem<T>> focusModel = getFocusModel();
                    selectionModel.select(ff);
                    int selectedIndex = selectionModel.getSelectedIndex();
                    if (!((FolderTreeViewSkin) getSkin()).isIndexVisible(selectedIndex)) {
                        scrollTo(selectedIndex);
                        focusModel.focus(selectedIndex);
                    }

                    ff.setExpanded(shouldExpand);
                    tmproot = ff;
                }
                j++;
            }
        }
    }

    public void setSelectionModelUpdated(OnSelectionModelUpdated selectionModelUpdated) {
        this.selectionModelUpdated = selectionModelUpdated;
    }

    public void setClickListener(OnClickListener<T> clickListener) {
        this.clickListener = clickListener;
    }

    public void setDoubleClickListener(OnDoubleClickListener doubleClickListener) {
        this.doubleClickListener = doubleClickListener;
    }

    public interface OnClickListener<T> {
        void onClick(TreeItem<T> item);
    }

    public interface OnDoubleClickListener {
        void onDoubleClick(File file);
    }

    public interface OnSelectionModelUpdated {
        void updated(File file);
    }

    public class FileTreeCell extends TreeCell<T> {
        private TextField textField;

        private ContextMenu addMenu = new ContextMenu();
        private EventHandler<? super MouseEvent> mListener;

        public FileTreeCell(EventHandler<? super MouseEvent> listener) {
            this.mListener = listener;
            setOnMouseClicked(mListener);
            geneteContextItem();

            //  addMenuItem.setOnAction((EventHandler) t -> {
            //  TreeItem<.PTreeItemElement> newEmployee = new TreeItem<>(null);
            //  getTreeItem().getChildren().add(newEmployee);
            // });

        }

        private void geneteContextItem() {
            MenuItem addMenuItem1 = new MenuItem("Add Employee");
            addMenuItem1.setOnAction(null);
            MenuItem addMenuItem2 = new MenuItem("Add Employee");
            addMenuItem2.setOnAction(null);
            MenuItem addMenuItem3 = new MenuItem("Add Employee");
            addMenuItem3.setOnAction(null);
            MenuItem addMenuItem4 = new MenuItem("Add Employee");
            addMenuItem4.setOnAction(null);
            SeparatorMenuItem sep1 = new SeparatorMenuItem();
            addMenu.getItems().add(addMenuItem1);
            addMenu.getItems().add(addMenuItem2);
            addMenu.getItems().add(sep1);
            addMenu.getItems().add(addMenuItem3);
            addMenu.getItems().add(addMenuItem4);
        }

        @Override
        public void startEdit() {
            super.startEdit();

            if (textField == null) {
                createTextField();
            }
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem().getFile().getName());
            setGraphic(getTreeItem().getGraphic());
        }

        @Override
        public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(getTreeItem().getGraphic());
                    if (!getTreeItem().isLeaf() && getTreeItem().getParent() != null) {
                        setContextMenu(addMenu);

                    }
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setOnKeyReleased(t -> {
                if (t.getCode() == KeyCode.ENTER) {
                    String text = textField.getText();
                    T PTreeElement = getItem();
                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }

    private class FolderTreeViewSkin<T> extends TreeViewSkin<T> {
        FolderTreeViewSkin(TreeView<T> treeView) {
            super(treeView);
        }

        boolean isIndexVisible(int index) {
            return flow.getFirstVisibleCell() != null &&
                    flow.getLastVisibleCell() != null &&
                    flow.getFirstVisibleCell().getIndex() <= index &&
                    flow.getLastVisibleCell().getIndex() >= index;
        }
    }


}
