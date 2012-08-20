/*
 * VideoSearchField.java
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
package de.dfki.covida.components.ui.video;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jme.animation.SpatialTransformer;
import com.jme.image.Texture;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.angelfont.BitmapFont.Align;
import com.jmex.awt.swingui.ImageGraphics;

import de.dfki.covida.ui.components.CovidaComponent;
import de.dfki.covida.ui.components.TextOverlay;
import de.dfki.covida.data.CovidaConfiguration;
import de.dfki.touchandwrite.action.DrawAction;
import de.dfki.touchandwrite.action.HWRAction;
import de.dfki.touchandwrite.action.PenActionEvent;
import de.dfki.touchandwrite.action.TouchAction;
import de.dfki.touchandwrite.action.TouchActionEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.DragEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.RotationGestureEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.ZoomEvent;
import de.dfki.touchandwrite.input.pen.event.ShapeEvent;
import de.dfki.touchandwrite.input.pen.hwr.HWRResultSet;
import de.dfki.touchandwrite.input.pen.hwr.HandwritingRecognitionEvent;
import de.dfki.touchandwrite.input.touch.event.TouchState;
import de.dfki.touchandwrite.math.FastMath;
import de.dfki.touchandwrite.visual.components.ComponentType;
import de.dfki.touchandwrite.visual.components.DrawingComponent;
import de.dfki.touchandwrite.visual.components.HWRSensitiveComponent;
import de.dfki.touchandwrite.visual.components.ImageBehaviorListener;
import de.dfki.touchandwrite.visual.input.PenInputHandler;
import de.dfki.touchandwrite.visual.input.TouchInputHandler;


/**
 * Component which displays annotation data of VideoComponent.
 * 
 * @author Tobias Zimmermann
 * 
 */
