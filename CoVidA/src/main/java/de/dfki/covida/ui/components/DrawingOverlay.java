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
package de.dfki.covida.ui.components;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.jme.image.Texture;
import com.jme.image.Texture2D;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.input.InputHandler;
import com.jme.math.Quaternion;
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
import com.jmex.awt.swingui.ImageGraphicsBaseImpl;

import de.dfki.covida.data.CovidaConfiguration;
import de.dfki.covida.data.PenData;
import de.dfki.covida.data.StrokeTrace;
import de.dfki.touchandwrite.action.DrawAction;
import de.dfki.touchandwrite.action.HWRAction;
import de.dfki.touchandwrite.action.PenActionEvent;
import de.dfki.touchandwrite.action.TouchAction;
import de.dfki.touchandwrite.action.TouchActionEvent;
import de.dfki.touchandwrite.analyser.pen.PenDataConversionUtil;
import de.dfki.touchandwrite.input.pen.event.ShapeEvent;
import de.dfki.touchandwrite.input.pen.hwr.HWRResultSet;
import de.dfki.touchandwrite.input.pen.hwr.HandwritingRecognitionEvent;
import de.dfki.touchandwrite.jme2.ShapeUtils;
import de.dfki.touchandwrite.shape.Circle;
import de.dfki.touchandwrite.shape.Ellipse;
import de.dfki.touchandwrite.shape.EllipticArc;
import de.dfki.touchandwrite.shape.LineSegment;
import de.dfki.touchandwrite.shape.Polygon;
import de.dfki.touchandwrite.shape.Quadrangle;
import de.dfki.touchandwrite.shape.Shape;
import de.dfki.touchandwrite.shape.Triangle;
import de.dfki.touchandwrite.visual.components.AbstractTouchAndWriteComponent;
import de.dfki.touchandwrite.visual.components.ComponentType;
import de.dfki.touchandwrite.visual.components.DrawingComponent;
import de.dfki.touchandwrite.visual.components.HWRSensitiveComponent;
import de.dfki.touchandwrite.visual.components.TouchPoint;
import de.dfki.touchandwrite.visual.components.TouchableComponent;
import de.dfki.touchandwrite.visual.input.PenInputHandler;
import de.dfki.touchandwrite.visual.input.TouchInputHandler;

/**
 * Drawing Overlay for videotouch
 * 
 * @author Tobias Zimmermann
 * 
 */
