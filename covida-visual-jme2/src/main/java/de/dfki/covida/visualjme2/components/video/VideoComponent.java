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
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.TextureManager;
import de.dfki.covida.covidacore.components.IControlableComponent;
import de.dfki.covida.covidacore.components.IVideoComponent;
import de.dfki.covida.covidacore.data.Annotation;
import de.dfki.covida.covidacore.data.AnnotationStorage;
import de.dfki.covida.covidacore.data.ShapePoints;
import de.dfki.covida.covidacore.tw.TouchAndWriteComponentHandler;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.videovlcj.AbstractVideoHandler;
import de.dfki.covida.videovlcj.rendered.RenderedVideoHandler;
import de.dfki.covida.visualjme2.animations.CovidaSpatialController;
import de.dfki.covida.visualjme2.animations.DragAnimation;
import de.dfki.covida.visualjme2.animations.ResetAnimation;
import de.dfki.covida.visualjme2.components.JMEComponent;
import de.dfki.covida.visualjme2.components.TextComponent;
import de.dfki.covida.visualjme2.components.video.fields.InfoFieldComponent;
import de.dfki.covida.visualjme2.components.video.fields.ListFieldComponent;
import de.dfki.covida.visualjme2.utils.*;
import de.dfki.touchandwrite.analyser.touch.gestures.events.RotationGestureEventImpl;
import de.dfki.touchandwrite.analyser.touch.gestures.events.ZoomEventImpl;
import de.dfki.touchandwrite.input.pen.event.ShapeEvent;
import de.dfki.touchandwrite.shape.ShapeType;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import uk.co.caprica.vlcj.player.MediaPlayer;

