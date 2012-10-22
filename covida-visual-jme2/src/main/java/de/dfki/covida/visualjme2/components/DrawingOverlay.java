/*
 * DrawingOverlay.java
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
package de.dfki.covida.visualjme2.components;

import com.jme.image.Texture;
import com.jme.image.Texture2D;
import com.jme.math.Quaternion;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jmex.awt.swingui.ImageGraphics;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import de.dfki.covida.visualjme2.utils.CovidaZOrder;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * Drawing Overlay for videotouch
 *
 * @author Tobias Zimmermann
 *
 */
public class DrawingOverlay extends Node {

    /**
     * Generated serialVersionUID
     */
    private static final long serialVersionUID = 3901990659651052688L;
    /**
     * Alpha composite for transparent panel.
     */
    private final AlphaComposite TRANSPARENT = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 0.0f);
    /**
     * Alpha composite for solid drawing color.
     */
    private final AlphaComposite SOLID = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 1.0f);
    /**
     * Logger.
     */
    private Logger log = Logger.getLogger(DrawingOverlay.class);
    /**
     * Width of this pen's texture.
     */
    private int height;
    /**
     * Height of this pen's texture.
     */
    private int width;
    /**
     * Texture state.
     */
    private TextureState ts;
    /**
     * The texture which has to be dynamically updated.
     */
    private Texture texture;
    /**
     * Last x position of the pen. (-1 if there was a pen up event)
     */
    private Map<String, Integer> lastX = new HashMap<>();
    /**
     * Last y position of the pen. (-1 if there was a pen up event)
     */
    private Map<String, Integer> lastY = new HashMap<>();
    /**
     * Drawing will be done with Java2D.
     */
    private ImageGraphics g2d;
    /**
     * Drawing board.
     */
    private Quad board;

    /**
     * Creates a new instane of {@link DrawingOverlay}
     * 
     * @param name Name as {@link String}
     * @param width Width as {@link Integer}
     * @param height Height as {@link Integer}
     */
    public DrawingOverlay(String name, int width, int height, int zOrder){
        super(name);
        this.width=  height;
        this.height = width;
        this.setZOrder(zOrder);
        board = new Quad("Drawingboard-Quad", width, height);
        board.setZOrder(getZOrder());
        Quaternion q = new Quaternion();
        q.fromAngles(0f, (float) Math.toRadians(180), (float) Math.toRadians(180));
        board.rotatePoints(q);
        setLightCombineMode(LightCombineMode.Off);
        generateTexture();
        board.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        board.setCullHint(Spatial.CullHint.Never);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(this, board));
    }

    /**
     * Creates a texture which can be used to draw the pen information.
     */
    private void generateTexture() {
        // ---- Texture state initialization ----
        ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setCorrectionType(TextureState.CorrectionType.Perspective);
        ts.setEnabled(true);
        texture = new Texture2D();
        texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
        texture.setWrap(Texture.WrapMode.Repeat);
        // ---- Drawable image initialization ----
        g2d = ImageGraphics.createInstance(height, width, 0);
        enableAntiAlias(g2d);
        BlendState alpha = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
        alpha.setEnabled(true);
        alpha.setBlendEnabled(true);
        alpha.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        alpha.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        alpha.setTestEnabled(true);
        alpha.setTestFunction(BlendState.TestFunction.GreaterThan);
        clear();
        texture.setImage(g2d.getImage());
        ts.setTexture(texture);
        board.setRenderState(alpha);
        board.setRenderState(ts);
        board.updateRenderState();
    }

    /**
     * Enables anti aliasing.
     *
     * @param graphics {@link Graphics2D}
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
     * Removes all the drawings from the board.
     */
    public void clear() {
        g2d.clearRect(0, 0, height, width);
        g2d.setComposite(TRANSPARENT);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, height, width);
        g2d.setComposite(SOLID);
        g2d.update();
    }

    /**
     * Updates the internal image.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param id pen id
     */
    public void updateImage(int x, int y, String id) {
        if (!lastX.containsKey(id)) {
            g2d.setColor(Color.WHITE);
            lastX.put(id, x);
            lastY.put(id, y);
        } else {
            this.g2d.setStroke(new BasicStroke(2));
            this.g2d.drawLine(
                    lastX.get(id),
                    lastY.get(id), x, y);
            lastX.put(id, x);
            lastY.put(id, y);
        }
    }

    /**
     * Draws the current strokes.
     *
     * @param r {@link Renderer}
     */
    @Override
    public void draw(Renderer r) {
        if (g2d != null && texture.getTextureId() > 0) {
            g2d.update(texture, false);
        }
        super.draw(r);
    }

    /**
     * Ends current draw stroke
     */
    public void endDrawStroke() {
        lastX.clear();
        lastY.clear();
    }
}