/*
 * InfoFieldComponent.java
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
package de.dfki.covida.visualjme2.components.video.fields;

import com.jme.animation.SpatialTransformer;
import com.jme.image.Texture;
import com.jme.image.Texture.WrapMode;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.TextureManager;
import de.dfki.covida.covidacore.data.Annotation;
import de.dfki.covida.covidacore.data.AnnotationStorage;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.covidacore.utils.VideoUtils;
import de.dfki.covida.visualjme2.animations.CloseAnimation;
import de.dfki.covida.visualjme2.animations.CloseAnimationType;
import de.dfki.covida.visualjme2.animations.OpenAnimation;
import de.dfki.covida.visualjme2.animations.ResetAnimation;
import de.dfki.covida.visualjme2.animations.SaveAnimation;
import de.dfki.covida.visualjme2.components.ControlButton;
import de.dfki.covida.visualjme2.components.JMEComponent;
import de.dfki.covida.visualjme2.components.TextComponent;
import de.dfki.covida.visualjme2.components.video.VideoComponent;
import de.dfki.covida.visualjme2.utils.AddControllerCallable;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import de.dfki.covida.visualjme2.utils.DetachChildCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import de.dfki.covida.visualjme2.utils.RemoveControllerCallable;
import de.dfki.touchandwrite.shape.ShapeType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Component which displays annotation data of VideoComponent.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class InfoFieldComponent extends JMEComponent {

    private static final int ANIMATION_DURATION = 750;
    private static final int DEFAULT_FONT_SIZE = 18;
    private static final int DEFAULT_HEIGHT = 250;
    private int textBeginY;
    private int width = 112;
    private int height = 250;
    private static final int DEFAULT_CHARACTER_LIMIT = 14;
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
    private List<Point> shapePoints;
    private VideoComponent video;
    private String title;
    private long time_start;
    private long time_end;
    private ListFieldComponent listField;
    private ArrayList<TextComponent> descriptionText;
    private TextureState tsSpacer;
    private Texture textureSpacer;
    private ShapeType shapeType;
    private TextComponent titleTextOverlay;
    private TextComponent timeOverlay;
    private TextComponent timeTextOverlay;
    private TextComponent descriptionOverlay;
    private boolean open;
    private int descriptionBeginY;
    private static final int FONT_SIZE = 30;
    protected Quad overlayDefault;
    private SpatialTransformer st;
    private ControlButton save;
    private ControlButton delete;

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
    public InfoFieldComponent(String resource, VideoComponent video,
            ListFieldComponent listField, int width, int height) {
        super("DisplayFieldComponent");
        this.video = video;
        this.width = width;
        this.height = height;
        defaultScale = new Vector3f(getLocalScale().x, getLocalScale().y, getLocalScale().z);
        defaultRotation = new Quaternion(getLocalRotation().x,
                getLocalRotation().y, getLocalRotation().z,
                getLocalRotation().w);
        defaultTranslation = new Vector3f(getLocalTranslation().x,
                getLocalTranslation().y, getLocalTranslation().z);
        image = resource;
        this.listField = listField;
    }

    /**
     * Initialize the overlays quads and texture states.
     *
     * @param alpha {@link BlendState}
     */
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
    }

    /**
     * Returns the font size.
     *
     * @return Font size as {@link Integer}
     */
    private int getFontSize() {
        return (int) (1.2f * (float) DEFAULT_FONT_SIZE * ((float) getHeight() / (float) DEFAULT_HEIGHT));
    }

    /**
     * Returns the font spacer size.
     *
     * @return Font spacer size as {@link Integer}
     */
    private int getFontSpacer() {
        return (int) ((float) getFontSize() * 0.8f);
    }

    /**
     * Returns the text spacer size.
     *
     * @return text spacer size as {@link Integer}
     */
    private int getTextSpacer() {
        return (int) ((float) getFontSize() / 5.f);
    }

    /**
     * Returns the character limit.
     *
     * @return character limit as {@link Integer}
     */
    private int getCharacterLimit() {
        return (int) ((float) DEFAULT_CHARACTER_LIMIT * ((float) getFontSize() / (float) DEFAULT_FONT_SIZE));
    }

    /**
     * Initilize the textures
     */
    private void initTextures() {
        // ---- Background Texture state initialization ----
        ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setCorrectionType(TextureState.CorrectionType.Perspective);
        ts.setEnabled(true);
        texture = TextureManager.loadTexture(getClass().getClassLoader().getResource(image));
        ts.setTexture(texture);

        quad = new Quad("Display image quad", width, height);
        quad.setRenderState(ts);
        quad.setRenderState(JMEUtils.initalizeBlendState());
        quad.updateRenderState();
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, quad));

        // Spacer
        tsSpacer = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        tsSpacer.setCorrectionType(TextureState.CorrectionType.Perspective);
        tsSpacer.setEnabled(true);
        textureSpacer = TextureManager.loadTexture(getClass().getClassLoader().getResource("media/textures/info_spacer.png"));
        tsSpacer.setTexture(textureSpacer);
    }

    /**
     * Sets title of the video
     *
     * @param title video title as {@link String}
     */
    private void setTitle(String title) {
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

    /**
     * Sets video time position.
     *
     * @param time position as {@link Long}
     */
    private void setTime(long time) {
        if (timeOverlay != null && timeTextOverlay != null) {
            this.time_start = time;
            this.time_end = time + 10;
            timeTextOverlay.setText(VideoUtils.getTimeCode(time));
        }
    }

    /**
     * Adds a spacer.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param width width of the spacer
     * @param height height of the spacer
     */
    private void addSpacer(int x, int y, int width, int height) {
        Quad spacerQuad = new Quad("Spacer", width, height);
        spacerQuad.setRenderState(tsSpacer);
        spacerQuad.setRenderState(JMEUtils.initalizeBlendState());
        spacerQuad.updateRenderState();
        spacerQuad.setLocalTranslation(x, y, 0);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, spacerQuad));
    }

    /**
     * Save annotation data to binary and XML files
     */
    private void saveData() {
        StringBuilder descriptions = new StringBuilder();
        for (TextComponent description : descriptionText) {
            descriptions.append(description.getText());
            descriptions.append(" ");
        }
        // TODO new annotation data if media has changed!
        Annotation annotation = new Annotation();
        annotation.time_start = time_start;
        annotation.time_end = time_end;
        annotation.shapeType = shapeType;
        annotation.shapePoints = shapePoints;
        annotation.description = descriptions.toString();
        AnnotationStorage.getInstance().getAnnotationData(video).annotations.add(annotation);
        clearDescriptionText();
        AnnotationStorage.getInstance().getAnnotationData(video).save();
        AnnotationStorage.getInstance().getAnnotationData(video).export();
    }

    /**
     * Sets the shape points for the current saved annotation.
     *
     * @param shapePoints
     */
    private void setShapePoints(List<Point> shapePoints) {
        this.shapePoints = shapePoints;
    }

    /**
     * Sets the video source of the current annotation.
     *
     * @param videoSource
     */
    private void setVideoSource(String videoSource) {
        this.videoSource = videoSource;
    }

    /**
     * Sets shape type of current annotation.
     *
     * @param shapeType {@link ShapeType}
     */
    private void setShapeType(ShapeType shapeType) {
        this.shapeType = shapeType;
    }

    /**
     * Initialize the component.
     */
    public void initComponent() {
        initTextures();
        textBeginY = (int) (quad.getHeight() / 2 - FONT_SIZE);
        initalizeListOverlayQuads(JMEUtils.initalizeBlendState());
        titleTextOverlay = new TextComponent(video, ActionName.NONE);
        titleTextOverlay.setFont(1);
        titleTextOverlay.setSize(getFontSize());
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, titleTextOverlay.node));
        titleTextOverlay.setLocalTranslation(0, textBeginY, 0);
