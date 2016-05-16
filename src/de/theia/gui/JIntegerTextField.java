package de.theia.gui;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * A simple {@link JTextField} extension which only allows integer inputs >= 0
 * 
 * @author maximilianstrauch
 */
public class JIntegerTextField extends JTextField {
    
    private final int min, max;
    
    public JIntegerTextField() {
        super.setDocument(new IntegerDocument());
        this.min = 0;
        this.max = Integer.MAX_VALUE;
    }
    
    public JIntegerTextField(int min, int max) {
        super.setDocument(new IntegerDocument());
        this.min = min;
        this.max = max;
    }
    
    public int getValue() {
        try {
            return Integer.parseInt(super.getText());
        } catch (Exception e) {
            return this.min;
        }
    }
    
    public void setValue(int n) {
        super.setText(String.valueOf(n));
    }
    
    private class IntegerDocument extends PlainDocument {

        @Override
        public void insertString(int offs, String str, AttributeSet a) 
                throws BadLocationException {
            if (str.matches("[0-9]+")) {
                super.insertString(offs, str, a);
            }
            
            // Apply range
            int val = getValue();
            if (val < min) {
                setValue(min);
            }
            if (val > max) {
                setValue(max);
            }
        }
        
    }
    
}
