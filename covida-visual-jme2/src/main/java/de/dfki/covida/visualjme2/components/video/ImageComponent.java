/*
 * ImageComponent.java
 *
 * Copyright (c) 2012, Markus Weber All rights reserved.
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
import de.dfki.covida.covidacore.components.IImageComponent;
import de.dfki.covida.covidacore.data.Annotation;
import de.dfki.covida.covidacore.data.AnnotationClass;
import de.dfki.covida.covidacore.data.AnnotationData;
import de.dfki.covida.covidacore.data.AnnotationStorage;
import de.dfki.covida.covidacore.data.CovidaConfiguration;
import de.dfki.covida.covidacore.data.ImageMediaData;
import de.dfki.covida.covidacore.data.Stroke;
import de.dfki.covida.covidacore.tw.TouchAndWriteComponentHandler;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.visualjme2.animations.CovidaSpatialController;
import de.dfki.covida.visualjme2.animations.DragAnimation;
import de.dfki.covida.visualjme2.animations.PreloadAnimation;
import de.dfki.covida.visualjme2.animations.ResetAnimation;
import de.dfki.covida.visualjme2.components.JMEComponent;
import de.dfki.covida.visualjme2.components.TextComponent;
import de.dfki.covida.visualjme2.components.video.fields.InfoFieldComponent;
import de.dfki.covida.visualjme2.components.video.fields.ListFieldComponent;
import de.dfki.covida.visualjme2.utils.AddControllerCallable;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import de.dfki.covida.visualjme2.utils.CovidaZOrder;
import de.dfki.covida.visualjme2.utils.DetachChildCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import de.dfki.covida.visualjme2.utils.RemoveControllerCallable;
import de.dfki.touchandwrite.analyser.touch.gestures.events.RotationGestureEventImpl;
import de.dfki.touchandwrite.analyser.touch.gestures.events.ZoomEventImpl;
import de.dfki.touchandwrite.input.pen.event.ShapeEvent;
import de.dfki.touchandwrite.shape.ShapeType;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Component to present an image.
 *
 * @author Markus Weber
 */
