package view;

import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

public class PTreeCellImpl extends TreeCell<PTreeItemElement> {
    //private TextField pathURL;
    private TextField textField;

    private ContextMenu addMenu = new ContextMenu();
    private EventHandler<? super MouseEvent> mListener;

    public PTreeCellImpl(EventHandler<? super MouseEvent> listener) {
        //this.pathURL = pathURL;
        this.mListener = listener;
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
    public void updateItem(PTreeItemElement item, boolean empty) {
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
                    setOnMouseClicked(mListener);

                }
            }
        }
    }

    private void createTextField() {
        textField = new TextField(getString());
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                String text = textField.getText();
                PTreeItemElement PTreeElement = getItem();
                if (PTreeElement.renameFile(text)) {
                    PTreeElement.setName(text);
                    commitEdit(PTreeElement);
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
    }

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }
}
