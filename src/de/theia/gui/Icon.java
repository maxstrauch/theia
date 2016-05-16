package de.theia.gui;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Provides the GUI icons
 * 
 * @author maximilianstrauch
 */
public class Icon {
    
    /**
     * All GUI icons as static attributes
     */
    public static final ImageIcon 
            RUN = load("de.theia.res.Run", 32, 0),
            STOP = load("de.theia.res.Stop", 32, 0),
            INFO = load("de.theia.res.Information", 32, 0),
            ADD = load("de.theia.res.Plus", 16, 0),
            BYTECODE = load("de.theia.res.RawAccess", 32, 0),
            CLEAR = load("de.theia.res.BinEmpty", 16, 0),
            DIALOG_ALERT = load("de.theia.res.DialogIcons", 64, 0),
            DIALOG_QUESTION = load("de.theia.res.DialogIcons", 64, 1),
            DIALOG_INFO = load("de.theia.res.DialogIcons", 64, 2);
            
    /**
     * Loads an icon from the given package
     * 
     * @param name The full qualified package name to load the icon from
     * @param size The size of the square icon
     * @param i The n-th icon from the given icon map
     * @return The icon
     */
    private static final ImageIcon load(String name, int size, int i) {
        try {
            BufferedImage image = (BufferedImage) ImageIO.read(
                    Icon.class.getResource(
                            "/" + name.replaceAll("\\.", "/") + ".png"
                    )
            );
            return new ImageIcon(image.getSubimage(i*size, 0, size, size));
        } catch (Exception ex) {
            System.err.println("Failed to load resource: " + ex);
            return null;
        }
    }
    
}
