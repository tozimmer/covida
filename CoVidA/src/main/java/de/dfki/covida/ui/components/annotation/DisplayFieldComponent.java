/*
 * DisplayFieldComponent.java
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
package de.dfki.covida.ui.components.annotation;

import com.jme.animation.SpatialTransformer;
import com.jme.image.Texture;
import com.jme.image.Texture.WrapMode;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.angelfont.BitmapText;
import com.jmex.awt.swingui.ImageGraphics;
import de.dfki.covida.data.ShapePoints;
import de.dfki.covida.data.VideoAnnotation;
import de.dfki.covida.data.VideoAnnotationData;
import de.dfki.covida.ui.components.AnimationHandler;
import de.dfki.covida.ui.components.CovidaComponent;
import de.dfki.covida.ui.components.TextOverlay;
import de.dfki.covida.ui.components.video.VideoComponent;
import de.dfki.touchandwrite.action.DrawAction;
import de.dfki.touchandwrite.action.HWRAction;
import de.dfki.touchandwrite.action.TouchAction;
import de.dfki.touchandwrite.action.TouchActionEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.DragEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.RotationGestureEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.ZoomEvent;
import de.dfki.touchandwrite.input.pen.hwr.HandwritingRecognitionEvent;
import de.dfki.touchandwrite.input.touch.event.TouchState;
import de.dfki.touchandwrite.shape.ShapeType;
import de.dfki.touchandwrite.visual.components.ComponentType;
import de.dfki.touchandwrite.visual.components.ImageBehaviorListener;
import de.dfki.touchandwrite.visual.input.TouchInputHandler;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Component which displays annotation data of VideoComponent.
 *
 * @author Tobias Zimmermann
 *
 */
public class DisplayFieldComponent extends CovidaComponent {

    /**
     * Generated ID
     */
    private static final long serialVersionUID = 4586629113667692802L;
    /**
     * Configuration
     */
    public ColorRGBA color = new ColorRGBA(1, 1, 1, 0);
    static ColorRGBA defaultColor = new ColorRGBA(1, 1, 1, 1);
    static ColorRGBA activeColor = new ColorRGBA(1, 0, 0, 1);
    static ColorRGBA selectedColor = new ColorRGBA(0, 1, 0, 1);
    static final int ANIMATION_DURATION = 750;
    private static final int DEFAULT_FONT_SIZE = 18;
    static final int DEFAULT_WIDTH = 112;
    static final int DEFAULT_HEIGHT = 250;
    private int textBeginY;
    static int WIDTH = 112;
    static int HEIGHT = 250;
    private static final int DEFAULT_CHARACTER_LIMIT = 14;
    /**
     * Annotation data
     */
    private VideoAnnotationData data;
    /**
     * Logger
     */
    private Logger log = Logger.getLogger(DisplayFieldComponent.class);
    /**
     * image
     */
    private String image;
    /**
     * State of the texture.
     */
    private TextureState ts;
    /**
     * Texture.
     */
    private Texture texture;
    /**
     * Quad for image
     */
    protected Quad quad;
    /**
     * Temporary variables
     */
    private String videoSource;
    private ShapePoints shapePoints;
    private List<ImageBehaviorListener> listeners;
    /**
     * Handwriting event action.
     */
    private HWRAction hwrAction;
    /**
     * Handwriting
     */
    protected List<HandwritingRecognitionEvent> hwrEvents;
    /**
     * Draw action.
     */
    private DrawAction drawAction;
    /**
     * Drawing will be done with Java2D.
     */
    protected ImageGraphics g2d;
    @SuppressWarnings("unused")
    private int lastY;
    @SuppressWarnings("unused")
    private int lastX;
    private boolean locked;
    private VideoComponent video;
    private Node snapshotNode;
//    private DisplayFieldComponentOverlay overlay;
    private String title;
    private long time_start;
    private long time_end;
    private DisplayFieldComponent listField;
    @SuppressWarnings("unused")
    private Color currentPenColor;
    private boolean penPressure;
    private float penThickness;
    private ArrayList<TextOverlay> entries;
    private ArrayList<TextOverlay> descriptionText;
    private int entry;
    private TextureState tsSpacer;
    private Texture textureSpacer;
    private Vector3f defaultScale;
    private Quaternion defaultRotation;
    private Vector3f defaultTranslation;
    private ShapeType shapeType;
    private TextOverlay titleTextOverlay;
    private TextOverlay timeOverlay;
    private TextOverlay timeTextOverlay;
    private TextOverlay descriptionOverlay;
    private boolean open;
    private RemoveHandler removeHandler;
    private Thread removeHandlerThread;
    private ArrayList<Long> times;
    private int descriptionBeginY;
    /*
     * Overlay
     */
    private TextOverlay textOverlay;
    private ArrayList<Quad> overlayMenu;
    private BitmapText txt;
    private static final int FONT_SIZE = 30;
    protected Quad overlayDefault;

    /**
     *
     * @return Node order
     */
    @Override
    public ArrayList<Spatial> getNodeOrder() {
        return (new ArrayList<Spatial>(getNode().getParent().getChildren()));
    }

