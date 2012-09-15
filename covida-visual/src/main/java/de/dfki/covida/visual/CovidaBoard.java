/*
 * CovidaBoard.java
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
package de.dfki.covida.visual;

import com.jme.animation.SpatialTransformer;
import com.jme.image.Texture;
import com.jme.image.Texture.WrapMode;
import com.jme.image.Texture2D;
import com.jme.input.InputHandler;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.TexCoords;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.awt.swingui.ImageGraphics;
import de.dfki.covida.CovidaCMDOptions;
import de.dfki.covida.covidacore.data.CovidaConfiguration;
import de.dfki.covida.covidacore.data.VideoFormat;
import de.dfki.covida.visual.components.DrawingOverlay;
import de.dfki.covida.visual.components.LockState;
import de.dfki.covida.visual.components.button.ClipboardButton;
import de.dfki.covida.visual.components.button.SearchButton;
import de.dfki.covida.visual.components.video.VideoComponent;
import de.dfki.covida.visual.components.video.VideoPreloadComponent;
import de.dfki.touchandwrite.action.TouchAction;
import de.dfki.touchandwrite.action.TouchActionEvent;
import de.dfki.touchandwrite.input.pen.hwr.HandwritingRecognitionEvent;
import de.dfki.touchandwrite.input.touch.event.TouchState;
import de.dfki.touchandwrite.math.FastMath;
import de.dfki.touchandwrite.shape.Shape;
import de.dfki.touchandwrite.visual.components.*;
import de.dfki.touchandwrite.visual.input.PenInputHandler;
import de.dfki.touchandwrite.visual.input.TouchInputHandler;
import java.awt.*;
import java.util.*;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Component to handle the VideoTouch application
 *
 * @author Tobias Zimmermann
 *
 */
