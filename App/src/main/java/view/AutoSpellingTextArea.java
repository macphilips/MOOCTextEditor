package view;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import org.fxmisc.richtext.PopupAlignment;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;
import org.fxmisc.richtext.StyledTextArea;
import spelling.AutoComplete;
import spelling.Dictionary;
import spelling.SpellingSuggest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoSpellingTextArea extends StyledTextArea<AutoSpellingTextArea.TextStyleInfo>  {

    private static final int NUM_COMPLETIONS = 6;
    private static final int NUM_SUGGESTIONS = 6;

    // track auto complete and suggestion states
    private boolean autoCompleteOn = false;
    private boolean suggest = false;

    // boolean to see if style needs update on plain text change
    private boolean needUpdate = true;

    // matches case of user typing for auto complete and ss
    // turn off if handling caps
    private boolean matchCase = true;

    // indices which contain word, set by getWordAtIndex
    private int startIndex;
    private int endIndex;

    // popup to display/select entry
    private Popup entriesPopup;

    private AutoComplete ac;
    private Dictionary dic;
    private SpellingSuggest ss;

    // set up reflection for suggest
    private static Method mHit;
    private static Method mGetCharacterIndex;
    private static Object styledView;
    private static Set<String> keywords;

    static {
        try {
            mHit = Class.forName("org.fxmisc.richtext.skin.StyledTextAreaView").getDeclaredMethod("hit", double.class,
                    double.class);
            mGetCharacterIndex = Class.forName("org.fxmisc.richtext.skin.CharacterHit")
                    .getDeclaredMethod("getCharacterIndex");

        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }

        mHit.setAccessible(true);
        mGetCharacterIndex.setAccessible(true);

        keywords = new HashSet<>();
        keywords.add("for");
        keywords.add("public");
        keywords.add("static");
        keywords.add("final");
        keywords.add("class");
        keywords.add("interface");
        keywords.add("enum");
        keywords.add("extends");
        keywords.add("implements");

    }


    public AutoSpellingTextArea(AutoComplete ac, SpellingSuggest ss, Dictionary dic) {
        super(new TextStyleInfo(true, Color.WHITE), (textNode, correct) -> {
            // define boolean Text node style
            if (!correct.valid) {
                textNode.setUnderline(true);
                textNode.setBackgroundFill(correct.color);
            }
            if (correct.valid && correct.keyword) {
                textNode.setFill(correct.color);
            }
        });
        // save objects passed in
        this.ac = ac;
        this.ss = ss;
        this.dic = dic;
        // init();
    }

    private void init() {

        // register mouse click for correcting misspelled words
        this.setOnMouseClicked(e -> {

            if ((e.getButton() == MouseButton.SECONDARY) && suggest) {
                // get StyledTextAreaView object
                styledView = getChildrenUnmodifiable().get(0);

                // get character hit on click and index
                Object chHit = invoke(mHit, styledView, e.getX(), e.getY());
                OptionalInt opInt = (OptionalInt) invoke(mGetCharacterIndex, chHit);

                // valid index clicked
                if (opInt.isPresent()) {
                    int index = opInt.getAsInt();

                    // check if index corresponds to misspelled word
                    if (!getStyleOfChar(index).valid) {
                        String possibleWord = getWordAtIndex(index);
                        showSuggestions(possibleWord, e);
                    }
                }

            }

        });

        // keep track of text changes, update spell check if needed
        this.plainTextChanges().subscribe(change -> {
            // could make more efficient
            if (suggest && needUpdate) {
                this.setStyleSpans(0, checkSpelling());
            }
            System.out.println(getCaretPosition() + " <=> " + change.getPosition());
            mAutoCompleteSubject.onNext(getCaretPosition());
        });

        entriesPopup = new Popup();

        //entriesPopup.getContent().add(scrollPane);
        //popup.show(getScene().getWindow());
        //entriesPopup = new ContextMenu();

        setPopupWindow(entriesPopup);
        setPopupAlignment(PopupAlignment.CARET_BOTTOM);
        createObservables();

///drag and drop

        setOnDragOver(event -> {
            System.out.println("Drag over");
            /* accept it only if it is  not dragged from the same node
             * and if it has a string data */
            if (event.getGestureSource() != AutoSpellingTextArea.this &&
                    event.getDragboard().hasString()) {
                /* allow for both copying and moving, whatever user chooses */
                event.acceptTransferModes(TransferMode.ANY);
                System.out.println(event.getDragboard().getString());
                // position caret at drag coordinates

                styledView = getChildrenUnmodifiable().get(0);

                // get character hit on click and index
                Object chHit = invoke(mHit, styledView, event.getX(), event.getY());
                OptionalInt opInt = (OptionalInt) invoke(mGetCharacterIndex, chHit);

                // valid index clicked
                if (opInt.isPresent()) {
                    int index = opInt.getAsInt();
                    AutoSpellingTextArea.this.positionCaret(index);

                }

            }

            event.consume();
        });


        setOnDragDropped(event -> {
            boolean success = false;
            if (event.getDragboard().hasString()) {
                insertText(getCaretPosition(), event.getDragboard().getString());
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

    }

    /**
     * Gets white space delimited word which contains character at pos in text
     * Also sets startIndex and endIndex instance variables.
     *
     * @param pos - index in text area
     * @return word at index
     */

    private String getWordAtIndex(int pos) {
        String text = this.getText().substring(0, pos);
        //System.out.println(text);
        // keeping track of index
        int index;

        // get first whitespace "behind caret"
        for (index = text.length() - 1; index >= 0 && !Character.isWhitespace(text.charAt(index)); index--) ;

        // get prefix and startIndex of word
        startIndex = index + 1;
        String prefix = text.substring(startIndex, text.length());

        // get first whitespace forward from caret
        for (index = pos; index < this.getLength() && !Character.isWhitespace(this.getText().charAt(index)); index++) ;

        String suffix = this.getText().substring(pos, index);
        endIndex = index;

        // replace regex wildcards (literal ".") with "\.". Looks weird but
        // correct...
        prefix = prefix.replaceAll("\\.", "\\.");
        suffix = suffix.replaceAll("\\.", "\\.");

        // combine both parts of words
        prefix = prefix + suffix;

        // return current word being typed
        return prefix;
    }

    /**
     * Populate the entry set with the options passed in
     *
     * @param options - list of auto complete options
     */
    private Node createOptions(List<String> options, boolean[] flags) {

        // If you'd like more entries, modify this line.
        int count = Math.min(options.size(), NUM_SUGGESTIONS);
        VBox vBox = new VBox();
        // add options to ContextMenu
        for (int i = 0; i < count; i++) {
            String str = options.get(i);

            // check if need to match case (flags will always be null is
            // matchCase is true)
            // see showSuggestions/Completions
            if (flags != null) {
                str = convertCaseUsingFlags(str, flags);
            }

            final String result = str;
            Label entryLabel = new Label(result);
            //CustomMenuItem item = new CustomMenuItem(entryLabel, true);
            // register event where user chooses word (click)
            entryLabel.setOnMouseClicked(actionEvent -> {
                needUpdate = false;
                replaceText(startIndex, endIndex, result);
                getWordAtIndex(startIndex);
                setStyle(startIndex, endIndex, new TextStyleInfo(true, Color.WHITE));
                needUpdate = true;
            });
            vBox.getChildren().add(entryLabel);

            // menuItems.add(item);
        }

        ScrollPane scrollPane = new ScrollPane(vBox);
        scrollPane.setMaxHeight(600);
        scrollPane.setMinHeight(100);
        scrollPane.setMaxWidth(400);
        scrollPane.setMinWidth(400);
        return scrollPane;

    }

    /**
     * Checks of text in text area builds style spans
     *
     * @return StyleSpans with misspelled words set to style false (!correct)
     */
    private StyleSpans<TextStyleInfo> checkSpelling() {
        String text = getText();
        String word;

        // keep track of end of matcher
        int lastEnd = 0;
        StyleSpansBuilder<TextStyleInfo> spansBuilder = new StyleSpansBuilder<>();

        // Pattern and Matcher to get whitespace delimited words
        Pattern pattern = Pattern.compile("[\\w'-]+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            word = matcher.group();

            // HINT: may need to change if handling caps
            boolean styleClass = dic.isWord(word);
            TextStyleInfo t;
            if (styleClass && keywords.contains(word)) {
                t = new TextStyleInfo(true, Color.BLUE);
                t.keyword = true;
            } else if (styleClass) {
                t = new TextStyleInfo(true, Color.WHITE);
            } else {
                t = new TextStyleInfo(false, Color.TOMATO);
            }
            spansBuilder.add(new TextStyleInfo(true, Color.WHITE), matcher.start() - lastEnd);
            spansBuilder.add(t, matcher.end() - matcher.start());
            lastEnd = matcher.end();
        }

        // set trailing characters to true
        spansBuilder.add(new TextStyleInfo(true, Color.WHITE), text.length() - lastEnd);

        // maybe finish out end of text with style true
        return spansBuilder.create();

    }

    void clearPopup() {
        entriesPopup.getContent().removeAll();
    }

    /**
     * show suggestions for word in menu at click point
     *
     * @param word  - word to get suggestions for
     * @param click - mouse click for displaying menu
     */
    private void showSuggestions(String word, MouseEvent click) {
        List<String> suggestions = ss.suggestions(word, NUM_SUGGESTIONS);

        // boolean array for matching case of use input
        boolean[] caseFlags = null;
        if (matchCase) {
            caseFlags = getCaseFlags(word);
        }
        clearPopup();
        entriesPopup.getContent().add(createOptions(suggestions, caseFlags));
        // references for creating menu items
        //Label sLabel;
        // CustomMenuItem item;
        // entriesPopup.getItems().clear();
        // List<CustomMenuItem> menuItems = createOptions(suggestions, caseFlags);
        // check if no suggestions found
        // if (suggestions.size() == 0) {
        // sLabel = new Label("No suggestions.");
        //  item = new CustomMenuItem(sLabel, true);
        //  item.setDisable(true);
        // ((LinkedList<CustomMenuItem>) menuItems).addFirst(item);
        //}
        // add "misspelled" word to dictionary
        //sLabel = new Label("Add to dictionary.");
        // item = new CustomMenuItem(sLabel, true);
        // register event to add word
        // item.setOnAction(actionEvent -> {
        // add word to both dictionaries
        //   dic.addWord(word);
        //   ((Dictionary) ac).addWord(word);
        //    setStyle(startIndex, endIndex, new TextStyleInfo(true, Color.WHITE));
        //});
        //menuItems.add(item);
        //entriesPopup.getItems().addAll(menuItems);
        // entriesPopup.show(getScene().getWindow(), click.getScreenX(), click.getScreenY());

    }

    private AutoCompleteResult getCompletions(String prefix) {
        // keep track of prefix
        // check if in middle of typing word

        System.out.println("getCompletions");
        AutoCompleteResult result = new AutoCompleteResult(prefix, new ArrayList<>());
        if (!prefix.equals("")) {
            // get completion options
            System.out.println("getting prediction");
            result.setResults((ac).predictCompletions(prefix, NUM_COMPLETIONS));
        }
        System.out.println("getCompletions done");
        return result;
    }

    private void handleAutocompleteResult(AutoCompleteResult prefix) {
        // check if options found
        System.out.print("Handling Result");
        List<String> options = prefix.getResults();
        if (options.size() > 0) {
            // boolean array for matching case of use input
            boolean[] caseFlags = null;
            if (matchCase) {
                caseFlags = getCaseFlags(prefix.getQuery());
            }

            clearPopup();
            entriesPopup.getContent().add(createOptions(options, caseFlags));
            //List<CustomMenuItem> menuOptions = createOptions(options, caseFlags);
            //entriesPopup.getItems().clear();
            //entriesPopup.getItems().addAll(menuOptions);

            if (!entriesPopup.isShowing()) {
                entriesPopup.show(getScene().getWindow());
            }
        }
        // no options for complete
        else {
            entriesPopup.hide();
        }
    }

    public void setSpelling(boolean state) {
        suggest = state;

        if (state == true && getText().length() > 0) {
            this.setStyleSpans(0, checkSpelling());
        }
        // change all text to true/correct style
        else {
            setStyle(0, getText().length(), new TextStyleInfo(true, Color.WHITE));
        }
    }

    public void setText(String text) {
        this.appendText(text);
        this.positionCaret(0);
        init();
        requestFocus();
    }

    public void setAutoComplete(boolean state) {
        autoCompleteOn = state;
    }

    /**
     * Returns a boolean array with true in position where word has an upper
     * case letter
     *
     * @param word
     * @return boolean array for uppercase or null if none
     */
    private boolean[] getCaseFlags(String word) {

        boolean[] flags = new boolean[word.length()];

        // for if should return array or null
        boolean anyUpperCase = false;

        for (int i = 0; i < flags.length; i++) {
            // if isUpperCase
            if (Character.isUpperCase(word.charAt(i))) {
                flags[i] = true;
                anyUpperCase = true;
            } else {
                flags[i] = false;
            }
        }

        if (anyUpperCase) {
            return flags;
        }

        return null;
    }

    /**
     * Converts characters in word passed in which have "true" in the parallel
     * index to flags array
     *
     * @param word
     * @param flags
     * @return string with uppercase in true positions
     */
    private String convertCaseUsingFlags(String word, boolean[] flags) {
        StringBuilder sb = new StringBuilder(word);

        for (int i = 0; i < flags.length && i < word.length(); i++) {
            if (flags[i]) {
                char c = sb.charAt(i);
                sb.setCharAt(i, Character.toUpperCase(c));
            }
        }

        return sb.toString();
    }

    // encapsulate Method.invoke method
    private static Object invoke(Method m, Object obj, Object... args) {
        try {
            return m.invoke(obj, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private PublishSubject<Integer> mAutoCompleteSubject = PublishSubject.create();

    private void createObservables() {
        mAutoCompleteSubject
                .debounce(400, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.computation())
                .map(position -> {
                    String word = getWordAtIndex(position);
                    System.out.println("getWord " + word + " " + word.length());
                    return getCompletions(word);

                })
                .observeOn(JavaFxScheduler.platform())
                .subscribe(new Observer<AutoCompleteResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(AutoCompleteResult result) {
                        System.out.println("onNext " + Thread.currentThread().getName());
                        handleAutocompleteResult(result);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    private class AutoCompleteResult {
        private String query;
        private List<String> results;

        public AutoCompleteResult(String query, List<String> results) {
            this.query = query;
            this.results = results;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public List<String> getResults() {
            return results;
        }

        public void setResults(List<String> results) {
            this.results = results;
        }

        public String getQuery() {
            return query;
        }
    }

    public static class TextStyleInfo {
        boolean valid;
        boolean keyword;
        Color color;

        public TextStyleInfo(boolean valid, Color color) {
            this.valid = valid;
            this.color = color;
        }
    }
}
