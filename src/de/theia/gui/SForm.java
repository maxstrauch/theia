package de.theia.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
* A simple form layout wrapper using the GridBagLayout
* 
* @author maximilianstrauch
*/
public class SForm extends JPanel {

    /**
    * Constraint for right field
    */
    private GridBagConstraints lastConstraints = null;

    /**
    * Constraint for left label field
    */
    private GridBagConstraints labelConstraints = null;

    /**
    * Containing {@link JPanel}
    */
    private final JPanel holder;

    /**
    * Constructs a new form object
    */
    private SForm(boolean opaque) {
        super(new BorderLayout());
        holder = new JPanel(new GridBagLayout());
        super.add(holder, BorderLayout.NORTH);
        holder.setOpaque(opaque);
        super.setOpaque(opaque);
        lastConstraints = new GridBagConstraints();
        lastConstraints.fill = GridBagConstraints.HORIZONTAL;
        lastConstraints.anchor = GridBagConstraints.NORTHWEST;
        lastConstraints.weightx = 1.0;
        lastConstraints.gridwidth = GridBagConstraints.REMAINDER;
        lastConstraints.insets = new Insets(2, 2, 2, 2);
        labelConstraints = (GridBagConstraints) lastConstraints.clone();
        labelConstraints.weightx = 0.0;
        labelConstraints.gridwidth = 1;
    }

    /**
    * Adds a last field
    *
    * @param c Component to add
    */
    public SForm addLastField(Component c) {
        GridBagLayout gbl = (GridBagLayout) holder.getLayout();
        gbl.setConstraints(c, lastConstraints);
        holder.add(c);
        return this;
    }

    /**
     * Adds an entire row to the form
     * 
     * @param label The label
     * @param c The input component
     */
    public SForm addRow(String label, Component c) {
        addLabel(label);
        addLastField(c);
        return this;
    }

    /**
    * Adds a normal label
    *
    * @param s Label text
    * @return The added label
    */
    public SForm addLabel(String s) {
        JLabel c = new JLabel("<html>" + s + "</html>");
        labelConstraints.fill = GridBagConstraints.NONE;
        c.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        GridBagLayout gbl = (GridBagLayout) holder.getLayout();
        gbl.setConstraints(c, labelConstraints);
        holder.add(c);
        return this;
    }

    /**
     * Creates a new instance
     */
    public static final SForm create() {
        return new SForm(false);
    }

}