/**
 * Component to display videos.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public final class VideoComponent extends JMEComponent implements
        IVideoComponent, IControlableComponent {

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
     * Info field with current annotation
     */
    private InfoFieldComponent infoField;
    /**
     * List field with all annotation
     */
    private ListFieldComponent listField;
    /**
     * Video controls
     */
    public VideoComponentControls controls;
    /**
     * Video slider
     */
    private VideoSlider slider;
    /**
     * Default overlay.
     */
    private Quad overlay;
    /**
     * Overlay for the video title
     */
    private TextComponent textOverlay;
    /**
     * {@link AbstractVideoHandler} which plays and renders the video.
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
     * Default overlay state (default texture state)
     */
    private TextureState overlayDefaultState;
    /**
     * Overlay state for selected video components (selected texture state)
     */
    private TextureState overlaySelectState;
    /**
     * Quad for dragging animation texture
     */
    private Quad overlayDrag;
    /**
     * Overlay state for dragging video components (dragging texture state)
     */
    private TextureState overlayDragState;
    /**
     * Overlay state for not dragging video components (not dragging texture
     * state)
     */
    private TextureState overlayDragBlankState;
    /**
     * Timer to determine how long video component was dragged
     */
    private long dragTimer;
    /**
     * Video title
     */
    private final String title;

    /**
     * Creates an instance of {@link VideoComponent}
     *
     * @param source video source location as {@link String}
     * @param title video title as {@link String}
     */
    public VideoComponent(String source, String title) {
        super("Video Component ");
        log.debug("Create video id:" + getId());
        this.title = title;
        video = new RenderedVideoHandler(source, title, this);
        setDefaultPosition();
        AnnotationStorage.getInstance().getAnnotationData(this).title = title;
        AnnotationStorage.getInstance().getAnnotationData(this).videoSource = source;
    }

    @Override
    public void create() {
        createControls();
        createFields();
        createOverlays();
        startTests();
        createVideo();
        setDrawable(true);
        setTouchable(true);
    }

    /**
     * Calculates the font size.
     *
     * @return Font size as {@link Integer}
     */
    private int getFontSize() {
        return (int) ((float) getHeight() / 9.f);
    }

    /**
     * Mehod which holds test method calls
     */
    public void startTests() {
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
//        if (video.getTitle().equals("CoVidA Demo")) {
//            video.draw(new Point(25, 25));
//            video.draw(new Point(25, 50));
//            video.draw(new Point(35, 65));
//            video.draw(new Point(70, 100));
//            video.draw(new Point(20, 85));
//            video.draw(new Point(25, 25));
//            move(getLocalTranslation().x, getLocalTranslation().y + 400);
//            rotate(180.f, new Vector3f(0, 0, 1));
//            hwrAction("Test");
//            hwrAction("Check");
//        } else {
//            attachList();
//            ArrayList<Long> entries = new ArrayList<>();
//            entries.add(Long.valueOf(15000));
//            entries.add(Long.valueOf(550000));
//            entries.add(Long.valueOf(6655000));
//            entries.add(Long.valueOf(7898000));
//            listField.updateEntries(entries);
//            listField.drawEntries();
//            node.setLocalScale(0.75f);
//        }
    }

    /**
     * Sets current annotations on the video on the info field
     */
    private void setNewAnnotationData() {
        // attach info field to video and make it visible
        attachAnnotation();
        pause();
        long time = video.getTime();
        Annotation annotation = new Annotation();
        annotation.description = "";
        annotation.shapePoints = video.getShapes();
        annotation.shapeType = ShapeType.POLYGON;
        annotation.time_end = time;
        annotation.time_start = time;
        // set annotation data
        infoField.setAnnotationData(annotation);
        toFront();
    }

    /**
     * Returns info field status
     *
     * @return true if info field is attached to the {@link VideoComponent}
     */
    private boolean hasInfoField() {
        return infoField.isOpen();
    }

    /**
     * Initialized the overlay quads and texture states.
     *
     * @param alpha {@link BlendState}
     */
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
        overlay = new Quad("Overlay-Default-Image-Quad",
                (1.15f) * getWidth(), (1.25f) * getHeight());
        overlay.setZOrder(CovidaZOrder.ui_overlay);
        overlay.setRenderState(overlayDefaultState);
        overlay.setRenderState(alpha);
        overlay.updateRenderState();
        Texture overlayDragTexture = TextureManager.loadTexture(getClass().getClassLoader().getResource("media/textures/overlay_drag.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlayDragTexture.setWrap(Texture.WrapMode.Clamp);
        overlayDragState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlayDragState.setTexture(overlayDragTexture);
        Texture overlayBlankTexture = TextureManager.loadTexture(getClass().getClassLoader().getResource("media/textures/bg_info_blank.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlayDragTexture.setWrap(Texture.WrapMode.Clamp);
        overlayDragBlankState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlayDragBlankState.setTexture(overlayBlankTexture);
        overlayDrag = new Quad("Overlay-Drag-Image-Quad", (1.15f) * getWidth(),
                (1.35f) * getHeight());
        overlayDrag.setZOrder(CovidaZOrder.ui_overlay-1);
        overlayDrag.setRenderState(overlayDragState);
        overlayDrag.setRenderState(alpha);
        overlayDrag.updateRenderState();
    }

    /**
     * Calculates scaling factor for snapshot texture.
     *
     * @param min min point as {@link Vector2f}
     * @param max max point as {@link Vector2f}
     * @return scale of snapshot
     */
    private float getSnapshotScale(Vector2f min, Vector2f max) {
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
     * @param min min point as {@link Vector2f}
     * @param max max point as {@link Vector2f}
     * @return snapshot center
     */
    private Vector3f getSnapshotCentrum(Vector2f min, Vector2f max) {
        float diffX = max.x - min.x;
        float diffY = max.y - min.y;
        float yPos = min.y + diffY;
        float xPos = min.x + diffX;
        yPos = 1 - ((yPos / (float) getHeight()) / 2.f);
        xPos = 1 - ((xPos / (float) getWidth()) / 2.f);
        return new Vector3f(xPos, yPos, 0);
    }

    /**
     * Detaches AnnotationList from this instance of VideoComponent.
     */
    public void detachList() {
        if (hasList()) {
            listField.close();
        }
    }

    /**
     * Sets title overlay status in the video {@link MediaPlayer}
     *
     * @param enabled if true title overlay is enabled
     */
    public void setTitleOverlayEnabled(boolean enabled) {
        video.setTitleOverlayEnabled(enabled);
    }

    /**
     * Enable time code overlay in the video {@link MediaPlayer}
     *
     * @param timeout timeout time in milliseconds
     */
    public void enableTimeCodeOverlay(long timeout) {
        video.enableTimeCodeOverlay(timeout);
    }

    /**
     * Creates the video controls.
     */
    public void createControls() {
        controls = new VideoComponentControls(this);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node,
                controls.node));
        slider = new VideoSlider(this);
        slider.getLocalTranslation().set(
                new Vector3f(-15, -22 - getHeight() / 2, 0));
        slider.setDefaultPosition();
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node,
                slider.node));
    }

    /**
     * Creates video quad and handler for the video.
     */
    public void createVideo() {
        VideoQuad videoQuad = new VideoQuad(video);
        videoQuad.setZOrder(CovidaZOrder.ui_background);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, videoQuad));
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
    public void createFields() {
        listField = new ListFieldComponent("media/textures/bg_list.png",
                this, (int) (getHeight() * (0.55f)), (int) (getHeight() * 1.2f));
        listField.setLocalTranslation(-getWidth() * (0.75f), 0, 0);
        listField.initComponent();
        listField.setDefaultPosition();
        infoField = new InfoFieldComponent("media/textures/bg_info.png",
                this, listField, (int) (getHeight() * (0.55f)),
                (int) (getHeight() * 1.2f));
        infoField.setLocalTranslation(getWidth() * (0.75f), 0, 0);
        infoField.initComponent();
        infoField.setDefaultPosition();
    }

    /**
     * Creates overlays
     */
    public void createOverlays() {
        textOverlay = new TextComponent(this, ActionName.NONE);
        textOverlay.setLocalTranslation(0, getHeight() / (1.50f) - getFontSize()
                / 2.f, 0);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, textOverlay.node));
        textOverlay.setSize(getFontSize());
        initalizeOverlayQuads(JMEUtils.initalizeBlendState());
        stDrag = DragAnimation.getController(overlayDrag);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, overlay));
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
        video.setTimePosition(time);
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
     * Returns repeating status.
     *
     * @return true if video repeating is enabled.
     */
    public boolean isRepeating() {
        return video.isRepeat();
    }

    /**
     * Shows AnnotationList from this instance of VideoComponent.
     *
     */
    public void attachList() {
        if (!hasList()) {
            GameTaskQueueManager.getManager().update(new AttachChildCallable(node, listField.node));
            listField.open();
        }
    }

    /**
     * Returns true if AnnotationList is attached.
     *
     * @return true if AnnotationList is attached.
     */
    public boolean hasList() {
        return listField.isOpen();
    }

    /**
     * Attachs AnnotationField
     */
    public void attachAnnotation() {
        if (!hasInfoField()) {
            GameTaskQueueManager.getManager().update(new AttachChildCallable(node, infoField.node));
            infoField.open();
        }
    }

    /**
     * Toggles select status of the {@link VideoComponent}
     */
    public void changeOverlays() {
        toggleSelected();
    }

    public Vector3f resetLocalTranslation(Vector3f local) {
        return local.add(new Vector3f(getLocalTranslation().x,
                getLocalTranslation().x, 0));
    }

    /**
     * Sets the local scale of the node from this instance of VideoComponent
     *
     * @param scale Video scale as {@link Float} Note that 1.f is the standart
     * scale
     */
    public void rescale(float scale) {
        this.setLocalScale(scale);
    }

    /**
     * Returns video dimensions.
     *
     * @return {@link Dimension} of the video.
     */
    public Dimension getVideoDimension() {
        return video.getDimension();
    }

    /**
     * Method which selects and deselects the component
     */
    public void toggleSelected() {
        if (!overlay.getRenderState(RenderState.StateType.Texture).equals(overlaySelectState)) {
            overlay.setRenderState(overlaySelectState);
            overlay.updateRenderState();
            attachControls();
            textOverlay.setText(video.getTitle());
        } else {
            overlay.setRenderState(overlayDefaultState);
            overlay.updateRenderState();
            detachMenu();
            textOverlay.setText("");
        }
    }

    /**
     * Detachs the controls from the video.
     */
    private void detachMenu() {
        controls.detach();
    }

    /**
     * Attachs the controls from the video.
     */
    private void attachControls() {
        controls.attach();
    }

    /**
     * Resets position of the VideoComponent with an animation
     *
     * @param angle - angle in radians
     */
    public void reset(float angle) {
        listField.reset();
        infoField.reset();
        if (node.getControllers().contains(stReset)) {
            GameTaskQueueManager.getManager().update(new RemoveControllerCallable(node, stReset));
        }
        stReset = ResetAnimation.getController(node, defaultScale, angle, defaultTranslation);
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node, stReset));
    }

    /**
     * Starts the drag animation.
     */
    public void startDragAnimation() {
        overlayDrag.setRenderState(overlayDragState);
        overlayDrag.updateRenderState();
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, overlayDrag));
        GameTaskQueueManager.getManager().update(new AddControllerCallable(overlayDrag, stDrag));
    }

    /**
     * Ends the drag animation
     */
    public void stopDragAniation() {
        overlayDrag.setRenderState(overlayDragBlankState);
        overlayDrag.updateRenderState();
        GameTaskQueueManager.getManager().update(new DetachChildCallable(node, overlayDrag));
        GameTaskQueueManager.getManager().update(new RemoveControllerCallable(overlayDrag, stDrag));
    }

    /**
     * Returns true if video component is ready.
     *
     * @return true if video is ready to play.
     */
    public boolean isReady() {
        return video.isReady();
    }

    /**
     * Sets the volome.
     *
     * @param i Volume as {@link Integer} Note that 0 is muted and 100 is full
     * volume.
     */
    public void setVolume(int i) {
        video.setVolume(i);
    }

    /**
     * Returns the end postion from the video as {@link Long}
     *
     * @return end position in ms as {@link Long}
     */
    public long getMaxTime() {
        return video.getMaxTime();
    }

    /**
     * Returns the current time position.
     *
     * @return current time position in ms as {@link Long}
     */
    public long getTime() {
        return video.getTime();
    }

    @Override
    public void dragAction(int id, int x, int y, int dx, int dy) {
        startDragAnimation();
        Vector3f translation = this.getLocalTranslation();
        Vector3f d = new Vector3f(dx, -dy, 0);
//        d = d.divideLocal(node.getWorldScale());
        translation = translation.add(d);
        move(translation.x, translation.y);
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

    @Override
    public void draw(int x, int y) {
        video.pause();
        Vector3f local = getLocal(x, y);
        int localX = (int) local.x;
        int localY = (int) local.y;
        localX += (getDimension().getWidth() / 2) / node.getLocalScale().x;
        localY = (int) ((getDimension().getHeight() / node.getLocalScale().y)
                - ((localY + (getDimension().getHeight() / 2)
                / node.getLocalScale().y)));
        video.draw(new Point(localX, localY));
    }

    @Override
    public void drawEnd(int x, int y) {
        draw(x, y);
        video.endDrawStroke();
    }

    @Override
    public void hwrAction(String hwr) {
        if (video.getShapes().isEmpty()) {
            List<Point> points = new ArrayList<>();
            points.add(new Point(5, 5));
            points.add(new Point(5, getHeight() - 5));
            points.add(new Point(getWidth() - 5, getHeight() - 5));
            points.add(new Point(getWidth() - 5, 5));
            points.add(new Point(5, 5));
            video.addShape(points);
            setNewAnnotationData();
        }
        video.clearDrawing();
        video.resumeAndPause();
        infoField.drawHwrResult(hwr);
    }

    @Override
    public void load(Annotation annotation) {
        attachAnnotation();
        clearAnnotation();
        infoField.setAnnotationData(annotation);
        video.setTimePosition(annotation.time_start);
        for (ShapePoints shape : annotation.shapePoints) {
            video.addShape(shape.points);
        }
        video.resumeAndPause();
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

    @Override
    public void cleanUp() {
        log.debug("cleanup video (id: " + getId() + ")");
        TouchAndWriteComponentHandler.getInstance().removeComponent(this);
        controls.cleanUp();
        infoField.cleanUp();
        listField.cleanUp();
        slider.cleanUp();
        video.cleanUp();
    }

    @Override
    public void zoomAction(ZoomEventImpl event) {
        float scale = display.x / ((float) getWidth() * node.getLocalScale().x);
        float ratio = ((event.getZoomRatio() - 1) / scale) + 1;
        rescale(node.getLocalScale().x * ratio);
        float minScale = ((getDisplaySize().width / 10) / getWidth());
        float maxScale = ((getDisplaySize().width / 1.2f) / getWidth());
        if (node.getLocalScale().x < minScale) {
            rescale(minScale);
        }
        if (node.getLocalScale().x > maxScale) {
            rescale(maxScale);
        }
    }

    @Override
    public void rotateAction(RotationGestureEventImpl event) {
        node.getLocalTranslation().subtractLocal(
                event.getPivotPoint().x, event.getPivotPoint().y,
                0);
        Quaternion q = new Quaternion();
        q.fromAngleAxis(event.getRotation(), Vector3f.UNIT_Z);
        node.getLocalRotation().multLocal(q.inverse());
        node.getLocalTranslation().addLocal(
                event.getPivotPoint().x, event.getPivotPoint().y, 0);
    }

    @Override
    public void onShapeEvent(ShapeEvent event) {
        video.setShapes(video.getDrawings());
        video.clearDrawing();
        setNewAnnotationData();
    }

    @Override
    public String getSource() {
        return video.getSource();
    }

    @Override
    public boolean toggle(ActionName action) {
        if (action.equals(ActionName.BACKWARD)) {
            if ((video.getTime() - video.getMaxTime() / 20) > 0) {
                setTimePosition(video.getTime()
                        - video.getMaxTime() / 20);
            } else {
                setTimePosition(0);
            }
            return false;
        } else if (action.equals(ActionName.CHANGEMEDIA)) {
            video.setMedia("http://www.youtube.com/watch?v=uBiN119_wvg");
            return false;
        } else if (action.equals(ActionName.CLOSE)) {
//            close();
            return false;
        } else if (action.equals(ActionName.FORWARD)) {
            if ((video.getTime() + video.getMaxTime() / 100) < video.getMaxTime()) {
                setTimePosition(video.getTime()
                        + video.getMaxTime() / 100);
            } else {
                setTimePosition(video.getMaxTime());
            }
            return false;
        } else if (action.equals(ActionName.LIST)) {
            if (!hasList()) {
                attachList();
                return true;
            } else {
                detachList();
                return false;
            }
        } else if (action.equals(ActionName.PLAYPAUSE)) {
            if (video.isPlaying()) {
                pause();
                return true;
            } else {
                resume();
                return false;
            }
        } else if (action.equals(ActionName.SOUND)) {
            if (video.getVolume() > 0) {
                video.setVolume(0);
                return false;
            } else {
                video.setVolume(100);
                return true;
            }
        } else if (action.equals(ActionName.SAVE)) {
            if (infoField.isOpen()) {
                this.infoField.save();
            }
        } else if (action.equals(ActionName.DELETE)) {
            if (infoField.isOpen()) {
                this.infoField.delete();
            }
        } else if (action.equals(ActionName.STOP)) {
            if (video.isPlaying()) {
                video.stop();
                return true;
            }
        }
        return false;
    }

    @Override
    public String getTitle() {
        return title;
    }

    boolean isPlaying() {
        return video.isPlaying();
    }

    public void clearAnnotation() {
        video.clearDrawing();
        video.clearShape();
        infoField.resetInfo();
        video.resumeAndPause();
    }

    public void deleteDescription(TextComponent aThis) {
        infoField.deleteDescription(aThis);
    }
}