package view;

import java.io.File;

public abstract class BaseElement {
    private File file;

    public BaseElement(File file) {
        this.file = file;
    }

    public File getFile() {
        return this.file;
    }

    @Override
    public String toString() {
        return this.file.getName();
    }

    protected void setFile(File file) {
        this.file = file;
    }
}
