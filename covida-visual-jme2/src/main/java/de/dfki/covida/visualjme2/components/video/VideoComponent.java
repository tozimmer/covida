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
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.TextureManager;
import de.dfki.covida.covidacore.components.IControlableComponent;
import de.dfki.covida.covidacore.components.IVideoComponent;
import de.dfki.covida.covidacore.data.Annotation;
import de.dfki.covida.covidacore.data.AnnotationClass;
import de.dfki.covida.covidacore.data.AnnotationData;
import de.dfki.covida.covidacore.data.AnnotationStorage;
import de.dfki.covida.covidacore.data.CovidaConfiguration;
import de.dfki.covida.covidacore.data.Stroke;
import de.dfki.covida.covidacore.data.VideoMediaData;
import de.dfki.covida.covidacore.tw.TouchAndWriteComponentHandler;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.videovlcj.AbstractVideoHandler;
import de.dfki.covida.videovlcj.rendered.RenderedVideoHandler;
import de.dfki.covida.visualjme2.animations.CovidaSpatialController;
import de.dfki.covida.visualjme2.animations.DragAnimation;
import de.dfki.covida.visualjme2.animations.PreloadAnimation;
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
import java.util.Calendar;
import java.util.UUID;
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
    private final VideoMediaData data;
    private Quad preloadScreen;
    private SpatialTransformer stPreload;

    /**
     * Creates an instance of {@link VideoComponent}
     *
     * @param source video source location as {@link String}
     * @param title video title as {@link String}
     * @param width video width
     * @param height video height
     * @param zOrder component z-Order
     */
    public VideoComponent(VideoMediaData data, int zOrder) {
        super("Video Component " + data.videoName, zOrder);
        log.debug("Create video id:" + getId());
        int maxHeight = CovidaConfiguration.getInstance().maxVideoHeight;
        if (data.height > maxHeight) {
            float scale = (float) data.height / (float) maxHeight;
            data.height = (int) ((float) data.height / scale);
            data.width = (int) ((float) data.width / scale);
        }
        this.data = data;
    }

    public void open() {
        preloadAnimation();
        video = new RenderedVideoHandler(data, this);
        video.initComponent();
        initializeAnnotationData();
        attachControls();
        if (getTitle().equals("ERmed-Cavallaro")) {
            Quaternion q = new Quaternion();
            q.fromAngleAxis(FastMath.DEG_TO_RAD * (180), new Vector3f(0, 0, 1));
            setLocalRotation(q);
            controls.setLocalRotation(q);
            slider.setLocalRotation(q);
            slider.node.setLocalTranslation(
                new Vector3f(0, 22 + getHeight() / 2, 0));
            listField.setLocalRotation(q);
            listField.setDefaultPosition();
            infoField.setLocalRotation(q);
            infoField.setDefaultPosition();
            setLocalScale(1.5f);
        }else if(getTitle().equals("Scan")){
            setLocalScale(1.5f);
        }
        setDefaultPosition();
    }

    private void preloadAnimation() {
        // Splash Screen
        preloadScreen = new Quad("Splash-Image-Quad", 64, 64);
        preloadScreen.setZOrder(CovidaZOrder.getInstance().getPreload());
        // set splash screen background Texture
        Texture splashTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource("media/textures/loading_small.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        splashTexture.setWrap(Texture.WrapMode.Repeat);
        preloadScreen.setCullHint(Spatial.CullHint.Inherit);
        TextureState splashTextureState = DisplaySystem.getDisplaySystem()
                .getRenderer().createTextureState();
        splashTextureState.setTexture(splashTexture);
        preloadScreen.setRenderState(splashTextureState);
        preloadScreen.setRenderState(JMEUtils.initalizeBlendState());
        preloadScreen.updateRenderState();
        GameTaskQueueManager.getManager().update(
                new AttachChildCallable(node, preloadScreen));
        stPreload = PreloadAnimation.getController(preloadScreen);
        GameTaskQueueManager.getManager().update(
                new AddControllerCallable(preloadScreen, stPreload));
    }

    private void initializeAnnotationData() {
        AnnotationData annotationData = AnnotationStorage.getInstance()
                .getAnnotationData(this);
        if (!annotationData.uuid.equals(data.uuid)) {
            annotationData.uuid = data.uuid;
            annotationData.write();
        }
    }

    @Override
    public void create() {
        createVideo();
        video.open();
        createFields();
        createOverlays();
        setDrawable(true);
        setTouchable(true);
        startTests();
        GameTaskQueueManager.getManager().update(new RemoveControllerCallable(
                preloadScreen, stPreload));
        GameTaskQueueManager.getManager().update(new DetachChildCallable(
                node, preloadScreen));
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
    private void setNewAnnotationData(String creator) {
        // attach info field to video and make it visible
        attachAnnotation();
        pause();
        long time = video.getTime();
        Annotation annotation = new Annotation();
        annotation.description = "";
        annotation.strokelist = video.getShapes();
        annotation.shapeType = ShapeType.POLYGON;
        annotation.time_end = time;
        annotation.time_start = time;
        annotation.creator = creator;
        annotation.date = Calendar.getInstance().getTime();
        annotation.classes = new ArrayList<>();
        // set annotation data
        infoField.setAnnotationData(annotation);
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
    private void createControls() {
        controls = new VideoComponentControls(this, getZOrder() - 2);
        attachChild(controls);
        slider = new VideoSlider(this, getZOrder() - 2);
        slider.node.setLocalTranslation(
                new Vector3f(0, -22 - getHeight() / 2, 0));
        slider.setDefaultPosition();
        attachChild(slider);
    }

    /**
     * Creates video quad and handler for the video.
     */
    private void createVideo() {
        createControls();
        VideoQuad videoQuad = new VideoQuad(video);
        videoQuad.setZOrder(getZOrder() + 1);
        attachChild(videoQuad);
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
                this, (int) (getHeight() * (0.55f)), (int) (getHeight() * 1.2f),
                getZOrder());
        listField.setLocalTranslation(-getWidth() * (0.75f), 0, 0);
        listField.initComponent();
        listField.setDefaultPosition();
        infoField = new InfoFieldComponent("media/textures/bg_info.png",
                this, listField, (int) (getHeight() * (0.55f)),
                (int) (getHeight() * 1.2f), getZOrder());
        infoField.setLocalTranslation(getWidth() * (0.75f), 0, 0);
        infoField.initComponent();
        infoField.setDefaultPosition();
    }

    /**
     * Creates overlays
     */
    private void createOverlays() {
        initalizeOverlayQuads(JMEUtils.initalizeBlendState());
        stDrag = DragAnimation.getController(overlayDrag);
        attachChild(overlay);
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
                getWidth() + (getHeight() * 0.265f), getHeight() * 1.265f);
        overlay.setZOrder(getZOrder() - 1);
        overlay.setRenderState(overlayDefaultState);
        overlay.setRenderState(alpha);
        overlay.updateRenderState();
        Texture overlayDragTexture = TextureManager.loadTexture(getClass()
                .getClassLoader().getResource("media/textures/overlay_drag.png"),
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
        overlayDragBlankState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlayDragBlankState.setTexture(overlayBlankTexture);
        overlayDrag = new Quad("Overlay-Drag-Image-Quad", getWidth() + 120,
                getHeight() + 120);
        overlayDrag.setZOrder(getZOrder() - 1);
        overlayDrag.setRenderState(overlayDragState);
        overlayDrag.setRenderState(alpha);
        overlayDrag.updateRenderState();
    }

    /**
     * Method to set video repeat flag
     *
     * @param repeat - repeat flag (true -> repeat video)
     */
    public void setRepeat(Boolean repeat) {
        if (video.isReady()) {
            video.setRepeat(repeat);
        }
    }

    /**
     * Stops the video
     */
    public void stop() {
        if (video.isReady()) {
            video.stop();
        }
    }

    /**
     * Changes the actual playing media
     *
     * @param source
     */
    public void setMedia(String source) {
        if (video.isReady()) {
            video.setMedia(source);
        }
    }

    /**
     * Pauses the video
     */
    public void pause() {
        if (video.isReady()) {
            video.pause();
        }
    }

    /**
     * Resumes a paused video
     *
     */
    public void resume() {
        if (video.isReady()) {
            video.resume();
        }
    }

    /**
     * Sets the video time position in frames
     *
     * @param time - time in frames
     */
    public void setTimePosition(long time) {
        if (video.isReady()) {
            video.setTimePosition(time);
        }
    }

    /**
     * Sets the video time position in percentage
     *
     * @param percentage
     */
    public void setTimePosition(float percentage) {
        if (video.isReady()) {
            video.setTimePostion(percentage);
        }
    }

    /**
     *
     * @param start
     * @param end
     */
    public void setTimeRange(long start, long end) {
        if (video.isReady()) {
            video.setTimeRange(start, end);
        }
    }

    /**
     * Changes video source of VideoComponent
     *
     * @param file
     */
    public void videoChange(File file) {
        if (video.isReady()) {
            video.setMedia(file.getAbsolutePath());
        }
    }

    /**
     * Returns repeating status.
     *
     * @return true if video repeating is enabled.
     */
    public boolean isRepeating() {
        if (video.isReady()) {
            return video.isRepeat();
        }
        return false;
    }

    /**
     * Shows AnnotationList from this instance of VideoComponent.
     *
     */
    public void attachList() {
        if (!hasList()) {
            attachChild(listField);
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
            attachChild(infoField);
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
        if (video.isReady()) {
            return video.getDimension();
        }
        return null;
    }

    /**
     * Method which selects and deselects the component
     */
    public void toggleSelected() {
        if (video.isPlaying()) {
            pause();
            overlay.setRenderState(overlayDefaultState);
            overlay.updateRenderState();
        } else {
            resume();
            controls.normalizeStop();
            overlay.setRenderState(overlaySelectState);
            overlay.updateRenderState();
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
        attachChild(overlayDrag);
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
    @Override
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
        if (video.isReady()) {
            video.setVolume(i);
        }
    }

    /**
     * Returns the end postion from the video as {@link Long}
     *
     * @return end position in ms as {@link Long}
     */
    public long getMaxTime() {
        if (video.isReady()) {
            return video.getMaxTime();
        }
        return 0;
    }

    /**
     * Returns the current time position.
     *
     * @return current time position in ms as {@link Long}
     */
    public long getTime() {
        if (video.isReady()) {
            return video.getTime();
        }
        return 0;
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
        if (video.isReady()) {
            video.pause();
            controls.highlightPlay();
            Vector3f local = getLocal(x, y);
            int localX = (int) local.x;
            int localY = (int) local.y;
            localX += (getDimension().getWidth() / 2) / node.getLocalScale().x;
            localY = (int) ((getDimension().getHeight() / node.getLocalScale().y)
                    - ((localY + (getDimension().getHeight() / 2)
                    / node.getLocalScale().y)));
            video.draw(new Point(localX, localY));
        }
    }

    @Override
    public void drawEnd(int x, int y) {
        if (video.isReady()) {
            draw(x, y);
            video.endDrawStroke();
        }
    }

    @Override
    public void hwrAction(String id, String hwr) {
        if (!video.isReady()) {
            return;
        }
        if (video.getShapes().strokelist.isEmpty()) {
            Stroke stroke = new Stroke();
            stroke.points.add(new Point(5, 5));
            stroke.points.add(new Point(5, getHeight() - 5));
            stroke.points.add(new Point(getWidth() - 5, getHeight() - 5));
            stroke.points.add(new Point(getWidth() - 5, 5));
            stroke.points.add(new Point(5, 5));
            video.addShape(stroke);
            String creator = CovidaConfiguration.getLoggedUser(id);
            setNewAnnotationData(creator);
        }
        video.clearDrawing();
        infoField.drawHwrResult(hwr);
    }

    @Override
    public void load(Annotation annotation) {
        if (!video.isReady()) {
            return;
        }
        attachAnnotation();
        clearAnnotation();
        infoField.setAnnotationData(annotation);
        video.setTimePosition(annotation.time_start);
        for (Stroke shape : annotation.strokelist.strokelist) {
            video.addShape(shape);
        }
    }

    /**
     * Method for calculation of video width
     *
     * @return video width
     */
    @Override
    public int getWidth() {
        if (video != null && video.isReady()) {
            return video.getWidth();
        }
        return data.width;
    }

    /**
     *
     * @return height of the VideoComponent
     */
    @Override
    public final int getHeight() {
        if (video != null && video.isReady()) {
            return video.getHeight();
        }
        return data.height;
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
        if (video.isReady()) {
            video.setShapes(video.getDrawings());
            video.clearDrawing();
            String creator = CovidaConfiguration.getLoggedUser(event.getDeviceAddress());
            setNewAnnotationData(creator);
        }
    }

    @Override
    public String getSource() {
        return data.videoSource;
    }

    @Override
    public boolean toggle(ActionName action) {
        if (video.isReady()) {
            if (action.equals(ActionName.BACKWARD)) {
                if ((video.getTime() - video.getMaxTime() / 20) > 0) {
                    setTimePosition(video.getTime()
                            - video.getMaxTime() / 20);
                } else {
                    setTimePosition(0);
                }
                return false;
            } else if (action.equals(ActionName.CLOSE)) {
                close();
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
                    controls.normalizeStop();
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
                }
                return true;
            } else if (action.equals(ActionName.RESET)) {
                reset(0);
            }
        }
        return false;
    }

    @Override
    public String getTitle() {
        return data.videoName;
    }

    boolean isPlaying() {
        if (video.isReady()) {
            return video.isPlaying();
        }
        return false;
    }

    public void clearAnnotation() {
        if (video.isReady()) {
            video.clearDrawing();
            video.clearShape();
            infoField.resetInfo();
        }
    }

    public void deleteDescription(TextComponent aThis) {
        infoField.deleteDescription(aThis);
    }

    @Override
    public UUID getUUID() {
        return data.uuid;
    }

    public void tagAction(AnnotationClass tag) {
        if (!video.isReady()) {
            return;
        }
        if (video.getShapes().strokelist.isEmpty()) {
            Stroke stroke = new Stroke();
            stroke.points.add(new Point(5, 5));
            stroke.points.add(new Point(5, getHeight() - 5));
            stroke.points.add(new Point(getWidth() - 5, getHeight() - 5));
            stroke.points.add(new Point(getWidth() - 5, 5));
            stroke.points.add(new Point(5, 5));
            video.addShape(stroke);
            String creator = CovidaConfiguration.getLoggedUser(null);
            setNewAnnotationData(creator);
        }
        video.clearDrawing();
        infoField.tagAction(tag);
    }

    public void setFieldZOrder() {
        infoField.setZOrder(node.getZOrder());
        listField.setZOrder(node.getZOrder());
    }
}