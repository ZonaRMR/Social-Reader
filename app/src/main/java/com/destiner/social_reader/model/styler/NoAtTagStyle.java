package com.destiner.social_reader.model.styler;

/**
 * Removes at tags.
 * Examples:
 * <ul>
 *     <li>"Sample text" will stay "Sample text"</li>
 *     <li>"Sample @tag" will become "Sample "</li>
 *     <li>"Some text @one @two @three" will become "Some text   "</li>
 * </ul>
 */
public class NoAtTagStyle extends Style {
    @Override
    protected String getOriginal() {
        return "@\\w+";
    }

    @Override
    protected String getReplacement() {
        return "";
    }
}
