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
package de.dfki.covida.visual.components.annotation;

import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import de.dfki.covida.covidacore.utils.HWRPostProcessing;
import de.dfki.covida.visual.components.TextOverlay;
import de.dfki.touchandwrite.action.DrawAction;
import de.dfki.touchandwrite.action.HWRAction;
import de.dfki.touchandwrite.action.TouchAction;
import de.dfki.touchandwrite.input.pen.hwr.HWRResultSet;
import de.dfki.touchandwrite.input.pen.hwr.HandwritingRecognitionEvent;
import de.dfki.touchandwrite.math.FastMath;
import de.dfki.touchandwrite.visual.components.ComponentType;
import java.util.ArrayList;
import java.util.HashMap;

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
        hwrResults = new ArrayList<>();
        mapping = new HashMap<>();
        entriesMapping = new ArrayList<>();
        data = new ArrayList<>();
        entryMap = new HashMap<>();
        titles = new ArrayList<>();
        hwr = new ArrayList<>();
        result = new HashMap<>();
        resultString = new HashMap<>();
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
        textureSpacer = TextureManager.loadTexture(getClass().getClassLoader()
                .getResource("media/textures/info_spacer.png"));
        tsSpacer.setTexture(textureSpacer);
    }

    @Override
    public void handwritingResult(HandwritingRecognitionEvent event) {
        if (!isOpen()) {
            return;
        }
        float x = event.getBoundingBox().getCenterOfGravity().x;
        float y = getDisplay().y - event.getBoundingBox().getCenterOfGravity().y;
        if (getLockState().onTop(-1, new Vector2f(x, y), this)) {
            hwrResults = HWRPostProcessing.getResult(event);
            update();
        }
    }

    @Override
    public void update() {
        int x = (int) (-width / 4.0f);
        for (int i = 0; i < hwrResults.size(); i++) {
            Node node = new Node("HWR Search Text Node");
            node.setLocalTranslation(x, getTextY(2 + i), 0);
            getNode().attachChild(node);
            TextOverlay textOverlay = new TextOverlay(node, this);
            textOverlay.registerWithInputHandler(touchInput);
            textOverlay.enableTouchGestures();
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
}