public class DrawingOverlay extends AbstractTouchAndWriteComponent implements
        DrawingComponent, HWRSensitiveComponent {

    /** Generated serial id */
    private static final long serialVersionUID = 3901990659651052688L;
    /** Alpha composite for transparent panel. */
    protected final AlphaComposite TRANSPARENT = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 0.0f);
    protected final AlphaComposite SOLID = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 1.0f);
    /** Logger. */
    private Logger log = Logger.getLogger(DrawingOverlay.class);
    /** Width of this pen's texture. */
    protected int boardWidth;
    /** Height of this pen's texture. */
    protected int boardHeight;
    /** The drawing board is registered with this handler. */
    private InputHandler registeredInputHandler;
    /** Touch action */
    private TouchAction touchAction;
    /** Draw action. */
    private DrawAction drawAction;
    /** Current stroke. */
    protected List<Vector3f> currentStroke;
    /** Strokes */
    protected List<StrokeTrace<Float>> strokes;
    /** Texture state. */
    protected TextureState ts;
    /** Texture state. */
    protected TextureState tsBackground;
    /** The texture which has to be dynamically updated. */
    protected Texture texture;
    /** Background image. */
    protected Texture bgTexture;
    /** Current color of the pen. Default is black. */
    private Map<Integer, Color> currentPenColor = new HashMap<Integer, Color>();
    /** Last x position of the pen. (-1 if there was a pen up event) */
    private Map<Integer, Integer> lastX = new HashMap<Integer, Integer>();
    /** Last y position of the pen. (-1 if there was a pen up event) */
    private Map<Integer, Integer> lastY = new HashMap<Integer, Integer>();
    /** Drawing will be done with Java2D. */
    protected ImageGraphics g2d;
    /** Is pen pressure considered. */
    private boolean penPressure = false;
    /** Thickness of the pen stroke */
    private float pen_thickness = 1.25f;
    /** Drawing board. */
    protected Quad board;
    /** Node which collects all shapes. */
    protected Node shapesNode = new Node("Shapes-Node");
    protected Color backgroundColor = Color.white;
    /** Detected shapes */
    protected List<Shape> shapes;
    /**
     * Map a <code>Shape</code> object into its corresponding
     * <code>Spatial</code> object
     */
    protected Map<Shape, Spatial> shapeSpatials;
    /** Mapping of ids and last seen touch event. */
    protected Map<Integer, TouchPoint> touchPoints;
    /** Current shape color. */
    protected Color shapeColor = Color.blue;
    /** Handwriting event action. */
    private HWRAction hwrAction;
    /** List of string, which has to be drawn. */
    private List<DisplayString> strings = new ArrayList<DisplayString>();

    /**
     * Different modes for drawing.
     * 
     * @author Tobias Zimmermann
     * 
     */
    protected enum DrawMode {

        GESTURE_MODE("Gesture Mode"), SHAPE_DETECTION_MODE("Shape Mode"), DRAW_MODE(
        "Draw Mode"), HWR_MODE("Handwriting Mode"), ERASER_MODE(
        "Eraser Mode"), AUTO_DETECTION_MODE("Automatic Mode Detection");
        private String display;

        private DrawMode(String displayText) {
            this.display = displayText;
        }

        /**
         * Display text.
         * 
         * @return
         */
        public String displayText() {
            return display;
        }
    }

    public class DisplayString {

        public DisplayString(String result, Point point) {
            this.string = result;
            this.point = point;
        }

        public String getString() {
            return this.string;
        }
        String string;
        Point point;
    }
    /** Current mode of the board. */
    protected DrawMode current_mode = DrawMode.AUTO_DETECTION_MODE;
    /** Handwriting */
    protected List<HandwritingRecognitionEvent> hwrEvents;
    /** Displays: Current mode. */
    // protected Text text;
    /** Stores the original resolution of the background image */
    protected int originalImageWidth, originalImageHeight;
    /** Marks the point, where the background image is drawn. */
    protected int backgroundX, backgroundY;
    /** Stores the scaled resolution of the background image */
    protected int scaledImageHeight, scaledImageWidth;
    /** Indicates if online mode detection is activated. */
    protected boolean onlineModeDetection;
    /** Flag which indicates of pen is active. */
    protected boolean penActive;
    private CovidaConfiguration config;
    private List<PenData> penColor;
    private Iterator<PenData> penColorIterator;

    /** Flag which indicates if the touch points should be displayed. */
    /**
     * Constructs a new drawing board which only listens to
     * 
     * @param name
     * @param limitWidth
     * @param limitHeight
     */
    public DrawingOverlay(String name, int limitWidth, int limitHeight) {
        this(name, limitWidth, limitHeight, true, false);
    }

    /**
     * Constructs a new drawing board which only listens to
     * 
     * @param name
     * @param limitWidth
     * @param limitHeight
     * @param onlinemodetection
     */
    public DrawingOverlay(String name, int limitWidth, int limitHeight,
            boolean onlinemodetection) {
        this(name, limitWidth, limitHeight, onlinemodetection, false);
    }

    /**
     * Constructs a new drawing board which only listens to
     * 
     * @param name
     * @param limitWidth
     * @param limitHeight
     * @param onlinemodetection
     */
    public DrawingOverlay(String name, int limitWidth, int limitHeight,
            boolean onlinemodetection, boolean touchDebug) {
        super(ComponentType.COMPONENT_2D, name);
        this.board = new Quad("Drawingboard-Quad", limitWidth, limitHeight);
        this.onlineModeDetection = onlinemodetection;
        this.boardHeight = limitHeight;
        this.boardWidth = limitWidth;
        this.registeredInputHandler = null;
        this.drawAction = new DrawAction(this);
        this.hwrAction = new HWRAction(this);
        this.currentStroke = new ArrayList<Vector3f>();
        this.strokes = new ArrayList<StrokeTrace<Float>>();
        this.hwrEvents = new ArrayList<HandwritingRecognitionEvent>();
        this.shapes = new ArrayList<Shape>();
        this.shapeSpatials = new HashMap<Shape, Spatial>();
        this.touchPoints = new HashMap<Integer, TouchPoint>();
        this.config = CovidaConfiguration.getInstance();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.dfki.touchandwrite.visual.components.TouchAndWriteComponent#initComponent
     * ()
     */
    public void initComponent() {
        penColor = config.pens;
        penColorIterator = penColor.iterator();
        Quaternion q = new Quaternion();
        // Rotation need because of ImageGraphics
        q.fromAngles(0f, (float) Math.toRadians(180), (float) Math.toRadians(180));
        this.board.rotatePoints(q);
        this.setLightCombineMode(LightCombineMode.Off);
        generateTexture();
        fullScreen();
        this.shapesNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        this.attachChild(this.board);
        this.attachChild(this.shapesNode);
    }

    /**
     * Translates quad that it fits to fullscreen.
     */
    private void fullScreen() {
        // final DisplaySystem display = DisplaySystem.getDisplaySystem();
        this.board.getLocalRotation().set(0, 0, 0, 1);
        this.board.getLocalTranslation().set(this.boardWidth / 2,
                this.boardHeight / 2, 0);
        this.board.getLocalScale().set(1, 1, 1);
        this.board.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        this.board.setCullHint(Spatial.CullHint.Never);
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

        g2d = ImageGraphics.createInstance(boardWidth, boardHeight, 0);
        enableAntiAlias(g2d);
        BlendState alpha = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
        alpha.setEnabled(true);
        alpha.setBlendEnabled(true);

        alpha.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        alpha.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        alpha.setTestEnabled(true);
        alpha.setTestFunction(BlendState.TestFunction.GreaterThan);
        refreshBoard();
        texture.setImage(g2d.getImage());
        ts.setTexture(texture);
        ts.apply();
        this.board.setRenderState(alpha);
        this.board.setRenderState(ts);
        this.board.updateRenderState();
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
        /*g2d.setColor(Color.RED);
        g2d.drawLine(100 - 10, 90, 100 + 10, 90);
        g2d.drawLine(100, 90 - 10, 100, 90 + 10);
        
        g2d.drawLine(100 - 10, 775, 100 + 10, 775);
        g2d.drawLine(100, 775 - 10, 100, 775 + 10);
        
        g2d.drawLine(1180 - 10, 775, 1180 + 10, 775);
        g2d.drawLine(1180, 775 - 10, 1180, 775 + 10);
        
        g2d.drawLine(1180 - 10, 90, 1180 + 10, 90);
        g2d.drawLine(1180, 90 - 10, 1180, 90 + 10);*/
        g2d.update();
    }

    /**
     * Stores the content of the board as a PNG image.
     * 
     * @param location
     */
    public void storeBoardContent(File location) {
        try {
            ImageIO.write(((ImageGraphicsBaseImpl) g2d).getAwtImage(), "PNG",
                    location);
        } catch (IOException e) {
            log.error(e);
        }
    }

    /**
     * @return the shapeSpatials
     */
    public Map<Shape, Spatial> getShapeSpatials() {
        return shapeSpatials;
    }

    /**
     * @return the shapesNode
     */
    public Node getShapesNode() {
        return shapesNode;
    }

    /**
     * @return the shapes
     */
    public List<Shape> getShapes() {
        return shapes;
    }

    /**
     * @return the strings
     */
    public List<DisplayString> getStrings() {
        return strings;
    }

    /**
     * @return the strokes
     */
    public List<StrokeTrace<Float>> getStrokes() {
        return strokes;
    }

    /**
     * @return the penActive
     */
    public boolean isPenActive() {
        return penActive;
    }

    /**
     * @param penActive
     *            the penActive to set
     */
    public void setPenActive(boolean penActive) {
        this.penActive = penActive;
    }

    /**
     * Sets the background for the drawing board.
     * 
     * @param background
     */
    public void setBackground(URL background) {
        try {
            Texture bg = loadImgAsTexture(background, this.boardWidth,
                    this.boardHeight);
            setBackgroundTexture(bg);
        } catch (IOException e) {
            log.error(e);
        }

    }

    /**
     * Sets the background texture.
     * 
     * @param bg
     */
    protected void setBackgroundTexture(Texture bg) {
        this.bgTexture = bg;
        tsBackground = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        tsBackground.setEnabled(true);
        this.tsBackground.setTexture(bgTexture);
        this.tsBackground.apply();
        Quaternion q = new Quaternion();
        // Rotation need because of ImageGraphics
        q.fromAngles(0f, (float) Math.toRadians(180), (float) Math.toRadians(180));
    }

    /**
     * Loads image and re-scales it to maximum size.
     * 
     * @param imageURL
     * @param limitWidth
     * @param limitHeight
     * @return
     * @throws IOException
     */
    protected Texture loadImgAsTexture(URL imageURL, int limitWidth,
            int limitHeight) throws IOException {
        Image img = ImageIO.read(imageURL);
        int width = 0, height = 0;

        originalImageWidth = img.getWidth(null);
        originalImageHeight = img.getHeight(null);
        if (originalImageHeight - limitHeight > originalImageWidth - limitWidth) {
            height = limitHeight;
            width = Math.round(originalImageWidth
                    * (limitHeight / (originalImageHeight * 1.0f)));

        } else { // Landscape
            height = Math.round(originalImageHeight
                    * (limitWidth / (originalImageWidth * 1.0f)));
            width = limitWidth;
        }
        scaledImageHeight = height;
        scaledImageWidth = width;
        backgroundX = (int) ((boardWidth - width) / 2.0);
        backgroundY = (int) ((boardHeight - height) / 2.0);
        // rescaled image
        BufferedImage scaledImage = new BufferedImage(boardWidth, boardHeight,
                BufferedImage.TYPE_INT_RGB);
        // Paint scaled version of image to new image
        Graphics2D graphics2D = scaledImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setColor(Color.white);
        graphics2D.fillRect(0, 0, boardWidth, boardHeight);
        graphics2D.drawImage(img, backgroundX, backgroundY, width, height, null);
        // clean up
        graphics2D.dispose();
        return TextureManager.loadTexture(scaledImage,
                MinificationFilter.BilinearNoMipMaps,
                MagnificationFilter.Bilinear, false);

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
        } catch (Exception e) {
            log.error(e);
        }
        super.draw(r);

    }

    public void updateShapeColor(Color awt) {
        ShapeUtils.changeShapeColor(awt);
    }

    /**
     * Updates the internal image.
     * 
     * @param x
     * @param y
     * @param i 
     */
    protected void updateImage(int x, int y, float force, int id) {
        if (!lastX.containsKey(new Integer(id))) {
            if (penColor.isEmpty()) {
                currentPenColor.put(id, Color.RED);
            } else {
                if (!currentPenColor.containsKey(new Integer(id))) {
                    if (!penColorIterator.hasNext()) {
                        penColorIterator = penColor.iterator();
                    }
                    currentPenColor.put(id, penColorIterator.next().penColor);
                }
            }
            g2d.setColor(currentPenColor.get(new Integer(id)));
            lastX.put(id, x);
            lastY.put(id, y);
        } else {
            if (this.penPressure) {
                this.g2d.setStroke(new BasicStroke(pen_thickness * force));
            } else {
                this.g2d.setStroke(new BasicStroke(pen_thickness));
            }
            this.g2d.drawLine(
                    lastX.get(new Integer(id)),
                    lastY.get(new Integer(id)), x, y);
            lastX.put(id, x);
            lastY.put(id, y);
        }
    }

    /**
     * Erases all strokes.
     * 
     * @param shape
     */
    private void eraseStrokes() {
        // int l_x = -1;
        // int l_y = -1;
        this.g2d.setColor(Color.white);
        refreshBoard();
        updateStrokes();
    }

