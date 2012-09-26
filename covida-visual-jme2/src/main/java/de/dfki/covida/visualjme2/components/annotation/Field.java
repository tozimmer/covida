/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.components.annotation;

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
import de.dfki.covida.visualjme2.components.CovidaJMEComponent;
import de.dfki.covida.visualjme2.components.TextOverlay;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import de.dfki.covida.visualjme2.utils.DetachChildCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import java.awt.Color;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Tobias Zimmermann
 */
public abstract class Field extends CovidaJMEComponent {

    /**
     * Config
     */
    public ColorRGBA color = new ColorRGBA(1, 1, 1, 0);
    static ColorRGBA defaultColor = new ColorRGBA(1, 1, 1, 1);
    static ColorRGBA activeColor = new ColorRGBA(1, 0, 0, 1);
    static ColorRGBA selectedColor = new ColorRGBA(0, 1, 0, 1);
    static final int ANIMATION_DURATION = 500;
    static final int FONT_SIZE = 30;
    static final int TEXT_SPACER = 2;
    static int textBeginY;
    /**
     * Logger
     */
    protected Logger log = Logger.getLogger(AnnotationClipboard.class);
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
    List<String> hwrResults;
    @SuppressWarnings("unused")
    protected Color currentPenColor;
    protected boolean penPressure;
    protected float penThickness;
    protected TextureState tsSpacer;
    protected Texture textureSpacer;
    protected boolean detach = true;
    protected ArrayList<TextOverlay> titles;
    protected int selectedTitle = -1;
    /**
     * maps index to video id
     */
    protected Map<Integer, AnnotationData> mapping;
    /**
     * Map with all search results mapped on video ids
     */
    protected HashMap<Integer, ArrayList<TextOverlay>> entryMap;
    /**
     * Map with all search results mapped to annotation ids
     */
    protected ArrayList<Map<Integer, Annotation>> entriesMapping;
    /**
     * HWR TextOverlays
     */
    protected ArrayList<TextOverlay> hwr;
    protected Map<Integer, Vector2f> lastTouch = new HashMap<>();
    protected float yDrag = 0;
    protected float xDrag = 0;
    protected SpatialTransformer st;

    public Field(String name) {
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
    }

    abstract void update();

    abstract float getTextY(int position);

    abstract void addSpacer(int x, int y, float angle, int width, int height);

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

    public boolean isClosing() {
        return detach;
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

    public boolean isOpen() {
        return !isClosing();
    }
}
