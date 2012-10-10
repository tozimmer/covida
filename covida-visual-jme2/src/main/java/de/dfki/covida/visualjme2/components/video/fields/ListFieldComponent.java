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
package de.dfki.covida.visualjme2.components.video.fields;

import com.jme.animation.SpatialTransformer;
import com.jme.image.Texture;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.TextureManager;
import de.dfki.covida.covidacore.data.Annotation;
import de.dfki.covida.covidacore.data.AnnotationData;
import de.dfki.covida.covidacore.data.AnnotationStorage;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.covidacore.utils.VideoUtils;
import de.dfki.covida.visualjme2.animations.CloseAnimation;
import de.dfki.covida.visualjme2.animations.CloseAnimationType;
import de.dfki.covida.visualjme2.animations.OpenAnimation;
import de.dfki.covida.visualjme2.animations.ResetAnimation;
import de.dfki.covida.visualjme2.components.JMEComponent;
import de.dfki.covida.visualjme2.components.TextComponent;
import de.dfki.covida.visualjme2.components.video.VideoComponent;
import de.dfki.covida.visualjme2.utils.AddControllerCallable;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import de.dfki.covida.visualjme2.utils.RemoveControllerCallable;
import java.util.ArrayList;
import java.util.List;

/**
 * Component which displays annotation data of VideoComponent.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class ListFieldComponent extends JMEComponent {

    /**
     * Animation duration
     */
    public static final int ANIMATION_DURATION = 750;
    /**
     * Default font size
     */
    private static final int DEFAULT_FONT_SIZE = 18;
    /**
     * Default height
     */
    private static final int DEFAULT_HEIGHT = 250;
    /**
     * Width
     */
    private int width = 112;
    /**
     * Height
     */
    private int height = 250;
    /**
     * Background image
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
     * Video
     */
    private VideoComponent video;
    /**
     * {@link List} of {@link TextComponent}s
     */
    private List<TextComponent> entries;
    /**
     * Texture state of the spacer
     */
    private TextureState tsSpacer;
    /**
     * Texture of the spacer
     */
    private Texture textureSpacer;
    /**
     * Indicates of the list field is open.
     */
    private boolean open;
    /**
     * Font size
     */
    private static final int FONT_SIZE = 30;
    /**
     * {@link SpatialTransformer} for the open and close animation
     */
    private SpatialTransformer st;
    private AnnotationData data;

    /**
     * List field component constructer.
     *
     * @param resource Background image resource as {@link String}
     * @param video {@link VideoComponent}
     * @param width List field width as {@link Integer}
     * @param height List field height as {@link Integer}
     */
    public ListFieldComponent(String resource, VideoComponent video, int width, int height) {
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
        entries = new ArrayList<>();
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
     * Initialize the component.
     */
    public void initComponent() {
        initTextures();
        int x = (int) (0);
        float y = getTextY(0);
        TextComponent to = new TextComponent(video, ActionName.NONE);
        to.setLocalTranslation(x, y, 0);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, to.node));
        to.setSize(getFontSize());
        to.setText("Entries:");
        to.setFont(1);
        addSpacer(x, (int) (y - (float) getFontSpacer() / 2.f),
                (int) (quad.getWidth() / 1.1f), getTextSpacer());
    }

    /**
     * Initialize the textures.
     */
    protected final void initTextures() {
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
     * Adds a spacer.
     *
     * @param x position as {@link Integer}
     * @param y position as {@link Integer}
     * @param width spacer width as {@link Integer}
     * @param height spacer height as {@link Integer}
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
     * Returns y position for text.
     *
     * @param position Position index as {@link Integer}
     * @return y position as {@link Float}
     */
    private float getTextY(int position) {
        return quad.getHeight() / 2.f
                - ((float) getFontSize() * ((float) (position) * 1.1f));
    }

    /**
     * Draw entries
     */
    public void drawEntries() {
        if (isOpen()) {
            // TODO max limit for list
            data = AnnotationStorage.getInstance().getAnnotationData(video);
            log.debug("Draw {} entries.", data.getAnnotations().size());
            for (TextComponent e : entries) {
                e.detach();
            }
            entries = new ArrayList<>();
            for (int i = 0; i < data.getAnnotations().size(); i++) {
                TextComponent entryTextOverlay = new TextComponent(video, ActionName.LOAD);
                entryTextOverlay.setLocalTranslation(0, getTextY(i + 1), 0);
                Annotation annotation = data.getAnnotations().get(i);
                String timeCode = VideoUtils.getTimeCode(annotation.time_start);
                entryTextOverlay.setText(timeCode);
                entryTextOverlay.setFont(1);
                entryTextOverlay.setTouchable(true);
                entryTextOverlay.setAnnotationUUID(annotation.uuid);
                entryTextOverlay.setSize((int) (getFontSize()));
                entryTextOverlay.fadeIn((float) ANIMATION_DURATION / 125.f);
                GameTaskQueueManager.getManager().update(new AttachChildCallable(node, entryTextOverlay.node));
                entries.add(entryTextOverlay);
                entryTextOverlay = new TextComponent(video, ActionName.LOAD);
                entryTextOverlay.setLocalTranslation(0, getTextY(i + 1) - getFontSize() / 2, 0);
                String[] split = annotation.description.split(" ");
                if (split.length > 0) {
                    entryTextOverlay.setText(split[0]);
                } else if (split.length > 1) {
                    entryTextOverlay.setText(split[0] + " ...");
                }
                entryTextOverlay.setFont(1);
                entryTextOverlay.setSize((int) (getFontSize() / 2));
                entryTextOverlay.setTouchable(true);
                entryTextOverlay.setAnnotationUUID(annotation.uuid);
                entryTextOverlay.fadeIn((float) ANIMATION_DURATION / 125.f);
                GameTaskQueueManager.getManager().update(new AttachChildCallable(node, entryTextOverlay.node));
                entries.add(entryTextOverlay);
            }
        }
    }

    /**
     * Open animation of the DisplayInfoComponent
     */
    public void open() {
        open = true;
        st = OpenAnimation.getController(node, ANIMATION_DURATION, defaultScale);
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node, st));
        drawEntries();
    }

    /**
     * Resets the position and scale of the list field component.
     */
    public void reset() {
        st = ResetAnimation.getController(node, defaultScale, defaultRotation, defaultTranslation);
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node, st));
    }

    /**
     * Returns the {@link VideoComponent}
     *
     * @return {@link VideoComponent}
     */
    public VideoComponent getVideo() {
        return video;
    }

    /**
     * Returns open status of the list field.
     *
     * @return true if list field is open
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
        if (node.getControllers().contains(st)) {
            GameTaskQueueManager.getManager().update(new RemoveControllerCallable(node, st));
        }
        // fade out entry list
        for (TextComponent e : entries) {
            e.fadeOut((float) ANIMATION_DURATION / 1000);
            e.setTouchable(false);
        }
        // Close animation List Field
        st = CloseAnimation.getController(node, ANIMATION_DURATION, CloseAnimationType.LIST_FIELD);
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node, st));
    }
}
