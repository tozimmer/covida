/*
 * VideoComponent.java
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
package de.dfki.covida.components.video;

import com.jme.animation.SpatialTransformer;
import com.jme.image.Texture;
import com.jme.image.Texture.WrapMode;
import com.jme.image.Texture2D;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.awt.swingui.ImageGraphics;
import de.dfki.covida.components.AnimationHandler;
import de.dfki.covida.components.CovidaComponent;
import de.dfki.covida.components.TextOverlay;
import de.dfki.covida.components.annotation.DisplayFieldComponent;
import de.dfki.covida.components.data.*;
import de.dfki.touchandwrite.action.*;
import de.dfki.touchandwrite.analyser.touch.gestures.events.DragEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.RotationGestureEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.ZoomEvent;
import de.dfki.touchandwrite.input.pen.event.ShapeEvent;
import de.dfki.touchandwrite.input.pen.hwr.HWRResultSet;
import de.dfki.touchandwrite.input.pen.hwr.HandwritingRecognitionEvent;
import de.dfki.touchandwrite.input.touch.event.TouchState;
import de.dfki.touchandwrite.input.touch.gesture.TouchGestureEvent.GestureState;
import de.dfki.touchandwrite.jme2.ShapeUtils;
import de.dfki.touchandwrite.shape.Polygon;
import de.dfki.touchandwrite.shape.Shape;
import de.dfki.touchandwrite.shape.ShapeType;
import de.dfki.touchandwrite.visual.components.ComponentType;
import de.dfki.touchandwrite.visual.components.DrawingComponent;
import de.dfki.touchandwrite.visual.components.GestureSensitiveComponent;
import de.dfki.touchandwrite.visual.components.HWRSensitiveComponent;
import de.dfki.touchandwrite.visual.input.PenInputHandler;
import de.dfki.touchandwrite.visual.input.TouchInputHandler;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.Queue;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;
import uk.co.caprica.vlcj.binding.internal.libvlc_state_t;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;

/**
 * Component to display videos.
 *
 * @author Tobias Zimmermann
 *
 */
public class VideoComponent extends CovidaComponent implements
        DrawingComponent, HWRSensitiveComponent, GestureSensitiveComponent {

    /**
     * Defines when a TouchEvent is a drag/rotation or zoom event and no longer
     * a single touch
     */
    private static final int THRESHOLD = 2;
    /**
     * Stores touch event count for every touchId
     */
    private Map<Integer, Integer> touchCount;
    /**
     * Generated serial id.
     */
    private static final long serialVersionUID = 3695261146791636755L;
    private static final float UPSCALE_FACTOR = 0.5f;
    private static final boolean EXACT_SHAPES = true;
    protected final AlphaComposite TRANSPARENT = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 0.0f);
    protected final AlphaComposite SOLID = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 1.0f);
    private File videoFile;
    private String videoSource;
    private Quad videoQuad;
    private VideoFormat format;
    private TextureState ts;
    private Texture texture;
    /**
     * Drawing will be done with Java2D.
     */
    protected ImageGraphics g2d;
    private MediaPlayerFactory factory;
    private DirectMediaPlayer mediaPlayer;
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
    private boolean repeating = true;
    private String mrl;
    private Thread repeatHandlerThread;
    private RepeatHandler repeatHandler;
    /**
     * Logger
     */
    private Logger log = Logger.getLogger(VideoComponent.class);
    /**
     * Is pen pressure considered.
     */
    private boolean penPressure = false;
    /**
     * Thickness of the pen stroke
     */
    private float pen_thickness = 1.0f;
    /**
     * Current stroke.
     */
    protected List<Vector3f> currentStroke;
    /**
     * Detected shapes
     */
    protected List<Shape> shapes;
    protected Color backgroundColor = Color.white;
    /**
     * Node which collects all shapes.
     */
    protected Node shapesNode = new Node("Shapes-Node");
    /**
     * Map a
     * <code>Shape</code> object into its corresponding
     * <code>Spatial</code> object
     */
    protected Map<Shape, Spatial> shapeSpatials;
    private DisplayFieldComponent infoField;
    private DisplayFieldComponent listField;
    /**
     * Shape overlay
     */
