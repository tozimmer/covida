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
import com.jme.image.Texture.WrapMode;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.TextureManager;
import com.jmex.awt.swingui.ImageGraphics;
import de.dfki.covida.covidacore.data.Annotation;
import de.dfki.covida.covidacore.data.AnnotationStorage;
import de.dfki.covida.covidacore.data.DisplayFieldType;
import de.dfki.covida.covidacore.data.ShapePoints;
import de.dfki.covida.covidacore.utils.VideoUtils;
import de.dfki.covida.visualjme2.animations.CloseAnimation;
import de.dfki.covida.visualjme2.animations.CloseAnimationType;
import de.dfki.covida.visualjme2.animations.OpenAnimation;
import de.dfki.covida.visualjme2.animations.ResetAnimation;
import de.dfki.covida.visualjme2.components.CovidaJMEComponent;
import de.dfki.covida.visualjme2.components.CovidaTextComponent;
import de.dfki.covida.visualjme2.components.video.VideoComponent;
import de.dfki.covida.visualjme2.utils.AddControllerCallable;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import de.dfki.covida.visualjme2.utils.DetachChildCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import de.dfki.covida.visualjme2.utils.RemoveControllerCallable;
import de.dfki.touchandwrite.shape.ShapeType;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 * Component which displays annotation data of VideoComponent.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class ListFieldComponent extends CovidaJMEComponent {

    /**
     * Configuration
     */
    public ColorRGBA color = new ColorRGBA(1, 1, 1, 0);
    static ColorRGBA defaultColor = new ColorRGBA(1, 1, 1, 1);
    static ColorRGBA activeColor = new ColorRGBA(1, 0, 0, 1);
    static ColorRGBA selectedColor = new ColorRGBA(0, 1, 0, 1);
    static final int ANIMATION_DURATION = 750;
    private static final int DEFAULT_FONT_SIZE = 18;
    static final int DEFAULT_WIDTH = 112;
    static final int DEFAULT_HEIGHT = 250;
    private int textBeginY;
    static int WIDTH = 112;
    static int HEIGHT = 250;
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
    private ShapePoints shapePoints;
    /**
     * Drawing will be done with Java2D.
     */
    protected ImageGraphics g2d;
    private boolean locked;
    private VideoComponent video;
    private String title;
    private long time_start;
    private long time_end;
    private ArrayList<CovidaTextComponent> entries;
    private ArrayList<CovidaTextComponent> descriptionText;
    private TextureState tsSpacer;
    private Texture textureSpacer;
    private ShapeType shapeType;
    private CovidaTextComponent titleTextOverlay;
    private CovidaTextComponent timeOverlay;
    private CovidaTextComponent timeTextOverlay;
    private CovidaTextComponent descriptionOverlay;
    private boolean open;
    private ArrayList<Long> times;
    private int descriptionBeginY;
    /*
     * Overlay
     */
    private CovidaTextComponent textOverlay;
    private static final int FONT_SIZE = 30;
    protected Quad overlayDefault;
    private SpatialTransformer st;

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
    public ListFieldComponent(String resource, VideoComponent video, int width, int height) {
        super("DisplayFieldComponent");
        this.video = video;
        WIDTH = width;
        HEIGHT = height;
        defaultScale = new Vector3f(getLocalScale().x, getLocalScale().y, getLocalScale().z);
        defaultRotation = new Quaternion(getLocalRotation().x,
                getLocalRotation().y, getLocalRotation().z,
                getLocalRotation().w);
        defaultTranslation = new Vector3f(getLocalTranslation().x,
                getLocalTranslation().y, getLocalTranslation().z);
        image = resource;
        entries = new ArrayList<>();
    }

    public DisplayFieldType getType() {
        return DisplayFieldType.LIST;
    }

    private int getFontSize() {
        return (int) (1.2f * (float) DEFAULT_FONT_SIZE * ((float) getHeight() / (float) DEFAULT_HEIGHT));
    }

    private int getFontSpacer() {
        return (int) ((float) getFontSize() * 0.8f);
    }

    private int getTextSpacer() {
        return (int) ((float) getFontSize() / 5.f);
    }

    private int getCharacterLimit() {
        return (int) ((float) DEFAULT_CHARACTER_LIMIT * ((float) getFontSize() / (float) DEFAULT_FONT_SIZE));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.dfki.touchandwrite.visual.components.TouchAndWriteComponent#initComponent
     * ()
     */
    public void initComponent() {
        initTextures();
        textBeginY = (int) (quad.getHeight() / 12.f);
        textOverlay = new CovidaTextComponent(this);
        textOverlay.setSize(FONT_SIZE);
        int x = (int) (0);
        float y = getTextY(0);
        CovidaTextComponent to = new CovidaTextComponent(this);
        to.setLocalTranslation(x, y, 0);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, to.node));
        to.setSize(getFontSize());
        to.setText("Entries:");
        to.setFont(1);
        addSpacer(x, (int) (y - (float) getFontSpacer() / 2.f),
                (int) (quad.getWidth() / 1.1f), getTextSpacer());
    }

    protected final void initTextures() {
        // ---- Background Texture state initialization ----
        ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setCorrectionType(TextureState.CorrectionType.Perspective);
        ts.setEnabled(true);
        texture = TextureManager.loadTexture(getClass().getClassLoader().getResource(image));
        ts.setTexture(texture);

        quad = new Quad("Display image quad", WIDTH, HEIGHT);
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

    public void setAnnotationData(Annotation annotation) {
        setTime(annotation.time_start);
        setTitle(AnnotationStorage.getInstance().getAnnotationData(video).title);
        setVideoSource(AnnotationStorage.getInstance().getAnnotationData(video).videoSource);
        setShapePoints(annotation.shapePoints);
        setShapeType(annotation.shapeType);
    }

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

    private void setTime(long time) {
        if (timeOverlay != null && timeTextOverlay != null) {
            this.time_start = time;
            this.time_end = time + 10;
            timeTextOverlay.setText(VideoUtils.getTimeCode(time));
//			if (video.getTimeCode(time).length() > getCharacterLimit()) {
//				float factor = (float) video.getTimeCode(time).length() / getCharacterLimit();
//				timeTextOverlay.setSize((int) ((float) getFontSize() / factor));
//			}	
        }
    }

    public void drawHwrResult(ArrayList<String> hwrResults) {
        if (descriptionOverlay != null && descriptionText != null) {
            descriptionOverlay.setText("Description:");
            descriptionOverlay.setSize(getFontSize());
            int count = hwrResults.size() + descriptionText.size();
            int start = descriptionText.size();
            if (start == 0) {
                descriptionBeginY = (int) (textBeginY - (float) getFontSpacer());
            }
            for (int i = start; i < count; i++) {
                descriptionBeginY = (int) (descriptionBeginY - (float) getFontSpacer() * 0.9f);
                CovidaTextComponent descriptionTextOverlay = new CovidaTextComponent(this);
                descriptionTextOverlay.setLocalTranslation(0, descriptionBeginY, 0);
                GameTaskQueueManager.getManager().update(new AttachChildCallable(node, descriptionTextOverlay.node));
                descriptionText.add(descriptionTextOverlay);
                descriptionText.get(descriptionText.size() - 1).setText(
                        hwrResults.get(i - start));
                descriptionText.get(descriptionText.size() - 1).setFont(1);
                descriptionText.get(descriptionText.size() - 1).setSize(
                        getFontSize());
                if (hwrResults.get(i - start).length() > getCharacterLimit()) {
                    float factor = (float) hwrResults.get(i - start).length()
                            / getCharacterLimit();
                    descriptionText.get(descriptionText.size() - 1).setSize(
                            (int) ((float) getFontSize() / factor));
                }
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

    private void addSpacer(int x, int y, int width, int height) {
        Quad spacerQuad = new Quad("Spacer", width, height);
        spacerQuad.setRenderState(tsSpacer);
        spacerQuad.setRenderState(JMEUtils.initalizeBlendState());
        spacerQuad.updateRenderState();
        spacerQuad.setLocalTranslation(x, y, 0);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, spacerQuad));
    }

    /**
     * Resets textNode
     */
    public void resetInfo() {
        for (CovidaTextComponent e : entries) {
            e.setColor(defaultColor);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public int getHeight() {
        return HEIGHT;
    }

    /**
     *
     * @return
     */
    @Override
    public int getWidth() {
        return WIDTH;
    }

    /**
     * Closes the DisplayInfoComponent
     */
    @Override
    public void close() {
        open = false;
        if (node.getControllers().contains(st)) {
            GameTaskQueueManager.getManager().update(new RemoveControllerCallable(node, st));
        }
        // fade out entry list
        for (CovidaTextComponent e : entries) {
            e.fadeOut((float) ANIMATION_DURATION / 1000);
        }
        // Close animation List Field
        st = CloseAnimation.getController(node, ANIMATION_DURATION, CloseAnimationType.LIST_FIELD);
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node, st));
        resetInfo();
    }

    /**
     * Saves annotation data from the InfoField
     */
    public void save() {
        log.debug("Save begin");
        open = false;
        if (node.getControllers().contains(st)) {
            GameTaskQueueManager.getManager().update(new RemoveControllerCallable(node, st));
        }
    }

    private float getTextY(int position) {
        return quad.getHeight() / 2.1f
                - ((float) getFontSize() * ((float) (position + 1) * 1.1f));
    }

    public void drawEntries() {
        if (times != null) {
            // TODO max limit for list
            log.debug("draw entries: " + times);
            for (CovidaTextComponent e : entries) {
                e.detach();
            }
            entries = new ArrayList<>();
            for (int i = 0; i < times.size(); i++) {
                CovidaTextComponent entryTextOverlay = new CovidaTextComponent(this);
                entryTextOverlay.setLocalTranslation(0, getTextY(i + 1), 0);
                GameTaskQueueManager.getManager().update(new AttachChildCallable(node, entryTextOverlay.node));
                entries.add(entryTextOverlay);
                log.debug("draw Entry: " + VideoUtils.getTimeCode(times.get(i)));
                entries.get(i).setText(VideoUtils.getTimeCode(times.get(i)));
                entries.get(i).setFont(1);
                entries.get(i).setSize((int) (getFontSize()));
                entries.get(i).fadeIn((float) ANIMATION_DURATION / 125.f);
            }
        }
    }

    /**
     *
     * @param times
     */
    public void updateEntries(ArrayList<Long> times) {
        this.times = times;
    }

    public String getDescription() {
        String descriptions = "";
        for (CovidaTextComponent description : descriptionText) {
            descriptions = descriptions + description.getText() + " ";
        }
        return descriptions;
    }

    public void lockPosition(boolean lock) {
        this.locked = lock;
    }

    /**
     * Detach DisplayInfoComponent
     */
    public void detach() {
        if (this.getParent() != null) {
            GameTaskQueueManager.getManager().update(new DetachChildCallable(getParent(), node));
            resetNode();
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
     *
     * @param x
     * @param y
     * @return <code>Integer</code> , entryId or -1 if there is no entry on x,y
     */
    public int getSelectedEntry(int x, int y) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).inArea(x, y)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets the active entry on the ListField
     *
     * @param entryID
     */
    public void setActiveEntry(int entryID) {
        if (entryID > -1 && entryID < entries.size()) {
            for (CovidaTextComponent e : entries) {
                e.setColor(defaultColor);
            }
            entries.get(entryID).setColor(activeColor);
        }
    }

    /**
     * Sets the selected entry on the ListField
     *
     * @param entryID
     */
    public void setSelectedEntry(int entryID) {
        if (entryID > -1 && entryID < entries.size() + 1) {
            for (CovidaTextComponent e : entries) {
                e.setColor(defaultColor);
            }
            entries.get(entryID).setColor(selectedColor);
            video.loadAnnotationData(entryID);
        }
    }

    // /**
    // * Loads annotation data from <code>XML</code> file
    // *
    // */
    // private void loadFromXML(int videoId){
    // ArrayList<ArrayList<Point>> points = new ArrayList<ArrayList<Point>>();
    // if(xmlParser.getIndex(videoId)!=null){
    // times_start = xmlParser.getStart(videoId);
    // times_end = xmlParser.getEnd(videoId);
    // titles = new ArrayList<String>();
    // for (int i=0; i<times_start.size(); i++){
    // titles.add(video.getName());
    // }
    // descriptions = xmlParser.getDescriptions(videoId);
    // points = xmlParser.getPoints(videoId);
    // spatials = new ArrayList<Spatial>();
    // Vector2f tempMax = new Vector2f(0,0);
    // Vector2f tempMin = new Vector2f(9999,9999);
    // List<Point> pl = new ArrayList<Point>();
    // for(int i=0; i<points.size(); i++){
    // for(Point point : points.get(i)){
    // pl.add(point);
    // if (point.x<tempMin.x){
    // tempMin.x = point.x;
    // }
    // if (point.x>tempMax.x){
    // tempMax.x = point.x;
    // }
    // if (point.y<tempMin.y){
    // tempMin.y = point.y;
    // }
    // if (point.y>tempMax.y){
    // tempMax.y = point.y;
    // }
    // }
    // // this.spatials.add(ShapeUtils.toPolygon(polygon));
    // }
    // shapePointList.add(pl);
    // listField.updateEntries(times_start);
    // }
    // }

    private void setShapePoints(ShapePoints shapePoints) {
        this.shapePoints = shapePoints;
    }

    private void setVideoSource(String videoSource) {
        this.videoSource = videoSource;
    }

    public void reset() {
        if (node.getControllers().contains(st)) {
            GameTaskQueueManager.getManager().update(new RemoveControllerCallable(node, st));
        }
        st = ResetAnimation.getController(node, defaultScale, defaultRotation, defaultTranslation);
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node, st));
    }

    public VideoComponent getVideo() {
        return video;
    }

    private void setShapeType(ShapeType shapeType) {
        this.shapeType = shapeType;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isLocked() {
        return locked;
    }

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
}