public class VideoSearchField extends CovidaComponent implements DrawingComponent,
        HWRSensitiveComponent {

    /** Generated ID */
    private static final long serialVersionUID = 4586629113667692802L;
    /** Config */
    public ColorRGBA color = new ColorRGBA(1, 1, 1, 0);
    static ColorRGBA defaultColor = new ColorRGBA(1, 1, 1, 1);
    static ColorRGBA activeColor = new ColorRGBA(1, 0, 0, 1);
    static ColorRGBA selectedColor = new ColorRGBA(0, 1, 0, 1);
    static final int ANIMATION_DURATION = 500;
    static final int FONT_SIZE = 19;
    static final int TEXT_SPACER = 4;
    static int textBeginY;
    /** Logger */
    private Logger log = Logger.getLogger(VideoSearchField.class);
    /** image */
    private String image;
    /** State of the texture. */
    private TextureState ts;
    /** Texture. */
    private Texture texture;
    /** Quad for image */
    protected Quad quad;
    private int width;
    private int height;
    private List<ImageBehaviorListener> listeners;
    /** Handwriting event action. */
    private HWRAction hwrAction;
    /** Handwriting */
    protected List<HandwritingRecognitionEvent> hwrEvents;
    /** Draw action. */
    private DrawAction drawAction;
    /** Drawing will be done with Java2D. */
    protected ImageGraphics g2d;
    ArrayList<String> hwrResults;
    private DetachHandler resetHandler;
    private Thread resetHandlerThread;
    private TouchInputHandler touchInputHandler;
    private TouchAction touchAction;
    @SuppressWarnings("unused")
    private Color currentPenColor;
    private boolean penPressure;
    private float penThickness;
    private TextureState tsSpacer;
    private Texture textureSpacer;
    private ArrayList<VideoComponent> videos;
    private boolean detach = true;
    private ArrayList<TextOverlay> titles;
    private Map<Integer, String> result = new HashMap<Integer, String>();
    private int selectedTitle = -1;
    /** maps index to video id */
    private Map<Integer, Integer> mapping;
    /** Map with all search results mapped on video ids */
    private HashMap<Integer, ArrayList<TextOverlay>> entryMap;
    /** Map with all search results mapped to annotation ids */
    private ArrayList<Map<Integer, Integer>> entriesMapping;
    /** HWR TextOverlays */
    private ArrayList<TextOverlay> hwr;
    private Map<Integer, Vector2f> lastTouch = new HashMap<Integer, Vector2f>();
    private float yDrag = 0;
    private float xDrag = 0;

    /**
     * Search field constructor
     * 
     * @param resource
     * @param video
     * @param listField
     * @param id
     * @param node
     * @param width
     * @param height
     */
    public VideoSearchField(String resource, ArrayList<VideoComponent> videos,
            Node node, int width, int height) {
        super(ComponentType.COMPONENT_2D, "AnnotationSearch", node);
        this.videos = videos;
        this.width = width;
        this.height = height;
        this.image = resource;
        hwrResults = new ArrayList<String>();
        hwrEvents = new ArrayList<HandwritingRecognitionEvent>();
        mapping = new HashMap<Integer, Integer>();
        entriesMapping = new ArrayList<Map<Integer, Integer>>();
        entryMap = new HashMap<Integer, ArrayList<TextOverlay>>();
        titles = new ArrayList<TextOverlay>();
        hwr = new ArrayList<TextOverlay>();
        result = new HashMap<Integer, String>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.dfki.touchandwrite.visual.components.TouchAndWriteComponent#initComponent
     * ()
     */
    @Override
    public void initComponent() {
        super.initComponent();
        super.setAlwaysOnTop(true);
        super.setRootNode(getNode().getParent());
        getNode().setLocalScale(new Vector3f(1, 1, 1));
        initTextures();
        textBeginY = (int) (quad.getHeight() / 2.0f);
        this.drawAction = new DrawAction(this);
        this.touchAction = new TouchAction(this);
        this.hwrAction = new HWRAction(this);
        int x = (int) (0);
        Node node = new Node("AnnotationSearch Entry Node");
        getNode().attachChild(node);
        node.setLocalTranslation(x, getTextY(0) - FONT_SIZE / 4.f, 0);
        TextOverlay caption = new TextOverlay(node, this);
        caption.setSize((int) (FONT_SIZE * 1.5f));
        caption.setText("Write here for annotation search:");
        caption.setFont(2);
        addSpacer(x, (int) (getTextY(0) - FONT_SIZE), 0,
                (int) (quad.getWidth() / 1.1f), TEXT_SPACER);
        x = (int) (getWidth() / 9.f);
        addSpacer(x, 0, 90, (int) (quad.getHeight() / 1.1f), TEXT_SPACER);
        x = (int) -(getWidth() / 4.f);
        addSpacer(x, 0, 90, (int) (quad.getHeight() / 1.1f), TEXT_SPACER);
    }

    private void initTextures() {
        // ---- Background Texture state initialization ----
        ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setCorrectionType(TextureState.CorrectionType.Perspective);
        ts.setEnabled(true);
        texture = TextureManager.loadTexture(getClass().getClassLoader().getResource(image));
        ts.setTexture(texture);
        quad = new Quad("Display image quad", width, height);
        quad.setRenderState(ts);
        quad.setRenderState(this.initalizeBlendState());
        quad.updateRenderState();
        getNode().attachChild(quad);
        // Spacer
        tsSpacer = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        tsSpacer.setCorrectionType(TextureState.CorrectionType.Perspective);
        tsSpacer.setEnabled(true);
        textureSpacer = TextureManager.loadTexture(getClass().getClassLoader().getResource("media/textures/info_spacer.png"));
        tsSpacer.setTexture(textureSpacer);
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

    public BlendState initalizeBlendStateT() {
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

    public void addListener(ImageBehaviorListener lis) {
        if (listeners == null) {
            this.listeners = new ArrayList<ImageBehaviorListener>();
        }
        this.listeners.add(lis);
    }

    @Override
    public void handwritingResult(HandwritingRecognitionEvent event) {
        // TODO pen id!
        log.debug("HWR Event: " + event.toString());
        if (getLockState().onTop(
                -1,
                new Vector2f(event.getBoundingBox().getCenterOfGravity().x,
                (getDisplay().y - event.getBoundingBox().getCenterOfGravity().y)), this)) {
            this.hwrEvents.add(event);
            int size = event.getHWRResultSet().getWords().size();
            hwrResults = new ArrayList<String>();
            for (int i = 0; i < size; i++) {
                this.hwrResults.add(event.getHWRResultSet().getWords().get(i).getCandidates().peek().getRecogntionResult());
            }
            update();
        }
    }

    public void clearHwrResults() {
        hwrResults.clear();
    }

    private void update() {
        if (this.hwrResults != null) {
            // TODO handle to much entries (display capacity)
            for (TextOverlay to : hwr) {
                to.detach();
            }
            for (TextOverlay to : titles) {
                to.detach();
            }
            if (entryMap.containsKey(mapping.get(selectedTitle))) {
                for (TextOverlay entry : entryMap.get(mapping.get(selectedTitle))) {
                    // TODO detach!
                    entry.fadeOut(1.f);
                }
            }
            int x = (int) (-width / 2.5f);
            mapping = new HashMap<Integer, Integer>();
            entriesMapping = new ArrayList<Map<Integer, Integer>>();
            entryMap = new HashMap<Integer, ArrayList<TextOverlay>>();
            titles = new ArrayList<TextOverlay>();
            hwr = new ArrayList<TextOverlay>();
            result = new HashMap<Integer, String>();
            for (int i = 0; i < hwrResults.size(); i++) {
                Node node = new Node("HWR Search Text Node");
                node.setLocalTranslation(x, getTextY(2 + i), 0);
                getNode().attachChild(node);
                hwr.add(new TextOverlay(node, this));
                hwr.get(i).setText(hwrResults.get(i));
                hwr.get(i).setSize(FONT_SIZE);
                hwr.get(i).setFont(1);
                hwr.get(i).setColor(new ColorRGBA(0.75f, 0.75f, 0.75f, 0));
                hwr.get(i).fadeIn((float) i * 1.f + 1.f);
            }
        }
        // TODO max limit for list
        int x;
        result = new HashMap<Integer, String>();
        for (TextOverlay title : titles) {
            title.detach();
        }
        titles = new ArrayList<TextOverlay>();
        search();
        log.debug("search results: " + result);
        int index = 0;
        for (int i = 0; i < CovidaConfiguration.getInstance().videoSources.size(); i++) {
            if (result.containsKey(new Integer(i))) {
                // display video title
                String title = CovidaConfiguration.getInstance().videoSources.get(i).videoName;
                log.debug("draw title: " + title);
                x = (int) (-quad.getWidth() / 4.15f);
                Node node = new Node("AnnotationSearch title node");
                node.setLocalTranslation(x, getTextY(index + 2), 0);
                getNode().attachChild(node);
                titles.add(new TextOverlay(node, this));
                titles.get(titles.size() - 1).setFont(1);
                titles.get(titles.size() - 1).setSize(FONT_SIZE);
                titles.get(titles.size() - 1).setAlign(Align.Left);
                titles.get(titles.size() - 1).setText(title);
                mapping.put(index, i);
                index++;
            }
        }
        if (titles.size() > index) {
            for (int i = index; i < titles.size(); i++) {
                titles.remove(i);
            }
        }
        if (titles.size() > 0) {
            setSelectedTitle(0);
        }
    }

    private float getTextY(int position) {
        return textBeginY - TEXT_SPACER - FONT_SIZE * (position)
                - (float) FONT_SIZE / 2.f;
    }

    /**
     * Checks the hwr result and chooses the best result.
     * 
     * @param hwrResultSet
     * @return
     */
    protected String checkHWRResult(HWRResultSet hwrResultSet) {
        hwrResultSet.getWords();
        return hwrResultSet.topResult();
    }

    @Override
    public void unRegisterWithInputHandler(TouchInputHandler input) {
        input.removeAction(touchAction);
    }

    @Override
    public void registerWithInputHandler(TouchInputHandler input) {
        input.addAction(touchAction);
    }

    @Override
    public void registerWithInputHandler(PenInputHandler input) {
        input.addAction(this.drawAction);
        input.addAction(this.hwrAction);
    }

    /**
     * 
     * @param x
     * @param y
     * @param angle
     *            - angle in degree
     * @param width
     * @param height
     */
    private void addSpacer(int x, int y, float angle, int width, int height) {
        Quaternion q = new Quaternion();
        q = q.fromAngleAxis(FastMath.DEG_TO_RAD * angle, new Vector3f(0, 0, 1));
        Quad spacerQuad = new Quad("Spacer", width, height);
        spacerQuad.setRenderState(tsSpacer);
        spacerQuad.setRenderState(initalizeBlendState());
        spacerQuad.updateRenderState();
        spacerQuad.setLocalTranslation(x, y, 0);
        spacerQuad.setLocalRotation(q);
        getNode().attachChild(spacerQuad);
    }

    @Override
    public void touch(Map<Integer, TouchActionEvent> event) {
        for (TouchActionEvent e : event.values()) {
            TouchState state = e.getTouchState();
            if (state == TouchState.TOUCH_BIRTH) {
                if (inArea(e.getX(), e.getY())) {
                    getLockState().forceTouchLock(e.getID(), getId());
                    lastTouch.put(e.getID(), new Vector2f(e.getX(), e.getY()));
                }
            }
            if (getLockState().isTouchLocked(e.getID())) {
                if (getLockState().getTouchLock(e.getID()) == getId()) {
                    if (state == TouchState.TOUCH_BIRTH) {
                        setActiveTitle(getSelectedTitle(e.getX(), e.getY()));
                        setActiveEntry(getSelectedEntry(e.getX(), e.getY()));
                    } else if (state == TouchState.TOUCH_LIVING) {
                        setActiveTitle(getSelectedTitle(e.getX(), e.getY()));
                        setActiveEntry(getSelectedEntry(e.getX(), e.getY()));
                        Vector3f nodePosition = getRootNode().getLocalTranslation();
                        if (nodePosition.y > getDisplay().y / 2.f) {
                            float diffY = (nodePosition.y - lastTouch.get(e.getID()).y) - (nodePosition.y - e.getY());
                            yDrag = yDrag + diffY;
                        } else {
                            float diffY = (nodePosition.y - lastTouch.get(e.getID()).y) - (nodePosition.y - e.getY());
                            yDrag = yDrag - diffY;
                        }
                        if (nodePosition.x > getDisplay().x / 2.f) {
                            float diffX = (nodePosition.x - lastTouch.get(e.getID()).x) - (nodePosition.x - e.getX());
                            xDrag = xDrag + diffX;
                        } else {
                            float diffX = (nodePosition.x - lastTouch.get(e.getID()).x) - (nodePosition.x - e.getX());
                            xDrag = xDrag - diffX;
                        }
                        lastTouch.put(e.getID(),
                                new Vector2f(e.getX(), e.getY()));
                    } else if (state == TouchState.TOUCH_DEAD) {
                        lastTouch.remove(e.getID());
                        if (yDrag > getDisplay().y / 30.f
                                && xDrag > getDisplay().x / 30.f) {
                            this.close();
                        }
                        yDrag = 0;
                        setSelectedTitle(getSelectedTitle(e.getX(), e.getY()));
                        setSelectedEntry(getSelectedEntry(e.getX(), e.getY()));
//                        getLockState().removeTouchLock(e.getID());
                    }
                }
            }
        }
    }

    @Override
    public void draw(Queue<PenActionEvent> penEvent) {
        // TODO
    }

    @Override
    public void draw(ShapeEvent shape) {
        /** do nothing */
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

    /**
     * 
     * @return
     */
    public int getHeight() {
        return height;
    }

    /**
     * 
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     * Closes the DisplayInfoComponent
     */
    public void close() {
        detach = true;
        for (int i = 0; i < getNode().getControllerCount(); i++) {
            getNode().removeController(i);
        }
        if (touchInputHandler != null) {
            unRegisterWithInputHandler(touchInputHandler);
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

    public boolean isClosing() {
        return detach;
    }

    private void search() {
        result = new HashMap<Integer, String>();
        if (hwrResults != null) {
            /** video index */
            int i;
            for (String hwr : hwrResults) {
                // exact search
                for (i = 0; i < CovidaConfiguration.getInstance().videoSources.size(); i++) {
                    for (String s : CovidaConfiguration.getInstance().videoSources.get(i).videoName.split(" ")) {
                        if (s.equals(hwr)) {
                            result.put(
                                    new Integer(i),
                                    CovidaConfiguration.getInstance().videoSources.get(i).videoName);
                            break;
                        }
                    }
                }
                // case insensitive search
                for (i = 0; i < CovidaConfiguration.getInstance().videoSources.size(); i++) {
                    for (String s : CovidaConfiguration.getInstance().videoSources.get(i).videoName.split(" ")) {
                        if (s.equalsIgnoreCase(hwr)) {
                            result.put(
                                    new Integer(i),
                                    CovidaConfiguration.getInstance().videoSources.get(i).videoName);
                            break;
                        }
                    }
                }
                // wrap around search
                for (i = 0; i < CovidaConfiguration.getInstance().videoSources.size(); i++) {
                    for (String s : CovidaConfiguration.getInstance().videoSources.get(i).videoName.split(" ")) {
                        if (s.contains(hwr)) {
                            result.put(
                                    new Integer(i),
                                    CovidaConfiguration.getInstance().videoSources.get(i).videoName);
                            break;
                        }
                    }
                }
                // Levenshtein-Distance
                for (i = 0; i < CovidaConfiguration.getInstance().videoSources.size(); i++) {
                    for (String s : CovidaConfiguration.getInstance().videoSources.get(i).videoName.split(" ")) {
                        int distance = StringUtils.getLevenshteinDistance(hwr,
                                s);
                        if (distance < 3) {
                            result.put(
                                    new Integer(i),
                                    CovidaConfiguration.getInstance().videoSources.get(i).videoName);
                            break;
                        }
                    }
                }
            }
        }
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
     * Open animation of the DisplayInfoComponent
     */
    public void open() {
        detach = false;
        for (int i = 0; i < getNode().getControllerCount(); i++) {
            getNode().removeController(i);
        }
        if (touchInputHandler != null) {
            registerWithInputHandler(touchInputHandler);
        }
        SpatialTransformer st = new SpatialTransformer(1);
        st.setObject(getNode(), 0, -1);
        st.setPosition(0, 0.f, new Vector3f(getNode().getLocalTranslation()));
        st.setPosition(0, 0.5f, new Vector3f(0, 75, 0));
        st.interpolateMissing();
        getNode().addController(st);
        update();
    }

    @Override
    public void unRegisterWithInputHandler(PenInputHandler input) {
        input.removeAction(drawAction);
        input.removeAction(hwrAction);
    }

    /**
     * 
     * @param x
     * @param y
     * @return <code>Integer</code> , entryId or -1 if there is no entry on x,y
     */
    public int getSelectedEntry(int x, int y) {
        if (entryMap.containsKey(mapping.get(selectedTitle))) {
            for (int i = 0; i < entryMap.get(mapping.get(selectedTitle)).size(); i++) {
                if (entryMap.get(mapping.get(selectedTitle)).get(i).inArea(x, y)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 
     * @param x
     * @param y
     * @return <code>Integer</code> , tilteId or -1 if there is no title on x,y
     */
    public int getSelectedTitle(int x, int y) {
        for (int i = 0; i < titles.size(); i++) {
            if (titles.get(i).inArea(x, y)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets the active entry on the ListField
     * 
     * @param index
     */
    public void setActiveEntry(int index) {
        if (entryMap.get(mapping.get(selectedTitle)) != null && index > -1
                && index < entryMap.get(mapping.get(selectedTitle)).size()) {
            for (TextOverlay entry : entryMap.get(mapping.get(selectedTitle))) {
                entry.setColor(defaultColor);
            }
            entryMap.get(mapping.get(selectedTitle)).get(index).setColor(activeColor);
            entryMap.get(mapping.get(selectedTitle)).get(index).scaleAnimation(2.f, 2.f);
        }
    }

    /**
     * Sets the selected entry on the ListField
     * 
     * @param index
     */
    public void setSelectedEntry(int index) {
        if (index > -1
                && index < entryMap.get(mapping.get(selectedTitle)).size() + 1) {
            for (TextOverlay entry : entryMap.get(mapping.get(selectedTitle))) {
                entry.setColor(defaultColor);
            }
            if (entryMap.size() > selectedTitle) {
                if (entryMap.get(mapping.get(selectedTitle)).size() > index) {
                    entryMap.get(mapping.get(selectedTitle)).get(index).setColor(selectedColor);
                    videos.get(mapping.get(selectedTitle)).loadAnnotationData(
                            entriesMapping.get(selectedTitle).get(index));
                    videos.get(mapping.get(selectedTitle)).pause();
                    videos.get(mapping.get(selectedTitle)).toFront();
                } else {
                    log.debug("entries.get(" + selectedTitle + ").size()<"
                            + index);
                }
            } else {
                log.debug("entries.size()<=" + selectedTitle);
            }

        }
    }

    /**
     * Sets the active entry on the ListField
     * 
     * @param titleID
     */
    public void setActiveTitle(int titleID) {
        if (titleID > -1 && titleID < titles.size()) {
            for (TextOverlay title : titles) {
                title.setColor(defaultColor);
            }
            titles.get(titleID).setColor(activeColor);
        }
    }

    /**
     * Sets the selected title on the SearchField
     * 
     * @param index
     */
    public void setSelectedTitle(int index) {
        if (index > -1 && index < titles.size() + 1) {
            if (entryMap.containsKey(mapping.get(index))) {
                for (TextOverlay title : titles) {
                    title.setColor(defaultColor);
                }
                if (entryMap.containsKey(mapping.get(selectedTitle))) {
                    for (TextOverlay entry : entryMap.get(mapping.get(selectedTitle))) {
                        // TODO detach!
                        entry.fadeOut(1.f);
                    }
                }
                titles.get(index).setColor(selectedColor);
                selectedTitle = index;
                for (TextOverlay entry : entryMap.get(mapping.get(index))) {
                    entry.attach(getNode());
                    entry.fadeIn(1.5f);
                }
            } else {
                log.debug("!(entryMap.containsKey(index)) index: " + index
                        + ")");
            }
        }
    }

    public void setTouchInputHandler(TouchInputHandler input) {
        log.debug("Set: " + input);
        touchInputHandler = input;
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
    protected void touchDeadAction(int touchId) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void touchAction(TouchActionEvent e) {
        
    }
}
class DetachHandler implements Runnable {

    private VideoSearchField searchField;
    private Object obj;
    private int delay;
    private Logger log = Logger.getLogger(DetachHandler.class);

    public Object getObject() {
        return obj;
    }

    public DetachHandler(VideoSearchField infoField, int delay) {
        this.searchField = infoField;
        this.delay = delay;
        obj = new Object();
    }

    public void run() {
        synchronized (obj) {
            try {
                obj.wait(delay);
            } catch (InterruptedException e) {
                log.error(e);
            }
            if (searchField.isClosing()) {
                searchField.detach();
            }
        }
    }
}