//    private Quad board;
//    private TextureState tsD;
//    private Texture2D textureD;
//    private ImageGraphics g2dD;
    private Vector3f defaultScale;
    private Quaternion defaultRotation;
    private Vector3f defaultTranslation;
    private boolean lock;
    private AnimationHandler animationHandler;
    private Thread animationHandlerThread;
    private VideoComponentControls controls;
    private VideoComponentListButton menu;
    private VideoSlider slider;
    private List<PenData> penColor;
    private CovidaConfiguration conf;
    private Iterator<PenData> penColorIterator;
    private Color currentShapeColor;
    private ShapeType shapeType;
    private int height;
    private boolean timeRange;
    private long timeStart;
    private long timeEnd;
    private ShapePoints exactShapePoints;
    private final GestureAction gestureAction;
    /**
     * Overlay
     */
    protected Quad overlaySelect;
    protected Quad overlayDefault;
    protected Quad overlayMenuDefault;
    protected Quad overlayDrag;
    private Quad infoDefault;
    private TextOverlay textOverlay;
    private boolean isDragging;
    private Node overlayNode;
    private DragAnimationHandler dragAnimationHandler;
    private Thread dragAnimationHandlerThread;
    private Node pivot;
    private TouchInputHandler touchInputHandler;
    private VideoSlider videoSlider;

    /**
     *
     * @param e
     */
    @Override
    protected void touchAction(TouchActionEvent e) {
        if (!(e.getTouchState() == TouchState.TOUCH_DEAD)) {
            if (touchCount.containsKey(e.getID())) {
                touchCount.put(e.getID(), touchCount.get(e.getID()) + 1);
            } else {
                touchCount.put(e.getID(), 1);
            }
        } else {
            getLockState().removeTouchLock(e.getID());
        }
    }

    public void touchGesture(GestureActionEvent event) {
        if (event.getEvent() instanceof DragEvent) {
            DragEvent e = (DragEvent) event.getEvent();
            if (getLockState().onTop(e, this)) {
                dragAction(e);
            }
            if (e.getState() == GestureState.GESTURE_END) {
//                getLockState().removeTouchLock(e.getTouchID());
            }
        } else if (event.getEvent() instanceof RotationGestureEvent) {
            RotationGestureEvent e = (RotationGestureEvent) event.getEvent();
            if (getLockState().onTop(e, this)) {
                rotationAction(e);
                if (e.getState().equals(GestureState.GESTURE_END)) {
//                    getLockState().removeTouchLockWithoutAction(e.getFirstTouch().getID());
//                    getLockState().removeTouchLockWithoutAction(e.getSecondTouch().getID());
                } else {
                }
            }
        } else if (event.getEvent() instanceof ZoomEvent) {
            ZoomEvent e = (ZoomEvent) event.getEvent();
            if (getLockState().onTop(e, this)) {
                zoomAction(e);
                if (e.getState().equals(GestureState.GESTURE_END)) {
//                    getLockState().removeTouchLockWithoutAction(e.getFirstTouch().getID());
//                    getLockState().removeTouchLockWithoutAction(e.getSecondTouch().getID());
                } else {
                }
            }
        }
    }

    /**
     * Callback for rendering the video.
     *
     * @author Tobias Zimmermann
     *
     */
    class RenderCallback extends RenderCallbackAdapter {

        private BufferedImage image;

        /**
         * Constructor
         */
        public RenderCallback() {
            super(new int[getWidth() * getHeight()]);
            image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(getWidth(), getHeight());
            image.setAccelerationPriority(1.0f);
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter#onDisplay(
         * int[])
         */
        public void onDisplay(int[] data) {
            image.setRGB(0, 0, getWidth(), getHeight(), data, 0, getWidth());
            updateVideo(image);
        }
    }

    /**
     * VideoComponent Constructor
     *
     * @param id
     * @param x
     * @param y
     * @param source
     * @param format
     * @param height
     * @param repeating
     * @param node
     */
    public VideoComponent(String source, int height, VideoFormat format,
            Node node) {
        super(ComponentType.COMPONENT_2D, "Video Display Component", node);
        // TODO correct dimension (performance problem) a.t.m. up scaling
        // (UPSCALE_FACTOR)
        getNode().setLocalScale(1.f / UPSCALE_FACTOR);
        this.defaultScale = new Vector3f(getNode().getLocalScale().x, getNode().getLocalScale().y, getNode().getLocalScale().z);
        this.defaultRotation = new Quaternion(getNode().getLocalRotation().x,
                getNode().getLocalRotation().y, getNode().getLocalRotation().z,
                getNode().getLocalRotation().w);
        this.defaultTranslation = new Vector3f(
                getNode().getLocalTranslation().x, getNode().getLocalTranslation().y, getNode().getLocalTranslation().z);
        videoSource = source;
        this.height = (int) (height * UPSCALE_FACTOR);
        this.format = format;
        videoQuad = new Quad("video", this.format.determineWidth(this.height), this.height);
        this.hwrEvents = new ArrayList<HandwritingRecognitionEvent>();
        this.drawAction = new DrawAction(this);
        this.touchAction = new TouchAction(this);
        this.gestureAction = new GestureAction(this);
        this.hwrAction = new HWRAction(this);
        this.currentStroke = new ArrayList<Vector3f>();
        this.hwrEvents = new ArrayList<HandwritingRecognitionEvent>();
        this.shapes = new ArrayList<Shape>();
        this.penColor = new ArrayList<PenData>();
        this.shapeSpatials = new HashMap<Shape, Spatial>();
    }

    /**
     * Initialize the VideoComponent
     *
     */
    @Override
    public void initComponent() {
        super.initComponent();
        touchCount = new HashMap<Integer, Integer>();
        exactShapePoints = new ShapePoints();
        conf = CovidaConfiguration.getInstance();
        penColor = conf.pens;
        penColorIterator = penColor.iterator();
        setDefaultRotation();
        Node infoFieldNode = new Node("Info Field Node - Video " + getId());
        infoFieldNode.setLocalTranslation(getWidth() * (0.75f), 0, 0);
        Node listFieldNode = new Node("List Field Node - Video " + getId());
        listFieldNode.setLocalTranslation(-getWidth() * (0.75f), 0, 0);
        videoQuad.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        videoQuad.setCullHint(Spatial.CullHint.Inherit);
        setLightCombineMode(LightCombineMode.Off);
        // shape overlay
//        board.getLocalRotation().set(0, 0, 0, 1);
//        board.getLocalTranslation().set(0, 0, 0);
//        board.getLocalScale().set(1, 1, 1);
//        board.setRenderQueueMode(Renderer.QUEUE_ORTHO);
//        board.setCullHint(Spatial.CullHint.Never);
        shapesNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        controls = new VideoComponentControls(this, getNode());
        controls.initComponent();
        Node sliderNode = new Node("Video: " + getId() + " Slider Node");
        sliderNode.getLocalTranslation().set(
                new Vector3f(0, -getHeight() / (1.775f), 0));
        getNode().attachChild(sliderNode);
        slider = new VideoSlider(this, sliderNode);
        slider.initComponent();
        Node menuNode = new Node("Video Menu Node");
        menuNode.getLocalTranslation().set(
                new Vector3f(-getWidth() / 1.90f, 0, 0));
        attachChild(menuNode);
        menu = new VideoComponentListButton(this, menuNode);
        menu.initComponent();
        generateTexture();
        this.attachChild(videoQuad);
//        overlay = new VideoComponentOverlay(this, controls, menu, slider);
//        overlay.initComponent();
        // Overlay
        Node node = new Node("Video: " + getId()
                + " Overlay - TextOverlay");
        node.setLocalTranslation(0, getHeight() / (1.50f) - getFontSize()
                / 2.f, 0);
        getNode().attachChild(node);
        textOverlay = new TextOverlay(node, this);
        textOverlay.setSize(getFontSize());
        overlayNode = new Node("Video Overlay Node");
        pivot = new Node("Pivot Video Overlay Node");
        initalizeOverlayQuads(initalizeBlendState());
        dragAnimationHandler = new DragAnimationHandler();
        dragAnimationHandler.initComponent(this, pivot);
        attachChild(overlayNode);
        attachChild(pivot);
        overlayNode.attachChild(this.overlayDefault);
        // list field
        listField = new DisplayFieldComponent("media/textures/bg_list.png",
                this, listFieldNode, (int) (getHeight() * (0.55f)),
                (int) (getHeight() * 1.2f));
        listField.initComponent();
        // Info Field
        infoField = new DisplayFieldComponent("media/textures/bg_info.png",
                this, listField, infoFieldNode, (int) (getHeight() * (0.55f)),
                (int) (getHeight() * 1.2f));
        infoField.initComponent();
        // shape overlay
//        getNode().attachChild(board);
        getNode().attachChild(this.shapesNode);
        this.drawAction = new DrawAction(this);
        this.touchAction = new TouchAction(this);
        this.hwrAction = new HWRAction(this);
        factory = new MediaPlayerFactory();
        mediaPlayer = factory.newDirectMediaPlayer(getWidth(), getHeight(),
                new RenderCallback());
    }

    private int getFontSize() {
        return (int) ((float) getHeight() / 9.f);
    }

    /**
     *
     * @return the videoSource String
     */
    public String getFile() {
        return this.videoSource;
    }

    /**
     * Makes a snapshot of the video
     *
     * @return video snapshot
     */
    private BufferedImage getSnapshot() {
        if (mediaPlayer == null) {
            return null;
        }
        return this.mediaPlayer.getSnapshot();
    }

    /**
     * Method for calculation of video width
     *
     * @return video width
     */
    public int getWidth() {
        return format.determineWidth(getHeight());
    }

    /**
     *
     * @return height of the VideoComponent
     */
    public int getHeight() {
        return height;
    }

    /**
     *
     * @return true if video is ready to play
     */
    public boolean isReady() {
        if (mediaPlayer != null) {
            return true;
        }
        return false;
    }

    /**
     *
     * @return video format
     */
    public VideoFormat getFormat() {
        return this.format;
    }

    /**
     * Resets position of the VideoComponent
     */
    public void resetNode() {
        this.getNode().setLocalTranslation(defaultTranslation);
        this.getNode().setLocalScale(defaultScale);
        this.getNode().setLocalRotation(defaultRotation);
    }

    /**
     * Resets position of the VideoComponent with an animation
     *
     * @param angle - angle in radians
     */
    public void reset(float angle) {
        if (!lock) {
            lock = true;
            listField.reset();
            infoField.reset();
            SpatialTransformer st = new SpatialTransformer(1);
            st.setObject(getNode(), 0, -1);
            st.setPosition(0, 0, getNode().getLocalTranslation());
            st.setRotation(0, 0, getNode().getLocalRotation());
            st.setScale(0, 0, getNode().getLocalScale());
            st.setPosition(0, 1, defaultTranslation);
            Quaternion q = new Quaternion();
            q = q.fromAngleAxis(angle, new Vector3f(0, 0, 1));
            st.setRotation(0, 1, q);
            st.setScale(0, 1, defaultScale);
            st.interpolateMissing();
            getNode().addController(st);
            animationHandler = new AnimationHandler(this, 1000);
            animationHandlerThread = new Thread(animationHandler);
            animationHandlerThread.start();
        }
    }

    /**
     * Removes animation controllers from VideoComponent
     */
    public void removeAnimation() {
        for (int i = 0; i < getNode().getControllerCount(); i++) {
            getNode().removeController(i);
        }
        lock = false;
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
        if (getWidth() < 1 || getHeight() < 1) {
            log.warn("width < 1");
            g2d = ImageGraphics.createInstance(1, 1, 0);
//            g2dD = ImageGraphics.createInstance(1, 1, 0);
        } else {
            g2d = ImageGraphics.createInstance(getWidth(), getHeight(), 0);
//            g2dD = ImageGraphics.createInstance(getWidth(), getHeight(), 0);
        }
        enableAntiAlias(g2d);
//        enableAntiAlias(g2dD);
        BlendState alpha = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
        alpha.setEnabled(true);
        alpha.setBlendEnabled(true);

        alpha.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        alpha.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        alpha.setTestEnabled(true);
        alpha.setTestFunction(BlendState.TestFunction.GreaterThan);
        texture.setImage(g2d.getImage());
        ts.setTexture(texture);
        // this.videoQuad.setRenderState(alpha);
        videoQuad.setRenderState(ts);
        videoQuad.updateRenderState();
//        // ---- Texture state initialization ----
//        tsD = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
//        tsD.setCorrectionType(TextureState.CorrectionType.Perspective);
//        tsD.setEnabled(true);
//        textureD = new Texture2D();
//        textureD.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
//        textureD.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
//        textureD.setWrap(Texture.WrapMode.Repeat);
//        // ---- Drawable image initialization ----
//        BlendState alphaD = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
//        alphaD.setEnabled(true);
//        alphaD.setBlendEnabled(true);
//        alphaD.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
//        alphaD.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
//        alphaD.setTestEnabled(true);
//        alphaD.setTestFunction(BlendState.TestFunction.GreaterThan);
//        refreshBoard();
//        textureD.setImage(g2dD.getImage());
//        tsD.setTexture(textureD);
//        this.board.setRenderState(alphaD);
//        this.board.setRenderState(tsD);
//        this.board.updateRenderState();
    }

