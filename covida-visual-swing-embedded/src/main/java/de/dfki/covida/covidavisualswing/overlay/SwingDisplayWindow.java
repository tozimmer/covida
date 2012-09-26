/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidavisualswing.overlay;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JWindow;


/**
 *
 * @author Tobias Zimmermann
 */
public class SwingDisplayWindow extends JWindow {


    public SwingDisplayWindow(){
        setBackground(new Color(0,0,0,0));
    }
    
    
    
    @Override
    public void paint (Graphics g){
        super.paint(g);
        try {
            BufferedImage img = ImageIO.read(getClass().getClassLoader()
                    .getResource("ui/bg_info_blank.png"));
            if (img != null){
                Graphics2D g2 = (Graphics2D) g;
                g2.setComposite(AlphaComposite.Src);
                synchronized(img){
                    g.drawImage(img, 0,0, img.getWidth(), img.getHeight(), null);
                }
            }
        } catch (IOException ex) {  
            Logger.getLogger(SwingDisplayWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
