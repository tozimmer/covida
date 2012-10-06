/*
 * VideoQuad.java
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
import com.jme.renderer.Renderer;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jmex.awt.swingui.ImageGraphics;
import de.dfki.covida.videovlcj.AbstractVideoHandler;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * VideoQuad
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class VideoQuad extends Quad {

    /**
     * Drawing will be done with Java2D.
     */
    protected ImageGraphics g2d;
    /**
     * Logger
     */
    private Logger log = LoggerFactory.getLogger(VideoQuad.class);
    /**
     * {@link VideoHandler} which plays and renders the video.
     */
    private AbstractVideoHandler video;
    /**
     * {@link Texture2D} with the rendered video and the shapes.
     */
    private Texture2D texture;

    /*
     * (non-Javadoc)
     *
     * @see com.jme.scene.TriMesh#draw(com.jme.renderer.Renderer)
     */
    public VideoQuad(AbstractVideoHandler video) {
        super(video.getTitle(), video.getWidth(), video.getHeight());
        setRenderQueueMode(Renderer.QUEUE_ORTHO);
        setCullHint(Spatial.CullHint.Inherit);
        this.video = video;
        TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setCorrectionType(TextureState.CorrectionType.Perspective);
        ts.setEnabled(true);
        texture = new Texture2D();
        texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
        texture.setWrap(Texture.WrapMode.Repeat);
        // ---- Drawable image initialization ----
        if (getWidth() < 1 || getHeight() < 1) {
            log.warn("width < 1");
            g2d = ImageGraphics.createInstance(1, 1, 0);
        } else {
            g2d = ImageGraphics.createInstance((int) getWidth(), (int) getHeight(), 0);
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

    @Override
    public void draw(Renderer r) {
        try {
            g2d.drawImage(video.getVideoImage(), null, 0, 0);
            g2d.update();
            if (g2d != null && texture.getTextureId() > 0) {
                g2d.update(texture, false);
            }
        } catch (Exception e) {
            log.error("Draw failed: " + e);
        }
        super.draw(r);
    }
}