//    private void refreshBoard() {
//        // first clear strokes
//        g2dD.clearRect(0, 0, getWidth(), getHeight());
//        // paint with transparent color
//        g2dD.setComposite(TRANSPARENT);
//        g2dD.setColor(this.backgroundColor);
//        g2dD.fillRect(0, 0, getWidth(), getHeight());
//        g2dD.setComposite(SOLID);
//        g2dD.update();
//    }
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
     * Updates the video image.
     *
     * @param image
     */
    public void updateVideo(BufferedImage image) {
        g2d.drawImage(image, null, 0, 0);
        g2d.update();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jme.scene.TriMesh#draw(com.jme.renderer.Renderer)
     */
    @Override
    public void draw(Renderer r) {
        try {
            if (g2d != null && texture.getTextureId() > 0) {
                g2d.update(texture, false);
            }
//            if (g2dD != null && textureD.getTextureId() > 0) {
//                g2dD.update(textureD, false);
//            }
        } catch (Exception e) {
        }
        super.draw(r);
    }

    /**
     * Method to set video repeat flag
     *
     * @param repeat - repeat flag (true -> repeat video)
     */
    public void setRepeat(Boolean repeat) {
        if (repeat) {
            this.repeating = true;
        } else {
            this.repeating = false;
        }
    }

    /**
     *
     * @return true if video is active
     */
    public boolean isActive() {
        return (mediaPlayer.isPlaying());
    }

    /**
     * Checks if actual video is finished
     *
     * @return true if video is finished
     */
    public boolean isFinished() {
        if (timeRange) {
            if (!(getTime() < getMaxTime())) {
                return true;
            }
        }
        return mediaPlayer.getMediaState().equals(libvlc_state_t.libvlc_Ended);
    }

    /**
     * Start video (Initialisation)
     */
    public void start() {
        if (this.videoFile != null) {
            mediaPlayer.playMedia(this.videoFile.toString());
            this.mrl = this.videoFile.toString();
        } else {
            mediaPlayer.playMedia(videoSource);
            this.mrl = videoSource;
        }
        mediaPlayer.setPlaySubItems(true);
        if (timeRange) {
            mediaPlayer.setTime(timeStart);
        }

    }

    public void startHandler() {
        repeatHandler = new RepeatHandler(this);
        repeatHandlerThread = new Thread(repeatHandler);
        repeatHandlerThread.start();
    }

    /**
     * Stops the video
     */
    public void stop() {
        if (isReady()) {
            this.mediaPlayer.prepareMedia(mrl);
            this.mediaPlayer.stop();
        }
    }

    /**
     * Plays the actual media from the beginning
     */
    public void replay() {
        if (isReady()) {
            this.mediaPlayer.playMedia(mrl);
        }
    }

    /**
     * Changes the actual playing media
     *
     * @param mrl
     */
    public void setMedia(String mrl) {
        this.mrl = mrl;
        if (isReady()) {
            this.mediaPlayer.playMedia(mrl);
        }
    }

    /**
     * Pauses the video
     */
    public void pause() {
        if (isReady()) {
            if (this.mediaPlayer.isPlaying()) {
                this.mediaPlayer.pause();
            }
        }
    }

    /**
     * Saves snapshot as png to
     * <code>file</code>
     *
     * @param file
     */
    public void saveSnapshot(File file) {
        BufferedImage test = mediaPlayer.getSnapshot();
        RenderedImage ri = (RenderedImage) test;
        try {
            ImageIO.write(ri, "png", file);
        } catch (IOException e) {
            log.error(e);
        }
    }

    /**
     * Resumes a paused video
     *
     */
    public void resume() {
        if (isReady()) {
            if (!this.mediaPlayer.isPlaying()) {
                this.mediaPlayer.play();
            }
        }
    }

    /**
     * Sets the video time position in frames
     *
     * @param time - time in frames
     */
    public void setTimePosition(long time) {
        if (isReady()) {
            mediaPlayer.setTime(time);
        }
    }

    /**
     * Sets the video time position in percentage
     *
     * @param percentage
     */
    public void setTimePosition(float percentage) {
        if (timeRange) {
            if (isReady()) {
                mediaPlayer.setTime((long) ((percentage * (getMaxTime() - timeStart)) + timeStart));
                updateSlider((float) getTime() / (float) getMaxTime());
            }
        } else {
            if (isReady()) {
                mediaPlayer.setTime((long) (percentage * getMaxTime()));
                updateSlider((float) getTime() / (float) getMaxTime());
            }
        }
    }

    /**
     * Returns video time position (Frame)
     *
     * @return
     * <code>long</code> lenght , -1 if no video is active
     */
    public long getTime() {
        if (timeRange) {
            if (isReady()) {
                return mediaPlayer.getTime() - timeStart;
            }
            return -1;
        } else {
            if (isReady()) {
                return mediaPlayer.getTime();
            }
            return -1;
        }

    }

    public String getTimeCode(long time) {
        if (isReady()) {
            int milliseconds = (int) (time % 1000);
            String msString;
            if (milliseconds < 10) {
                msString = "0" + String.valueOf(milliseconds);
            } else {
                msString = String.valueOf(milliseconds);
            }
            time = (long) ((float) time / 1000.f);
            int seconds = (int) (time % 60);
            String sString;
            if (seconds < 10) {
                sString = "0" + String.valueOf(seconds);
            } else {
                sString = String.valueOf(seconds);
            }
            time = (long) ((float) time / 60.f);
            int minutes = (int) (time % 60);
            String mString;
            if (minutes < 10) {
                mString = "0" + String.valueOf(minutes);
            } else {
                mString = String.valueOf(minutes);
            }
            time = (long) ((float) time / 60.f);
            int hours = (int) (time % 60);
            String hString;
            if (hours < 10) {
                hString = "0" + String.valueOf(hours);
            } else {
                hString = String.valueOf(hours);
            }
            return (hString + ":" + mString + ":" + sString + ":" + msString);
        }
        return "";
    }

    /**
     *
     * @param start
     * @param end
     */
    public void setTimeRange(long start, long end) {
        this.timeRange = true;
        this.timeStart = start;
        this.timeEnd = end;
    }

    public String getVideoProgress() {
        if (!timeRange) {
            int p = (int) (mediaPlayer.getPosition() * 100);
            return String.valueOf(p) + " %";
        } else {
            int p = (int) ((this.getTime() / this.getMaxTime()) * 100);
            return String.valueOf(p) + " %";
        }
    }

    /**
     * Returns video source length in frames
     *
     * @return
     * <code>long</code> lenght , -1 if no video is active
     * @return -1 if video is not ready;
     */
    public long getMaxTime() {
        if (timeRange) {
            if (isReady()) {
                return timeEnd;
            }
            return -1;
        } else {
            if (isReady()) {
                return mediaPlayer.getLength();
            }
            return -1;
        }

    }

    /**
     *
     * @return fps - frames per second
     * @return -1 if video is not Ready
     */
    public float getFps() {
        if (isReady()) {
            return mediaPlayer.getFps();
        }
        return -1;
    }

    public void setSpeed(float rate) {
        if (isReady()) {
            mediaPlayer.setRate(rate);
        }
    }

    public float getSpeed() {
        if (isReady()) {
            return mediaPlayer.getRate();
        }
        return -1;
    }

    public void resetSpeed() {
        if (isReady()) {
            mediaPlayer.setRate(1.f);
        }
    }

    /**
     * Changes video source of VideoComponent
     *
     * @param file
     */
    public void videoChange(File file) {
        this.videoFile = file;
        this.mrl = file.toString();
    }

    /**
     * Releases native sources
     */
    @Override
    public boolean cleanUp() {
        log.debug("cleanup " + getId());
        if (repeatHandler != null) {
            this.repeatHandler.cleanUp();
            while (repeatHandlerThread.isAlive()) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    log.error(e);
                }
            }
            this.controls.cleanUp();
            this.infoField.cleanUp();
            this.listField.cleanUp();
            this.slider.cleanUp();
            this.repeating = false;
            if (isReady()) {
                this.mediaPlayer.release();
            }
            this.factory.release();
        }
        return true;
    }

    /**
     * Sets the volume for the video.
     *
     * @param value -
     * <code>Integer</code> - Volume in percentage
     */
    public void setVolume(int value) {
        if (isReady()) {
            this.mediaPlayer.setVolume(value);
        }
    }

    /**
     *
     *
     * @return
     */
    public boolean isRepeating() {
        return this.repeating;
    }

    @Override
    public void activatePenPressure() {
        penPressure = true;
    }

    @Override
    public void deactivatePenPressure() {
        penPressure = false;
    }

    @Override
    public float getPenThickness() {
        return this.pen_thickness;
    }

    @Override
    public boolean isPenPressureActivated() {
        return this.penPressure;
    }

    @Override
    public void setCurrentPenColor(Color color) {
        this.currentShapeColor = color;
    }

    @Override
    public void setPenThickness(float thickness) {
        this.pen_thickness = thickness;
    }

    @Override
    public void draw(Queue<PenActionEvent> penEvent) {
        for (PenActionEvent evt : penEvent) {
            if (inArea(evt)) {
//                switch (evt.getType()) {
//                    case PEN_UP:
//                        lastX.remove(evt.getId());
//                        lastY.remove(evt.getId());
//                        penActive = false;
//                        break;
//                    case PEN_DOWN:
//                        penActive = true;
//                    case PEN_MOVE:
////                        updateImage(evt.getAbsoluteX(), evt.getAbsoluteY(), evt.getForce(), evt.getId());
//                        this.currentStroke.add(new Vector3f(evt.getAbsoluteX(), evt.getAbsoluteY(), 0.0f));
//                        break;
//                }
                Vector3f local = getLocal(evt.getAbsoluteX(), this.display.y
                        - evt.getAbsoluteY());
                exactShapePoints.add(new Point((int) local.x, (int) local.y));
                this.pause();
            }
        }
    }