    /**
     * List field constructor
     *
     * @param resource
     * @param id
     * @param node
     * @param width
     * @param height
     */
    public DisplayFieldComponent(String resource, VideoComponent video,
            Node node, int width, int height) {
        this(resource, video, null, node, width, height);
    }

    /**
     * Info field constructor
     *
     * @param resource
     * @param video
     * @param listField
     * @param id
     * @param node
     * @param width
     * @param height
     */
    public DisplayFieldComponent(String resource, VideoComponent video,
            DisplayFieldComponent listField, Node node, int width, int height) {
        super(ComponentType.COMPONENT_2D, "DisplayFieldComponent", node);
        this.video = video;
        WIDTH = width;
        HEIGHT = height;
        defaultScale = new Vector3f(getNode().getLocalScale().x, getNode().getLocalScale().y, getNode().getLocalScale().z);
        defaultRotation = new Quaternion(getNode().getLocalRotation().x,
                getNode().getLocalRotation().y, getNode().getLocalRotation().z,
                getNode().getLocalRotation().w);
        defaultTranslation = new Vector3f(getNode().getLocalTranslation().x,
                getNode().getLocalTranslation().y, getNode().getLocalTranslation().z);
        entry = -1;
        image = resource;
        entries = new ArrayList<TextOverlay>();
        overlayMenu = new ArrayList<Quad>();
        if (listField != null) {
            this.listField = listField;
            data = VideoAnnotationData.load(new File(video.getFile() + ".xml"));
            hwrEvents = new ArrayList<HandwritingRecognitionEvent>();
            snapshotNode = new Node("DisplayInfoComponent Snapshot Node");
        }
    }

    public DisplayFieldType getType() {
        if (snapshotNode != null) {
            if (entry != -1) {
                return DisplayFieldType.EDIT;
            }
            return DisplayFieldType.INFO;
        }
        return DisplayFieldType.LIST;
    }

    private int getFontSize() {
        return (int) (1.2f * (float) DEFAULT_FONT_SIZE * ((float) getHeight() / (float) DEFAULT_HEIGHT));
    }

    private int getFontSpacer() {
        return (int) ((float) getFontSize() * 0.8f);
    }

    private int getTextSpacer() {
        return (int) ((float) getFontSize() / 5.f);
    }

    private int getCharacterLimit() {
        return (int) ((float) DEFAULT_CHARACTER_LIMIT * ((float) getFontSize() / (float) DEFAULT_FONT_SIZE));
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
        super.setRootNode(video.getRootNode());
        removeHandler = new RemoveHandler(getNode(), 1);
        removeHandlerThread = new Thread(removeHandler);
        initTextures();
        textBeginY = (int) (quad.getHeight() / 12.f);
        // Attach overlay
//        overlay = new DisplayFieldComponentOverlay(this);
//        overlay.initComponent();
//        getNode().attachChild(overlay);
        /*
         * Overlay
         */
        textOverlay = new TextOverlay(getNode(), this);
        textOverlay.setSize(FONT_SIZE);
        if (getType().equals(DisplayFieldType.INFO)) {
            initalizeOverlayQuads(initalizeBlendState());
            getNode().attachChild(this.overlayDefault);
        } else {
            initalizeListOverlayQuads(initalizeBlendState());
        }
        /*
         * Overlay End
         */
        touchAction = new TouchAction(this);
        if (getType().equals(DisplayFieldType.LIST)) {
            int x = (int) (0);
            float y = getTextY(0);
            Node node = new Node("DisplayInfoComponent Entry Node");
            node.setLocalTranslation(x, y, 0);
            getNode().attachChild(node);
            TextOverlay to = new TextOverlay(node, this);
            to.setSize(getFontSize());
            to.setText("Entries:");
            to.setFont(1);
            addSpacer(x, (int) (y - (float) getFontSpacer() / 2.f),
                    (int) (quad.getWidth() / 1.1f), getTextSpacer());
        } else if (getType().equals(DisplayFieldType.INFO)
                || getType().equals(DisplayFieldType.EDIT)) {
            Node node = new Node("Info Field - Title Text Node");
            getNode().attachChild(node);
            node.setLocalTranslation(0, textBeginY, 0);
            titleTextOverlay = new TextOverlay(node, this);
            titleTextOverlay.setFont(1);
            titleTextOverlay.setSize(getFontSize());
            int y = (int) (titleTextOverlay.getNode().getLocalTranslation().y + ((float) titleTextOverlay.getFontSize() / 2.f));
            addSpacer(0, y, (int) (quad.getWidth() / 1.1f), getTextSpacer());
            node = new Node("Info Field - Time Node");
            getNode().attachChild(node);
            textBeginY = (int) (textBeginY - (float) getFontSpacer() * 1.25f - getTextSpacer());
            node.setLocalTranslation(0, textBeginY, 0);
            timeOverlay = new TextOverlay(node, this);
            timeOverlay.setFont(1);
            timeOverlay.setSize(getFontSize());
            node = new Node("Info Field - Time Text Node");
            getNode().attachChild(node);
            timeOverlay.setText("Time:");
            textBeginY = (int) (textBeginY - (float) getFontSpacer() * 1.25f);
            node.setLocalTranslation(0, textBeginY, 0);
            timeTextOverlay = new TextOverlay(node, this);
            timeTextOverlay.setFont(1);
            timeTextOverlay.setSize(getFontSize());
            y = (int) (timeTextOverlay.getNode().getLocalTranslation().y + ((float) timeTextOverlay.getFontSize() / 2.f));
            addSpacer(0, y, (int) (quad.getWidth() / 1.1f), getTextSpacer());
            node = new Node("Info Field - Description Node");
            getNode().attachChild(node);
            textBeginY = (int) (textBeginY - (float) getFontSpacer() * 1.25f - getTextSpacer());
            node.setLocalTranslation(0, textBeginY, 0);
            descriptionOverlay = new TextOverlay(node, this);
            descriptionOverlay.setFont(1);
            descriptionOverlay.setSize(getFontSize());
            descriptionText = new ArrayList<TextOverlay>();
            if (data.size() > 0) {
                listField.updateEntries(data.getTimeList());
            }
        }
    }

