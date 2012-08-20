/*
 * AnnotationClipboard.java
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
package de.dfki.covida.components.ui.annotation;

import com.jme.animation.SpatialTransformer;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import de.dfki.covida.ui.components.TextOverlay;
import de.dfki.covida.data.VideoAnnotationData;
import de.dfki.covida.components.ui.video.VideoComponent;
import de.dfki.touchandwrite.action.DrawAction;
import de.dfki.touchandwrite.action.HWRAction;
import de.dfki.touchandwrite.action.TouchAction;
import de.dfki.touchandwrite.input.pen.hwr.HWRResultSet;
import de.dfki.touchandwrite.input.pen.hwr.HandwritingRecognitionEvent;
import de.dfki.touchandwrite.math.FastMath;
import de.dfki.touchandwrite.visual.components.ComponentType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Component which displays annotation data of VideoComponent.
 *
 * @author Tobias Zimmermann
 *
 */
public class AnnotationClipboard extends Field {

    /**
     * Search field constructor
     *
     * @param resource
     * @param video
     * @param listField
     * @param id
     * @param node
     * @param width
     * @param height
     */
    public AnnotationClipboard(String resource, Node node, int width, int height) {
        super(ComponentType.COMPONENT_2D, "AnnotationSearch", node);
        this.width = width;
        this.height = height;
        this.image = resource;
        hwrResults = new ArrayList<String>();
        hwrEvents = new ArrayList<HandwritingRecognitionEvent>();
        mapping = new HashMap<Integer, Integer>();
        entriesMapping = new ArrayList<Map<Integer, Integer>>();
        data = new ArrayList<VideoAnnotationData>();
        entryMap = new HashMap<Integer, ArrayList<TextOverlay>>();
        titles = new ArrayList<TextOverlay>();
        hwr = new ArrayList<TextOverlay>();
        result = new HashMap<Integer, ArrayList<Integer>>();
        resultString = new HashMap<Integer, ArrayList<String>>();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.dfki.touchandwrite.visual.components.TouchAndWriteComponent#initComponent
     * ()
     */
    @Override
    public void initComponent() {
        super.initComponent();
        super.setAlwaysOnTop(true);
        super.setRootNode(getNode().getParent());
        getNode().setLocalScale(new Vector3f(1, 1, 1));
        initTextures();
        textBeginY = (int) (quad.getHeight() / 2.75f);
        this.drawAction = new DrawAction(this);
        this.touchAction = new TouchAction(this);
        this.hwrAction = new HWRAction(this);
        int x = (int) (0);
        Node node = new Node("AnnotationSearch Entry Node");
        getNode().attachChild(node);
        node.setLocalTranslation(x, getTextY(0) - FONT_SIZE / 4.f, 0);
        TextOverlay caption = new TextOverlay(node, this);
        caption.setSize((int) (FONT_SIZE * 1.5f));
        caption.setText("Clipboard:");
        caption.setFont(2);
        hwrResults.add("Test");
//        addSpacer(x, (int) (getTextY(0) - FONT_SIZE), 0,
//                (int) (quad.getWidth() / 1.1f), TEXT_SPACER);
//        x = (int) (getWidth() / 9.f);
//        addSpacer(x, 0, 90, (int) (quad.getHeight() / 1.1f), TEXT_SPACER);
//        x = (int) -(getWidth() / 4.f);
//        addSpacer(x, 0, 90, (int) (quad.getHeight() / 1.1f), TEXT_SPACER);
    }

    @Override
    protected void initTextures() {
        // ---- Background Texture state initialization ----
        ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setCorrectionType(TextureState.CorrectionType.Perspective);
        ts.setEnabled(true);
        texture = TextureManager.loadTexture(getClass().getClassLoader().getResource(image));
        ts.setTexture(texture);
        quad = new Quad("Display image quad", width, height);
        quad.setRenderState(ts);
        quad.setRenderState(this.initalizeBlendState());
        quad.updateRenderState();
        getNode().attachChild(quad);
        // Spacer
        tsSpacer = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        tsSpacer.setCorrectionType(TextureState.CorrectionType.Perspective);
        tsSpacer.setEnabled(true);
        textureSpacer = TextureManager.loadTexture(getClass().getClassLoader().getResource("media/textures/info_spacer.png"));
        tsSpacer.setTexture(textureSpacer);
    }

    @Override
    public void handwritingResult(HandwritingRecognitionEvent event) {
        // TODO pen id!
//        log.debug("HWR Event: " + event.toString());
        if (!isOpen()) {
            return;
        }
        if (getLockState().onTop(
                -1,
                new Vector2f(event.getBoundingBox().getCenterOfGravity().x,
                (getDisplay().y - event.getBoundingBox().getCenterOfGravity().y)), this)) {
            this.hwrEvents.add(event);
            int size = event.getHWRResultSet().getWords().size();
            for (int i = 0; i < size; i++) {
                if(hwrResults.contains(event.getHWRResultSet().getWords().get(i).getCandidates().peek().getRecogntionResult()))
                this.hwrResults.add(event.getHWRResultSet().getWords().get(i).getCandidates().peek().getRecogntionResult());
            }
            update();
        }
    }

    public void update() {
        int x = (int) (-width / 4.0f);
        
        for (int i = 0; i < hwrResults.size(); i++) {
            Node node = new Node("HWR Search Text Node");
            node.setLocalTranslation(x, getTextY(2 + i), 0);
            getNode().attachChild(node);
            TextOverlay textOverlay = new TextOverlay(node, this);
            textOverlay.setDragEnabled(true);
            textOverlay.registerWithInputHandler(touchInput);
            textOverlay.setText(hwrResults.get(i));
            textOverlay.setSize(FONT_SIZE);
            textOverlay.setFont(1);
            textOverlay.setColor(new ColorRGBA(0.75f, 0.75f, 0.75f, 0));
            textOverlay.fadeIn((float) i * 1.f + 1.f);
            hwr.add(textOverlay);
        }
    }

    @Override
    protected float getTextY(int position) {
        return textBeginY - TEXT_SPACER - FONT_SIZE * (position)
                - (float) FONT_SIZE / 2.f;
    }

    /**
     * Checks the hwr result and chooses the best result.
     *
     * @param hwrResultSet
     * @return
     */
    @Override
    protected String checkHWRResult(HWRResultSet hwrResultSet) {
        hwrResultSet.getWords();
        return hwrResultSet.topResult();
    }

    /**
     *
     * @param x
     * @param y
     * @param angle - angle in degree
     * @param width
     * @param height
     */
    @Override
    protected void addSpacer(int x, int y, float angle, int width, int height) {
        Quaternion q = new Quaternion();
        q = q.fromAngleAxis(FastMath.DEG_TO_RAD * angle, new Vector3f(0, 0, 1));
        Quad spacerQuad = new Quad("Spacer", width, height);
        spacerQuad.setRenderState(tsSpacer);
        spacerQuad.setRenderState(initalizeBlendState());
        spacerQuad.updateRenderState();
        spacerQuad.setLocalTranslation(x, y, 0);
        spacerQuad.setLocalRotation(q);
        getNode().attachChild(spacerQuad);
    }

    /**
     * Closes the DisplayInfoComponent
     */
    @Override
    public void close() {
        detach = true;
        for (int i = 0; i < getNode().getControllerCount(); i++) {
            getNode().removeController(i);
        }

        SpatialTransformer st = new SpatialTransformer(1);
        // Close animation (Info Field)
        st.setObject(getNode(), 0, -1);
        st.setPosition(0, 0.f, new Vector3f(getNode().getLocalTranslation()));
        st.setPosition(0, 0.5f, new Vector3f(-(float) getHeight() / 2.f,
                -(float) getHeight() / 2.f, 0));
        st.interpolateMissing();
        getNode().addController(st);
        resetHandler = new DetachHandler(this, 500);
        resetHandlerThread = new Thread(resetHandler);
        resetHandlerThread.start();
    }
}