public class CovidaBoard extends AbstractTouchAndWriteComponent implements
        TouchableComponent {

    private static int TRESHOLD = 3;
    private boolean eeeSlate = true;
    private final ArrayList<ClipboardButton> clipboardButtons;

    /**
     * @return the videoCount
     */
    protected int getVideoCount() {
        return configuration.videos.size();
    }
    /**
     * Alpha composite for transparent panel.
     */
    protected final AlphaComposite TRANSPARENT = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 0.0f);
    protected final AlphaComposite SOLID = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 1.0f);
    /**
     * Logger.
     */
    private Logger log = Logger.getLogger(CovidaBoard.class);
    /**
     * Width of this pen's texture.
     */
    protected int boardWidth;
    /**
     * Height of this pen's texture.
     */
    protected int boardHeight;
    /**
     * The drawing board is registered with this handler.
     */
    private InputHandler registeredInputHandler;
    /**
     * Touch action
     */
    private TouchAction touchAction;
    /**
     * Texture state.
     */
    protected TextureState ts;
    /**
     * Texture state.
     */
    protected TextureState tsBackground;
    /**
     * The texture which has to be dynamically updated.
     */
    protected Texture texture;
    /**
     * Background image.
     */
    protected Texture bgTexture;
    /**
     * Drawing will be done with Java2D.
     */
    protected ImageGraphics g2d;
    /**
     * Drawing board.
     */
    protected Quad background;
    /**
     * Drawing board.
     */
    protected Quad board;
    private DrawingOverlay drawingOverlay;
    protected Color backgroundColor = Color.white;
    /**
     * Map a
     * <code>Shape</code> object into its corresponding
     * <code>Spatial</code> object
     */
    protected Map<Shape, Spatial> shapeSpatials;
    /**
     * Mapping of IDs and last seen touch event.
     */
    protected Map<Integer, TouchPoint> touchPoints;
    /**
     * Current shape color.
     */
    protected Color shapeColor = Color.blue;
    /**
     * MRT image
     */
    protected DisplayImageComponent mrtImage;
    /**
     * Display Resolution Handler
     */
    private Vector2f display;
    /**
     * Handwriting
     */
    protected List<HandwritingRecognitionEvent> hwrEvents;
    /**
     * Stores the original resolution of the background image
     */
    protected int originalImageWidth, originalImageHeight;
    /**
     * Marks the point, where the background image is drawn.
     */
    protected int backgroundX, backgroundY;
    /**
     * Stores the scaled resolution of the background image
     */
    protected int scaledImageHeight, scaledImageWidth;
    private ArrayList<VideoComponent> videos;
    private ArrayList<Node> leafNodes;
    private Node rootNode;
    private Node videoNode;
    private SplashHandler splashHandler;
    private Thread splashHandlerThread;
    private Quad splash;
    private TouchInputHandler touchInputHandler;
    private PenInputHandler penInputHandler;
    private CovidaConfiguration configuration;
    private LockState lockState;
    protected Map<Integer, Node> touchNodes;
    private ArrayList<Integer> iteratorList;
    private Vector2f borders;
    private CovidaCMDOptions opt;
    private ArrayList<SearchButton> searchButtons;
    private Node menuNode;
    private ArrayList<Quad> overlayReset;
    protected HashMap<Integer, Dimension> videoDimensions;
    protected ArrayList<Integer> indexes;
    /**
     * max Height in % of display.y
     */
    private int maxHeight;
    /**
     * max width in pixel
     */
    private int maxWidth;
    private ArrayList<VideoPreloadComponent> preloadVideo;
    private boolean initialized;
    private List<VideoFormat> videoFormat;
    /**
     * Stores touch event count for every touchId
     */
    protected Map<Integer, Integer> touchCount;
    /**
     * Stores touch positions
     */
    protected Map<Integer, ArrayList<Vector3f>> touchPositions;

    public CovidaBoard(CovidaCMDOptions opt) {
        super(ComponentType.COMPONENT_2D, "CoVidA Board");
        this.opt = opt;
        rootNode = new Node("Root Node");
        videoNode = new Node("Video Node");
        touchNodes = new HashMap<>();
        videoFormat = new ArrayList<>();
        lockState = LockState.getInstance();
        display = new Vector2f(DisplaySystem.getDisplaySystem().getWidth(),
                DisplaySystem.getDisplaySystem().getHeight());
        board = new Quad("Drawingboard-Quad", this.display.x, this.display.y);
        drawingOverlay = new DrawingOverlay("Drawingboard-Quad",
                (int) this.display.x, (int) this.display.y);
        drawingOverlay.initComponent();
        background = new Quad("Background-Image-Quad", this.display.x,
                this.display.y);
        splash = new Quad("Background-Image-Quad", this.display.x,
                this.display.y);
        boardHeight = (int) this.display.y;
        boardWidth = (int) this.display.x;
        registeredInputHandler = null;
        touchAction = new TouchAction(this);
        hwrEvents = new ArrayList<>();
        videos = new ArrayList<>();
        leafNodes = new ArrayList<>();
        touchCount = new HashMap<>();
        iteratorList = new ArrayList<>();
        searchButtons = new ArrayList<>();
        clipboardButtons = new ArrayList<>();
        touchPositions = new HashMap<>();
        preloadVideo = new ArrayList<>();
    }

    /**
     * @return the videos
     */
    protected ArrayList<VideoComponent> getVideos() {
        return videos;
    }

    /**
     * Initialize method for the class VideoTouchBoard
     *
     * @param opt
     */
    @Override
    public void initComponent() {
        configuration = CovidaConfiguration.getInstance();
        // TODO
//        configuration = CovidaConfiguration.load(new File(opt.getConfiguration()));
        // Initialization
        Quaternion q = new Quaternion();
        // Rotation need because of ImageGraphics
        q.fromAngles(0f, (float) Math.toRadians(180),
                (float) Math.toRadians(180));
        board.rotatePoints(q);
        background.rotatePoints(q);
        initalizeTextures(initalizeBlendState());
        fullScreen();
        indexes = new ArrayList<>();
        videoDimensions = new HashMap<>();
        for (int i = 0; i < configuration.videos.size(); i++) {
            int index = getRandomUniqueIterator();
            indexes.add(index);
        }
        // Splash Screen
        attachChild(splash);
        SpatialTransformer st = new SpatialTransformer(1);
        st.setObject(splash, 0, -1);
        st.setScale(0, 0, new Vector3f(1.0f, 1.0f, 1.0f));
        st.setScale(0, 2.0f, new Vector3f(0.4f, 0.4f, 1.0f));
        st.setScale(0, 4.0f, new Vector3f(1.0f, 1.0f, 1.0f));
        st.interpolateMissing();
        st.setRepeatType(SpatialTransformer.RT_CYCLE);
        addController(st);
        splashHandler = new SplashHandler(this);
        splashHandlerThread = new Thread(splashHandler);
        splashHandlerThread.start();
    }

    protected VideoPreloadComponent preLoadVideo(int i) {
        // preload video to get video dimensions
        int index = indexes.get(i);
        preloadVideo.add(new VideoPreloadComponent(configuration.videoSources.get(index).videoSource));
        this.attachChild(preloadVideo.get(preloadVideo.size() - 1));
        return preloadVideo.get(preloadVideo.size() - 1);
    }

    protected ArrayList<VideoPreloadComponent> getPreloadedVideo() {
        return preloadVideo;
    }

    protected void init() {
        log.debug("Initialization of the covida board");
        this.setLightCombineMode(LightCombineMode.Off);
        maxWidth = 0;
        maxHeight = 0;
        // TODO Video Size Correction
        for (int i = 0; i < configuration.videos.size(); i++) {
            int size = (int) (configuration.videos.get(i).size * 0.01f * display.y);
            VideoFormat format = new VideoFormat(
                    (float) videoDimensions.get(indexes.get(i)).width
                    / (float) videoDimensions.get(indexes.get(i)).height);
            videoFormat.add(format);
            if (format.determineWidth(size) > maxWidth) {
                maxWidth = format.determineWidth(size);
                if (eeeSlate) {
                    size = 300;
                    maxHeight = 300;
                    maxWidth = format.determineWidth(size);
                    borders = new Vector2f(1, 1);
                } else {
                    if (maxWidth * 3 > display.x * 0.9f) {
                        float height = (display.x * 0.25f) / format.getRatio();
                        configuration.videos.get(i).size = (int) ((height / display.y) * 100.f);
                        size = (int) (configuration.videos.get(i).size * 0.01f * display.y);
                        maxWidth = format.determineWidth(size);
                        log.debug("VideoSize correction Width:" + maxWidth
                                + " Height(%):" + configuration.videos.get(i).size);
                    }
                    if (configuration.videos.get(i).size > maxHeight) {
                        maxHeight = configuration.videos.get(i).size;
                    }
                    borders = new Vector2f((display.x - (maxWidth * 3.f)) / 4.f,
                            (display.y - (maxHeight * 0.01f * display.y * 2.f)) / 3.f);
                }
            }
        }

        log.debug("Borders: " + borders);
        log.debug("Attach background");

        rootNode.attachChild(background);
        rootNode.attachChild(videoNode);
        rootNode.attachChild(board);

        createVideos();
//        createCornerMenus();
    }

    /**
     * Creates the corner menus and buttons
     */
    private void createCornerMenus() {
        // attach search buttons
        menuNode = new Node("VideoTouchBoardMenu Node");
        rootNode.attachChild(menuNode);
        Vector3f translation = new Vector3f(0, 0, 0);
        float gap = display.x / 16.0f;
        log.debug("Attach MenuButtons");
        for (int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    translation = new Vector3f(gap, gap, 0);
                    break;
                case 1:
                    translation = new Vector3f(display.x - gap, gap, 0);
                    break;
                case 2:
                    translation = new Vector3f(display.x - gap, display.y - gap, 0);
                    break;
                case 3:
                    translation = new Vector3f(gap, display.y - gap, 0);
                    break;
            }
            Node menuButtonNode = new Node("VideoTouchBoard - MenuButton Node " + i);
            menuButtonNode.setLocalTranslation(translation);
            Quaternion q2 = new Quaternion();
            q2.fromAngleAxis(FastMath.DEG_TO_RAD * (i * 90), new Vector3f(0, 0,
                    1));
            menuButtonNode.setLocalRotation(q2);
            if (i == 0 || i == 2) {
                SearchButton button = new SearchButton((int) gap,
                        (int) (gap * 1.2f), menuButtonNode, videos);
                searchButtons.add(button);
                button.initComponent();
            } else {
                ClipboardButton button = new ClipboardButton((int) gap,
                        (int) (gap * 1.2f), menuButtonNode);
                clipboardButtons.add(button);
                button.initComponent();
            }
            menuNode.attachChild(menuButtonNode);
            log.debug("Attach drawing overlay");
            rootNode.attachChild(drawingOverlay);
        }
    }

    private void createVideos() {
        // Create videos:
        log.debug("Create videos");
        for (int i = 0; i < configuration.videos.size(); i++) {
            Node node = new Node("Leaf Node " + leafNodes.size());
            if (eeeSlate) {
                node.setLocalTranslation(580 + (600 * i), 425, 0);
            } else {
                node.setLocalTranslation(getInitPosition(true, i),
                        getInitPosition(false, i), 0);
            }
            // Create video field
            VideoComponent video;
            int index = indexes.get(i);
            int size = (int) (configuration.videos.get(i).size * 0.01f * display.y);
            VideoFormat format = new VideoFormat(
                    (float) videoDimensions.get(index).width
                    / (float) videoDimensions.get(index).height);
            if (eeeSlate) {
                video = new VideoComponent(
                        configuration.videoSources.get(index).videoSource, 450,
                        format, node);
            } else {
                video = new VideoComponent(
                        configuration.videoSources.get(index).videoSource, size,
                        format, node);
            }
            video.setRepeat(true);
            video.setName(configuration.videoSources.get(index).videoName);
            if (configuration.videoSources.get(index).time_start > 0) {
                if (configuration.videoSources.get(index).time_end > 0) {
                    log.debug("Time Range id: " + index + " Range: "
                            + configuration.videoSources.get(index).time_start
                            + " - "
                            + configuration.videoSources.get(index).time_end);
                    video.setTimeRange(
                            configuration.videoSources.get(index).time_start,
                            configuration.videoSources.get(index).time_end);
                }
            }
            this.videoNode.attachChild(node);
            node.attachChild(video);
            this.leafNodes.add(node);
            video.initComponent();
            videos.add(video);
        }
    }

    protected boolean isInitialized() {
        return initialized;
    }

    protected void openOverlays() {
        for (VideoComponent video : videos) {
            video.changeOverlays();
            video.attachAnnotation();
            video.attachList();
        }
        for (SearchButton search : searchButtons) {
            search.changeOverlay();
        }
        for (ClipboardButton clipboard : clipboardButtons) {
            clipboard.changeOverlay();
        }
    }

    protected void closeOverlays() {
        for (VideoComponent video : videos) {
            video.changeOverlays();
            video.detachList();
            video.detachAnnotation();
        }
        for (SearchButton search : searchButtons) {
            search.changeOverlay();
        }
        for (ClipboardButton clipboard : clipboardButtons) {
            clipboard.changeOverlay();
        }
        // this.app.refreshMenus();
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

    private void initalizeTextures(BlendState alpha) {
        // ---- Texture state initialization ----
        ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setCorrectionType(TextureState.CorrectionType.Perspective);
        ts.setEnabled(true);
        texture = new Texture2D();
        texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
        texture.setWrap(Texture.WrapMode.Repeat);

        // ---- Drawable image initialization ----
        g2d = ImageGraphics.createInstance(boardWidth, boardHeight, 0);
        enableAntiAlias(g2d);

        refreshBoard();
        texture.setImage(g2d.getImage());
        ts.setTexture(texture);
        this.board.setRenderState(alpha);
        this.board.setRenderState(ts);
        this.board.updateRenderState();

        // set background Texture
        Texture backgroundTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(
                configuration.texturePath + "1280x800.jpg"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        backgroundTexture.setWrap(WrapMode.Repeat);

        Vector2f[] texCoords = new Vector2f[4];
        texCoords[0] = new Vector2f(0, 0);
        texCoords[3] = new Vector2f(1, 0);
        texCoords[1] = new Vector2f(0, 1);
        texCoords[2] = new Vector2f(1, 1);
        this.background.setTextureCoords(TexCoords.makeNew(texCoords));

        TextureState backgroundTextureState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        backgroundTextureState.setTexture(backgroundTexture);
        this.background.setRenderState(backgroundTextureState);
        this.background.updateRenderState();

        // set splash screen background Texture
        Texture splashTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(
                configuration.texturePath + "splash.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        splashTexture.setWrap(WrapMode.Repeat);

        this.background.setTextureCoords(TexCoords.makeNew(texCoords));

        TextureState splashTextureState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        splashTextureState.setTexture(splashTexture);
        this.splash.setRenderState(splashTextureState);
        this.splash.updateRenderState();

        // reset textures
        this.overlayReset = new ArrayList<>();
        ArrayList<String> textureList = new ArrayList<>();
        textureList.add("media/textures/reset.png");
        textureList.add("media/textures/reset_0.png");
        textureList.add("media/textures/reset_1.png");
        textureList.add("media/textures/reset_2.png");
        textureList.add("media/textures/reset_3.png");
        textureList.add("media/textures/reset_4.png");
        for (int i = 0; i < textureList.size(); i++) {
            Texture overlayMenuTexture = TextureManager.loadTexture(getClass().getClassLoader().getResource(textureList.get(i)),
                    Texture.MinificationFilter.BilinearNearestMipMap,
                    Texture.MagnificationFilter.Bilinear);
            overlayMenuTexture.setWrap(WrapMode.Clamp);

            TextureState overlayMenuState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
            overlayMenuState.setTexture(overlayMenuTexture);

            this.overlayReset.add(new Quad(("Overlay-Reset-Quad-0" + i),
                    display.x * 0.1f, display.x * 0.1f));

            overlayReset.get(overlayReset.size() - 1).setRenderState(
                    overlayMenuState);
            overlayReset.get(overlayReset.size() - 1).setRenderState(alpha);
            overlayReset.get(overlayReset.size() - 1).updateRenderState();
            overlayReset.get(overlayReset.size() - 1).getLocalTranslation().set(new Vector3f(0, 0, 0));
        }
    }

    private int getInitPosition(boolean xAxis, int index) {
        int offset;
        int position;
        int size = (int) (maxHeight * 0.01f * display.y);
        if (eeeSlate) {
            if (xAxis) {
                if (configuration.videos.get(index).position > 2) {
                    position = configuration.videos.get(index).position - 3;
                } else {
                    position = configuration.videos.get(index).position;
                }
                offset = (int) (this.borders.x + (maxWidth / 2) + position
                        * (maxWidth + this.borders.x));
            } else {
                if (configuration.videos.get(index).position > 2) {
                    position = 0;
                } else {
                    position = 1;
                }
                offset = (int) (this.borders.y + (size / 2) + position
                        * (size + this.borders.y));
            }
            return offset;
        } else {
            if (xAxis) {
                if (configuration.videos.get(index).position > 2) {
                    position = configuration.videos.get(index).position - 3;
                } else {
                    position = configuration.videos.get(index).position;
                }
                offset = (int) (this.borders.x + (maxWidth / 2) + position
                        * (maxWidth + this.borders.x));
            } else {
                if (configuration.videos.get(index).position > 2) {
                    position = 0;
                } else {
                    position = 1;
                }
                offset = (int) (this.borders.y + (size / 2) + position
                        * (size + this.borders.y));
            }
            return offset;
        }

    }

    private int getRandomUniqueIterator() {
        Random random = new java.util.Random();
        int i;
        do {
            i = random.nextInt(configuration.videoSources.size());
        } while (iteratorList.contains(new Integer(i)));
        iteratorList.add(i);
        return i;
    }

    /**
     * attach nodes
     */
    public void attachNodes() {
        if (this.hasChild(splash)) {
            this.detachChild(splash);
        }
        if (!this.hasChild(rootNode)) {
            this.attachChild(rootNode);
            // rootNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);
            // rootNode.setZOrder(5);
            // System.out.println("ATTACHING NODES");
            // TODO
        }
    }

    /**
     * Starts all videos
     */
    public void startVideos() {
        for (VideoComponent video : this.videos) {
            video.start();
            video.startHandler();
            video.setVolume(0);
        }
        log.info("VideoTouchBoard initialised.");
        initialized = true;
    }

    /**
     * Stops all videos
     */
    public void stopVideos() {
        for (VideoComponent video : this.videos) {
            video.stop();
        }
    }

    public void cleanUp() {
        for (VideoComponent video : videos) {
            video.cleanUp();
        }
    }

    /**
     * Pauses all videos
     *
     */
    void pauseVideos() {
        for (VideoComponent video : this.videos) {
            video.pause();
        }

    }

    /**
     * Translates quad that it fits to fullscreen.
     */
    private void fullScreen() {
        this.setCullHint(Spatial.CullHint.Dynamic);
        this.board.getLocalRotation().set(0, 0, 0, 1);
        this.board.getLocalTranslation().set(this.boardWidth / 2,
                this.boardHeight / 2, 0);
        this.board.getLocalScale().set(1, 1, 1);
        // this.board.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        this.board.setCullHint(Spatial.CullHint.Inherit);
        this.background.getLocalRotation().set(0, 0, 0, 1);
        this.background.getLocalTranslation().set(this.boardWidth / 2,
                this.boardHeight / 2, 0);
        this.background.getLocalScale().set(1, 1, 1);
        // this.background.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        this.background.setCullHint(Spatial.CullHint.Inherit);
        this.splash.getLocalRotation().set(0, 0, 0, 1);
        this.splash.getLocalTranslation().set(this.boardWidth / 2,
                this.boardHeight / 2, 0);
        this.splash.getLocalScale().set(1, 1, 1);
        // this.splash.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        this.splash.setCullHint(Spatial.CullHint.Inherit);
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
     * Removes all the drawings from the board.
     */
    public void refreshBoard() {
        // first clear strokes
        g2d.clearRect(0, 0, boardWidth, boardHeight);
        // paint with transparent color
        g2d.setComposite(TRANSPARENT);
        g2d.setColor(this.backgroundColor);
        g2d.fillRect(0, 0, boardWidth, boardHeight);
        g2d.setComposite(SOLID);
        g2d.update();
    }

    public int getID() {
        return 9999;
    }

    @Override
    public void unRegisterWithInputHandler(TouchInputHandler input) {
        input.removeAction(touchAction);
    }

    private void removeResetOverlay(int id) {
        for (Integer i : touchNodes.keySet()) {
            if (touchNodes.get(i).getLocalScale().x == 0) {
                touchNodes.get(i).removeFromParent();
                touchNodes.remove(i);
            }
        }
        if (touchNodes.containsKey(id)) {
            Node tmp = new Node("Touch Temp Node " + id);
            tmp.getLocalTranslation().set(
                    new Vector3f(touchNodes.get(id).getLocalTranslation().clone()));
            tmp.getLocalRotation().set(
                    new Quaternion(touchNodes.get(id).getLocalRotation().clone()));
            List<Spatial> spatials = touchNodes.get(id).getChildren();
            int begin = 0;
            int limit = Math.min(spatials.size(), 5);
            if (touchPositions.get(id).size() > 10) {
                begin = 1;
                limit = limit + 1;
            }
            for (int i = begin; i < limit; i++) {
                Quad quad = new Quad(("Overlay-Reset-Quad-" + i), overlayReset.get(i).getWidth(), overlayReset.get(i).getHeight());
                quad.setRenderState(overlayReset.get(i).getRenderState(
                        StateType.Texture));
                quad.setRenderState(overlayReset.get(i).getRenderState(
                        StateType.Blend));
                quad.updateRenderState();
                tmp.attachChild(quad);
            }
            touchNodes.get(id).getParent().attachChild(tmp);
            touchNodes.get(id).detachAllChildren();
            touchNodes.get(id).getParent().detachChild(touchNodes.get(id));
            touchNodes.remove(id);
            SpatialTransformer st = new SpatialTransformer(1);
            st.setObject(tmp, 0, -1);
            st.setRotation(0, 0, tmp.getLocalRotation());
            st.setScale(0, 0, tmp.getLocalScale());
            st.setScale(0, 0.5f, new Vector3f(2, 2, 2));
            st.setScale(0, 1.f, new Vector3f(0, 0, 0));
            st.interpolateMissing();
            tmp.addController(st);
        }
    }

    /**
     * Touch Event
     *
     */
    @Override
    public void touch(Map<Integer, TouchActionEvent> event) {
        for (TouchActionEvent e : event.values()) {
            if (!lockState.isTouchLocked(e.getID()) && lockState.inArea(e.getX(), e.getY())) {
                return;
            }
            TouchState state = e.getTouchState();
            if (state.equals(TouchState.TOUCH_DEAD)) {
                if (touchCount.containsKey(e.getID())) {
                    if (touchCount.get(e.getID()) > TRESHOLD * 5) {
                        Vector2f norm = new Vector2f(0, -1);
                        Vector2f point = new Vector2f(e.getX()
                                - touchPositions.get(e.getID()).get(0).x,
                                e.getY()
                                - touchPositions.get(e.getID()).get(0).y);
                        for (VideoComponent video : videos) {
                            video.reset(norm.angleBetween(point));
                        }
                    }
                    touchCount.remove(e.getID());
                    ArrayList<Vector3f> list = new ArrayList<>();
                    list.add(new Vector3f(e.getX(), e.getY(), 0));
                    touchPositions.put(e.getID(), list);
                    lockState.removeTouchLock(e.getID());
                    removeResetOverlay(e.getID());
                }
            }
            if (lockState.onTop(e.getID(), new Vector2f(e.getX(), e.getY()), this)) {
                if (!state.equals(TouchState.TOUCH_DEAD)) {
                    if (!touchCount.containsKey(e.getID())) {
                        touchCount.put(e.getID(), 1);
                        lockState.forceTouchLock(e.getGroupID(), this.getID());
                        ArrayList<Vector3f> list = new ArrayList<>();
                        list.add(new Vector3f(e.getX(), e.getY(), 0));
                        touchPositions.put(e.getID(), list);
                        if (!touchNodes.containsKey(e.getID())) {
                            touchNodes.put(e.getID(),
                                    new Node("TouchNode " + e.getID()));
                            touchNodes.get(e.getID()).setLocalTranslation(
                                    e.getX(), e.getY(), 0);
                            rootNode.attachChild(touchNodes.get(e.getID()));
                            Quad quad = new Quad(("Overlay-Reset-Quad"),
                                    overlayReset.get(0).getWidth(),
                                    overlayReset.get(0).getHeight());
                            quad.setRenderState(overlayReset.get(0).getRenderState(StateType.Texture));
                            quad.setRenderState(overlayReset.get(0).getRenderState(StateType.Blend));
                            quad.updateRenderState();
                            touchNodes.get(e.getID()).attachChild(quad);
                            Vector2f norm = new Vector2f(0, -1);
                            Vector2f point = new Vector2f(e.getX()
                                    - (display.x / 2), e.getY()
                                    - (display.y / 2));
                            Quaternion q = new Quaternion();
                            q = q.fromAngleAxis(norm.angleBetween(point),
                                    new Vector3f(0, 0, 1));
                            touchNodes.get(e.getID()).getLocalRotation().set(q);
                        } else {
                            Vector2f norm = new Vector2f(0, -1);
                            Vector2f point = new Vector2f(e.getX()
                                    - (display.x / 2), e.getY()
                                    - (display.y / 2));
                            Quaternion q = new Quaternion();
                            q = q.fromAngleAxis(norm.angleBetween(point),
                                    new Vector3f(0, 0, 1));
                            touchNodes.get(e.getID()).getLocalRotation().set(q);
                            touchNodes.get(e.getID()).getLocalTranslation().set(e.getX(), e.getY(), 0);
                        }
                    } else {
                        if (touchCount.containsKey(e.getID())) {
                            if (touchCount.get(e.getID()) < TRESHOLD) {
                                touchCount.put(e.getID(),
                                        touchCount.get(e.getID()) + 1);
                            } else {
                                if (!touchNodes.containsKey(e.getID())) {
                                    touchNodes.put(e.getID(), new Node(
                                            "TouchNode " + e.getID()));
                                    touchNodes.get(e.getID()).setLocalTranslation(e.getX(),
                                            e.getY(), 0);
                                    rootNode.attachChild(touchNodes.get(e.getID()));
                                    touchNodes.get(e.getID()).attachChild(
                                            overlayReset.get(0));
                                } else {
                                    // Adds new touch position if touch had
                                    // moved
                                    if (!touchPositions.get(e.getID()).get(touchPositions.get(e.getID()).size() - 1).equals(new Vector3f(e.getX(), e.getY(), 0))) {
                                        touchPositions.get(e.getID()).add(
                                                new Vector3f(e.getX(),
                                                e.getY(), 0));
                                    }
                                    if (touchPositions.get(e.getID()).size() == TRESHOLD + 1) {
                                        Quad quad = new Quad(
                                                ("Overlay-Reset-Quad-0"),
                                                overlayReset.get(1).getWidth(),
                                                overlayReset.get(1).getHeight());
                                        quad.setRenderState(overlayReset.get(1).getRenderState(
                                                StateType.Texture));
                                        quad.setRenderState(overlayReset.get(1).getRenderState(StateType.Blend));
                                        quad.updateRenderState();
                                        touchNodes.get(e.getID()).detachAllChildren();
                                        touchNodes.get(e.getID()).attachChild(
                                                quad);
                                    } else if (touchPositions.get(e.getID()).size() > TRESHOLD) {
                                        touchCount.put(e.getID(),
                                                touchCount.get(e.getID()) + 1);
                                    }
                                }
                                Vector2f norm = new Vector2f(0, -1);
                                Vector2f point = new Vector2f(
                                        e.getX()
                                        - touchPositions.get(e.getID()).get(0).x, e.getY()
                                        - touchPositions.get(e.getID()).get(0).y);
                                Quaternion q = new Quaternion();
                                q = q.fromAngleAxis(norm.angleBetween(point),
                                        new Vector3f(0, 0, 1));
                                touchNodes.get(e.getID()).getLocalRotation().set(q);
                                touchNodes.get(e.getID()).getLocalTranslation().set(e.getX(), e.getY(), 0);
                                attachResetOverlay(e);
                            }
                        } else {
                            touchCount.put(e.getID(), 1);
                            ArrayList<Vector3f> list = new ArrayList<>();
                            list.add(new Vector3f(e.getX(), e.getY(), 0));
                            touchPositions.put(e.getID(), list);
                            attachResetOverlay(e);
                        }
                    }
                } else {
                }
            }
        }
    }

    private void attachResetOverlay(TouchActionEvent e) {
        if (touchCount.get(e.getID()) > TRESHOLD * 5) {
            if (touchNodes.get(e.getID()).getChildren().size() < 5) {
                Quad quad = new Quad(("Overlay-Reset-Quad-4"), overlayReset.get(5).getWidth(), overlayReset.get(5).getHeight());
                quad.setRenderState(overlayReset.get(5).getRenderState(
                        StateType.Texture));
                quad.setRenderState(overlayReset.get(5).getRenderState(
                        StateType.Blend));
                quad.updateRenderState();
                touchNodes.get(e.getID()).attachChild(quad);
            }
        } else if (touchCount.get(e.getID()) > TRESHOLD * 4) {
            if (touchNodes.get(e.getID()).getChildren().size() < 4) {
                Quad quad = new Quad(("Overlay-Reset-Quad-3"), overlayReset.get(4).getWidth(), overlayReset.get(4).getHeight());
                quad.setRenderState(overlayReset.get(4).getRenderState(
                        StateType.Texture));
                quad.setRenderState(overlayReset.get(4).getRenderState(
                        StateType.Blend));
                quad.updateRenderState();
                touchNodes.get(e.getID()).attachChild(quad);
            }
        } else if (touchCount.get(e.getID()) > TRESHOLD * 3) {
            if (touchNodes.get(e.getID()).getChildren().size() < 3) {
                Quad quad = new Quad(("Overlay-Reset-Quad-2"), overlayReset.get(3).getWidth(), overlayReset.get(3).getHeight());
                quad.setRenderState(overlayReset.get(3).getRenderState(
                        StateType.Texture));
                quad.setRenderState(overlayReset.get(3).getRenderState(
                        StateType.Blend));
                quad.updateRenderState();
                touchNodes.get(e.getID()).attachChild(quad);
            }
        } else if (touchCount.get(e.getID()) > TRESHOLD * 2) {
            if (touchNodes.get(e.getID()).getChildren().size() < 2) {
                Quad quad = new Quad(("Overlay-Reset-Quad-1"), overlayReset.get(2).getWidth(), overlayReset.get(2).getHeight());
                quad.setRenderState(overlayReset.get(2).getRenderState(
                        StateType.Texture));
                quad.setRenderState(overlayReset.get(2).getRenderState(
                        StateType.Blend));
                quad.updateRenderState();
                touchNodes.get(e.getID()).attachChild(quad);
            }
        }
    }

    @Override
    public void registerWithInputHandler(TouchInputHandler input) {
        this.touchInputHandler = input;
    }

    public void registerTouch() {
        touchInputHandler.addAction(touchAction);

        videos.get(0).registerWithInputHandler(touchInputHandler);
        // drag, rotate, zoom Videos
        for (VideoComponent video : videos) {
            video.registerWithInputHandler(touchInputHandler);
        }
        for (SearchButton search : searchButtons) {
            search.registerWithInputHandler(touchInputHandler);
        }
        for (ClipboardButton clipboard : clipboardButtons) {
            clipboard.registerWithInputHandler(touchInputHandler);
            clipboard.getClipboard().update();
        }
    }

    public void registerWithInputHandler(PenInputHandler input) {
        this.penInputHandler = input;
    }

    public void unRegisterWithInputHandler(PenInputHandler input) {
        // TODO
    }

    public void registerPen() {
        if (registeredInputHandler != null) {
            // TODO
        }
        registeredInputHandler = penInputHandler;
        if (penInputHandler != null) {
            // video clipping / annotations
            for (VideoComponent video : videos) {
                video.registerWithInputHandler(penInputHandler);
            }
            // drawing feedback
            drawingOverlay.registerWithInputHandler(penInputHandler);
            for (SearchButton menu : searchButtons) {
                menu.getAnnotationSearchField().registerWithInputHandler(penInputHandler);
            }
            for (ClipboardButton clipboard : clipboardButtons) {
                clipboard.getClipboard().registerWithInputHandler(penInputHandler);
            }
        }
    }

    /**
     * @param log the log to set
     */
    public void setLog(Logger log) {
        this.log = log;
    }

    /**
     * @return the log
     */
    public Logger getLog() {
        return log;
    }

    public boolean isReady() {
        if (videos != null) {
            if (videos.size() == configuration.videos.size()) {
                log.debug("videos.size() == videoCount");
                if (videos.get(configuration.videos.size() - 1).isReady()
                        && touchInputHandler != null && touchAction != null
                        && penInputHandler != null) {
                    log.debug("##################");
                    log.debug("#CoVidA is ready.#");
                    log.debug("##################");
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isSensitiveArea(int id, int x, int y) {
        return true;
    }
}