    private void initTextures() {
        // ---- Background Texture state initialization ----
        ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setCorrectionType(TextureState.CorrectionType.Perspective);
        ts.setEnabled(true);
        texture = TextureManager.loadTexture(getClass().getClassLoader().getResource(image));
        ts.setTexture(texture);

        quad = new Quad("Display image quad", WIDTH, HEIGHT);
        quad.setRenderState(ts);
        quad.setRenderState(this.initalizeBlendState());
        quad.updateRenderState();
        getNode().attachChild(quad);
        if (getType().equals(DisplayFieldType.INFO)) {
            snapshotNode.setLocalTranslation(0, HEIGHT * (0.329f), 0);
            getNode().attachChild(snapshotNode);
        } else {
        }
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

    public void setSnapshot() {
        setSnapshot(data.size());
    }

    public void setSnapshot(int index) {
        TextureManager.clearCache();
        Texture snapshotTexture = TextureManager.loadTexture(video.getFile()
                + "." + index + ".png",
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        int height = snapshotTexture.getImage().getHeight();
        int width = snapshotTexture.getImage().getWidth();
        snapshotNode.detachAllChildren();
        if ((float) height / (float) width > 1) {
            snapshotNode.setLocalScale(new Vector3f((float) width
                    / (float) height, 1, 1));
        } else {
            snapshotNode.setLocalScale(new Vector3f(1, (float) height
                    / (float) width, 1));
        }
        if (snapshotNode != null) {
            Quad snapshotQuad = new Quad("snapshot", getWidth() * (0.55f),
                    getWidth() * (0.55f));
            snapshotQuad.setRenderState(initalizeBlendState());
            snapshotQuad.updateRenderState();
            snapshotNode.attachChild(snapshotQuad);
            TextureState snapshotTextureState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
            snapshotTextureState.setTexture(snapshotTexture);
            snapshotQuad.setRenderState(snapshotTextureState);
            snapshotNode.attachChild(snapshotQuad);
            snapshotQuad.updateRenderState();
        }
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

    protected void fireMinZoomEvent() {
        for (ImageBehaviorListener lis : listeners) {
            lis.zoomedToMin();
        }
    }

    public void addListener(ImageBehaviorListener lis) {
        if (listeners == null) {
            this.listeners = new ArrayList<ImageBehaviorListener>();
        }
        this.listeners.add(lis);
    }

    public void setTitle(String title) {
        if (titleTextOverlay != null && title != null) {
            this.title = title;
            titleTextOverlay.setText(title);
            titleTextOverlay.setSize(getFontSize());
            if (title.length() > getCharacterLimit()) {
                float factor = (float) title.length() / getCharacterLimit();
                titleTextOverlay.setSize((int) ((float) getFontSize() / factor));
            }
        }
    }

    public void setTime(long time) {
        if (timeOverlay != null && timeTextOverlay != null) {
            this.time_start = time;
            this.time_end = time + 10;
            timeTextOverlay.setText(video.getTimeCode(time));
//			if (video.getTimeCode(time).length() > getCharacterLimit()) {
//				float factor = (float) video.getTimeCode(time).length() / getCharacterLimit();
//				timeTextOverlay.setSize((int) ((float) getFontSize() / factor));
//			}	
        }
    }

    public void drawHwrResult(ArrayList<String> hwrResults) {
        if (descriptionOverlay != null && descriptionText != null) {
            descriptionOverlay.setText("Description:");
            descriptionOverlay.setSize(getFontSize());
            int count = hwrResults.size() + descriptionText.size();
            int start = descriptionText.size();
            if (start == 0) {
                descriptionBeginY = (int) (textBeginY - (float) getFontSpacer());
            }
            for (int i = start; i < count; i++) {
                Node node = new Node("Display Field Description Node");
                descriptionBeginY = (int) (descriptionBeginY - (float) getFontSpacer() * 0.9f);
                node.setLocalTranslation(0, descriptionBeginY, 0);
                getNode().attachChild(node);
                descriptionText.add(new TextOverlay(node, this));
                descriptionText.get(descriptionText.size() - 1).setText(
                        hwrResults.get(i - start));
                descriptionText.get(descriptionText.size() - 1).setFont(1);
                descriptionText.get(descriptionText.size() - 1).setSize(
                        getFontSize());
                if (hwrResults.get(i - start).length() > getCharacterLimit()) {
                    float factor = (float) hwrResults.get(i - start).length()
                            / getCharacterLimit();
                    descriptionText.get(descriptionText.size() - 1).setSize(
                            (int) ((float) getFontSize() / factor));
                }
            }
            int size = this.descriptionText.size();
            if (size > 5) {
                // TODO Possibility to display more than 5 entries
            }
        } else {
            log.debug("ID: "
                    + getId()
                    + " !(descriptionOverlay != null && descriptionText != null)");
        }
    }

    public void clearDescriptionText() {
        for (int i = 0; i < descriptionText.size(); i++) {
            if (getNode().hasChild(descriptionText.get(i).getNode())) {
                getNode().detachChild(descriptionText.get(i).getNode());
            } else {
                log.debug("TextOverlay Node wasn't attached during clearDescriptionText");
            }
        }
        descriptionText = new ArrayList<TextOverlay>();
        descriptionBeginY = textBeginY;
    }

    @Override
    public void unRegisterWithInputHandler(TouchInputHandler input) {
        input.removeAction(touchAction);
    }

    @Override
    public void registerWithInputHandler(TouchInputHandler input) {
        input.addAction(touchAction);
    }

    private void addSpacer(int x, int y, int width, int height) {
        Quad spacerQuad = new Quad("Spacer", width, height);
        spacerQuad.setRenderState(tsSpacer);
        spacerQuad.setRenderState(this.initalizeBlendState());
        spacerQuad.updateRenderState();
        spacerQuad.setLocalTranslation(x, y, 0);
        getNode().attachChild(spacerQuad);
    }

    /**
     * Resets textNode
     */
    public void resetInfo() {
        if (getType().equals(DisplayFieldType.INFO)
                || getType().equals(DisplayFieldType.EDIT)) {
            entry = -1;
            clearDescriptionText();
        } else if (getType().equals(DisplayFieldType.LIST)) {
            for (TextOverlay e : entries) {
                e.setColor(defaultColor);
            }
        }
    }

    /**
     *
     * @return
     */
    public int getHeight() {
        return HEIGHT;
    }

    /**
     *
     * @return
     */
    public int getWidth() {
        return WIDTH;
    }

    /**
     * Closes the DisplayInfoComponent
     */
    @Override
    public void close() {
        open = false;
        SpatialTransformer st = getController();
        if (getType().equals(DisplayFieldType.INFO)) {
            // methods for info field closing
            if (getType().equals(DisplayFieldType.INFO)) {
                title = null;
                time_start = 0;
                resetInfo();
                video.clearBoard();
                // Close animation (Info Field)
                st.setObject(getNode(), 0, -1);
                Quaternion x0 = new Quaternion();
                x0.fromAngleAxis(0, new Vector3f(0, 0, 1));
                st.setRotation(0, 0, x0);
                Quaternion x180 = new Quaternion();
                x180.fromAngleAxis(FastMath.DEG_TO_RAD * 180, new Vector3f(0,
                        0, 1));
                st.setRotation(0, (float) ANIMATION_DURATION / 2000.f, x180);
                st.setScale(0, (float) ANIMATION_DURATION / 2000.f,
                        new Vector3f(0.5f, 0.5f, 0.5f));
                Quaternion x360 = new Quaternion();
                x360.fromAngleAxis(FastMath.DEG_TO_RAD * 360, new Vector3f(0,
                        0, 1));
                st.setRotation(0, (float) ANIMATION_DURATION / 1000.f, x360);
                st.setScale(0, (float) ANIMATION_DURATION / 1000.f,
                        new Vector3f(0.0f, 0.0f, 0.0f));
                st.interpolateMissing();
            }
        } else if (getType().equals(DisplayFieldType.LIST)) {
            // fade out entry list
            for (TextOverlay e : entries) {
                e.fadeOut((float) ANIMATION_DURATION / 1000);
            }
            // Close animation List Field
            st.setObject(getNode(), 0, -1);
            st.setScale(0, 0f, new Vector3f(getNode().getLocalScale()));
            st.setPosition(0, 0f, new Vector3f(getNode().getLocalTranslation()));
            st.setScale(0, (float) ANIMATION_DURATION / 1000.f, new Vector3f(
                    0.0f, 1, 1));
            st.setPosition(0, (float) ANIMATION_DURATION / 1000.f,
                    defaultTranslation);
            st.interpolateMissing();
        } else if (getType().equals(DisplayFieldType.EDIT)) {
            resetInfo();
            removeEntry(entry);
            listField.updateEntries(data.getTimeList());
            listField.drawEntries();
            title = null;
            time_start = 0;
            video.clearBoard();
            // Close animation (Info Field)
            st.setObject(getNode(), 0, -1);
            Quaternion x0 = new Quaternion();
            x0.fromAngleAxis(0, new Vector3f(0, 0, 1));
            st.setRotation(0, 0, x0);
            Quaternion x180 = new Quaternion();
            x180.fromAngleAxis(FastMath.DEG_TO_RAD * 180, new Vector3f(0, 0, 1));
            st.setRotation(0, (float) ANIMATION_DURATION / 2000.f, x180);
            st.setScale(0, (float) ANIMATION_DURATION / 2000.f, new Vector3f(
                    0.5f, 0.5f, 0.5f));
            Quaternion x360 = new Quaternion();
            x360.fromAngleAxis(FastMath.DEG_TO_RAD * 360, new Vector3f(0, 0, 1));
            st.setRotation(0, (float) ANIMATION_DURATION / 1000.f, x360);
            st.setScale(0, (float) ANIMATION_DURATION / 1000.f, new Vector3f(
                    0.0f, 0.0f, 0.0f));
            st.interpolateMissing();
            entry = -1;
        }
        resetInfo();
        removeHandler.setTarget(0);
        if (!removeHandlerThread.isAlive()) {
            removeHandlerThread = new Thread(removeHandler);
            removeHandlerThread.start();
        }
    }

    /**
     * Removes annotation entry
     *
     * @param entry
     */
    private void removeEntry(int entry) {
        data.remove(entry);
    }

    /**
     *
     */
    public void resetNode() {
        this.getNode().setLocalTranslation(defaultTranslation);
        this.getNode().setLocalScale(defaultScale);
        this.getNode().setLocalRotation(defaultRotation);
    }

    /**
     * Saves annotation data from the InfoField
     */
    public void save() {
        log.debug("Save begin");
        open = false;
        if (getType().equals(DisplayFieldType.INFO)) {
            saveData();
            video.clearBoard();
            listField.updateEntries(data.getTimeList());
            listField.drawEntries();
            // Save animation
            SpatialTransformer st = getController();
            st.setObject(getNode(), 0, -1);
            st.setScale(0, 0.f, new Vector3f(getNode().getLocalScale()));
            st.setScale(0, 0.5f, new Vector3f(0.0f, 1, 1));
            st.interpolateMissing();
            resetInfo();
        } else if (getType().equals(DisplayFieldType.EDIT)) {
            saveData(entry);
            entry = -1;
            video.clearBoard();
            // Save animation
            SpatialTransformer st = getController();
            st.setObject(getNode(), 0, -1);
            st.setScale(0, 0.25f, new Vector3f(0.5f, 1, 1));
            st.setScale(0, 0.5f, new Vector3f(0.0f, 1, 1));
            st.interpolateMissing();
        }
        removeHandler.setTarget(0);
        if (!removeHandlerThread.isAlive()) {
            removeHandlerThread = new Thread(removeHandler);
            removeHandlerThread.start();
        }
    }

    private float getTextY(int position) {
        return quad.getHeight() / 2.1f
                - ((float) getFontSize() * ((float) (position + 1) * 1.1f));
    }

    private void drawEntries() {
        if (times != null) {
            // TODO max limit for list
            if (getType().equals(DisplayFieldType.LIST)) {
                log.debug("draw entries: " + times);
                for (TextOverlay e : entries) {
                    e.detach();
                }
                entries = new ArrayList<TextOverlay>();
                for (int i = 0; i < times.size(); i++) {
                    Node node = new Node("DisplayInfoComponent Entry Node");
                    node.setLocalTranslation(0, getTextY(i + 1), 0);
                    getNode().attachChild(node);

                    // TODO java.lang.OutOfMemoryError: Direct buffer memory
                    entries.add(new TextOverlay(node, this));

                    log.debug("draw Entry: " + video.getTimeCode(times.get(i)));
                    entries.get(i).setText(video.getTimeCode(times.get(i)));
                    entries.get(i).setFont(1);
                    entries.get(i).setSize((int) (getFontSize()));
                    entries.get(i).fadeIn((float) ANIMATION_DURATION / 125.f);
                }
            } else {
                log.debug("draw enries -> Type != LIST!");
            }
        }
    }

    /**
     *
     * @param times
     */
    private void updateEntries(ArrayList<Long> times) {
        this.times = times;
    }

    /**
     * Save annotation data to binary and XML files
     */
    private void saveData() {
        if (getType().equals(DisplayFieldType.INFO)) {
            String descriptions = "";
            boolean first = true;
            for (TextOverlay description : descriptionText) {
                if (first) {
                    descriptions = description.getText();
                    first = false;
                } else {
                    descriptions = descriptions + " " + description.getText();
                }
            }
            // TODO new annotation data if media has changed!
            data.title = title;
            data.videoSource = videoSource;
            VideoAnnotation annotation = new VideoAnnotation();
            annotation.time_start = time_start;
            annotation.time_end = time_end;
            annotation.shapeType = shapeType;
            annotation.shapePoints = shapePoints;
            annotation.description = descriptions;
            data.annotations.add(annotation);
            clearDescriptionText();
            data.save();
            data.export();
        }
    }

    /**
     * Save edited annotation Data to binary and XML files
     */
    private void saveData(int entryID) {
        if (getType().equals(DisplayFieldType.EDIT) && data.size() > entryID) {
            String descriptions = "";
            boolean first = true;
            for (TextOverlay description : descriptionText) {
                if (first) {
                    descriptions = description.getText();
                    first = false;
                } else {
                    descriptions = descriptions + " " + description.getText();
                }
            }
            // TODO new annotation data if media has changed!
            data.title = title;
            data.videoSource = videoSource;
            data.annotations.get(entryID).time_start = time_start;
            data.annotations.get(entryID).time_end = time_end;
            data.annotations.get(entryID).shapeType = shapeType;
            data.annotations.get(entryID).shapePoints = shapePoints;
            data.annotations.get(entryID).description = descriptions;
            clearDescriptionText();
            data.export();
            data.save();
        }

    }

    public void lockPosition(boolean lock) {
        this.locked = lock;
    }

    /**
     * Detach DisplayInfoComponent
     */
    public void detach() {
        if (this.getParent() != null) {
            this.getParent().detachChild(this);
            resetNode();
        }
    }

    /**
     * Open animation of the DisplayInfoComponent
     */
    public void open() {
        open = true;
        SpatialTransformer st = getController();
        st.setScale(0, 0f, new Vector3f(getNode().getLocalScale()));
        st.setScale(0, (float) ANIMATION_DURATION / 1000.f, defaultScale);
        st.interpolateMissing();
        removeHandler.setTarget(defaultScale.x);
        if (!removeHandlerThread.isAlive()) {
            removeHandlerThread = new Thread(removeHandler);
            removeHandlerThread.start();
        }
        if (getType().equals(DisplayFieldType.LIST)) {
            drawEntries();
        }
    }

    private SpatialTransformer getController() {
        SpatialTransformer st;
        if (getNode().getControllerCount() > 1) {
            for (int i = 1; i < getNode().getControllerCount(); i++) {
                getNode().removeController(i);
            }
        }
        if (getNode().getControllerCount() > 0) {
            st = (SpatialTransformer) getNode().getController(0);
        } else {
            st = new SpatialTransformer(1);
            getNode().addController(st);
            st.setObject(getNode(), 0, -1);
        }
        return st;
    }

    /**
     *
     * @param x
     * @param y
     * @return
     * <code>Integer</code> , entryId or -1 if there is no entry on x,y
     */
    public int getSelectedEntry(int x, int y) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).inArea(x, y)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets the active entry on the ListField
     *
     * @param entryID
     */
    public void setActiveEntry(int entryID) {
        if (entryID > -1 && entryID < entries.size()) {
            for (TextOverlay e : entries) {
                e.setColor(defaultColor);
            }
            entries.get(entryID).setColor(activeColor);
        }
    }

    /**
     * Sets the selected entry on the ListField
     *
     * @param entryID
     */
    public void setSelectedEntry(int entryID) {
        if (entryID > -1 && entryID < entries.size() + 1) {
            entry = entryID;
            for (TextOverlay e : entries) {
                e.setColor(defaultColor);
            }
            entries.get(entryID).setColor(selectedColor);
            video.loadAnnotationData(entryID);
        }
    }

    // /**
    // * Loads annotation data from <code>XML</code> file
    // *
    // */
    // private void loadFromXML(int videoId){
    // ArrayList<ArrayList<Point>> points = new ArrayList<ArrayList<Point>>();
    // if(xmlParser.getIndex(videoId)!=null){
    // times_start = xmlParser.getStart(videoId);
    // times_end = xmlParser.getEnd(videoId);
    // titles = new ArrayList<String>();
    // for (int i=0; i<times_start.size(); i++){
    // titles.add(video.getName());
    // }
    // descriptions = xmlParser.getDescriptions(videoId);
    // points = xmlParser.getPoints(videoId);
    // spatials = new ArrayList<Spatial>();
    // Vector2f tempMax = new Vector2f(0,0);
    // Vector2f tempMin = new Vector2f(9999,9999);
    // List<Point> pl = new ArrayList<Point>();
    // for(int i=0; i<points.size(); i++){
    // for(Point point : points.get(i)){
    // pl.add(point);
    // if (point.x<tempMin.x){
    // tempMin.x = point.x;
    // }
    // if (point.x>tempMax.x){
    // tempMax.x = point.x;
    // }
    // if (point.y<tempMin.y){
    // tempMin.y = point.y;
    // }
    // if (point.y>tempMax.y){
    // tempMax.y = point.y;
    // }
    // }
    // // this.spatials.add(ShapeUtils.toPolygon(polygon));
    // }
    // shapePointList.add(pl);
    // listField.updateEntries(times_start);
    // }
    // }
    /**
     *
     * @param entryID
     */
    public void loadData(int entryID) {
        if (this.getType().equals(DisplayFieldType.INFO)
                || this.getType().equals(DisplayFieldType.EDIT)) {
            if (data.size() > entryID) {
                resetInfo();
                setEntry(entryID);
                video.clearBoard();
                video.updateImage(data.annotations.get(entryID).shapeType,
                        data.annotations.get(entryID).shapePoints.getShapePoints());
                video.setTimePosition(data.annotations.get(entryID).time_start);
                setSnapshot(entryID);
                setTime(data.annotations.get(entryID).time_start);
                video.pause();
                setTitle(data.title);
                ArrayList<String> descriptions = new ArrayList<String>();
                descriptions.addAll(Arrays.asList(
                        data.annotations.get(entryID).description.split(" ")));
                drawHwrResult(descriptions);
            } else {
                log.debug("VideoAnnotationData has not enough entries VideoId: "
                        + video.getId()
                        + " title: "
                        + video.getName()
                        + " "
                        + data);
            }

        }
    }

    private void setEntry(int entryID) {
        entry = entryID;
    }

    public void setShapePoints(ShapePoints shapePoints) {
        this.shapePoints = shapePoints;
    }

    public void setVideoSource(String videoSource) {
        this.videoSource = videoSource;
    }

    public void reset() {
        SpatialTransformer st = getController();
        st.setPosition(0, 0, getNode().getLocalTranslation());
        st.setRotation(0, 0, getNode().getLocalRotation());
        st.setScale(0, 0, getNode().getLocalScale());
        st.setPosition(0, 1, defaultTranslation);
        st.setRotation(0, 1, defaultRotation);
        st.setScale(0, 1, defaultScale);
        st.interpolateMissing();
        if (!removeHandlerThread.isAlive()) {
            removeHandlerThread = new Thread(removeHandler);
            removeHandlerThread.start();
        }
    }

    public VideoComponent getVideo() {
        return video;
    }

    public VideoAnnotationData getAnnotationData() {
        return data;
    }

    @Override
    protected void dragAction(DragEvent event) {
    }

    @Override
    protected void rotationAction(RotationGestureEvent event) {
    }

    @Override
    protected void zoomAction(ZoomEvent event) {
    }

    public void setShapeType(ShapeType shapeType) {
        this.shapeType = shapeType;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    protected void touchDeadAction(int touchId) {
        // TODO Auto-generated method stub
    }

    /*
     * Overlay
     */
//    private BlendState initalizeBlendState() {
//        BlendState alpha = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
//        alpha.setEnabled(true);
//        alpha.setBlendEnabled(true);
//
//        alpha.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
//        alpha.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
//        alpha.setTestEnabled(true);
//        alpha.setTestFunction(BlendState.TestFunction.GreaterThan);
//        return alpha;
//    }
    private void initalizeOverlayQuads(BlendState alpha) {
        // Overlay Default
        Texture overlayDefaultTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(
                "media/textures/bg_info_default.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlayDefaultTexture.setWrap(WrapMode.Clamp);

        TextureState overlayDefaultState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlayDefaultState.setTexture(overlayDefaultTexture);

        this.overlayDefault = new Quad("Overlay-Default-Image-Quad",
                getWidth(), getHeight());

        overlayDefault.setRenderState(overlayDefaultState);
        overlayDefault.setRenderState(alpha);
        overlayDefault.updateRenderState();
        overlayDefault.getLocalTranslation().set(0, 0, 0);

        ArrayList<String> textureList = new ArrayList<String>();
        textureList.add("media/textures/bg_info_h.png");
        textureList.add("media/textures/bg_info_x.png");

        for (int i = 0; i < 2; i++) {
            Texture overlayMenuTexture = TextureManager.loadTexture(getClass().getClassLoader().getResource(textureList.get(i)),
                    Texture.MinificationFilter.BilinearNearestMipMap,
                    Texture.MagnificationFilter.Bilinear);
            overlayMenuTexture.setWrap(WrapMode.Clamp);

            TextureState overlayMenuState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
            overlayMenuState.setTexture(overlayMenuTexture);

            this.overlayMenu.add(new Quad(
                    ("Overlay-Video-Menu-Image-Quad-0" + i), getWidth(),
                    getHeight()));

            overlayMenu.get(overlayMenu.size() - 1).setRenderState(
                    overlayMenuState);
            overlayMenu.get(overlayMenu.size() - 1).setRenderState(alpha);
            overlayMenu.get(overlayMenu.size() - 1).updateRenderState();
            overlayMenu.get(overlayMenu.size() - 1).getLocalTranslation().set(new Vector3f(0, 0, 0));
        }
    }

    private void initalizeListOverlayQuads(BlendState alpha) {
        // Overlay Default
        Texture overlayDefaultTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(
                "media/textures/bg_info_blank.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlayDefaultTexture.setWrap(WrapMode.Clamp);

        TextureState overlayDefaultState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlayDefaultState.setTexture(overlayDefaultTexture);

        this.overlayDefault = new Quad("Overlay-Default-Image-Quad",
                getWidth(), getHeight());

        overlayDefault.setRenderState(overlayDefaultState);
        overlayDefault.setRenderState(alpha);
        overlayDefault.updateRenderState();
        overlayDefault.getLocalTranslation().set(0, 0, 0);

        ArrayList<String> textureList = new ArrayList<String>();
        textureList.add("media/textures/bg_info_blank.png");
        textureList.add("media/textures/bg_info_blank.png");

        for (int i = 0; i < 2; i++) {
            Texture overlayMenuTexture = TextureManager.loadTexture(getClass().getClassLoader().getResource(textureList.get(i)),
                    Texture.MinificationFilter.BilinearNearestMipMap,
                    Texture.MagnificationFilter.Bilinear);
            overlayMenuTexture.setWrap(WrapMode.Clamp);

            TextureState overlayMenuState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
            overlayMenuState.setTexture(overlayMenuTexture);

            this.overlayMenu.add(new Quad(
                    ("Overlay-Video-Menu-Image-Quad-0" + i), getWidth(),
                    getHeight()));

            overlayMenu.get(overlayMenu.size() - 1).setRenderState(
                    overlayMenuState);
            overlayMenu.get(overlayMenu.size() - 1).setRenderState(alpha);
            overlayMenu.get(overlayMenu.size() - 1).updateRenderState();
            overlayMenu.get(overlayMenu.size() - 1).getLocalTranslation().set(new Vector3f(0, 0, 0));
        }
    }

    /**
     * @param e
     */
    private void setOverlay(float xC, float yC) {
        if (getType().equals(DisplayFieldType.INFO)
                || getType().equals(DisplayFieldType.EDIT)) {
            int x = (int) (((getLocal(xC, yC).x + getWidth() / 2) / getWidth()) * 2);
            int y = (int) (((getLocal(xC, yC).y + getHeight() / 2) / getHeight()) * 10);
            if (x > -1 && x < 2 && y < 1) {
                lockPosition(true);
                if (getNode().hasChild(this.overlayMenu.get(x))) {
                } else if (!getNode().hasChild(this.overlayDefault)) {
                    for (int i = 0; i < 2; i++) {
                        if (getNode().hasChild(this.overlayMenu.get(i))) {
                            getNode().detachChild(this.overlayMenu.get(i));
                            getNode().attachChild(this.overlayMenu.get(x));
                        }
                    }
                } else {
                    getNode().detachChild(this.overlayDefault);
                    getNode().attachChild(this.overlayMenu.get(x));
                }
                if (getType().equals(DisplayFieldType.INFO)) {
                    if (x == 0) {
                        textOverlay.setText("Save Data");
                    } else if (x == 1) {
                        textOverlay.setText("Close");
                    }
                } else if (getType().equals(DisplayFieldType.EDIT)) {
                    if (x == 0) {
                        textOverlay.setText("Edit Data");
                    } else if (x == 1) {
                        textOverlay.setText("Delete");
                    }
                }
            } else {
                textOverlay.setText("");
            }
        } else {
            log.debug("Wrong DisplayFieldType");
        }
    }

    @Override
    protected void touchAction(TouchActionEvent e) {
        if (e.getTouchState().equals(TouchState.TOUCH_DEAD)) {
            getLockState().removeTouchLock(e.getID());
        } else {
            if (getType().equals(DisplayFieldType.INFO)
                    || getType().equals(DisplayFieldType.EDIT)) {
                toFront();
                setOverlay(e.getX(), e.getY());
                if (getNode().hasChild(txt)) {
                    getNode().detachChild(txt);
                }
                if (!getNode().hasChild(this.overlayDefault)) {
                    for (int i = 0; i < 2; i++) {
                        if (getNode().hasChild(
                                this.overlayMenu.get(i))) {
                            getNode().detachChild(
                                    this.overlayMenu.get(i));
                            getNode().attachChild(
                                    this.overlayDefault);
                        }
                    }
                    textOverlay.setText("");
                    int x = (int) (((getLocal(e.getX(), e.getY()).x + getWidth() / 2) / getWidth()) * 2);
                    int y = (int) (((getLocal(e.getX(), e.getY()).y + getHeight() / 2) / getHeight()) * 10);
                    log.debug("x = " + x);
                    if (x > -1 && x < 2 && y < 1) {
                        if (x == 0) {
                            save();
                        } else if (x == 1) {
                            close();
                        }
                    }
                }
            } else if (getType().equals(DisplayFieldType.LIST)) {
                if (getLockState().isTouchLocked(e.getID())) {
                    if (getLockState().getTouchLock(e.getID()) == getId()) {
                        toFront();
                        int entryID = getSelectedEntry(
                                e.getX(), e.getY());
                        setSelectedEntry(entryID);

                    }
                }
            }
        }
    }
}

class RemoveHandler implements Runnable {

    private float target;
    private Node node = null;
    /**
     * Logger
     */
    private Logger log = Logger.getLogger(AnimationHandler.class);

    public RemoveHandler(Node node, int target) {
        this.node = node;
        this.target = target;
    }

    public void setTarget(float x) {
        this.target = x;
    }

    public void run() {
        log.debug("RemoveHandler startet");
        while (node.getLocalScale().x != target) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
        for (int i = 0; i < node.getControllerCount(); i++) {
            node.removeController(i);
        }
        log.debug("RemoveHandler ended");
    }
}
