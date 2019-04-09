package view;

import java.io.File;

public class PTreeItemElement extends BaseElement {
    protected File file;
    protected String name;

    public PTreeItemElement() {
        super(null);
    }

    public PTreeItemElement(File file) {
        super(file);
        this.file = file;
        name = file.getName();
    }

    @Override
    public String toString() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public boolean renameFile(String text) {
        return file.renameTo(new File(file.getParent(), text));
    }

    public String getName() {
        return name;
    }
}
