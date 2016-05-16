package de.theia.gui;

import java.awt.Color;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * Document filter to highlight keywords, variables and numbers of the
 * LOOP, WHILE and GOTO language
 * 
 * @author maximilianstrauch
 */
public  class KeywordDocumentFilter extends DocumentFilter {

    /**
     * The keywords to highlight
     */
    private static final String[] KEYWORDS = {
        "loop", "do", "end", "while", "if", "then", "else", "goto"
    };
        
    /**
     * The underlying styled document
     */
    private final StyledDocument styledDocument;

    /**
     * The attribute sets to style the different tokens
     */    
    private final AttributeSet neutral, keyword, var, num;
 
    /**
     * Creates a new instance of this filter
     * 
     * @param pane The pane to be used on
     */
    public KeywordDocumentFilter(JTextPane pane) {
        styledDocument = pane.getStyledDocument();
        StyleContext styleContext = StyleContext.getDefaultStyleContext();
        
        neutral = styleContext.addAttribute(
                styleContext.getEmptySet(), 
                StyleConstants.Foreground, 
                Color.BLACK // Default text color
        );
        var = styleContext.addAttribute(
                styleContext.getEmptySet(), 
                StyleConstants.Foreground, 
                new Color(0x009900)
        );
        num = styleContext.addAttribute(
                styleContext.getEmptySet(), 
                StyleConstants.Foreground, 
                new Color(0xc67b00)
        );
        AttributeSet tmp = styleContext.addAttribute(
                styleContext.getEmptySet(), 
                StyleConstants.Foreground, 
                new Color(0x0000e6)
        );
        keyword = styleContext.addAttribute(
                tmp, 
                StyleConstants.Bold, 
                true
        );
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String text, 
            AttributeSet attributeSet) throws BadLocationException {
        super.insertString(fb, offset, text, attributeSet);
        fireTextChanged();
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) 
            throws BadLocationException {
        super.remove(fb, offset, length);
        fireTextChanged();
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, 
            String text, AttributeSet attributeSet) throws BadLocationException {
        super.replace(fb, offset, length, text, attributeSet);
        fireTextChanged();
    }

    /**
     * Runs the style update in EDT
     */
    private void fireTextChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                try {
                    update();
                } catch (Exception e) {
                    System.err.println("Failed to highlight text: " + e);
                }
            }
        });
    }

    /**
     * Updates the text styles
     */
    private synchronized void update() throws BadLocationException {
        char[] text = styledDocument.getText(0, styledDocument.getLength()).toCharArray();
        
        // Clear existing styles
        styledDocument.setCharacterAttributes(0, text.length, neutral, true);

        // Loop through the text
        main: for (int i = 0; i < text.length; i++) {
            for (String kw : KEYWORDS) {
                if (match(kw, text, i)) {
                    styledDocument.setCharacterAttributes(i, kw.length(), keyword, false);
                    
                    // Skip the rest of the keyword
                    i += kw.length() - 1;
                    continue main;
                }
            }
            
            // Check for numbers and variables
            int k = i;
            boolean isNumber = true;
            if (text[k] == 'x' || text[k] == 'X') {
                k++;
                isNumber = false;
            }
            
            while (k < text.length && Character.isDigit(text[k])) {
                k++;
            }
            
            if (k > i) {
                styledDocument.setCharacterAttributes(i, k-i, isNumber ? num : var, false);
            }
            
            // Skip the rest of the matched var/num
            i = k;
        }
    }
    
    /**
     * Tests if a char array contains a string "toMatch" starting from the current
     * location "offset"
     * 
     * @param toMatch The string to check for
     * @param arr The char array to check
     * @param offset The offset to begin
     * @return <code>true</code> if the string "toMatch" starts at position
     * "offset" in "arr" or <code>false</code>
     */
    public static final boolean match(String toMatch, char[] arr, int offset) {
        int len = toMatch.length();
        if (arr.length < offset+len) {
            return false;
        }
        int j = 0;
        for (int i = offset; i < offset+len; i++) {
            if (toMatch.charAt(j) != arr[i]) {
                return false;
            }
            j++;
        }
        return true;
    }
    
}