public class ImageComponent extends JMEComponent implements
        IImageComponent, IControlableComponent {

    /**
     * Upscale factor to change quality of the image as {@link Float} Note that
     * 1.f represents full quality and that the {@code UPSCALE_FACTOR} must be
     * greater 0.f .
     */
    private static final float UPSCALE_FACTOR = 1.f;
    /**
     * Info field with current annotation
     */
    private InfoFieldComponent infoField;
    /**
     * List field with all annotation
     */
    private ListFieldComponent listField;
    /**
     * Default overlay.
     */
    private Quad overlay;
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
     * Overlay state for selected image components (selected texture state)
     */
    private TextureState overlaySelectState;
    /**
     * Quad for dragging animation texture
     */
    private Quad overlayDrag;
    /**
     * Overlay state for dragging image components (dragging texture state)
     */
    private TextureState overlayDragState;
    /**
     * Overlay state for not dragging image components (not dragging texture
     * state)
     */
    private TextureState overlayDragBlankState;
    /**
     * Data description
     */
    private final ImageMediaData data;
    /**
     * Image data.
     */
    private BufferedImage image;
    private Quad preloadScreen;
    private SpatialTransformer stPreload;
    private ImageQuad imageQuad = null;

    /**
     * Creates an instance of {@link VideoComponent}
     *
     * @param data - media data of image
     * @param zOrder component z-Order
     */
    public ImageComponent(ImageMediaData data, int zOrder) {
        super("Image Component " + data.imageName, zOrder);
        log.debug("Create image [id] : " + getId() + " [name] : " + data.imageName);
        final int maxHeight = CovidaConfiguration.getInstance().maxVideoHeight;

        try {
            image = ImageIO.read(new File(data.imageSource));
        } catch (IOException ex) {
            Logger.getLogger(ImageComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (data.width == -1 && data.height == -1) {
            data.width = image.getWidth();
            data.height = image.getHeight();
        }
        if (data.height > maxHeight) {
            float scale = (float) data.height / (float) maxHeight;
            data.height = (int) ((float) data.height / scale);
            data.width = (int) ((float) data.width / scale);
        }
        this.data = data;
    }

    /**
     * Opens the image.
     */
    public void open() {
        preloadAnimation();
        create();
        initializeAnnotationData();
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
        createOverlays();
        createFields();
        createImage();
        setDrawable(true);
        setTouchable(true);
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
     * Sets current annotations on the image on the info field
     */
    private void setNewAnnotationData(String creator) {
        // attach info field to image and make it visible
        attachAnnotation();
        Annotation annotation = new Annotation();
        annotation.description = "";
        annotation.shapeType = ShapeType.POLYGON;
        annotation.creator = creator;
        annotation.date = Calendar.getInstance().getTime();
        annotation.classes = new ArrayList<>();
        annotation.time_start = 0L;
        annotation.time_end = 0L;
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
     * Creates image quad and handler for the image.
     */
    private void createImage() {
        imageQuad = new ImageQuad(this.data, this.image);
        imageQuad.setZOrder(getZOrder() + 1);
        attachChild(imageQuad);

        if (UPSCALE_FACTOR > 0.0f) {
            setLocalScale(1.f / UPSCALE_FACTOR);
        } else {
            log.warn("Invalid UPSCALE_FACTOR, must be greater 0.0f.");
        }
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
        overlayDefaultTexture.setWrap(Texture.WrapMode.Clamp);
        overlayDefaultState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlayDefaultState.setTexture(overlayDefaultTexture);
        Texture overlaySelectTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(
                "media/textures/overlay_select.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlaySelectTexture.setWrap(Texture.WrapMode.Clamp);
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

    public Vector3f resetLocalTranslation(Vector3f local) {
        return local.add(new Vector3f(getLocalTranslation().x,
                getLocalTranslation().x, 0));
    }

    /**
     * Sets the local scale of the node from this instance of VideoComponent
     *
     * @param scale Video scale as {@link Float} Note that 1.f is the standard
     * scale
     */
    public void rescale(float scale) {
        this.setLocalScale(scale);
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
    public void stopDragAnimation() {
        overlayDrag.setRenderState(overlayDragBlankState);
        overlayDrag.updateRenderState();
        GameTaskQueueManager.getManager().update(new DetachChildCallable(node, overlayDrag));
        GameTaskQueueManager.getManager().update(new RemoveControllerCallable(overlayDrag, stDrag));
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
        stopDragAnimation();
    }

    @Override
    public void touchBirthAction(int id, int x, int y) {
    }

    @Override
    public void draw(int x, int y) {
        Vector3f local = getLocal(x, y);
        int localX = (int) local.x;
        int localY = (int) local.y;
        localX += (getDimension().getWidth() / 2) / node.getLocalScale().x;
        localY = (int) ((getDimension().getHeight() / node.getLocalScale().y)
                - ((localY + (getDimension().getHeight() / 2)
                / node.getLocalScale().y)));
        imageQuad.draw(new Point(localX, localY));
    }

    @Override
    public void drawEnd(int x, int y) {
        draw(x, y);
        imageQuad.endDrawStroke();
    }

    /**
     * Method for calculation of image width
     *
     * @return image width
     */
    @Override
    public int getWidth() {
        return data.width;
    }

    /**
     *
     * @return height of the VideoComponent
     */
    @Override
    public final int getHeight() {
        return data.height;
    }

    @Override
    public void cleanUp() {
        log.debug("cleanup video (id: " + getId() + ")");
        TouchAndWriteComponentHandler.getInstance().removeComponent(this);
        infoField.cleanUp();
        listField.cleanUp();
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
        String creator = CovidaConfiguration.getLoggedUser(event.getDeviceAddress());
        setNewAnnotationData(creator);
    }

    @Override
    public String getSource() {
        return data.imageSource;
    }

    @Override
    public boolean toggle(ActionName action) {
        return true;
    }

    @Override
    public String getTitle() {
        return data.imageName;
    }

    @Override
    public void clearAnnotation() {
        infoField.resetInfo();
    }

    public void deleteDescription(TextComponent aThis) {
        infoField.deleteDescription(aThis);
    }

    @Override
    public UUID getUUID() {
        return data.uuid;
    }

    public void tagAction(AnnotationClass tag) {

        infoField.tagAction(tag);
    }

    public void setFieldZOrder() {
        infoField.setZOrder(node.getZOrder());
        listField.setZOrder(node.getZOrder());
    }

    @Override
    public void load(Annotation annotation) {
    }
}