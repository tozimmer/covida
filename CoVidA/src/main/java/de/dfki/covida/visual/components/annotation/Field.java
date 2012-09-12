/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visual.components.annotation;

import com.jme.animation.SpatialTransformer;
import com.jme.image.Texture;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jmex.awt.swingui.ImageGraphics;
import de.dfki.covida.data.VideoAnnotationData;
import de.dfki.covida.visual.components.CovidaComponent;
import de.dfki.covida.visual.components.TextOverlay;
import de.dfki.touchandwrite.action.DrawAction;
import de.dfki.touchandwrite.action.HWRAction;
import de.dfki.touchandwrite.action.PenActionEvent;
import de.dfki.touchandwrite.action.TouchActionEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.DragEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.RotationGestureEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.ZoomEvent;
import de.dfki.touchandwrite.input.pen.event.ShapeEvent;
import de.dfki.touchandwrite.input.pen.hwr.HWRResultSet;
import de.dfki.touchandwrite.input.pen.hwr.HandwritingRecognitionEvent;
import de.dfki.touchandwrite.visual.components.ComponentType;
import de.dfki.touchandwrite.visual.components.DrawingComponent;
import de.dfki.touchandwrite.visual.components.HWRSensitiveComponent;
import de.dfki.touchandwrite.visual.components.ImageBehaviorListener;
import de.dfki.touchandwrite.visual.input.PenInputHandler;
import de.dfki.touchandwrite.visual.input.TouchInputHandler;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Tobias Zimmermann
 */
