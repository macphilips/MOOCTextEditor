package view;

import org.fxmisc.richtext.StyleSpans;

import java.util.Collection;

public interface LangType {
    String format(String source);

    StyleSpans<Collection<String>> computeHighlighting(String text);
}
