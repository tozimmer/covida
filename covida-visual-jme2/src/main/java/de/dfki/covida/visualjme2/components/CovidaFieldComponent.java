/*
 * CovidaFieldComponent.java
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

import com.jme.animation.SpatialTransformer;
import com.jme.image.Texture;
import com.jme.math.Vector2f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.TextureManager;
import com.jmex.awt.swingui.ImageGraphics;
import de.dfki.covida.covidacore.data.Annotation;
import de.dfki.covida.covidacore.data.AnnotationData;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import de.dfki.covida.visualjme2.utils.DetachChildCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import java.awt.Color;
import java.util.*;

/**
 * CovidaFieldComponent
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public abstract class CovidaFieldComponent extends CovidaJMEComponent {

    /**
     * Config
     */
    protected ColorRGBA color = new ColorRGBA(1, 1, 1, 0);
    protected static ColorRGBA defaultColor = new ColorRGBA(1, 1, 1, 1);
    protected static ColorRGBA activeColor = new ColorRGBA(1, 0, 0, 1);
    protected static ColorRGBA selectedColor = new ColorRGBA(0, 1, 0, 1);
    protected static final int ANIMATION_DURATION = 500;
    protected static final int FONT_SIZE = 30;
    protected static final int TEXT_SPACER = 2;
    protected int textBeginY;
    protected boolean open;
    /**
     * image
     */
    protected String image;
    /**
     * State of the texture.
     */
    protected TextureState ts;
    /**
     * Texture.
     */
    protected Texture texture;
    /**
     * Quad for image
     */
    protected Quad quad;
    protected int width;
    protected int height;
    /**
     * Drawing will be done with Java2D.
     */
    protected ImageGraphics g2d;
    protected List<String> hwrResults;
    @SuppressWarnings("unused")
    protected Color currentPenColor;
    protected boolean penPressure;
    protected float penThickness;
    protected TextureState tsSpacer;
    protected Texture textureSpacer;
    protected ArrayList<CovidaTextComponent> titles;
    protected int selectedTitle = -1;
    /**
     * maps index to video id
     */
    protected Map<Integer, AnnotationData> mapping;
    /**
     * Map with all search results mapped on video ids
     */
    protected HashMap<Integer, ArrayList<CovidaTextComponent>> entryMap;
    /**
     * Map with all search results mapped to annotation ids
     */
    protected ArrayList<Map<Integer, Annotation>> entriesMapping;
    /**
     * HWR TextOverlays
     */
    protected ArrayList<CovidaTextComponent> hwr;
    protected Map<Integer, Vector2f> lastTouch = new HashMap<>();
    protected float yDrag = 0;
    protected float xDrag = 0;
    protected SpatialTransformer st;

    public CovidaFieldComponent(String name) {
        super(name);
    }

    protected final void initTextures() {
        // ---- Background Texture state initialization ----
        ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setCorrectionType(TextureState.CorrectionType.Perspective);
        ts.setEnabled(true);
        texture = TextureManager.loadTexture(getClass().getClassLoader().getResource(image));
        ts.setTexture(texture);
        quad = new Quad("Display image quad", width, height);
        quad.setRenderState(ts);
        quad.setRenderState(JMEUtils.initalizeBlendState());
        quad.updateRenderState();
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, quad));
        // Spacer
        tsSpacer = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        tsSpacer.setCorrectionType(TextureState.CorrectionType.Perspective);
        tsSpacer.setEnabled(true);
        textureSpacer = TextureManager.loadTexture(getClass().getClassLoader().getResource("media/textures/info_spacer.png"));
        tsSpacer.setTexture(textureSpacer);
        open = true;
    }

    protected abstract void update();

    protected abstract float getTextY(int position);

    protected abstract void addSpacer(int x, int y, float angle, int width, int height);

    /**
     *
     * @return
     */
    @Override
    public int getHeight() {
        return height;
    }

    /**
     *
     * @return
     */
    @Override
    public int getWidth() {
        return width;
    }

    /**
     * Detach DisplayInfoComponent
     */
    protected void detach() {
        if (this.getParent() != null) {
            GameTaskQueueManager.getManager().update(new DetachChildCallable(getParent(), node));
        }
    }

    /**
     * Closes the DisplayInfoComponent
     */
    @Override
    public abstract void close();

    /**
     * Open animation of the DisplayInfoComponent
     */
    public abstract void open();

    public void clearHwrResults() {
        hwrResults.clear();
    }

    public void reset() {
        // TODO
    }

    public abstract boolean isOpen();
}
