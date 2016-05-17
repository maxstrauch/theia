package de.theia.gui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
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
     * Returns a list of different resolutions of the app icon
     * 
     * @return Different app icons: 16p, 32p, 48p, 64p
     */
    public static final List<Image> getAppIcons() {
        List<Image> image = new ArrayList<>();
        
        try {
            BufferedImage buf = (BufferedImage) ImageIO.read(
                    Icon.class.getResource("/de/theia/res/AppIcons.png")
            );
            image.add(buf.getSubimage(144, 0, 16, 16));
            image.add(buf.getSubimage(112, 0, 32, 32));
            image.add(buf.getSubimage(64, 0, 48, 48));
            image.add(buf.getSubimage(0, 0, 64, 64));
        } catch (Exception ex) {
            System.err.println("Failed to load app icons: " + ex);
        }
        
        return image;
    }
    
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
