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
package de.dfki.covida.visualjme2.components.fields;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Quad;
import com.jme.util.GameTaskQueueManager;
import de.dfki.covida.covidacore.components.IControlableComponent;
import de.dfki.covida.covidacore.data.AnnotationClass;
import de.dfki.covida.covidacore.data.AnnotationClassList;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.visualjme2.animations.CloseAnimation;
import de.dfki.covida.visualjme2.animations.OpenAnimation;
import de.dfki.covida.visualjme2.components.ClassButton;
import de.dfki.covida.visualjme2.components.FieldComponent;
import de.dfki.covida.visualjme2.components.TextComponent;
import de.dfki.covida.visualjme2.utils.AddControllerCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import de.dfki.covida.visualjme2.utils.RemoveControllerCallable;
import de.dfki.touchandwrite.math.FastMath;
import java.util.ArrayList;

/**
 * Component which displays annotation dataList of VideoComponent.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class AnnotationClasses extends FieldComponent implements
        IControlableComponent {

    /**
     * Creates a new instance of {@link AnnotationClipboard}
     *
     * @param resource Image resource location as {@link String}
     * @param width {@link Integer}
     * @param height {@link Integer}
     */
    public AnnotationClasses(String resource, int width, int height, int zOrder) {
        super("AnnotationClipboard", zOrder);
        this.width = width;
        this.height = height;
        this.image = resource;
        hwr = new ArrayList<>();
        super.setAlwaysOnTop(true);
        setLocalScale(new Vector3f(1, 1, 1));
        initTextures();
        textBeginY = (int) (quad.getWidth() / 2.f);
        TextComponent caption = new TextComponent(this, ActionName.NONE,
                getZOrder());
        attachChild(caption);
        caption.setLocalTranslation(0, getTextY(0), 0);
        caption.setSize((int) (FONT_SIZE * 1.5f));
        caption.setText("Classes:");
        caption.setFont(2);
        update();
    }

    @Override
    public final void update() {
        Vector3f local = new Vector3f(-getWidth() / 3.2f, getHeight() / 2 - 3 * FONT_SIZE, 0);
        for (AnnotationClass annotationClass : AnnotationClassList.load().annotationClasses) {
            ClassButton classButton = new ClassButton(annotationClass,
                    local,(int) (getWidth() / 3.1f), getHeight() / 6, getZOrder() - 1);
            attachChild(classButton);
            if (local.x < getWidth() / 3.1f) {
                local = local.add(getWidth()/ 3.1f, 0, 0);
            }else{
                local = local.add(-(2*(getWidth()/ 3.1f)), -getHeight()/5.5f, 0);
            }
        }
    }

    @Override
    protected final float getTextY(int position) {
        return textBeginY - 5 - FONT_SIZE * (position);
    }

    @Override
    protected void addSpacer(int x, int y, float angle, int width, int height) {
        Quaternion q = new Quaternion();
        q = q.fromAngleAxis(FastMath.DEG_TO_RAD * angle, new Vector3f(0, 0, 1));
        Quad spacerQuad = new Quad("Spacer", width, height);
        spacerQuad.setZOrder(getZOrder());
        spacerQuad.setRenderState(tsSpacer);
        spacerQuad.setRenderState(JMEUtils.initalizeBlendState());
        spacerQuad.updateRenderState();
        spacerQuad.setLocalTranslation(x, y, 0);
        spacerQuad.setLocalRotation(q);
        attachChild(spacerQuad);
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
        st = OpenAnimation.getController(node, ANIMATION_DURATION, defaultScale, defaultTranslation);
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node, st));
        update();
    }

    @Override
    public boolean toggle(ActionName action) {
        if (action.equals(ActionName.CLOSE)) {
            if (isOpen()) {
                close();
                return false;
            } else {
                open();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void hwrAction(String id, String hwr) {
        if (open) {
            overlay.clear();
            hwrResults.add(hwr);
            update();
        }
    }

    public void deleteDescription(TextComponent aThis) {
        hwrResults.remove(aThis.getText());
        aThis.detach();
    }
}
