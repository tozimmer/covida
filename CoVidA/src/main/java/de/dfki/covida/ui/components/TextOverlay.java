/*
 * TextOverlay.java
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
package de.dfki.covida.ui.components;

import com.jme.animation.SpatialTransformer;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jmex.angelfont.BitmapFont.Align;
import com.jmex.angelfont.BitmapText;
import com.jmex.scene.TimedLifeController;
import de.dfki.touchandwrite.action.GestureAction;
import de.dfki.touchandwrite.action.GestureActionEvent;
import de.dfki.touchandwrite.action.TouchActionEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.DragEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.RotationGestureEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.ZoomEvent;
import de.dfki.touchandwrite.input.touch.gesture.TouchGestureEvent;
import de.dfki.touchandwrite.visual.components.ComponentType;
import de.dfki.touchandwrite.visual.components.GestureSensitiveComponent;
import de.dfki.touchandwrite.visual.input.TouchInputHandler;
import org.apache.log4j.Logger;


public class TextOverlay extends CovidaComponent implements GestureSensitiveComponent {

    /**
     *
     */
    private static final long serialVersionUID = 966923412338994711L;
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
    private Logger log = Logger.getLogger(TextOverlay.class);
    private CovidaComponent component;
    private SpatialTransformer st;
    private RemoveControllerHandler removeHandler;
    private Thread removeHandlerThread;
    private TextOverlayData textOverlayData;
    private BitmapText txt;
    private Thread animationHandlerThread;
    private AnimationHandler animationHandler;
    private DragAnimationHandler dragAnimationHandler;
    private Thread dragAnimationHandlerThread;
    private boolean isDragging;
