package view;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Extractor {


    private String text;
    private int length;
    private int startIndex;
    private int endIndex;

    public Extractor() {
    }

    public Extractor(String text) {
        this.text = text;
    }

    public static void main(String args[]) {
        String file = "C:\\Users\\tmoro\\IdeaProjects\\MOOCTextEditor\\App\\src\\main\\java\\view\\TestCase.txt";
        StringBuilder builder = new StringBuilder();
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(file)));
            int n;
            byte[] bytes = new byte[1024];
            while ((n = bis.read(bytes)) != -1) {
                builder.append(new String(bytes, 0, n));
            }
            String code = builder.toString();

            int pos = 43;
            Extractor ex = new Extractor(code);
            System.out.println("Line => (" + ex.getLine(13) + ")");
            System.out.println("Line => (" + ex.getLine(28) + ")");
            System.out.println("Line => (" + ex.getLine(43) + ")");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getLine(int pos) {
        String text = this.getText().substring(0, pos);
        // keeping track of index
        int index;

        // get first whitespace "behind caret"
        boolean found = false;
        for (index = text.length() - 1; index >= 2; index--) {
            if ((text.charAt(index - 1) == '\r' && text.charAt(index) == '\n')
                    || (text.charAt(index - 1) == '\n' && text.charAt(index) == '\r')) {
                found = true;
                break;
            }
        }
        if (!found) startIndex = --index;
        else
            startIndex = ++index;

        String prefix = text.substring(startIndex, text.length());
        // get first whitespace forward from caret
        found = false;
        for (index = pos; index < this.getLength() - 2; index++) {
            if ((this.getText().charAt(index) == '\r' && this.getText().charAt(index + 1) == '\n') || (this.getText().charAt(index) == '\n' && this.getText().charAt(index + 1) == '\r')) {
                found = true;
                break;
            }
        }

        if (!found)
            endIndex = index + 2;
        else
            endIndex = index;

        String suffix = this.getText().substring(pos, endIndex);

        // replace regex wildcards (literal ".") with "\.". Looks weird but
        // correct...
        prefix = prefix.replaceAll("\\.", "\\.");
        suffix = suffix.replaceAll("\\.", "\\.");

        // combine both parts of words
        prefix = prefix + suffix;

        // return current word being typed
        return prefix;

    }

    public String getText() {
        return text;
    }


    public int getLength() {
        return text.length();
    }

}
