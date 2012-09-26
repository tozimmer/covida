/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.videovlcj.embedded;

import de.dfki.covida.covidacore.data.ShapePoints;
import de.dfki.covida.videovlcj.IVideoGraphicsHandler;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
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
 * @author Tobias
 */
public class EmbeddedVideoOverlay extends JWindow implements IVideoGraphicsHandler {

    private float transition;
    private long timer;

    public EmbeddedVideoOverlay(int w, int h) {
        setBackground(new Color(0, 0, 0, 0));
        setSize(w, h);
        timer = System.currentTimeMillis();
        transition = 1.0f;
    }

    @Override
    public void paint(Graphics g) {
//        BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment()
//                .getDefaultScreenDevice().getDefaultConfiguration()
//                .createCompatibleImage(getWidth(), getHeight());
//        Graphics2D g2d = image.createGraphics();
//        g2d.setColor(Color.RED);
//        BasicStroke bs = new BasicStroke(3);
//        g2d.setStroke(bs);
//        g2d.drawLine(30, 30, 400, 400);
//        g2d.drawString("foienbovwb", 55, 55);
//        g2d.setComposite(AlphaComposite.Src);
//        synchronized (image) {
//            g.drawImage(image, 0, 0, this);
//        }

//        try {
//            BufferedImage img = ImageIO.read(getClass().getClassLoader()
//                    .getResource("ui/bg_info_blank.png"));
//            if (img != null) {
//                Graphics2D g2d = (Graphics2D) g;
//                g2d.setComposite(AlphaComposite.Src);
//                g2d.setColor(Color.RED);
//                BasicStroke bs = new BasicStroke(3);
//                g2d.setStroke(bs);
//                g2d.drawLine(30, 30, 400, 400);
//                g2d.drawString("foienbovwb", 55, 55);
//                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
//                synchronized (img) {
//                    g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
//                }
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(EmbeddedVideoOverlay.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        transition -= (System.currentTimeMillis() - timer) / 2000;
//        timer = System.currentTimeMillis();
//        if (transition < 0.0f) {
//            transition = 1.0f;
//        }
        try {
            BufferedImage img = ImageIO.read(getClass().getClassLoader()
                    .getResource("ui/search_field_color.png"));
            if (img != null) {
                Graphics2D g2 = img.createGraphics();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0f));
                g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
                g.dispose();
            }
        } catch (IOException ex) {
            Logger.getLogger(EmbeddedVideoOverlay.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public ShapePoints getDrawing() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clearShape() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ShapePoints getSavedShape() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setShape(ShapePoints points) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setHWR(String hwr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clearDrawing() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