public abstract class Field extends CovidaComponent implements
        DrawingComponent, HWRSensitiveComponent {

    /**
     * Generated ID
     */
    protected static final long serialVersionUID = 4586629113667692802L;
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
     * Annotation data
     */
    protected ArrayList<VideoAnnotationData> data;
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
    protected List<ImageBehaviorListener> listeners;
    /**
     * Handwriting event action.
     */
    protected HWRAction hwrAction;
    /**
     * Draw action.
     */
    protected DrawAction drawAction;
    /**
     * Drawing will be done with Java2D.
     */
    protected ImageGraphics g2d;
    List<String> hwrResults;
    protected DetachHandler resetHandler;
    protected Thread resetHandlerThread;
    @SuppressWarnings("unused")
    protected Color currentPenColor;
    protected boolean penPressure;
    protected float penThickness;
    protected TextureState tsSpacer;
    protected Texture textureSpacer;
    protected boolean detach = true;
    protected ArrayList<TextOverlay> titles;
    protected Map<Integer, ArrayList<Integer>> result = new HashMap<>();
    protected Map<Integer, ArrayList<String>> resultString = new HashMap<>();
    protected int selectedTitle = -1;
    /**
     * maps index to video id
     */
    protected Map<Integer, Integer> mapping;
    /**
     * Map with all search results mapped on video ids
     */
    protected HashMap<Integer, ArrayList<TextOverlay>> entryMap;
    /**
     * Map with all search results mapped to annotation ids
     */
    protected ArrayList<Map<Integer, Integer>> entriesMapping;
    /**
     * HWR TextOverlays
     */
    protected ArrayList<TextOverlay> hwr;
    protected Map<Integer, Vector2f> lastTouch = new HashMap<>();
    protected float yDrag = 0;
    protected float xDrag = 0;
    protected TouchInputHandler touchInput;

    public Field(ComponentType type, String name, Node node) {
        super(type, name, node);
    }

    abstract void update();

    abstract float getTextY(int position);

    abstract String checkHWRResult(HWRResultSet hwrResultSet);

    abstract void addSpacer(int x, int y, float angle, int width, int height);

    abstract void initTextures();

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
            this.getParent().detachChild(this);
        }
    }

    /**
     * Closes the DisplayInfoComponent
     */
    @Override
    public void close() {
        detach = true;
        for (int i = 0; i < getNode().getControllerCount(); i++) {
            getNode().removeController(i);
        }

        SpatialTransformer st = new SpatialTransformer(1);
        // Close animation (Info Field)
        st.setObject(getNode(), 0, -1);
        st.setPosition(0, 0.f, new Vector3f(getNode().getLocalTranslation()));
        st.setPosition(0, 0.5f, new Vector3f(-(float) getHeight() / 2.f,
                -(float) getHeight() / 2.f, 0));
        st.interpolateMissing();
        getNode().addController(st);
        resetHandler = new DetachHandler(this, 500);
        resetHandlerThread = new Thread(resetHandler);
        resetHandlerThread.start();
    }

    /**
     * Open animation of the DisplayInfoComponent
     */
    public void open() {
        detach = false;
        for (int i = 0; i < getNode().getControllerCount(); i++) {
            getNode().removeController(i);
        }
        SpatialTransformer st = new SpatialTransformer(1);
        st.setObject(getNode(), 0, -1);
        st.setPosition(0, 0.f, new Vector3f(getNode().getLocalTranslation()));
        st.setPosition(0, 0.5f, new Vector3f(0, 75, 0));
        st.interpolateMissing();
        getNode().addController(st);
        update();
    }

    public void clearHwrResults() {
        hwrResults.clear();
    }

    public void addListener(ImageBehaviorListener lis) {
        if (listeners == null) {
            this.listeners = new ArrayList<>();
        }
        this.listeners.add(lis);
    }

    /*
     * (non-Javadoc)
     *
     * @seede.dfki.touchandwrite.visual.components.TouchAndWriteComponent#
     * getTypeOfComponent()
     */
    @Override
    public ComponentType getTypeOfComponent() {
        return ComponentType.COMPONENT_2D;
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
    public void registerWithInputHandler(PenInputHandler input) {
        input.addAction(this.drawAction);
        input.addAction(this.hwrAction);
    }

    @Override
    public void registerWithInputHandler(TouchInputHandler input) {
        touchInput = input;
    }

    @Override
    public void unRegisterWithInputHandler(PenInputHandler input) {
        input.removeAction(drawAction);
        input.removeAction(hwrAction);
    }

    protected BlendState initalizeBlendStateT() {
        // to handle texture transparency:
        // create a blend state
        final BlendState bs = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
        // activate blending
        bs.setBlendEnabled(true);
        // set the source function
        bs.setSourceFunctionAlpha(BlendState.SourceFunction.SourceAlpha);
        // set the destination function
        bs.setDestinationFunctionAlpha(BlendState.DestinationFunction.OneMinusSourceAlpha);
        // set the blend equation between source and destination
        bs.setBlendEquation(BlendState.BlendEquation.Subtract);
        bs.setTestEnabled(false);
        // activate the blend state
        bs.setEnabled(true);
        return bs;
    }

    public void reset() {
        // TODO
    }

    public boolean isOpen() {
        return !isClosing();
    }

    @Override
    protected void dragAction(DragEvent event) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void rotationAction(RotationGestureEvent event) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void zoomAction(ZoomEvent event) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void touchDeadAction(TouchActionEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void touchDeadAction(int id) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void touchAliveAction(TouchActionEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void touchBirthAction(TouchActionEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void draw(Queue<PenActionEvent> penEvent) {
    }

    @Override
    public void draw(ShapeEvent shape) {
    }

    @Override
    public void setCurrentPenColor(Color color) {
        this.currentPenColor = color;
    }

    @Override
    public void activatePenPressure() {
        this.penPressure = true;
    }

    @Override
    public void deactivatePenPressure() {
        this.penPressure = false;

    }

    @Override
    public boolean isPenPressureActivated() {
        return penPressure;
    }

    @Override
    public float getPenThickness() {
        return penThickness;
    }

    @Override
    public void setPenThickness(float thickness) {
        this.penThickness = thickness;
    }
}
