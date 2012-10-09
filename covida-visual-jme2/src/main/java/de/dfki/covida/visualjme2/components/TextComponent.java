/*
 * TextComponent.java
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
package de.dfki.covida.visualjme2.components;

import com.jme.animation.SpatialTransformer;
import com.jme.math.FastMath;
import com.jme.math.TransformMatrix;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.util.GameTaskQueueManager;
import com.jmex.angelfont.BitmapFont.Align;
import com.jmex.angelfont.BitmapText;
import com.jmex.scene.TimedLifeController;
import de.dfki.covida.covidacore.components.IControlButton;
import de.dfki.covida.covidacore.components.IControlableComponent;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.visualjme2.animations.DragAnimation;
import de.dfki.covida.visualjme2.animations.ScaleAnimation;
import de.dfki.covida.visualjme2.utils.AddControllerCallable;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import de.dfki.covida.visualjme2.utils.DetachChildCallable;
import de.dfki.covida.visualjme2.utils.FontLoader;
import de.dfki.covida.visualjme2.utils.RemoveControllerCallable;

/**
 * TextComponent
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class TextComponent extends JMEComponent implements IControlButton {

    /**
     * Config
     */
    private ColorRGBA color = new ColorRGBA(1, 1, 1, 0);
    private Align align = Align.Center;
    /**
     * font size
     */
    private int size = 18;
    /**
     * font type
     */
    private int font = 0;
    /**
     * actual displayed string
     */
    private String text = "";
    private IControlableComponent component;
    private SpatialTransformer stDrag;
    private SpatialTransformer stScale;
    private FontLoader textOverlayData;
    private BitmapText txt;
    private boolean isDragging;
    private boolean enabled;
    private ActionName action;
    private float ANIMATIONTIME;

    /**
     * Displays Text
     *
     * @param component controlable componnet
     */
    public TextComponent(IControlableComponent component) {
        super(component.getName() + " Text Overlay");
        this.component = component;
        textOverlayData = FontLoader.getInstance();
        txt = new BitmapText(textOverlayData.getBitmapFont(font), false);
        init();
    }

    private void init() {
        stScale = ScaleAnimation.getController(node, 2.f, 2.f);
        stDrag = DragAnimation.getController(node);
        update();
    }

    public void update() {

        txt.setText(text);
        txt.setSize(size);
        txt.setLocalTranslation(0, (float) size / 2.f, 0);
        txt.setAlignment(align);
        try {
            txt.update();
        } catch (NullPointerException e) {
            log.error("", e);
        }
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, txt));
    }

    public void detach() {
        if (node.hasChild(txt)) {
            GameTaskQueueManager.getManager().update(new DetachChildCallable(node, txt));
        }
    }

    public void attach() {
        if (!node.hasChild(txt)) {
            GameTaskQueueManager.getManager().update(new AttachChildCallable(node, txt));
        }
    }

    public void fadeOut(float time) {
        txt.setDefaultColor(color);
        TimedLifeController fader = new TimedLifeController(time) {
            @Override
            public void updatePercentage(float percentComplete) {
                color.a = 1 - percentComplete;
            }
        };
        GameTaskQueueManager.getManager().update(new AddControllerCallable(txt, fader));
        fader.setActive(true);
    }

    public void fadeIn(float time) {
        txt.setDefaultColor(color);
        TimedLifeController fader = new TimedLifeController(time) {
            @Override
            public void updatePercentage(float percentComplete) {
                color.a = percentComplete;
            }
        };
        GameTaskQueueManager.getManager().update(new AddControllerCallable(txt, fader));
        fader.setActive(true);
    }

    /**
     * Sets font type:<p> 0 - youngtech <br> 1 - ubuntu with outline<br> 2 -
     * karabinE <br> 3 - ubuntu
     *
     * @param id
     */
    public void setFont(int id) {
        GameTaskQueueManager.getManager().update(new DetachChildCallable(node, txt));
        txt = new BitmapText(textOverlayData.getBitmapFont(id), false);
        update();
    }

    public void setAlign(Align align) {
        this.align = align;
        update();
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    public void setSize(int size) {
        this.size = size;
        update();
    }

    public void setText(String text) {
        if (text == null) {
            return;
        }
        this.text = text;
        update();
    }

    @Override
    protected int getWidth() {
        if (text == null) {
            return 0;
        }
        return (int) (((float) text.length() * (float) size) / 2.f);
    }

    @Override
    protected int getHeight() {
        return size;
    }

    public void setColor(ColorRGBA color) {
        txt.setDefaultColor(color);
        update();
    }

    public int getFontSize() {
        return size;
    }

    public boolean isDragging() {
        return isDragging;
    }

    public void startScaleAnimation() {
        if (node.getControllers().contains(stScale)) {
            GameTaskQueueManager.getManager().update(new RemoveControllerCallable(node, stScale));
            stScale = ScaleAnimation.getController(node, 2.0f, 2.0f);
        }
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node, stScale));
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public void dragAction(int id, int x, int y, int dx, int dy) {
        Vector3f translation = this.getLocalTranslation();
        Vector3f d = new Vector3f(dx, -dy, 0);
//        d.subtract(node.getWorldTranslation(), d);
        d = d.divideLocal(node.getWorldScale());
        d = node.getWorldRotation().inverse().mult(d, d);
        translation = translation.add(d);
        node.setLocalTranslation(translation);
    }

    @Override
    public void touchBirthAction(int id, int x, int y) {
        if (enabled && getParent() != null) {
            SpatialTransformer controller = ScaleAnimation.getController(txt,
                    2.0f, ANIMATIONTIME);
            GameTaskQueueManager.getManager().update(new AddControllerCallable(
                    txt, controller));
        }
    }

    @Override
    public void touchDeadAction(int id, int x, int y) {
        log.debug(getName() + " touch");
        if (enabled && getParent() != null) {
            SpatialTransformer controller = ScaleAnimation.getController(txt,
                    1.0f, ANIMATIONTIME);
            GameTaskQueueManager.getManager().update(new AddControllerCallable(
                    txt, controller));
            if (inArea(x, y)) {
                toggle();
            }
        }
    }

    @Override
    public void toggle() {
        log.debug(action.toString());
        setActive(component.toggle(action));
    }

    @Override
    public void setActive(boolean activated) {
    }

    @Override
    public boolean getActive() {
        return false;
    }
}
