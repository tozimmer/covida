/*
 * AnnotationSearchField.java
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
import com.jme.util.GameTaskQueueManager;
import de.dfki.covida.covidacore.components.IControlableComponent;
import de.dfki.covida.covidacore.data.Annotation;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.visualjme2.animations.CloseAnimation;
import de.dfki.covida.visualjme2.animations.OpenAnimation;
import de.dfki.covida.visualjme2.components.FieldComponent;
import de.dfki.covida.visualjme2.components.TextComponent;
import de.dfki.covida.visualjme2.utils.AddControllerCallable;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import de.dfki.covida.visualjme2.utils.RemoveControllerCallable;
import de.dfki.touchandwrite.math.FastMath;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Component which displays annotation dataList of VideoComponent.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class AnnotationSearchField extends FieldComponent implements
        IControlableComponent {

    /**
     * Map with all search results mapped on video ids
     */
    protected HashMap<Integer, ArrayList<TextComponent>> entryMap;
    /**
     * Map with all search results mapped to annotation ids
     */
    protected ArrayList<Map<Integer, Annotation>> entriesMapping;
    /**
     * List of entries {@link TextComponent}
     */
    protected ArrayList<TextComponent> entries;

    /**
     * Creates a new instance of {@link AnnotationSearchField}
     * 
     * @param resource Image resource location as {@link String}
     * @param width {@link Integer}
     * @param height {@link Integer}
     */
    public AnnotationSearchField(String resource, int width, int height) {
        super("AnnotationSearch");
        this.width = width;
        this.height = height;
        this.image = resource;
        setDrawable(true);
        hwrResults = new ArrayList<>();
        entriesMapping = new ArrayList<>();
        entryMap = new HashMap<>();
        entries = new ArrayList<>();
        hwr = new ArrayList<>();
        super.setAlwaysOnTop(true);
        setLocalScale(new Vector3f(1, 1, 1));
        initTextures();
        textBeginY = (int) (quad.getHeight() / 2.0f);
        int x = (int) (0);
        TextComponent caption = new TextComponent(this);
        caption.setLocalTranslation(x, getTextY(0) - FONT_SIZE / 4.f, 0);
        caption.setSize((int) (FONT_SIZE * 1.5f));
        caption.setText("Write here for annotation search:");
        caption.setFont(2);
        caption.setTouchable(true);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, caption.node));
        addSpacer(x, (int) (getTextY(0) - FONT_SIZE), 0,
                (int) (quad.getWidth() / 1.1f), TEXT_SPACER);
        x = (int) (getWidth() / 9.f);
        addSpacer(x, 0, 90, (int) (quad.getHeight() / 1.1f), TEXT_SPACER);
        x = (int) -(getWidth() / 4.f);
        addSpacer(x, 0, 90, (int) (quad.getHeight() / 1.1f), TEXT_SPACER);
    }

    @Override
    public void clearHwrResults() {
        hwrResults.clear();
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
    protected final void addSpacer(int x, int y, float angle, int width, int height) {
        Quaternion q = new Quaternion();
        q = q.fromAngleAxis(FastMath.DEG_TO_RAD * angle, new Vector3f(0, 0, 1));
        Quad spacerQuad = new Quad("Spacer", width, height);
        spacerQuad.setRenderState(tsSpacer);
        spacerQuad.setRenderState(JMEUtils.initalizeBlendState());
        spacerQuad.updateRenderState();
        spacerQuad.setLocalTranslation(x, y, 0);
        spacerQuad.setLocalRotation(q);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, spacerQuad));
    }

    @Override
    public void close() {
        open = false;
        if (node.getControllers().contains(st)) {
            GameTaskQueueManager.getManager().update(new RemoveControllerCallable(node, st));
        }
        // Close animation
        st = CloseAnimation.getController(node, ANIMATION_DURATION, (float) getWidth(), (float) getHeight());
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node, st));
    }

    @Override
    public void open() {
        open = true;
        if (node.getControllers().contains(st)) {
            GameTaskQueueManager.getManager().update(new RemoveControllerCallable(node, st));
        }
        // Open animation
        st = OpenAnimation.getController(node, ANIMATION_DURATION);
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node, st));
        update();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public boolean toggle(ActionName action) {
        if (isOpen()) {
            close();
            return false;
        } else {
            open();
            return true;
        }
    }

    @Override
    public void hwrAction(String hwr) {
        if (open) {
            hwrResults.clear();
            hwrResults.add(hwr);
            update();
        }
    }

    @Override
    protected void update() {
        for (int i = 0; i < hwrResults.size(); i++) {
            int x = (int) (-width / 2.5f);
            TextComponent hwrText = new TextComponent(this);
            hwrText.setLocalTranslation(x, getTextY(2 + i), 0);
            GameTaskQueueManager.getManager().update(new AttachChildCallable(node, hwrText.node));
            hwr.add(hwrText);
            hwr.get(i).setText(hwrResults.get(i));
            hwr.get(i).setSize(FONT_SIZE);
            hwr.get(i).setFont(1);
            hwr.get(i).setColor(new ColorRGBA(0.75f, 0.75f, 0.75f, 0));
            hwr.get(i).fadeIn((float) i * 1.f + 1.f);
        }
    }
}