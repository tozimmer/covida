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
package de.dfki.covida.visualjme2.components.annotation;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.shape.Quad;
import de.dfki.covida.visualjme2.animations.CloseAnimation;
import de.dfki.covida.visualjme2.animations.OpenAnimation;
import de.dfki.covida.visualjme2.components.TextOverlay;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import de.dfki.touchandwrite.math.FastMath;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Component which displays annotation dataList of VideoComponent.
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
    public AnnotationClipboard(String resource, int width, int height) {
        super("AnnotationSearch");
        this.width = width;
        this.height = height;
        this.image = resource;
        hwrResults = new ArrayList<>();
        mapping = new HashMap<>();
        entriesMapping = new ArrayList<>();
        entryMap = new HashMap<>();
        titles = new ArrayList<>();
        hwr = new ArrayList<>();
        super.setAlwaysOnTop(true);
        setLocalScale(new Vector3f(1, 1, 1));
        initTextures();
        textBeginY = (int) (quad.getHeight() / 2.75f);
        int x = (int) (0);
        TextOverlay caption = new TextOverlay(this);
        nodeHandler.addAttachChildRequest(this, caption);
        caption.setLocalTranslation(x, getTextY(0) - FONT_SIZE / 4.f, 0);
        caption.setSize((int) (FONT_SIZE * 1.5f));
        caption.setText("Clipboard:");
        caption.setFont(2);
        hwrResults.add("Test");
    }

    @Override
    public void update() {
        int x = (int) (-width / 4.0f);
        for (int i = 0; i < hwrResults.size(); i++) {
            TextOverlay textOverlay = new TextOverlay(this);
            textOverlay.setLocalTranslation(x, getTextY(2 + i), 0);
            nodeHandler.addAttachChildRequest(this, textOverlay);
            textOverlay.setText(hwrResults.get(i));
            textOverlay.setSize(FONT_SIZE);
            textOverlay.setFont(1);
            textOverlay.setColor(new ColorRGBA(0.75f, 0.75f, 0.75f, 0));
            textOverlay.fadeIn((float) i * 1.f + 1.f);
            hwr.add(textOverlay);
        }
    }

    @Override
    protected final float getTextY(int position) {
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
        spacerQuad.setRenderState(JMEUtils.initalizeBlendState());
        spacerQuad.updateRenderState();
        spacerQuad.setLocalTranslation(x, y, 0);
        spacerQuad.setLocalRotation(q);
        nodeHandler.addAttachChildRequest(this, spacerQuad);
    }

    @Override
    public void close() {
        detach = true;
        if (getControllers().contains(st)) {
            nodeHandler.addRemoveControllerRequest(this, st);
        }
        // Close animation
        st = CloseAnimation.getController(this, ANIMATION_DURATION, (float) getWidth(), (float) getHeight());
        nodeHandler.addAddControllerRequest(this, st);
    }

    @Override
    public void open() {
        detach = false;
        if (getControllers().contains(st)) {
            nodeHandler.addRemoveControllerRequest(this, st);
        }
        // Open animation
        st = OpenAnimation.getController(this, ANIMATION_DURATION);
        nodeHandler.addAddControllerRequest(this, st);
        update();
    }
}