//        int y = (int) (titleTextOverlay.getLocalTranslation().y + ((float) titleTextOverlay.getFontSize() / 2.f));
//        addSpacer(0, y, (int) (quad.getWidth() / 1.1f), getTextSpacer());
        textBeginY = (int) (textBeginY - (float) getFontSpacer() * 1.25f - getTextSpacer());
        timeOverlay = new TextComponent(video, ActionName.NONE);
        timeOverlay.setLocalTranslation(0, textBeginY + FONT_SIZE, 0);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, timeOverlay.node));
        timeOverlay.setFont(1);
        timeOverlay.setSize(getFontSize());
        timeOverlay.setText("Time:");
        timeTextOverlay = new TextComponent(video, ActionName.NONE);
        timeTextOverlay.setLocalTranslation(0, textBeginY, 0);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, timeTextOverlay.node));
        timeTextOverlay.setFont(1);
        timeTextOverlay.setSize(getFontSize());
        int y = (int) (timeTextOverlay.getLocalTranslation().y + ((float) timeTextOverlay.getFontSize() / 2.f));
        addSpacer(0, y, (int) (quad.getWidth() / 1.1f), getTextSpacer());
        textBeginY = (int) (textBeginY - (float) getFontSpacer() * 1.25f - getTextSpacer());
        descriptionOverlay = new TextComponent(video, ActionName.NONE);
        descriptionOverlay.setLocalTranslation(0, textBeginY, 0);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, descriptionOverlay.node));
        descriptionOverlay.setFont(1);
        descriptionOverlay.setSize(getFontSize());
        descriptionText = new ArrayList<>();
        if (AnnotationStorage.getInstance().getAnnotationData(video).size() > 0) {
            listField.drawEntries();
        }
        save = new ControlButton(ActionName.SAVE, video,
                "media/textures/video_controls_save.png",
                "media/textures/video_controls_save.png", 64, 64);
        save.setLocalTranslation(-getWidth() / 2 + 32, -getHeight() / 2 + 32, 0);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, save.node));
        delete = new ControlButton(ActionName.DELETE, video,
                "media/textures/video_control_delete.png",
                "media/textures/video_control_delete.png", 64, 64);
        delete.setLocalTranslation(getWidth() / 2 - 32, -getHeight() / 2 + 32, 0);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, delete.node));
    }

    /**
     * Sets the current {@link Annotation}.
     *
     * @param annotation {@link Annotation} to set.
     */
    public void setAnnotationData(Annotation annotation) {
        setTime(annotation.time_start);
        setTitle(AnnotationStorage.getInstance().getAnnotationData(video).title);
        setVideoSource(AnnotationStorage.getInstance().getAnnotationData(video).videoSource);
        setShapePoints(annotation.shapePoints);
        setShapeType(annotation.shapeType);
    }

    /**
     * Draws the hwr result in the info field as annotation descrition.
     *
     * @param hwrResults hwr results
     */
    public void drawHwrResult(String hwrResults) {
        if (descriptionOverlay != null && descriptionText != null) {
            descriptionOverlay.setText("Description:");
            descriptionOverlay.setSize(getFontSize());
            if (descriptionText.isEmpty()) {
                descriptionBeginY = (int) (textBeginY - (float) getFontSpacer());
            }
            descriptionBeginY = (int) (descriptionBeginY - (float) getFontSpacer() * 0.9f);
            TextComponent descriptionTextOverlay = new TextComponent(video, ActionName.COPY);
            descriptionTextOverlay.setLocalTranslation(0, descriptionBeginY, 0);
            descriptionTextOverlay.setDefaultPosition();
            descriptionTextOverlay.setTouchable(true);
            GameTaskQueueManager.getManager().update(new AttachChildCallable(node, descriptionTextOverlay.node));
            descriptionText.add(descriptionTextOverlay);
            descriptionText.get(descriptionText.size() - 1).setText(hwrResults);
            descriptionText.get(descriptionText.size() - 1).setFont(1);
            descriptionText.get(descriptionText.size() - 1).setSize(
                    getFontSize());
            if (hwrResults.length() > getCharacterLimit()) {
                float factor = (float) hwrResults.length()
                        / getCharacterLimit();
                descriptionText.get(descriptionText.size() - 1).setSize(
                        (int) ((float) getFontSize() / factor));
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

    /**
     * Clears the description text on the info field.
     */
    public void clearDescriptionText() {
        for (int i = 0; i < descriptionText.size(); i++) {
            if (node.hasChild(descriptionText.get(i).node)) {
                GameTaskQueueManager.getManager().update(new DetachChildCallable(node, descriptionText.get(i).node));
            } else {
                log.debug("TextOverlay Node wasn't attached during clearDescriptionText");
            }
        }
        descriptionText = new ArrayList<>();
        descriptionBeginY = textBeginY;
    }

    /**
     * Resets textNode
     */
    public void resetInfo() {
        clearDescriptionText();
    }

    /**
     * Saves annotation data from the InfoField
     */
    public void save() {
        log.debug("Save begin");
        saveData();
        st = SaveAnimation.getController(node, ANIMATION_DURATION, CloseAnimationType.INFO_FIELD);
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node, st));
        close();
    }

    /**
     * Gets the current displayed annotation description.
     *
     * @return Annotation description {@link String}
     */
    public String getDescription() {
        StringBuilder stringBuilder = new StringBuilder("");
        for (TextComponent description : descriptionText) {
            stringBuilder.append(description.getText());
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    /**
     * Open animation of the DisplayInfoComponent
     */
    public void open() {
        open = true;
        save.setTouchable(true);
        delete.setTouchable(true);
        for (TextComponent text : descriptionText) {
            text.setTouchable(true);
        }
        st = OpenAnimation.getController(node, ANIMATION_DURATION, defaultScale);
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node, st));
    }

    /**
     * Loads and removes annoation data from {@link AnnotationStorage}.
     *
     * @param index {@link Annotation} index
     */
    public void loadData(int index) {
        if (AnnotationStorage.getInstance().getAnnotationData(video).size() > index) {
            resetInfo();
            video.draw(AnnotationStorage.getInstance().getAnnotationData(video).annotations.get(index).shapePoints);
            video.setTimePosition(AnnotationStorage.getInstance()
                    .getAnnotationData(video).annotations.get(index).time_start);
            setTime(AnnotationStorage.getInstance().getAnnotationData(video).annotations.get(index).time_start);
            video.pause();
            setTitle(AnnotationStorage.getInstance().getAnnotationData(video).title);
            ArrayList<String> descriptions = new ArrayList<>();
            descriptions.addAll(Arrays.asList(
                    AnnotationStorage.getInstance().getAnnotationData(video).annotations.get(index).description.split(" ")));
            for (String description : descriptions) {
                drawHwrResult(description);
            }
            AnnotationStorage.getInstance().getAnnotationData(video).annotations.remove(index);
        } else {
            log.debug("VideoAnnotationData has not enough entries VideoId: "
                    + video.getId()
                    + " title: "
                    + video.node.getName()
                    + " "
                    + AnnotationStorage.getInstance().getAnnotationData(video));
        }
    }

    /**
     * Resets the info fields position and scale.
     */
    public void reset() {
        if (node.getControllers().contains(st)) {
            GameTaskQueueManager.getManager().update(new RemoveControllerCallable(node, st));
        }
        st = ResetAnimation.getController(node, defaultScale, defaultRotation, defaultTranslation);
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node, st));
    }

    /**
     * Returns the {@link VideoComponent}.
     *
     * @return {@link VideoComponent}
     */
    public VideoComponent getVideo() {
        return video;
    }

    /**
     * Returns info field status.
     *
     * @return true if info field is open.
     */
    public boolean isOpen() {
        return open;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void close() {
        open = false;
        save.setTouchable(false);
        delete.setTouchable(false);
        for (TextComponent text : descriptionText) {
            text.setTouchable(false);
        }
        title = null;
        time_start = 0;
        resetInfo();
        listField.drawEntries();
    }
    
    public void delete() {
        // Delete animation (Info Field)
        st = CloseAnimation.getController(node, ANIMATION_DURATION, CloseAnimationType.INFO_FIELD);
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node, st));
        close();
    }
}
