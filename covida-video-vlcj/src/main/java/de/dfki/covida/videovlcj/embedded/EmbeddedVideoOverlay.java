/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.videovlcj.embedded;

import de.dfki.covida.covidacore.data.ShapePoints;
import de.dfki.covida.videovlcj.IVideoGraphicsHandler;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

/**
 *
 * @author Tobias
 */
public class EmbeddedVideoOverlay extends Window implements IVideoGraphicsHandler {
    private Image imageToBeDraw;

    public EmbeddedVideoOverlay(GraphicsConfiguration gc, int w, int h) {
        super(new JFrame(gc), gc);
        BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration()
                .createCompatibleImage(w, h);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(Color.RED);
        BasicStroke bs = new BasicStroke(3);
        g2d.setStroke(bs);
        g2d.drawLine(30, 30, 400, 400);
        g2d.drawString("foienbovwb", 55, 55);
        imageToBeDraw = image;
    }
    
    @Override
    public void paint(Graphics g) {
        //This will draw drawImageIntoJFrame.jpg into JFrame
        g.drawImage(imageToBeDraw, 0, 0, this);
    }

    public ShapePoints getDrawing() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clearShape() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ShapePoints getSavedShape() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setShape(ShapePoints points) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setHWR(String hwr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clearDrawing() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
