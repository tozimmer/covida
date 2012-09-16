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
package de.dfki.covida.visualjme2.components.video;

import com.jme.animation.SpatialTransformer;
import com.jme.image.Texture;
import com.jme.image.Texture.WrapMode;
import com.jme.image.Texture2D;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.awt.swingui.ImageGraphics;
import de.dfki.covida.covidacore.components.IVideoComponent;
import de.dfki.covida.covidacore.data.*;
import de.dfki.covida.videovlcj.AbstractVideoHandler;
import de.dfki.covida.videovlcj.rendered.RenderedVideoHandler;
import de.dfki.covida.visualjme2.animations.CovidaSpatialController;
import de.dfki.covida.visualjme2.animations.DragAnimation;
import de.dfki.covida.visualjme2.animations.ResetAnimation;
import de.dfki.covida.visualjme2.components.CovidaJMEComponent;
import de.dfki.covida.visualjme2.components.TextOverlay;
import de.dfki.covida.visualjme2.components.annotation.DisplayFieldComponent;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import de.dfki.touchandwrite.input.pen.event.ShapeEvent;
import de.dfki.touchandwrite.shape.ShapeType;
import java.awt.*;
import java.io.File;
import org.apache.log4j.Logger;

/**
 * Component to display videos.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public final class VideoComponent extends CovidaJMEComponent implements IVideoComponent {

    /**
     * Logger
     */
    private Logger log = Logger.getLogger(VideoComponent.class);
    /**
     * Upscale factor to change quality of the video as {@link Float} Note that
     * 1.f represents full quality and that the {@code UPSCALE_FACTOR} must be
     * greater 0.f .
     */
    private static final float UPSCALE_FACTOR = 1.f;
    /**
     * Threshold in ms for select touch time
     */
    private static final long THRESHOLD = 750;
    /**
     * Video {@link Quad}
     */
    private Quad videoQuad;
    /**
     * Drawing will be done with Java2D.
     */
    protected ImageGraphics g2d;
    /**
     * Info field with current annotation
     */
    private DisplayFieldComponent infoField;
    /**
     * List field with all annotation
     */
    private DisplayFieldComponent listField;
    /**
     * Video controls
     */
    private VideoComponentControls controls;
    /**
     * Video list field button
     */
    private VideoComponentListButton listButton;
    /**
     * Video slider
     */
    private VideoSlider slider;
    /**
     * Default overlay.
     */
    protected Quad overlay;
    /**
     * Overlay for the video title
     */
    private TextOverlay textOverlay;
    /**
     * {@link VideoHandler} which plays and renders the video.
     */
    private AbstractVideoHandler video;
    /**
     * {@link SpatialTransformer} for the drag animation.
     */
    private SpatialTransformer stDrag;
    /**
     * {@link SpatialTransformer} for the reset animation.
     */
    private CovidaSpatialController stReset;
    /**
     * {@link Texture2D} with the rendered video and the shapes.
     */
    private Texture2D texture;
    private TextureState overlayDefaultState;
    private TextureState overlaySelectState;
    private Quad overlayDrag;
    private TextureState overlayDragState;
    private TextureState overlayDragBlankState;
    private long dragTimer;
    

    /**
     * Creates an instance of {@link VideoComponent}
     *
     * @param source
     * @param height
     * @param format
     * @param node
     */
    public VideoComponent(String source, String title, int height, VideoFormat format) {
        super("Video Component ");
        super.setName(title);
        log.debug("Create video id:" + getId());
        video = new RenderedVideoHandler(source, title, (int) (height * UPSCALE_FACTOR), format.determineWidth((int) (height * UPSCALE_FACTOR)));
        setDefaultPosition();
        createControls();
        createVideo();
        createFields();
        createOverlays();
        startTests();
    }

    private void startTests() {
//        DrawTest drawTest = new DrawTest(video);
//        Thread drawTestThread = new Thread(drawTest);
//        drawTestThread.start();
//        HWRTest hwrTest = new HWRTest(this);
//        Thread hwrTestThread=  new Thread(hwrTest);
//        hwrTestThread.start();
//        AttachTest attachTest = new AttachTest(this);
//        Thread attachThread =  new Thread(attachTest);
//        attachThread.start();
//        RotateTest rotateTest = new RotateTest(this);
//        Thread rotateThread = new Thread(rotateTest);
//        rotateThread.start();
    }

    public void setTitleOverlayEnabled(boolean enabled) {
        video.setTitleOverlayEnabled(enabled);
    }

    public void enableTimeCodeOverlay(long timeout) {
        video.enableTimeCodeOverlay(timeout);
    }

    /**
     * Creates the video controls.
     */
    private void createControls() {
        controls = new VideoComponentControls(this);
        nodeHandler.addAttachChildRequest(this, controls);
        slider = new VideoSlider(this);
        slider.getLocalTranslation().set(
                new Vector3f(-15, -30 - getHeight() / 2, 0));
        slider.setDefaultPosition();
        nodeHandler.addAttachChildRequest(this, slider);
        listButton = new VideoComponentListButton(this);
        listButton.getLocalTranslation().set(
                new Vector3f(-getWidth() / 1.90f, 0, 0));
        nodeHandler.addAttachChildRequest(this, listButton);
    }

    /**
     * Creates video quad and handler for the video.
     */
    private void createVideo() {
        this.videoQuad = new Quad("Video " + getId() + " Texture Quad", video.getWidth(), video.getHeight());
        this.videoQuad.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        this.videoQuad.setCullHint(Spatial.CullHint.Inherit);
        // ---- Texture state initialization ----
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
            g2d = ImageGraphics.createInstance(getWidth(), getHeight(), 0);
        }
        enableAntiAlias(g2d);
        texture.setImage(g2d.getImage());
        ts.setTexture(texture);
        videoQuad.setRenderState(ts);
        videoQuad.updateRenderState();
        Quaternion q = new Quaternion();
        // Rotation need because of ImageGraphics
        q.fromAngles(0f, (float) Math.toRadians(180),
                (float) Math.toRadians(180));
        videoQuad.rotatePoints(q);
        nodeHandler.addAttachChildRequest(this, videoQuad);

        video.setLoggin(false);
        video.setSlider(slider);
        video.setControls(controls);

        if (UPSCALE_FACTOR > 0.0f) {
            setLocalScale(1.f / UPSCALE_FACTOR);
        } else {
            log.warn("Invalid UPSCALE_FACTOR, must be greater 0.0f.");
        }
    }

    /**
     * Creates info and list field.
     */
    private void createFields() {
        listField = new DisplayFieldComponent("media/textures/bg_list.png",
                this, (int) (getHeight() * (0.55f)), (int) (getHeight() * 1.2f));
        listField.setLocalTranslation(-getWidth() * (0.75f), 0, 0);
        listField.initComponent();
        listField.setDefaultPosition();
        infoField = new DisplayFieldComponent("media/textures/bg_info.png",
                this, listField, (int) (getHeight() * (0.55f)),
                (int) (getHeight() * 1.2f));
        infoField.setLocalTranslation(getWidth() * (0.75f), 0, 0);
        infoField.initComponent();
        infoField.setDefaultPosition();
    }

    @Override
    public void dragAction(int id, int x, int y, int dx, int dy) {
        startDragAnimation();
        Vector3f translation = this.getLocalTranslation();
        move(translation.x + dx, translation.y - dy);
    }

    @Override
    public void dragEndAction(int id, int x, int y, int dx, int dy) {
        stopDragAniation();
    }

    @Override
    public void touchBirthAction(int id, int x, int y) {
        dragTimer = System.currentTimeMillis();
    }

    @Override
    public void touchDeadAction(int id, int x, int y) {
        if (System.currentTimeMillis() - dragTimer < THRESHOLD) {
            toggleSelected();
        }
    }

    /**
     * Creates overlays
     */
    private void createOverlays() {
        textOverlay = new TextOverlay(this);
        textOverlay.setLocalTranslation(0, getHeight() / (1.50f) - getFontSize()
                / 2.f, 0);
        nodeHandler.addAttachChildRequest(this, textOverlay);
        textOverlay.setSize(getFontSize());
        initalizeOverlayQuads(JMEUtils.initalizeBlendState());
        stDrag = DragAnimation.getController(overlayDrag);
        attachChild(overlayDrag);
        nodeHandler.addAddControllerRequest(overlayDrag, stDrag);
        nodeHandler.addAttachChildRequest(this, overlay);
    }

    /**
     * Adds points to shape which will be drawn on the video
     *
     * @param points {@link ShapePoints} which contains the point data.
     */
    public void draw(ShapePoints points) {
        video.setShape(points);
    }

    @Override
    public void draw(int x, int y) {
        int localX = x + getPosX();
        int localY = (int) display.getY() - y;
        localY += getPosY();
        localX += getDimension().getWidth() / 2;
        localY += getDimension().getHeight() / 2;
        video.draw(new Point(localX, localY));
    }

    @Override
    public void hwrAction(String hwr) {
        video.clearDrawing();
        video.setHWR(hwr);
    }

    /**
     * Calculates the {@link Font} size.
     *
     * @return Font size as {@link Integer}
     */
    private int getFontSize() {
        return (int) ((float) getHeight() / 9.f);
    }

    /**
     * Method for calculation of video width
     *
     * @return video width
     */
    @Override
    public int getWidth() {
        return video.getWidth();
    }

    /**
     *
     * @return height of the VideoComponent
     */
    @Override
    public final int getHeight() {
        return video.getHeight();
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

    /*
     * (non-Javadoc)
     *
     * @see com.jme.scene.TriMesh#draw(com.jme.renderer.Renderer)
     */
    @Override
    public void draw(Renderer r) {
        try {
            g2d.drawImage(video.getVideoImage(), null, 0, 0);
            g2d.update();
            if (g2d != null && texture.getTextureId() > 0) {
                g2d.update(texture, false);
            }
        } catch (Exception e) {
            log.error(e);
        }
        super.draw(r);
    }

    /**
     * Method to set video repeat flag
     *
     * @param repeat - repeat flag (true -> repeat video)
     */
    public void setRepeat(Boolean repeat) {
        video.setRepeat(repeat);
    }

    /**
     * Start video (Initialisation)
     */
    public void start() {
        video.start();
    }

    /**
     * Stops the video
     */
    public void stop() {
        video.stop();
    }

    /**
     * Changes the actual playing media
     *
     * @param source
     */
    public void setMedia(String source) {
        video.setMedia(source);
    }

    /**
     * Pauses the video
     */
    public void pause() {
        video.pause();
    }

    /**
     * Resumes a paused video
     *
     */
    public void resume() {
        video.resume();
    }

    /**
     * Sets the video time position in frames
     *
     * @param time - time in frames
     */
    public void setTimePosition(long time) {
        video.setTimePostion(time);
    }

    /**
     * Sets the video time position in percentage
     *
     * @param percentage
     */
    public void setTimePosition(float percentage) {
        video.setTimePostion(percentage);
    }

    /**
     *
     * @param start
     * @param end
     */
    public void setTimeRange(long start, long end) {
        video.setTimeRange(start, end);
    }

    /**
     * Changes video source of VideoComponent
     *
     * @param file
     */
    public void videoChange(File file) {
        video.setMedia(file.getAbsolutePath());
    }

    /**
     * Releases native sources
     */
    @Override
    public void cleanUp() {
        log.debug("cleanup video (id: " + getId() + ")");
        this.controls.cleanUp();
        this.infoField.cleanUp();
        this.listField.cleanUp();
        this.slider.cleanUp();
        video.cleanup();
    }

    /**
     *
     *
     * @return
     */
    public boolean isRepeating() {
        return video.isRepeat();
    }

    private void setNewAnnotationData() {
        // attach info field to video and make it visible
        attachAnnotation();
        pause();
        listField.resetInfo();
        long time = video.getTime();
        Annotation annotation = new Annotation();
        annotation.description = "";
        annotation.shapePoints = video.getShape();
        annotation.shapeType = ShapeType.POLYGON;
        annotation.time_end = time;
        annotation.time_start = time;
        // set annotation data
        infoField.setAnnotationData(annotation);
        toFront();
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
     * Hides AnnotationList from this instance of VideoComponent
     *
     */
    public void detachList() {
        if (hasList()) {
            listField.close();
            listButton.detachAnimation();
        }
    }

    /**
     * Shows AnnotationList from this instance of VideoComponent
     *
     */
    public void attachList() {
        if (!hasList()) {
            nodeHandler.addAttachChildRequest(this, listField);
            listField.open();
            listButton.attachAnimation();
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
            nodeHandler.addAttachChildRequest(this, infoField);
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
        toggleSelected();
    }

    private boolean hasAnnotation() {
        return infoField.isOpen();
    }

    public Vector3f resetLocalTranslation(Vector3f local) {
        return local.add(new Vector3f(getLocalTranslation().x,
                getLocalTranslation().x, 0));
    }

    /**
     * Sets the local scale of the node from this instance of VideoComponent
     *
     * @param scale
     */
    public void rescale(float scale) {
        this.setLocalScale(scale);
    }

    public Dimension getVideoDimension() {
        return video.getDimension();
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
        setLocalRotation(rotation);
    }

    private void initalizeOverlayQuads(BlendState alpha) {
        Texture overlayDefaultTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(
                "media/textures/overlay_default.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlayDefaultTexture.setWrap(WrapMode.Clamp);
        overlayDefaultState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlayDefaultState.setTexture(overlayDefaultTexture);
        Texture overlaySelectTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(
                "media/textures/overlay_select.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlaySelectTexture.setWrap(WrapMode.Clamp);
        overlaySelectState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlaySelectState.setTexture(overlaySelectTexture);
        this.overlay = new Quad("Overlay-Default-Image-Quad",
                (1.15f) * getWidth(), (1.275f) * getHeight());
        overlay.setRenderState(overlayDefaultState);
        overlay.setRenderState(alpha);
        overlay.updateRenderState();
        Texture overlayDragTexture = TextureManager.loadTexture(getClass().getClassLoader()
                .getResource("media/textures/overlay_drag.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlayDragTexture.setWrap(Texture.WrapMode.Clamp);
        overlayDragState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlayDragState.setTexture(overlayDragTexture);
        Texture overlayBlankTexture = TextureManager.loadTexture(getClass()
                .getClassLoader().getResource("media/textures/bg_info_blank.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlayDragTexture.setWrap(Texture.WrapMode.Clamp);
        overlayDragBlankState = DisplaySystem.getDisplaySystem().getRenderer()
                .createTextureState();
        overlayDragBlankState.setTexture(overlayBlankTexture);
        overlayDrag = new Quad("Overlay-Drag-Image-Quad", (1.15f) * getWidth(),
                (1.35f) * getHeight());
        overlayDrag.setRenderState(overlayDragState);
        overlayDrag.setRenderState(alpha);
        overlayDrag.updateRenderState();
        attachChild(overlayDrag);
    }

    public void toggleSelected() {
        if (!overlay.getRenderState(RenderState.StateType.Texture).equals(overlaySelectState)) {
            overlay.setRenderState(overlaySelectState);
            overlay.updateRenderState();
            attachControls();
            textOverlay.setText(getName());
        } else {
            overlay.setRenderState(overlayDefaultState);
            overlay.updateRenderState();
            detachMenu();
            textOverlay.setText("");
        }
    }

    private void detachMenu() {
        nodeHandler.addDetachChildRequest(this, controls);
    }

    private void attachControls() {
        nodeHandler.addAttachChildRequest(this, controls);
    }

    /**
     * Resets position of the VideoComponent with an animation
     *
     * @param angle - angle in radians
     */
    public void reset(float angle) {
        listField.reset();
        infoField.reset();
        if (getControllers().contains(stReset)) {
            nodeHandler.addRemoveControllerRequest(this, stReset);
        }
        stReset = ResetAnimation.getController(this, defaultScale, angle, defaultTranslation);
        nodeHandler.addAddControllerRequest(this, stReset);
    }

    public void startDragAnimation() {
        overlayDrag.setRenderState(overlayDragState);
        overlayDrag.updateRenderState();
        nodeHandler.addAddControllerRequest(overlayDrag, stDrag);
    }

    public void stopDragAniation() {
        overlayDrag.setRenderState(overlayDragBlankState);
        overlayDrag.updateRenderState();
        nodeHandler.addRemoveControllerRequest(overlayDrag, stDrag);
    }

    public boolean isReady() {
        return video.isReady();
    }

    public void setVolume(int i) {
        video.setVolume(i);
    }

    public long getMaxTime() {
        return video.getMaxTime();
    }

    public long getTime() {
        return video.getTime();
    }

    String getVideoProgress() {
        return video.getVideoProgress();
    }

    @Override
    public void onShapeEvent(ShapeEvent event) {
        video.setShape(video.getDrawing());
        video.clearDrawing();
        setNewAnnotationData();
    }

    @Override
    public String getSource() {
        return video.getSource();
    }
}