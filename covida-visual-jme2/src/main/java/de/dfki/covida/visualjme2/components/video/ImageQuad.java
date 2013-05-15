/*
 * ImageQuad.java
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
package de.dfki.covida.visualjme2.components.video;

import com.jme.image.Texture;
import com.jme.image.Texture2D;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.renderer.Renderer;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jmex.awt.swingui.ImageGraphics;
import de.dfki.covida.covidacore.data.ImageMediaData;
import de.dfki.covida.covidacore.data.Stroke;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DrawingQuad
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class ImageQuad extends Quad {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5408416424492049111L;
    /**
     * Drawing will be done with Java2D.
     */
    protected ImageGraphics g2d;
    /**
     * Logger
     */
    private Logger log = LoggerFactory.getLogger(VideoQuad.class);
    /**
     * {@link Texture2D} with the rendered video and the shapes.
     */
    private Texture2D texture;
    private BufferedImage image;
    private Collection<Collection<Point>> pointsToDraw;
    private Color defaultG2DColor = Color.WHITE;
    private List<Stroke> drawedPoints;
    private float scale;
    /*
     * (non-Javadoc)
     *
     */

    public ImageQuad(ImageMediaData data, BufferedImage image) {
        super(data.imageName, data.width, data.height);
        setRenderQueueMode(Renderer.QUEUE_ORTHO);
        setCullHint(Spatial.CullHint.Inherit);
        TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setCorrectionType(TextureState.CorrectionType.Perspective);
        ts.setEnabled(true);
        this.image = image;
        scale = image.getHeight() / (float) data.height;
        this.pointsToDraw = new ConcurrentLinkedQueue<>();
        this.drawedPoints = new ArrayList<>();
        texture = new Texture2D();
        texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
        texture.setWrap(Texture.WrapMode.Repeat);
        // ---- Drawable image initialization ----
        if (getWidth() < 1 || getHeight() < 1) {
            log.warn("width < 1");
            g2d = ImageGraphics.createInstance(1, 1, 0);
        } else {
            g2d = ImageGraphics.createInstance((int) image.getWidth(), (int) image.getHeight(), 0);
            g2d.drawImage(image, null, 0, 0);
        }
        enableAntiAlias(g2d);
        texture.setImage(g2d.getImage());
        ts.setTexture(texture);
        setRenderState(ts);
        updateRenderState();
        Quaternion q = new Quaternion();
        // Rotation need because of ImageGraphics
        q.fromAngles(0f, (float) Math.toRadians(180),
                (float) Math.toRadians(180));
        rotatePoints(q);
    }

    /**
     * Enables anti aliasing.
     *
     * @param graphics
     */
    private void enableAntiAlias(Graphics2D graphics) {
        RenderingHints hints = graphics.getRenderingHints();
        if (hints == null) {
            hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            hints.put(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }
        graphics.setRenderingHints(hints);
    }

    /**
     * Adds a point to {@code pointsToDraw}
     *
     * @param point {@link Point}
     */
    public void draw(Point point) {

        if (drawedPoints.isEmpty()) {
            Stroke stroke = new Stroke();
            drawedPoints.add(stroke);
        }
        drawedPoints.get(drawedPoints.size() - 1).points.add(point);
        Iterator<Collection<Point>> iterator = pointsToDraw.iterator();
        Collection<Point> last = null;
        while (iterator.hasNext()) {
            last = iterator.next();
        }
        if (last == null) {
            last = new ConcurrentLinkedQueue<>();
        }
        last.add(point);
        pointsToDraw.add(last);
    }

    public void endDrawStroke() {
        drawedPoints.add(new Stroke());
        Collection<Point> newStroke = new ConcurrentLinkedQueue<>();
        pointsToDraw.add(newStroke);
    }

    /**
     * Draws {@code pointsToDraw} on {@link Graphics2D}
     *
     * @param g2d {@link Graphics2D}
     */
    private void drawPoints(Graphics2D g2d) {
        BasicStroke bs = new BasicStroke(2);
        g2d.setStroke(bs);
        for (Collection<Point> points : pointsToDraw) {
            int size = points.size();
            int[] xPoints = new int[size];
            int[] yPoints = new int[size];
            int i = 0;
            for (Point point : points) {
                if (i < size) {
                    xPoints[i] = (int) (point.x * scale);
                    yPoints[i] = (int) (point.y * scale);
                }
                i++;
            }
            g2d.setColor(Color.red);
            g2d.drawPolyline(xPoints, yPoints, size);
        }
    }

    @Override
    public void draw(Renderer r) {
        if (g2d == null) {
            log.error("Draw failed");
            return;
        }
        if (image != null) {
            g2d.drawImage(image, null, 0, 0);
            drawPoints(g2d);
        }
        g2d.update();
        if (texture.getTextureId() > 0) {
            g2d.update(texture, false);
        }
        super.draw(r);
    }
}