//    private final GestureAction gestureAction;
    private TouchInputHandler touchInput;

    /**
     * Displays Text
     *
     * @param node - Node which the Text should be attached
     * @param rootNode - rootNode for onTop detection
     */
    public TextOverlay(Node node, CovidaComponent component) {
        super(ComponentType.COMPONENT_2D, "Text Overlay Component", node);
        super.initComponent();
//        this.gestureAction = new GestureAction(this);
        this.component = component;
        super.setRootNode(this.component.getRootNode());
        textOverlayData = TextOverlayData.getInstance();
        st = new SpatialTransformer(1);
        getNode().addController(st);
        st.setObject(getNode(), 0, -1);
        txt = new BitmapText(textOverlayData.getBitmapFont(font), false);
        getNode().setCullHint(Spatial.CullHint.Never);
        getNode().setRenderQueueMode(Renderer.QUEUE_ORTHO);
        getNode().setTextureCombineMode(TextureCombineMode.CombineClosest);
        removeHandler = new RemoveControllerHandler(getNode(), 1);
        removeHandlerThread = new Thread(removeHandler);
        log.debug("TextOverlay created ID: " + getId());
        init();
    }

    private void init() {
        initDragOverlay(initalizeBlendState());
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
            log.error(e);
        }
        getNode().attachChild(txt);
    }

    public void detach() {
        txt.removeFromParent();
    }

    public void enableTouchGestures() {
        touchInput.addAction(new GestureAction((this)));
    }

    public void attach() {
        getNode().attachChild(txt);
    }

    public void fadeOut(float time) {
        txt.setDefaultColor(color);
        TimedLifeController fader = new TimedLifeController(time) {

            /**
             *
             */
            private static final long serialVersionUID = 6426373978649178012L;

            public void updatePercentage(float percentComplete) {
                color.a = 1 - percentComplete;
            }
        };
        txt.addController(fader);
        fader.setActive(true);
    }

    public void fadeIn(float time) {
        txt.setDefaultColor(color);
        TimedLifeController fader = new TimedLifeController(time) {

            /**
             *
             */
            private static final long serialVersionUID = 6426373978649178012L;

            public void updatePercentage(float percentComplete) {
                color.a = percentComplete;
            }
        };
        txt.addController(fader);
        fader.setActive(true);
    }

    public void scaleAnimation(float scale, float time) {
        st.setScale(0, 0f, new Vector3f(getNode().getLocalScale()));
        st.setScale(0, time / 2.f, new Vector3f(scale, 2.0f, 1));
        st.setScale(0, time, new Vector3f(1.0f, 1, 1));
        st.interpolateMissing();
        if (!removeHandlerThread.isAlive()) {
            removeHandlerThread = new Thread(removeHandler);
            removeHandlerThread.start();
        }
    }

    public void attach(Node node) {
        node.attachChild(getNode());
    }

    /**
     * Sets font type:<p> 0 - youngtech <br> 1 - ubuntu with outline<br> 2 -
     * karabinE <br> 3 - ubuntu
     *
     * @param id
     */
    public void setFont(int id) {
        txt.removeFromParent();
        txt = new BitmapText(textOverlayData.getBitmapFont(id), false);
        update();
    }

    public void setAlign(Align align) {
        this.align = align;
        update();
    }

    @Override
    public void registerWithInputHandler(TouchInputHandler input) {
        input.addAction(touchAction);
        this.touchInput = input;
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
    public boolean inArea(float x, float y) {
        Vector3f local = getLocal(x, y);
        if (align == Align.Left) {
            if (local.x < this.getWidth()
                    && local.x > 0
                    && local.y < this.getHeight()
                    && local.y > 0) {
                return true;
            } else {
                return false;
            }
        } else {
            if (Math.abs(local.x) < this.getWidth() / 2
                    && local.y < this.getHeight()
                    && local.y > 0) {
                return true;
            } else {
                return false;
            }
        }

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

    protected void touchDeadAction(int touchId) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void dragAction(DragEvent event) {
        if (event.getState().equals(DragEvent.GestureState.GESTURE_UPDATE)
                && event.getTranslation() != null) {
            startDragAnimation();
            move(getNode().getLocalTranslation().getX()
                    + event.getTranslation().x * scrnsize.x / getNode().getLocalScale().x, getNode().getLocalTranslation().getY()
                    - event.getTranslation().y * scrnsize.y / getNode().getLocalScale().y);
        } else if (event.getState().equals(DragEvent.GestureState.GESTURE_END)) {
            stopDragAniation();
            getLockState().removeTouchLock(event.getTouchID());
        } else if (event.getState().equals(DragEvent.GestureState.GESTURE_BEGIN)) {
            if (animationHandlerThread == null
                    || !animationHandlerThread.isAlive()) {
                animationHandlerThread = new Thread(
                        animationHandler);
                animationHandlerThread.start();
            }
        }
    }

    public boolean isDragging() {
        return isDragging;
    }

    public void startDragAnimation() {
        getNode().attachChild(this.overlayDrag);
        if (dragAnimationHandlerThread == null
                || !dragAnimationHandlerThread.isAlive()) {
            dragAnimationHandlerThread = new Thread(
                    dragAnimationHandler);
            dragAnimationHandlerThread.start();
        }
        isDragging = true;
    }

    public void stopDragAniation() {
        isDragging = false;
    }

    @Override
    protected void rotationAction(RotationGestureEvent event) {
    }

    @Override
    protected void zoomAction(ZoomEvent event) {
    }

    @Override
    public void touchGesture(GestureActionEvent event) {
        if (event.getEvent() instanceof DragEvent) {
            DragEvent e = (DragEvent) event.getEvent();
            if (inArea(event)) {
                dragAction(e);
            }
            if (e.getState() == TouchGestureEvent.GestureState.GESTURE_END) {
//                getLockState().removeTouchLock(e.getTouchID());
            }
        }
    }

    @Override
    protected void touchBirthAction(TouchActionEvent e) {
 
    }

    @Override
    protected void touchAliveAction(TouchActionEvent e) {

    }

    @Override
    protected void touchDeadAction(TouchActionEvent e) {

    }
}
class RemoveControllerHandler implements Runnable {

    private int target;
    private Node node = null;
    /**
     * Logger
     */
    private Logger log = Logger.getLogger(AnimationHandler.class);

    public RemoveControllerHandler(Node node, int target) {
        this.node = node;
        this.target = target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public void run() {
        log.debug("RemoveHandler startet");
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            log.error(e);
        }
        while (node.getLocalScale().x != target) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
        for (int i = 0; i < node.getControllerCount(); i++) {
            node.removeController(i);
        }
        log.debug("RemoveHandler ended");
    }
}
