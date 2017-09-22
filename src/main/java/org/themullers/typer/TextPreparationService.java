package org.themullers.typer;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TextPreparationService {
    
    private static final int WRAP_COL = 80;
    
    public List<String> toLines(String text) {
        
        // wrap the text  
        text = wrapMultiLine(text, WRAP_COL);

        // convert to an array of strings
        List<String> lines = Arrays.asList(text.split("\n"));

        return lines;
    }
    
    protected String wrapMultiLine(String text, int maxLen) {
        BufferedReader reader = new BufferedReader(new StringReader(text));
        return reader.lines().map(f->wrapSingleLine(f, maxLen)).collect(Collectors.joining("\n"));
    }
    
    protected String wrapSingleLine(String text, int maxLen) {
        text = text.trim();
        int len = text.length();
        
        // if we're under the max length, then there's nothing to do
        if (len <= maxLen) {
            return text;
        }
        
        // start at the margin and look back for a space to break the line
        for (int i = maxLen; i > 0; i--) {
            if (Character.isWhitespace(text.charAt(i))) {
                return text.substring(0, i) + "\n" + wrapSingleLine(text.substring(i), maxLen);
            }
        }
 
        // if there aren't any spaces within the max length, then just wrap as soon as we can after the max length
        for (int i = maxLen; i <= len; i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                return text.substring(0, i) + "\n" + wrapSingleLine(text.substring(i), maxLen);
            }
        }
        
        // there weren't any spaces; give up and return the original string
        return text;
    }
}