//
//    /**
//     *  Updates the internal image.
//     * 
//     * @param x
//     * @param y
//     * @param force
//     * @param id 
//     */
//    protected void updateImage(int x, int y, float force, int id) {
//        if (!lastX.containsKey(new Integer(id))) {
//            if (penColor.isEmpty()) {
//                currentPenColor.put(id, Color.RED);
//            } else {
//                if (!currentPenColor.containsKey(new Integer(id))) {
//                    if (!penColorIterator.hasNext()) {
//                        penColorIterator = penColor.iterator();
//                    }
//                    currentPenColor.put(id, penColorIterator.next().penColor);
//                }
//            }
//            g2dD.setColor(currentPenColor.get(new Integer(id)));
//            lastX.put(id, x);
//            lastY.put(id, y);
//        } else {
//            if (this.penPressure) {
//                this.g2dD.setStroke(new BasicStroke(pen_thickness * force));
//            } else {
//                this.g2dD.setStroke(new BasicStroke(pen_thickness));
//            }
//            Vector3f lastLocal = getLocal((float) lastX.get(new Integer(id)), (float) lastY.get(new Integer(id)));
//            Vector3f local = getLocal((float) x, (float) y);
////            local = local.add(new Vector3f(((float) getWidth())/2.0f, ((float) getHeight())/2.0f, 0.0f));
////            lastLocal = lastLocal.add(new Vector3f(((float) getWidth())/2.0f, ((float) getHeight())/2.0f, 0.0f));
//            this.g2dD.drawLine((int) lastLocal.x + (int) ((float) getWidth() / 2.0f), (int) lastLocal.y + (int) ((float) getHeight() / 2.0f), (int) local.x + (int) ((float) getWidth() / 2.0f), (int) local.y + (int) ((float) getHeight() / 2.0f));
//            lastX.put(id, x);
//            lastY.put(id, y);
//        }
//    }

