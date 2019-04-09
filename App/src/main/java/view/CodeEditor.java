package view;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.RichTextChange;
import org.fxmisc.richtext.StyleSpans;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class CodeEditor extends CodeArea {

    public void setAutoComplete(boolean autoComplete) {
        this.autoComplete = autoComplete;
    }

    private boolean autoComplete = false;
    private boolean addClosingBrace = false;


    private LangType langType;
    private LangType h = null;
    private ExecutorService executor;

    public CodeEditor() {
        this(new JavaLangType());
    }

    public CodeEditor(LangType type) {
        this.langType = type;
        init();

    }

    private PublishSubject<RichTextChange<Collection<String>>> highlightSubject = PublishSubject.create();

    private void createObservable() {
        highlightSubject
                .debounce(400, TimeUnit.MILLISECONDS)
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .map(this::highlight)
                .subscribeOn(Schedulers.computation())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(new Observer<StyleSpans<Collection<String>>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(StyleSpans<Collection<String>> styleSpans) {

                        applyHighlighting(styleSpans);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private boolean shouldCloseBrace = false;
    private int parenCounter = 0;
    private int angbraCounter = 0;
    private int sqCounter = 0;
    private int dqCounter = 0;
    private String newline = "\r\n";

    private void init() {
        setParagraphGraphicFactory(LineNumberFactory.get(this));
        createObservable();
        richChanges().subscribe(collectionRichTextChange -> {
            highlightSubject.onNext(collectionRichTextChange);
        });

        final KeyCombination saveShortcut = new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN);


        currentParagraphProperty().addListener((observable, oldValue, newValue) -> System.out.println("paragraph changed " + oldValue + " " + newValue + " " + getCaretPosition()));
        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            int currpos = getCaretPosition();

            escapeClosingBrace(currpos);
            escapeAngleBracket(currpos);
            escapeQuoteD(currpos);
            escapeQuote(currpos);
            escapeClosingParen(currpos);

        });
        addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            int currpos = getCaretPosition();
            if (event.getCode() == KeyCode.ENTER && shouldCloseBrace) {
                System.out.println("Enter pressed");
                System.out.println("caret " + currpos);
                int level = getIndentLevel(currpos);
                System.out.println("level " + level);
                insertText(currpos,
                        newline + getSpace(level - 1) + "}");
                insertText(currpos, getSpace(level));
                shouldCloseBrace = false;

            }


        });
        //caretColumnProperty().addListener((observable, oldValue, newValue) -> System.out.println("Caret position => " + newValue + " " + oldValue));
        addEventHandler(KeyEvent.KEY_TYPED, event -> {
            int currpos = getCaretPosition();
            System.out.println("caret => " + currpos + " character => " + event.toString());
            if (event.getCharacter().equals("{")) {
                shouldCloseBrace = true;
            }
            if (event.getCharacter().equals("}")) {
                shouldCloseBrace = false;
            }
            if (event.getCharacter().equals("(")) {
                insertText(currpos, ")");
                replaceText(currpos, currpos, "");
            }
            if (event.getCharacter().equals("[")) {
                insertText(currpos, "]");
                replaceText(currpos, currpos, "");
            }
            if (event.getCharacter().equals("\"")) {
                insertText(currpos, "\"");
                replaceText(currpos, currpos, "");
            }
            if (event.getCharacter().equals("\'")) {
                insertText(currpos, "\'");
                replaceText(currpos, currpos, "");
            }
            if (event.getCharacter().equals("<")) {
                insertText(currpos, ">");
                replaceText(currpos, currpos, "");
            }


        });
    }

    private void escapeAngleBracket(int currpos) {
        if (currpos < getLength()) {
            char c = getText().charAt(currpos);
            if (c == '>') {
                deleteNextChar();
            }
        }
    }

    private void escapeQuoteD(int currpos) {
        if (currpos < getLength()) {
            char c = getText().charAt(currpos);
            if (c == '\"') {
                deleteNextChar();
                deleteNextChar();
            }
        }
    }

    private void escapeQuote(int currpos) {
        if (currpos < getLength()) {
            char c = getText().charAt(currpos);
            System.out.println("CHARACTER => "+c);
            if (c == '\'') {
                deleteNextChar();
                deletePreviousChar();
            }
        }
    }

    private void escapeClosingParen(int currpos) {
        if (currpos < getLength()) {
            char c = getText().charAt(currpos);
            if (c == ')') {
                deleteNextChar();
                deletePreviousChar();
            }
        }
    }

    private void escapeClosingBrace(int currpos) {
        if (currpos < getLength()) {
            char c = getText().charAt(currpos);
            if (c == ']') {
                deleteNextChar();
            }
        }
    }

    private String getSpace(int level) {
        StringBuilder buildSpace = new StringBuilder();
        for (int i = 0; i < level; i++) {
            buildSpace.append("    ");
        }
        return buildSpace.toString();
    }


    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        setStyleSpans(0, highlighting);
    }

    private StyleSpans<Collection<String>> highlight(RichTextChange<Collection<String>> s) {
        String text = getText();
        return langType.computeHighlighting(text);
    }

    private int getIndentLevel(int pos) {
        String text = this.getText().substring(0, pos);
        int level = 0;
        for (char c : text.toCharArray()) {
            if (c == '{') {
                level++;
            } else if (c == '}') {
                level--;
            }
        }
        return level;
    }

}
