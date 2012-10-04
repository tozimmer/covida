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
import de.dfki.covida.covidacore.data.*;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.videovlcj.AbstractVideoHandler;
import de.dfki.covida.videovlcj.rendered.RenderedVideoHandler;
import de.dfki.covida.visualjme2.animations.CovidaSpatialController;
import de.dfki.covida.visualjme2.animations.DragAnimation;
import de.dfki.covida.visualjme2.animations.ResetAnimation;
import de.dfki.covida.visualjme2.components.CovidaJMEComponent;
import de.dfki.covida.visualjme2.components.CovidaTextComponent;
import de.dfki.covida.visualjme2.components.video.fields.DisplayFieldComponent;
import de.dfki.covida.visualjme2.components.video.fields.ListFieldComponent;
import de.dfki.covida.visualjme2.utils.AddControllerCallable;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import de.dfki.covida.visualjme2.utils.DetachChildCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import de.dfki.covida.visualjme2.utils.RemoveControllerCallable;
import de.dfki.touchandwrite.input.pen.event.ShapeEvent;
import de.dfki.touchandwrite.shape.ShapeType;
import java.awt.*;
import java.io.File;
import uk.co.caprica.vlcj.player.MediaPlayer;

/**
 * Component to display videos.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public final class VideoComponent extends CovidaJMEComponent implements
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
    private DisplayFieldComponent infoField;
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
    protected Quad overlay;
    /**
     * Overlay for the video title
     */
    private CovidaTextComponent textOverlay;
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
     * Overlay state for not dragging video components 
     * (not dragging texture state)
     */
    private TextureState overlayDragBlankState;
    /**
     * Timer to determine how long video component was dragged
     */
    private long dragTimer;

    /**
     * Creates an instance of {@link VideoComponent}
     *
     * @param source video source location as {@link String}
     * @param title video title as {@link String}
     * @param height video height as {@link Integer}
     * @param format {@link VideoFormat} to determine width of the video
     */
    public VideoComponent(String source, String title, int height, VideoFormat format) {
        super("Video Component ");
        log.debug("Create video id:" + getId());
        video = new RenderedVideoHandler(source, title, (int) (height * UPSCALE_FACTOR), format.determineWidth((int) (height * UPSCALE_FACTOR)));
        setDefaultPosition();
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
     * Mehod which holds test method calls
     */
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

    /**
     * Sets current annotations on the video on the info field
     */
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
    }

    /**
     * Calculates scaling factor for snapshot texture.
     *
     * @param diffX
     * @param diffY
     * @return
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
     * @param min
     * @param max
     * @return
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
     * Hides AnnotationList from this instance of VideoComponent.
     *
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
     * @param timeout 
     */
    public void enableTimeCodeOverlay(long timeout) {
        video.enableTimeCodeOverlay(timeout);
    }

    /**
     * Creates the video controls.
     */
    public void createControls() {
        controls = new VideoComponentControls(this);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, controls.node));
        slider = new VideoSlider(this);
        slider.getLocalTranslation().set(
                new Vector3f(-15, -30 - getHeight() / 2, 0));
        slider.setDefaultPosition();
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, slider.node));
    }

    /**
     * Creates video quad and handler for the video.
     */
    public void createVideo() {
        VideoQuad videoQuad = new VideoQuad(video);

        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, videoQuad));

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
    public void createFields() {
        listField = new ListFieldComponent("media/textures/bg_list.png",
                this, (int) (getHeight() * (0.55f)), (int) (getHeight() * 1.2f));
        listField.setLocalTranslation(-getWidth() * (0.75f), 0, 0);
        listField.initComponent();
        listField.setDefaultPosition();
        attachList();
        infoField = new DisplayFieldComponent("media/textures/bg_info.png",
                this, listField, (int) (getHeight() * (0.55f)),
                (int) (getHeight() * 1.2f));
        infoField.setLocalTranslation(getWidth() * (0.75f), 0, 0);
        infoField.initComponent();
        infoField.setDefaultPosition();
        attachAnnotation();
    }

    /**
     * Creates overlays
     */
    public void createOverlays() {
        textOverlay = new CovidaTextComponent(this);
        textOverlay.setLocalTranslation(0, getHeight() / (1.50f) - getFontSize()
                / 2.f, 0);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, textOverlay.node));
        textOverlay.setSize(getFontSize());
        initalizeOverlayQuads(JMEUtils.initalizeBlendState());
        stDrag = DragAnimation.getController(overlayDrag);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, overlay));
    }

    /**
     * Adds points to shape which will be drawn on the video
     *
     * @param points {@link ShapePoints} which contains the point data.
     */
    public void draw(ShapePoints points) {
        video.setShape(points);
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
     *
     *
     * @return
     */
    public boolean isRepeating() {
        return video.isRepeat();
    }
        
    /**
     * Loads annotation data into InfoField.
     *
     * @param index Index of the annotation to load.
     */
    public void loadAnnotationData(int index) {
        infoField.loadData(index);
        attachAnnotation();
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
     * @return
     */
    public boolean hasList() {
        return listField.isOpen();
    }

    /**
     * Shows AnnotationField
     */
    public void attachAnnotation() {
        if (!hasInfoField()) {
            GameTaskQueueManager.getManager().update(new AttachChildCallable(node, infoField.node));
            infoField.open();
        }
    }

    /**
     * Shows AnnotationField
     */
    public void detachAnnotation() {
        if (hasInfoField()) {
            infoField.close();
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

    private void detachMenu() {
        GameTaskQueueManager.getManager().update(new DetachChildCallable(node, controls.node));
    }

    private void attachControls() {
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, controls.node));
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

    public void startDragAnimation() {
        overlayDrag.setRenderState(overlayDragState);
        overlayDrag.updateRenderState();
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, overlayDrag));
        GameTaskQueueManager.getManager().update(new AddControllerCallable(overlayDrag, stDrag));
    }

    public void stopDragAniation() {
        overlayDrag.setRenderState(overlayDragBlankState);
        overlayDrag.updateRenderState();
        GameTaskQueueManager.getManager().update(new DetachChildCallable(node, overlayDrag));
        GameTaskQueueManager.getManager().update(new RemoveControllerCallable(overlayDrag, stDrag));
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

    public String getVideoProgress() {
        return video.getVideoProgress();
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

    @Override
    public boolean toggle(ActionName action) {
        log.debug(action.toString());
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
                this.infoField.close();
            }
        }
        return false;
    }
}