//    /**
//     * Erases all strokes.
//     * 
//     * @param shape
//     */
//    private void eraseStrokes() {
//        // int l_x = -1;
//        // int l_y = -1;
//        this.g2d.setColor(Color.white);
//        refreshBoard();
//        updateStrokes();
//    }
//    private void updateStrokes() {
//        g2d.setColor(shapeColor);
//        for (StrokeTrace<Float> trace : strokes) {
//            List<Float> x = trace.getX();
//            List<Float> y = trace.getY();
//            List<Float> force = trace.getForce();
//            for (int i = 1; i < x.size(); i++) {
//                if (this.penPressure) {
//                    this.g2d.setStroke(new BasicStroke(pen_thickness
//                            * force.get(i)));
//                } else {
//                    this.g2d.setStroke(new BasicStroke(pen_thickness));
//                }
//                this.g2d.drawLine(PenDataConversionUtil.convertX2int(x.get(i - 1)), PenDataConversionUtil.convertY2int(y.get(i - 1)), PenDataConversionUtil.convertX2int(x.get(i)), PenDataConversionUtil.convertY2int(y.get(i)));
//            }
//        }
//    }
    @Override
    public void draw(ShapeEvent shape) {
        if (penColor.isEmpty()) {
            currentShapeColor = Color.RED;
        } else {
            if (!penColorIterator.hasNext()) {
                penColorIterator = penColor.iterator();
            }
            currentShapeColor = penColorIterator.next().penColor;
            if (currentShapeColor == null) {
                currentShapeColor = Color.RED;
            }
        }
        ShapeUtils.changeShapeColor(currentShapeColor);
        // TODO pen id!
        if (this.getLockState().onTop(-1, shape, this)) {
            // delete old data
            shapes.clear();
            getLockState().removeTouchLock(-1);
            clearBoard();
            ShapePoints shapePoints = new ShapePoints();
            infoField.resetInfo();
            if (EXACT_SHAPES) {
                shapePoints = exactShapePoints;
            } else {
                Vector3f local;
                Point first = null;
                for (Shape s : shape.getDetectedShapes()) {
                    if (isSupportedShape(s)) {
                        // iteration over all points of the shape
                        for (Point point : s.getPoints()) {
                            // get local points
                            local = getLocal(point.x, this.display.y - point.y);
                            if (first == null) {
                                first = new Point((int) local.x, (int) local.y);
                            }
                            shapePoints.add(new Point((int) local.x,
                                    (int) local.y));
                        }
                        if (s.getShapeType() != null) {
                            shapeType = s.getShapeType();
                        }
                    }
                    if (shapePoints.size() < 2) {
                        log.debug("shapePoints.size()<2");
                        shapePoints.add(new Point(0, 0));
                        shapePoints.add(new Point(0, 0));
                    }
                }
                shapePoints.add(first);
            }
            // draw and save new shapes
            Vector2f tempMax = new Vector2f(0, 0);
            Vector2f tempMin = new Vector2f(9999, 9999);
            for (Point point : shapePoints.getShapePoints()) {
                if (point.x < tempMin.x) {
                    tempMin.x = point.x;
                }
                if (point.x > tempMax.x) {
                    tempMax.x = point.x;
                }
                if (point.y < tempMin.y) {
                    tempMin.y = point.y;
                }
                if (point.y > tempMax.y) {
                    tempMax.y = point.y;
                }
            }
            if (shapePoints.size() < 2) {
                log.debug("shapePoints.size()<2");
                shapePoints.add(new Point(0, 0));
                shapePoints.add(new Point(0, 0));
            }
            if (shapeType == null) {
                shapeType = ShapeType.POLYGON;
            }
            updateImage(shapeType, shapePoints.getShapePoints());
            // get snapshot data
            tempMin.x = tempMin.x + (float) getWidth() / 2.f;
            tempMin.y = tempMin.y + (float) getHeight() / 2.f;
            tempMax.x = tempMax.x + (float) getWidth() / 2.f;
            tempMax.y = tempMax.y + (float) getHeight() / 2.f;
            float diffX = tempMax.x - tempMin.x;
            float diffY = tempMax.y - tempMin.y;
            int w = (int) (getWidth() * diffX / getWidth());
            int h = (int) (getHeight() * diffY / getHeight());
            int x = (int) (((getWidth() * ((tempMin.x + (diffX / 2.f)) / getWidth()))) - (float) w / 2.f);
            int y = (int) (((getHeight() * ((1 - (tempMin.y + (diffY / 2.f))
                    / getHeight()))) / 2.f) + (float) h / 2.f);
            if (!(mediaPlayer == null)
                    || !(mediaPlayer.getVideoDimension() == null)) {
                w = (int) (mediaPlayer.getVideoDimension().width * diffX / getWidth());
                h = (int) (mediaPlayer.getVideoDimension().height * diffY / getHeight());
                x = (int) (((mediaPlayer.getVideoDimension().width * ((tempMin.x + (diffX / 2.f)) / getWidth()))) - (float) w / 2.f);
                y = (int) (((mediaPlayer.getVideoDimension().height * ((1 - (tempMin.y + (diffY / 2.f))
                        / getHeight()))) / 2.f) + (float) h / 2.f);
            } else {
                log.warn("mediaPlayer.getVideoDimension() == null");
            }
            RenderedImage ri = getSnapshot(x, y, w, h);
            try {
                ImageIO.write(ri, "png", new File(videoSource + "."
                        + getAnnotationData().size() + ".png"));
            } catch (IOException e) {
                log.error(e);
            }
            setAnnotationData(shapePoints);
            exactShapePoints = new ShapePoints();
        }
    }

    private void setAnnotationData(ShapePoints shapePoints) {
        // attach info field to video and make it visible
        attachAnnotation();
        pause();
        // set annotation data
        listField.resetInfo();
        long time = this.getTime();
        infoField.setTime(time);
        String title = this.name;
        infoField.setTitle(title);
        infoField.setVideoSource(videoSource);
        infoField.setShapeType(shapeType);
        infoField.setShapePoints(shapePoints);
        infoField.setSnapshot();
        toFront();
    }

    private RenderedImage getSnapshot(int x, int y, int w, int h) {
        BufferedImage test;
        try {
            test = getSnapshot().getSubimage(x, y, w, h);
        } catch (RasterFormatException e) {
            log.error(e);
            test = getSnapshot();
        }
        if (test == null) {
            return null;
        }
        return (RenderedImage) test;
    }

    /**
     * Loads annotation data into InfoField
     *
     * @param entryID
     */
    public void loadAnnotationData(int entryID) {
        infoField.loadData(entryID);
        attachAnnotation();
    }

    /**
     *
     * @author Tobias Zimmermann
     * @param type
     * @param list
     * @return
     */
    // TODO support for all shape types
    private Spatial createSpatialFromPoints(ShapeType type, List<Point> list) {
        Spatial spatial;
        Polygon p;
        if (type == null) {
            type = ShapeType.POLYGON;
            log.debug("Type == null");
        }
        if (list == null) {
            list = new ArrayList<Point>();
            log.debug("list == null");
        }
        switch (type) {
            case ELLIPTIC_ARC:
                p = new Polygon(list);
                spatial = ShapeUtils.toPolygon(p, false);
                break;
            // EllipticArc ea = new EllipticArc();
            // spatial = ShapeUtils.toEllipticArc(ea);
            // break;
            case ELLIPSE:
                p = new Polygon(list);
                spatial = ShapeUtils.toPolygon(p, false);
                break;
            // Ellipse e = new Ellipse();
            // spatial = ShapeUtils.toEllipse(e);
            // break;
            case CIRCLE:
                p = new Polygon(list);
                spatial = ShapeUtils.toPolygon(p, false);
                break;
            // Circle c = new Circle();
            // spatial = ShapeUtils.toCircle(c);
            // break;
            case RECTANGLE:
                p = new Polygon(list);
                spatial = ShapeUtils.toPolygon(p, false);
                break;
            case RHOMBUS:
                p = new Polygon(list);
                spatial = ShapeUtils.toPolygon(p, false);
                break;
            case PARALLELOGRAM:
                p = new Polygon(list);
                spatial = ShapeUtils.toPolygon(p, false);
                break;
            case TRIANGLE:
                p = new Polygon(list);
                spatial = ShapeUtils.toPolygon(p, false);
                break;
            // Triangle t = new Triangle();
            // spatial = ShapeUtils.toTriMesh(t);
            // break;
            case LINE:
                p = new Polygon(list);
                spatial = ShapeUtils.toPolygon(p, false);
                break;
            // LineSegment l = new LineSegment(shapePoints.get(0).x,
            // shapePoints.get(0).y,
            // shapePoints.get(shapePoints.size()-1).x,shapePoints.get(shapePoints.size()-1).y);
            // spatial = ShapeUtils.toLine(l);
            // break;
            case POLYGON:
                p = new Polygon(list);
                spatial = ShapeUtils.toPolygon(p, false);
                break;
            default:
                p = new Polygon(list);
                spatial = ShapeUtils.toPolygon(p, false);
                break;
        }
        return spatial;
    }

    /**
     * Calculates scaling factor for snapshot texture
     *
     * @param diffX
     * @param diffY
     * @return
     */
    protected float getSnapshotScale(Vector2f min, Vector2f max) {
        float diffX = max.x - min.x;
        float diffY = max.y - min.y;
        float scaleX = diffX / (float) getWidth();
        float scaleY = diffY / (float) getHeight();
        float scale;
        if (scaleX > scaleY) {
            scale = scaleX;
        } else {
            scale = scaleY;
        }
        return scale;
    }

    /**
     * Calculates snapshot center
     *
     * @param min
     * @param max
     * @return
     */
    protected Vector3f getSnapshotCentrum(Vector2f min, Vector2f max) {
        float diffX = max.x - min.x;
        float diffY = max.y - min.y;
        float yPos = min.y + diffY;
        float xPos = min.x + diffX;
        yPos = 1 - ((yPos / (float) getHeight()) / 2.f);
        xPos = 1 - ((xPos / (float) getWidth()) / 2.f);
        return new Vector3f(xPos, yPos, 0);
    }

    /**
     * Creates snapshot of selected shape on VideoComponent
     *
     * @param max
     * @param min
     * @param center
     * @return Snapshot texture
     */
    public Texture generateSnapshotTexture(Vector2f max, Vector2f min,
            Vector3f center) {
        Texture snapshotTexture;
        snapshotTexture = new Texture2D();
        snapshotTexture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        snapshotTexture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
        snapshotTexture.setWrap(Texture.WrapMode.Repeat);
        snapshotTexture.setWrap(WrapMode.Clamp);
        // Get image
        com.jme.image.Image image = g2d.getImage();
        log.debug(center);
        // TEST

        // Set image to texture
        snapshotTexture.setImage(image);

        // Set scale
        snapshotTexture.setScale(new Vector3f(getSnapshotScale(min, max),
                getSnapshotScale(min, max), 0));
        // Set center
        snapshotTexture.setTranslation(center);
        return snapshotTexture;
    }

    /**
     * Hides AnnotationList from this instance of VideoComponent
     *
     */
    public void detachList() {
        if (hasList()) {
            listField.close();
            menu.listDetached();
        }
    }

    /**
     * Shows AnnotationList from this instance of VideoComponent
     *
     */
    public void attachList() {
        if (!hasList()) {
            getNode().attachChild(listField.getNode());
            listField.open();
            menu.listAttached();
        }
    }

    /**
     * Returns true if AnnotationList is attached
     *
     * @return
     */
    public boolean hasList() {
        return listField.isOpen();
    }

    /**
     * Shows AnnotationField
     */
    public void attachAnnotation() {
        if (!hasAnnotation()) {
            getNode().attachChild(infoField.getNode());
            infoField.open();
        }
    }

    /**
     * Shows AnnotationField
     */
    public void detachAnnotation() {
        if (hasAnnotation()) {
            infoField.close();
        }
    }

    public void changeOverlays() {
        attachOverlays();
    }

    private boolean hasAnnotation() {
        return infoField.isOpen();
    }

    /**
     * Checks if shape is supported.
     *
     * @param s
     * @return
     */
    protected boolean isSupportedShape(Shape s) {
        return true;
    }

    /**
     * Removes all shapes from this instance of VideoComponent
     */
    public void clearBoard() {
        shapesNode.detachAllChildren();
    }

    public Vector3f resetLocalTranslation(Vector3f local) {
        return local.add(new Vector3f(getNode().getLocalTranslation().x,
                getNode().getLocalTranslation().x, 0));
    }

    @Override
    public void registerWithInputHandler(PenInputHandler input) {
        input.addAction(drawAction);
        input.addAction(hwrAction);
    }

    @Override
    public void unRegisterWithInputHandler(PenInputHandler input) {
        input.removeAction(drawAction);
        input.removeAction(hwrAction);
    }

    @Override
    public void registerWithInputHandler(TouchInputHandler input) {
        slider.registerWithInputHandler(input);
        menu.registerWithInputHandler(input);
        controls.registerWithInputHandler(input);
        listField.registerWithInputHandler(input);
        infoField.registerWithInputHandler(input);
        input.addAction(touchAction);
        input.addAction(gestureAction);
    }

    @Override
    public void unRegisterWithInputHandler(TouchInputHandler input) {
        input.removeAction(touchAction);
        input.removeAction(gestureAction);
    }

    @Override
    public void handwritingResult(HandwritingRecognitionEvent event) {
        log.debug("HWR: " + event.getHWRResultSet().topResult());
        // TODO Pen id!
        if (getLockState().onTop(
                -1,
                new Vector2f(event.getBoundingBox().getCenterOfGravity().x,
                (display.y - event.getBoundingBox().getCenterOfGravity().y)), this)) {
            exactShapePoints = new ShapePoints();
            this.hwrEvents.add(event);
            drawHWRResult(event);
            getLockState().removeTouchLock(-1);
        } else {
            // log.debug("ID: "+getId()+" !onTop");
        }
    }

    /**
     * Draws the HWR result.
     *
     * @param event
     */
    protected void drawHWRResult(HandwritingRecognitionEvent event) {
        log.debug(event.getHWRResultSet().topResult());
        getLockState().removeTouchLock(-1);
        if (!hasAnnotation()) {
            shapes.clear();
            clearBoard();
            ShapePoints shapePoints = new ShapePoints();
            shapePoints.add(new Point(-getWidth() / 2, -getHeight() / 2));
            shapePoints.add(new Point(-getWidth() / 2, getHeight() / 2));
            shapePoints.add(new Point(getWidth() / 2, getHeight() / 2));
            shapePoints.add(new Point(getWidth() / 2, -getHeight() / 2));
            shapePoints.add(new Point(-getWidth() / 2, -getHeight() / 2));
            shapeType = ShapeType.POLYGON;
            infoField.resetInfo();
            updateImage(shapeType, shapePoints.getShapePoints());
            int x = 0;
            int y = 0;
            int w = this.getVideoDimension().width - 2;
            int h = this.getVideoDimension().height - 2;
            RenderedImage ri = getSnapshot(x, y, w, h);
            try {
                ImageIO.write(ri, "png", new File(videoSource + "."
                        + getAnnotationData().size() + ".png"));
            } catch (IOException e) {
                log.error(e);
            }
            setAnnotationData(shapePoints);
        }
        ArrayList<String> hwrResults = new ArrayList<String>();
        int size = event.getHWRResultSet().getWords().size();
        for (int i = 0; i < size; i++) {
            hwrResults.add(event.getHWRResultSet().getWords().get(i).getCandidates().peek().getRecogntionResult());
        }
        infoField.drawHwrResult(hwrResults);
        log.debug("ID: " + getId() + " HWR: " + hwrResults);
        // If there is no shape, mark the hole frame
    }

    /**
     * Checks the HWR result and chooses the best result.
     *
     * @param hwrResultSet
     * @return
     */
    protected String checkHWRResult(HWRResultSet hwrResultSet) {
        return hwrResultSet.topResult();
    }

    /**
     * Method to update slider position
     *
     * @param percentage
     */
    public void updateSlider(float percentage) {
        slider.updateSlider(percentage);
    }

    /**
     * Update the shape quad
     *
     * @param shapeType
     * @param list
     */
    public void updateImage(ShapeType shapeType, List<Point> list) {
        ShapeUtils.setLineWidth(4);
        shapesNode.attachChild(createSpatialFromPoints(shapeType, list));
        ShapeUtils.setLineWidth(1);
        ShapeUtils.changeShapeColor(Color.WHITE);
        shapesNode.attachChild(createSpatialFromPoints(shapeType, list));
    }

    public void setDefaultRotation() {
        Quaternion q = new Quaternion();
        // Rotation need because of ImageGraphics
        q.fromAngles(0f, (float) Math.toRadians(180),
                (float) Math.toRadians(180));
        this.videoQuad.rotatePoints(q);
//        this.board.rotatePoints(q);
    }

    /**
     * Method to get the annotation data corresponding to this instance of
     * VideoComponent
     *
     * @return VideoAnnotationData
     */
    public VideoAnnotationData getAnnotationData() {
        return infoField.getAnnotationData();
    }

    /**
     * Sets the local scale of the node from this instance of VideoComponent
     *
     * @param scale
     */
    public void rescale(float scale) {
        this.getNode().setLocalScale(scale);
    }

    public Dimension getVideoDimension() {
        if(mediaPlayer == null || mediaPlayer.getVideoDimension() == null){
            // TODO
            return new Dimension(getWidth(), getHeight());
        }
        return mediaPlayer.getVideoDimension();
    }

    /**
     * Rotate the node from this instance of VideoComponent
     *
     * @param angle
     * @param axis
     */
    public void rotate(float angle, Vector3f axis) {
        Quaternion rotation = new Quaternion();
        rotation.fromAngleAxis(angle, axis);
        this.getNode().setLocalRotation(rotation);
    }

    @Override
    protected void dragAction(DragEvent event) {
        if (event.getState().equals(DragEvent.GestureState.GESTURE_UPDATE)
                && event.getTranslation() != null) {

            startDragAnimation();
            move(getNode().getLocalTranslation().getX()
                    + (event.getTranslation().x * getWidth() / getNode().getLocalScale().x)
                    , getNode().getLocalTranslation().getY()
                    - (event.getTranslation().y * getHeight() / getNode().getLocalScale().y));
//            log.debug("X: "+event.getTranslation().x+" Y: "+event.getTranslation().y);
        } else if (event.getState().equals(DragEvent.GestureState.GESTURE_END)) {
            stopDragAniation();
            getLockState().removeTouchLock(event.getTouchID());
        } else if (event.getState().equals(DragEvent.GestureState.GESTURE_BEGIN)) {
            if (animationHandlerThread == null
                    || !animationHandlerThread.isAlive()) {
                animationHandlerThread = new Thread(
                        animationHandler);
                animationHandlerThread.start();
            }
        }
    }

    @Override
    protected void rotationAction(RotationGestureEvent event) {
        getNode().getLocalTranslation().subtractLocal(
                event.getPivotPoint().x, event.getPivotPoint().y,
                0);
        Quaternion q = new Quaternion();
        q.fromAngleAxis(event.getRotation() / getNode().getLocalScale().x, Vector3f.UNIT_Z);
        getNode().getLocalRotation().multLocal(q.inverse());
        getNode().getLocalTranslation().addLocal(
                event.getPivotPoint().x, event.getPivotPoint().y,
                0);
        if (event.getState().equals(DragEvent.GestureState.GESTURE_END)) {
            getLockState().removeTouchLock(event.getFirstTouch().getID());
            getLockState().removeTouchLock(event.getSecondTouch().getID());
        }
    }

    @Override
    protected void zoomAction(ZoomEvent event) {
//        Vector3f scale = getNode().getLocalScale();
//        float diff = UPSCALE_FACTOR * event.getZoomDistance()
//                / (getWidth() / scale.x);
        float scale = display.x / ((float) getWidth() * getNode().getLocalScale().x);
        float ratio = ((event.getZoomRatio() - 1) / scale) + 1;
        rescale(getNode().getLocalScale().x * ratio);
        if (getNode().getLocalScale().x < 1.5f) {
            rescale(1.5f);
        }
        if (getNode().getLocalScale().x > 2.5f * scale) {
            rescale(2.5f * scale);
        }
        if (event.getState().equals(GestureState.GESTURE_END)) {
            if (event.getState().equals(DragEvent.GestureState.GESTURE_END)) {
                getLockState().removeTouchLock(event.getFirstTouch().getID());
                getLockState().removeTouchLockWithoutAction(event.getSecondTouch().getID());
            }
        }
    }

    /**
     * Overlay
     */
    private void attachMenu() {
        menu.registerWithInputHandler(touchInputHandler);
    }

    private BlendState initalizeBlendState() {
        BlendState alpha = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
        alpha.setEnabled(true);
        alpha.setBlendEnabled(true);

        alpha.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        alpha.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        alpha.setTestEnabled(true);
        alpha.setTestFunction(BlendState.TestFunction.GreaterThan);
        return alpha;
    }

    private void initalizeOverlayQuads(BlendState alpha) {
        // Overlay Default
        Texture overlayDefaultTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(
                "media/textures/overlay_default.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlayDefaultTexture.setWrap(WrapMode.Clamp);

        TextureState overlayDefaultState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlayDefaultState.setTexture(overlayDefaultTexture);

        // Overlay select 1
        Texture overlaySelectTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(
                "media/textures/overlay_select.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlaySelectTexture.setWrap(WrapMode.Clamp);

        TextureState overlaySelectState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlaySelectState.setTexture(overlaySelectTexture);

        // Overlay drag 1
        Texture overlayDragTexture = TextureManager.loadTexture(getClass().getClassLoader().getResource("media/textures/overlay_drag.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlayDragTexture.setWrap(WrapMode.Clamp);

        TextureState overlayDragState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlayDragState.setTexture(overlayDragTexture);

        // Overlay video menu default
        Texture overlayVideoMenuDefault = TextureManager.loadTexture(getClass().getClassLoader().getResource("media/textures/video_menu.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlayVideoMenuDefault.setWrap(WrapMode.Clamp);

        TextureState overlayVideoMenuDefaultState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlayVideoMenuDefaultState.setTexture(overlayVideoMenuDefault);

        this.overlayDefault = new Quad("Overlay-Default-Image-Quad",
                (1.15f) * getWidth(), (1.275f) * getHeight());

        overlayDefault.setRenderState(overlayDefaultState);
        overlayDefault.setRenderState(alpha);
        overlayDefault.updateRenderState();
        overlayDefault.getLocalTranslation().set(0, 0, 0);

        this.overlaySelect = new Quad("Overlay-Default-Image-Quad",
                (1.15f) * getWidth(), (1.275f) * getHeight());

        overlaySelect.setRenderState(overlaySelectState);
        overlaySelect.setRenderState(alpha);
        overlaySelect.updateRenderState();
        overlaySelect.getLocalTranslation().set(0, 0, 0);

        this.overlayDrag = new Quad("Overlay-Drag-Image-Quad", (1.15f) * getWidth(),
                (1.35f) * getHeight());

        overlayDrag.setRenderState(overlayDragState);
        overlayDrag.setRenderState(alpha);
        overlayDrag.updateRenderState();
        overlayDrag.getLocalTranslation().set(0, 0, 0);

        this.overlayMenuDefault = new Quad("Overlay-Video-Menu-Image-Quad",
                getWidth(), (0.15f) * getHeight());

        overlayMenuDefault.setRenderState(overlayVideoMenuDefaultState);
        overlayMenuDefault.setRenderState(alpha);
        overlayMenuDefault.updateRenderState();
        overlayMenuDefault.getLocalTranslation().set(
                new Vector3f(0, -height / (1.4f), 0));

        this.infoDefault = new Quad("Default-Info-Quad", (1.04f) * getWidth(),
                (1.3f) * getHeight());

        infoDefault.setRenderState(overlayDefaultState);
        infoDefault.setRenderState(alpha);
        infoDefault.updateRenderState();
        infoDefault.getLocalTranslation().set(0, 0, 0);
    }

    private boolean isUnderThreshold(int touchId) {
        if (touchCount.containsKey(touchId)) {
            return touchCount.get(touchId) < THRESHOLD;
        } else {
            return true;
        }
    }

    public void attachOverlays() {
        if (overlayNode.hasChild(this.overlayDefault)) {
            overlayNode.detachChild(this.overlayDefault);
            overlayNode.attachChild(this.overlaySelect);
            attachControls();
            textOverlay.setText(getName());
        } else {
            overlayNode.detachChild(this.overlaySelect);
            overlayNode.attachChild(this.overlayDefault);
            detachMenu();
            textOverlay.detach();
        }
    }

    @SuppressWarnings("unused")
    private void detachSlider() {
        videoSlider.detachSlider();
        if (touchInputHandler != null) {
            videoSlider.unRegisterWithInputHandler(touchInputHandler);
        }
    }

    private void attachSlider() {
        videoSlider.attachSlider();
        if (touchInputHandler != null) {
            videoSlider.registerWithInputHandler(touchInputHandler);
        }
    }

    private void detachMenu() {
        controls.detachControls();
        if (touchInputHandler != null) {
            controls.unRegisterWithInputHandler(touchInputHandler);
        }
    }

    private void attachControls() {
        controls.attachControls();
        if (touchInputHandler != null) {
            controls.registerWithInputHandler(touchInputHandler);
        }
    }

    public boolean isDragging() {
        return isDragging;
    }

    public void startDragAnimation() {
        pivot.attachChild(this.overlayDrag);
        if (dragAnimationHandlerThread == null
                || !dragAnimationHandlerThread.isAlive()) {
            dragAnimationHandlerThread = new Thread(
                    dragAnimationHandler);
            dragAnimationHandlerThread.start();
        }
        isDragging = true;
    }

    public void stopDragAniation() {
        isDragging = false;
    }

    @Override
    protected void touchDeadAction(int touchId) {
        stopDragAniation();
        if (isUnderThreshold(touchId)) {
            attachOverlays();
        }
        if (touchCount.containsKey(touchId)) {
            touchCount.remove(touchId);
        }
    }
}

/**
 * RepeatHandler <p> handles slider updates an video repeating
 *
 * @author Tobias Zimmermann
 *
 */
class RepeatHandler implements Runnable {

    private VideoComponent video;
    private boolean close;
    private Logger log = Logger.getLogger(RepeatHandler.class);

    public void cleanUp() {
        this.close = true;
    }

    public RepeatHandler(VideoComponent video) {
        this.video = video;
    }

    public void run() {
        while (!close) {
            video.updateSlider((float) video.getTime()
                    / (float) video.getMaxTime());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error(e);
            }
            if (!close && video.isFinished()) {
                if (video.isRepeating()) {
                    video.replay();
                }
            }
        }
    }
}

class DragAnimationHandler implements Runnable {

    private VideoComponent video;
    private final Object obj;
    private Node pivot;
    private Logger log = Logger.getLogger(AnimationHandler.class);

    public Object getObject() {
        return obj;
    }

    public DragAnimationHandler() {
        obj = new Object();
    }

    public void initComponent(VideoComponent video, Node node) {
        this.video = video;
        this.pivot = node;
    }

    public synchronized void run() {
        synchronized (obj) {
            while (video.isDragging()) {
                try {
                    SpatialTransformer st = new SpatialTransformer(1);
                    st.setObject(pivot, 0, -1);
                    Quaternion x0 = new Quaternion();
                    x0.fromAngleAxis(0, new Vector3f(0, 0, 1));
                    st.setScale(0, 0, new Vector3f(1.0f, 1.0f, 1.0f));
                    Quaternion x180 = new Quaternion();
                    x180.fromAngleAxis(FastMath.DEG_TO_RAD * 180, new Vector3f(
                            0, 0, 1));
                    st.setScale(0, 0.25f, new Vector3f(0.9f, 0.9f, 0.9f));
                    Quaternion x360 = new Quaternion();
                    x360.fromAngleAxis(FastMath.DEG_TO_RAD * 360, new Vector3f(
                            0, 0, 1));
                    st.setScale(0, 0.5f, new Vector3f(1.0f, 1.0f, 1.0f));
                    st.interpolateMissing();
                    pivot.addController(st);
                    obj.wait(500);
                    pivot.removeController(0);
                } catch (InterruptedException e) {
                    log.error(e);
                }
            }
            pivot.detachAllChildren();
        }
    }
}