//	/**
//	 * Erases all strokes.
//	 * 
//	 * @param shape
//	 */
//	private void eraseStrokes(ShapeEvent shape) {
//		// int l_x = -1;
//		// int l_y = -1;
//		this.g2d.setColor(Color.white);
//		refreshBoard();
//		updateStrokes();
//	}
    protected void updateStrokes() {
        g2d.setColor(this.shapeColor);
        for (StrokeTrace<Float> trace : this.strokes) {
            List<Float> x = trace.getX();
            List<Float> y = trace.getY();
            List<Float> force = trace.getForce();
            for (int i = 1; i < x.size(); i++) {
                if (this.penPressure) {
                    this.g2d.setStroke(new BasicStroke(pen_thickness
                            * force.get(i)));
                } else {
                    this.g2d.setStroke(new BasicStroke(pen_thickness));
                }
                this.g2d.drawLine(PenDataConversionUtil.convertX2int(x.get(i - 1)), PenDataConversionUtil.convertY2int(y.get(i - 1)), PenDataConversionUtil.convertX2int(x.get(i)), PenDataConversionUtil.convertY2int(y.get(i)));
            }
        }
        for (DisplayString string : this.strings) {
            g2d.drawString(string.string, string.point.x, string.point.y);
        }
    }

    /**
     * Updates the image.
     * 
     * @param s
     */
    public void updateImage(Shape s) {
        Spatial spatial = createSpatialFromShape(s);
        newSpatialAddedFor(s, spatial);
        // Erase all previous shapes from the overlay
        this.shapesNode.detachAllChildren();
        this.shapesNode.attachChild(spatial);
        shapeSpatials.put(s, spatial);
    }

    /**
     * Every time a new shape is detected a spatial is generated for the
     * detected shape.
     * 
     * @param s
     *            - event coming from the Touch&Write server
     * @param spatial
     *            - mapped spatial
     */
    protected void newSpatialAddedFor(Shape s, Spatial spatial) {
        // Inherited classes can overwrite this method,
        // if they want to be informed about a new shape
    }

    /**
     *  Given a shape, creates a corresponding <code>Spatial</code>
     *         object and returns it.
     * 
     * @author Moheb
     * @param s
     *            the <code>Shape</code>
     * @return the <code>Spatial</code> corresponding to the <code>Shape</code>
     * */
    public Spatial createSpatialFromShape(Shape s) {
        Spatial spatial = null;

        switch (s.getShapeType()) {
            case ELLIPTIC_ARC:
                EllipticArc ea = (EllipticArc) s;
                spatial = ShapeUtils.toEllipticArc(ea);
                break;
            case ELLIPSE:
                Ellipse e = (Ellipse) s;
                spatial = ShapeUtils.toEllipse(e);
                break;
            case CIRCLE:
                Circle c = (Circle) s;
                spatial = ShapeUtils.toCircle(c);
                break;
            case RECTANGLE:
            //Quadrangle re = (Quadrangle) s;
            //spatial = ShapeUtils.toQuadrangle(re, s.getShapeType());
            //break;
            case RHOMBUS:
            case PARALLELOGRAM:
                Quadrangle r = (Quadrangle) s;
                spatial = ShapeUtils.toQuad(r, s.getShapeType());
                break;
            case TRIANGLE:
                Triangle t = (Triangle) s;
                spatial = ShapeUtils.toTriMesh(t);
                break;
            case LINE:
                LineSegment l = (LineSegment) s;
                spatial = ShapeUtils.toLine(l);
                break;
            case POLYGON:
                Polygon p = (Polygon) s;
                spatial = ShapeUtils.toPolygon(p);
            default:
                break;
        }

        return spatial;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.dfki.touchandwrite.visual.components.DrawingComponent#draw(de.dfki
     * .touchandwrite.action.PenActionEvent)
     */
    public void draw(Queue<PenActionEvent> events) {
        if (!(current_mode == DrawMode.ERASER_MODE)) {
            for (PenActionEvent evt : events) {
                switch (evt.getType()) {
                    case PEN_UP:
                        lastX.remove(evt.getId());
                        lastY.remove(evt.getId());
                        penActive = false;
                        break;
                    case PEN_DOWN:
                        penActive = true;
                    case PEN_MOVE:
                        updateImage(evt.getAbsoluteX(), evt.getAbsoluteY(), evt.getForce(), evt.getId());
                        this.currentStroke.add(new Vector3f(evt.getAbsoluteX(), evt.getAbsoluteY(), 0.0f));
                        break;

                }
            }
        }
    }

    /**
     * Erasing a round area
     * 
     * @param x
     * @param y
     * @param radius
     */
    protected void eraserArea(int x, int y, int radius) {
        this.g2d.setColor(this.backgroundColor);
        this.g2d.fillOval(x - (int) (radius * 1.0) / 2, this.boardHeight - y
                - (int) (radius * 1.0) / 2, radius, radius);

    }

    /**
     * 
     * @param color
     * @param penID
     */
    public void setCurrentPenColor(Color color, int penID) {
        this.currentPenColor.put(penID, color);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.dfki.touchandwrite.visual.components.DrawingComponent#activatePenPressure
     * ()
     */
    public void activatePenPressure() {
        this.penPressure = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @seede.dfki.touchandwrite.visual.components.DrawingComponent#
     * deactivatePenPressure()
     */
    public void deactivatePenPressure() {
        this.penPressure = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @seede.dfki.touchandwrite.visual.components.DrawingComponent#
     * isPenPressureActivated()
     */
    public boolean isPenPressureActivated() {
        return this.penPressure;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.dfki.touchandwrite.visual.components.DrawingComponent#getPenThickness
     * ()
     */
    public float getPenThickness() {
        return pen_thickness;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.dfki.touchandwrite.visual.components.DrawingComponent#setPenThickness
     * (float)
     */
    public void setPenThickness(float penThickness) {
        pen_thickness = penThickness;
    }

    @Override
    public ComponentType getTypeOfComponent() {
        return ComponentType.COMPONENT_2D;
    }

    public void registerWithInputHandler(PenInputHandler inputHandler) {
        if (registeredInputHandler != null) {
            registeredInputHandler.removeAction(this.drawAction);
        }
        registeredInputHandler = inputHandler;
        if (inputHandler != null) {
            inputHandler.addAction(this.drawAction);
            inputHandler.addAction(this.hwrAction);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.dfki.touchandwrite.visual.components.DrawingComponent#draw(de.dfki
     * .touchandwrite.input.pen.event.ShapeEvent)
     */
    public void draw(ShapeEvent shape) {
        this.eraseStrokes();
//		if (current_mode == DrawMode.SHAPE_DETECTION_MODE
//				|| current_mode == DrawMode.AUTO_DETECTION_MODE) {
//			eraseStrokes(shape);
//			for (Shape s : shape.getDetectedShapes()) {
//				if (isSupportedShape(s)) {
//					updateImage(s);
//					this.shapes.add(s);
//				}
//			}
//		}
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.dfki.touchandwrite.visual.components.TouchableComponent#isSensitiveArea
     * (int, int, int)
     */
    public boolean isSensitiveArea(int id, int x, int y) {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @seede.dfki.touchandwrite.visual.components.TouchableComponent#
     * registerWithInputHandler
     * (de.dfki.touchandwrite.visual.input.TouchInputHandler)
     */
    public void registerWithInputHandler(TouchInputHandler input) {
        input.addAction(touchAction);
    }

    /*
     * (non-Javadoc)
     * 
     * @seede.dfki.touchandwrite.visual.components.TouchableComponent#
     * unRegisterWithInputHandler
     * (de.dfki.touchandwrite.visual.input.TouchInputHandler)
     */
    public void unRegisterWithInputHandler(TouchInputHandler input) {
        input.removeAction(touchAction);
    }

    /*
     * (non-Javadoc)
     * 
     * @seede.dfki.touchandwrite.visual.components.PenSensitiveComponent#
     * unRegisterWithInputHandler
     * (de.dfki.touchandwrite.visual.input.PenInputHandler)
     */
    public void unRegisterWithInputHandler(PenInputHandler input) {
        input.removeAction(this.drawAction);
        input.removeAction(this.hwrAction);
    }

    /*
     * (non-Javadoc)
     * 
     * @seede.dfki.touchandwrite.visual.components.HWRSensitiveComponent#
     * handwritingResult
     * (de.dfki.touchandwrite.input.pen.hwr.HandwritingRecognitionEvent)
     */
    public void handwritingResult(HandwritingRecognitionEvent event) {
        this.eraseStrokes();
//		if (current_mode == DrawMode.HWR_MODE
//				|| current_mode == DrawMode.AUTO_DETECTION_MODE) {
//			this.hwrEvents.add(event);
//			drawHWRResult(event);
//		}
    }

    /**
     * Draws the HWR result.
     * 
     * @param event
     */
    protected void drawHWRResult(HandwritingRecognitionEvent event) {
//		String result = checkHWRResult(event.getHWRResultSet());
//		g2d.drawRect((int)event.getBoundingBox().getMinX(), (int)event.getBoundingBox()
//				.getMinY(), event.getBoundingBox().getWidth(), event
//				.getBoundingBox().getHeigth());
//		this.strings.add(new DisplayString(result, new Point((int) Math
//				.round(event.getBoundingBox().getAx()) - 10, (int) Math
//				.round(event.getBoundingBox().getAy()) + 20)));
//		g2d.drawString(result,
//				(int) Math.round(event.getBoundingBox().getAx()) - 10,
//				(int) Math.round(event.getBoundingBox().getAy()) + 20);
//		this.strokes.addAll(event.getTraces());
    }

    /**
     * Checks the hwr result and chooses the best result.
     * 
     * @param hwrResultSet
     * @return
     */
    protected String checkHWRResult(HWRResultSet hwrResultSet) {
        return hwrResultSet.topResult();
    }

    @Override
    public void setCurrentPenColor(Color color) {
        // TODO Auto-generated method stub
    }
}
