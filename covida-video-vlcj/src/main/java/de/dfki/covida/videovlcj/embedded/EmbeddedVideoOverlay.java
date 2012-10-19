/*
 * EmbeddedVideoOverlay.java
 *
 * Copyright (c) 2012, Tobias Zimmermann All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.dfki.covida.videovlcj.embedded;

import de.dfki.covida.covidacore.data.Stroke;
import de.dfki.covida.covidacore.data.StrokeList;
import de.dfki.covida.videovlcj.IVideoGraphicsHandler;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JWindow;

/**
 * Overlay for the {@link EmbeddedVideoHandler}
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class EmbeddedVideoOverlay extends JWindow implements IVideoGraphicsHandler {

    private float transition;
    private long timer;

    /**
     * Creates a new instance of {@link EmbeddedVideoOverlay}
     * 
     * @param w width
     * @param h height
     */
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
    public List<Stroke> getDrawings() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clearShapes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StrokeList getSavedShapes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addShape(Stroke stroke) {
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

    void draw(Point point) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTimecode(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
