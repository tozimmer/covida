/*
 * VideoSlider.java
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
package de.dfki.covida.ui.components.video;

import com.jme.image.Texture;
import com.jme.image.Texture.WrapMode;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.angelfont.BitmapFont.Align;
import de.dfki.covida.ui.components.CovidaComponent;
import de.dfki.covida.ui.components.TextOverlay;
import de.dfki.touchandwrite.action.PenActionEvent;
import de.dfki.touchandwrite.action.TouchActionEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.DragEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.RotationGestureEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.ZoomEvent;
import de.dfki.touchandwrite.input.pen.event.ShapeEvent;
import de.dfki.touchandwrite.input.pen.hwr.HandwritingRecognitionEvent;
import de.dfki.touchandwrite.input.touch.event.TouchState;
import de.dfki.touchandwrite.visual.components.ComponentType;
import de.dfki.touchandwrite.visual.components.DrawingComponent;
import de.dfki.touchandwrite.visual.components.HWRSensitiveComponent;
import de.dfki.touchandwrite.visual.input.PenInputHandler;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Time slider for VideoComponent
 *
 * @author Tobias Zimmermann
 *
 */
public class VideoSlider extends CovidaComponent implements DrawingComponent,
        HWRSensitiveComponent {

    /**
     *
     */
    private static final long serialVersionUID = -668703009933127672L;
    private ArrayList<Quad> overlaySlider;
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 100;
    private VideoComponent video;
    private boolean isDragging;
    private static final int FONT_SIZE = 100;
    private TextOverlay textOverlay;
    private TextOverlay timeOverlay;
    private Node sliderNode;
    
    public VideoSlider(VideoComponent video, Node node) {
        super(ComponentType.COMPONENT_2D, "Video Slider Component", node);
        getNode().setLocalScale(
                new Vector3f((float) video.getWidth() / (float) getWidth(),
                (float) video.getWidth() / (float) getWidth(), 1));
        this.video = video;
        sliderNode = new Node("Slider Texture Node");
        getNode().attachChild(sliderNode);
        Node timeNode = new Node("Silder time node");
        timeNode.setLocalTranslation(-getWidth() * 0.45f, getHeight(), 0);
        sliderNode.attachChild(timeNode);
        timeOverlay = new TextOverlay(timeNode, this);
        timeOverlay.setAlign(Align.Left);
        Node textNode = new Node("Silder text node");
        textNode.setLocalTranslation(-getWidth() * 0.515f, getHeight(), 0);
        sliderNode.attachChild(textNode);
        textOverlay = new TextOverlay(textNode, this);
        textOverlay.setAlign(Align.Right);
    }
    
    @Override
    public void initComponent() {
        super.initComponent();
        super.setRootNode(video.getRootNode());
        initalizeOverlayQuads(initalizeBlendState());
    }
    
    private void initalizeOverlayQuads(BlendState alpha) {
        overlaySlider = new ArrayList<Quad>();
        ArrayList<String> textureList = new ArrayList<String>();
        textureList.add("media/textures/slider.png");
        for (int i = 0; i < textureList.size(); i++) {
            Texture overlaySliderTexture = TextureManager.loadTexture(
                    getClass().getClassLoader().getResource(
                    textureList.get(i)),
                    Texture.MinificationFilter.BilinearNearestMipMap,
                    Texture.MagnificationFilter.Bilinear);
            overlaySliderTexture.setWrap(WrapMode.Clamp);
            TextureState overlaySliderState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
            overlaySliderState.setTexture(overlaySliderTexture);
            overlaySlider.add(new Quad(
                    ("Overlay-Video-Slider-Image-Quad-0" + i), getWidth(),
                    getHeight()));
            overlaySlider.get(overlaySlider.size() - 1).setRenderState(
                    overlaySliderState);
            overlaySlider.get(overlaySlider.size() - 1).setRenderState(alpha);
            overlaySlider.get(overlaySlider.size() - 1).updateRenderState();
            sliderNode.attachChild(overlaySlider.get(overlaySlider.size() - 1));
        }
    }
    
    public void attachSlider() {
        if (!video.getRootNode().hasChild(getNode())) {
            video.getRootNode().attachChild(getNode());
        }
    }
    
    public void detachSlider() {
        if (video.getRootNode().hasChild(getNode())) {
            video.getRootNode().detachChild(getNode());
        }
    }
    
    @Override
    public void registerWithInputHandler(PenInputHandler input) {
        /**
         * Do Nothing
         */
    }
    
    @Override
    public void unRegisterWithInputHandler(PenInputHandler input) {
        /**
         * Do Nothing
         */
    }
    
    @Override
    public void draw(Queue<PenActionEvent> penEvent) {
        // TODO
    }
    
    @Override
    public boolean isSensitiveArea(int id, int x, int y) {
        if (getLockState().isTouchLocked(id)) {
            if (getLockState().getTouchLock(id) == getId()) {
                return true;
            }
        }
        return inArea(x, y);
    }

    /**
     *
     * @param percentage
     */
    public void updateSlider(float percentage) {
        move((float) getWidth() * percentage);
    }

    /**
     *
     * @param x
     */
    public void move(float x) {
        if (FastMath.abs(x) < getWidth()) {
            sliderNode.setLocalTranslation(x, 0, 0);
        }
    }
    
    @Override
    public void handwritingResult(HandwritingRecognitionEvent set) {
        /**
         * Do Nothing
         */
    }
    
    @Override
    public void draw(ShapeEvent shape) {
        /**
         * Do Nothing
         */
    }
    
    @Override
    public void setCurrentPenColor(Color color) {
        /**
         * Do Nothing
         */
    }
    
    @Override
    public void activatePenPressure() {
        /**
         * Do Nothing
         */
    }
    
    @Override
    public void deactivatePenPressure() {
        /**
         * Do Nothing
         */
    }
    
    @Override
    public boolean isPenPressureActivated() {
        return false;
    }
    
    @Override
    public float getPenThickness() {
        return 0;
    }
    
    @Override
    public void setPenThickness(float thickness) {
        /**
         * Do Nothing
         */
    }
    
    public VideoComponent getVideo() {
        return video;
    }
    
    @Override
    protected void dragAction(DragEvent event) {
        // TODO Auto-generated method stub
    }
    
    @Override
    protected void rotationAction(RotationGestureEvent event) {
        // TODO Auto-generated method stub
    }
    
    @Override
    protected void zoomAction(ZoomEvent event) {
        // TODO Auto-generated method stub
    }
    
    @Override
    protected int getHeight() {
        return HEIGHT;
    }
    
    @Override
    protected int getWidth() {
        return WIDTH;
    }
    
    @Override
    protected void touchDeadAction(int touchId) {
        this.isDragging = false;
        // TODO Fade out animation
        textOverlay.detach();
        timeOverlay.detach();
    }
    
    @Override
    protected void touchBirthAction(TouchActionEvent e) {
        touchAliveAction(e);
    }
    
    @Override
    protected void touchAliveAction(TouchActionEvent e) {
        if (!isDragging) {
            // TODO Animation
            // sliderNode.attachChild(this.overlayDrag);
            // animationHandlerThread = new
            // Thread(animationHandler);
            // animationHandlerThread.start();
            this.isDragging = true;
        }
        Vector3f result = video.getLocal(e.getX(), e.getY());
        if (FastMath.abs(result.x) < (video.getWidth() / 2.0f)) {
            // move(result.x+getWidth()/2.0f);
            video.setTimePosition((result.x + video.getWidth() / 2.0f)
                    / video.getWidth());
            textOverlay.setSize(FONT_SIZE);
            textOverlay.setText(video.getVideoProgress());
            timeOverlay.setSize(FONT_SIZE);
            timeOverlay.setText(video.getTimeCode(video.getTime()));
        }
    }
    
    @Override
    protected void touchDeadAction(TouchActionEvent e) {
        getLockState().removeTouchLock(e.getID());
    }
}
