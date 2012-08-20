/*
 * CovidaComponent.java
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
import com.jme.image.Texture;
import com.jme.math.Matrix4f;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import de.dfki.covida.ui.components.video.VideoComponent;
import de.dfki.touchandwrite.action.PenActionEvent;
import de.dfki.touchandwrite.action.TouchAction;
import de.dfki.touchandwrite.action.TouchActionEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.DragEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.RotationGestureEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.ZoomEvent;
import de.dfki.touchandwrite.input.pen.event.ShapeEvent;
import de.dfki.touchandwrite.input.touch.event.TouchState;
import de.dfki.touchandwrite.shape.Shape;
import de.dfki.touchandwrite.visual.components.ComponentType;
import de.dfki.touchandwrite.visual.components.TouchComponent;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Tobias Zimmermann
 *
 */
public abstract class CovidaComponent extends TouchComponent {

    /**
     *
     */
    private static final long serialVersionUID = -1624946338865865903L;
    private LockState lockState;
    protected Vector2f display;
    private Node node;
    protected boolean newPenAction = false;
    protected TouchAction touchAction;
    protected final Vector2f scrnsize;
    protected Quad overlayDrag;
    /**
     * Logger
     */
    private Logger log = Logger.getLogger(CovidaComponent.class);
    /**
     * Flag which indicates of pen is active.
     */
    private boolean isAlwaysOnTop = false;
    /**
     * id
     */
    private int id;
    private Node rootNode;

    public CovidaComponent(ComponentType type, String nameOfComponent, Node node) {
        super(type, nameOfComponent);
        this.setLockState(LockState.getInstance());
        this.touchAction = new TouchAction(this);
        this.id = lockState.registerComponent(this);
        this.node = node;
        this.setRootNode(node);
        this.display = new Vector2f(
                DisplaySystem.getDisplaySystem().getWidth(), DisplaySystem.getDisplaySystem().getHeight());
        scrnsize = display;
    }

    public CovidaComponent() {
        super(ComponentType.COMPONENT_2D, "PreLoadVideo");
        this.setLockState(LockState.getInstance());
        this.touchAction = new TouchAction(this);
        this.id = -1;
        this.node = new Node();
        this.setRootNode(node);
        this.display = new Vector2f(
                DisplaySystem.getDisplaySystem().getWidth(), DisplaySystem.getDisplaySystem().getHeight());
        scrnsize = display;
    }

    protected final BlendState initalizeBlendState() {
        BlendState alpha = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
        alpha.setEnabled(true);
        alpha.setBlendEnabled(true);

        alpha.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        alpha.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        alpha.setTestEnabled(true);
        alpha.setTestFunction(BlendState.TestFunction.GreaterThan);
        return alpha;
    }

    protected void initDragOverlay(BlendState alpha) {
        // Overlay drag 1
        Texture overlayDragTexture = TextureManager.loadTexture(getClass().getClassLoader().getResource("media/textures/overlay_drag.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlayDragTexture.setWrap(Texture.WrapMode.Clamp);

        TextureState overlayDragState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlayDragState.setTexture(overlayDragTexture);

        this.overlayDrag = new Quad("Overlay-Drag-Image-Quad", (1.15f) * getWidth(),
                (1.35f) * getHeight());

        overlayDrag.setRenderState(overlayDragState);
        overlayDrag.setRenderState(alpha);
        overlayDrag.updateRenderState();
        overlayDrag.getLocalTranslation().set(0, 0, 0);
    }

    public void killControllers() {
        log.debug("kill controllers " + getId());
        for (int i = 0; i < getNode().getControllerCount(); i++) {
            getNode().removeController(i);
        }
    }

    public void addController(SpatialTransformer st) {
        getNode().addController(st);
    }

    /**
     * Move the node from this instance of VideoComponent to position x,y
     *
     * @param x
     * @param y
     */
    protected void move(float x, float y) {
        getNode().setLocalTranslation(x, y, 0);
        // log.debug("Move "+this.getNameOfComponent()+" x: "+x+" y: "+y);
    }

    public void toFront() {
        // log.debug("To Front "+getId());
        Node n = getNode().getParent();
        n.detachChild(getNode());
        n.attachChild(getNode());
    }

    public Vector3f getLocal(Node node, float x, float y) {
        Matrix4f store = new Matrix4f();
        node.getLocalToWorldMatrix(store);
        store = store.invert();
        return store.mult(new Vector3f(x, y, 0));
    }

    /**
     * Returns the index in the list of Children of the root
     * <code>Node</code>
     *
     * @return
     * <code>Integer</code>
     */
    public int getNodeIndex() {
        if (getRootNode() == null) {
            log.error("rootNode == null! " + this.getNameOfComponent());
            return -1;
        }
        if (getRootNode().getParent() != null
                && getRootNode().getParent() != getRootNode()
                && getRootNode().getParent().getChildren() != null) {
//			log.debug(this.getNameOfComponent()
//					+ " ID "
//					+ getId()
//					+ " Node index: "
//					+ getRootNode().getParent().getChildren()
//							.lastIndexOf(getRootNode()));
            return getRootNode().getParent().getChildren().lastIndexOf(getRootNode());
        }
        return -1;
    }

    /**
     *
     * @return Node order
     */
    public ArrayList<Spatial> getNodeOrder() {
        return (new ArrayList<Spatial>(getNode().getParent().getChildren()));
    }

    @Override
    public void touch(Map<Integer, TouchActionEvent> event) {
        for (TouchActionEvent e : event.values()) {
            if (getLockState().onTop(e.getID(), new Vector2f(e.getX(), e.getY()),
                    this)) {
                touchAction(e);
            }
            if (e.getTouchState() == TouchState.TOUCH_DEAD) {
                touchDeadAction(e.getID());
                getLockState().removeTouchLock(e.getID());
            }
        }
    }

    /**
     * @param node the node to set
     */
    protected void setNode(Node node) {
        this.node = node;
    }

    /**
     *
     * @param x
     * @param y
     * @return
     */
    public Vector3f getLocal(float x, float y) {
        Matrix4f store = new Matrix4f();
        getNode().getLocalToWorldMatrix(store);
        store = store.invert();
        return store.mult(new Vector3f(x, y, 0));
    }

    /**
     * @return the isAlwaysOnTop
     */
    protected boolean isAlwaysOnTop() {
        return isAlwaysOnTop;
    }

    /**
     * @param isAlwaysOnTop the isAlwaysOnTop to set
     */
    protected void setAlwaysOnTop(boolean isAlwaysOnTop) {
        this.isAlwaysOnTop = isAlwaysOnTop;
    }

    /**
     * @return the display
     */
    public Vector2f getDisplay() {
        return display;
    }

    /**
     * @return the newPenAction
     */
    protected boolean isNewPenAction() {
        return newPenAction;
    }

    @Override
    public boolean isSensitiveArea(int id, int x, int y) {
        if (!(getNode().getLocalScale().x == 0)
                && !(getNode().getLocalScale().y == 0)) {
            if (getLockState().isTouchLocked(id)) {
                if (getLockState().getTouchLock(id) == getId()) {
                    return true;
                }
            }
            return inArea(x, y);
        } else {
            return false;
        }
    }

    /**
     *
     * @param x
     * @param y
     * @return
     */
    public boolean inArea(float x, float y) {
        Vector3f result = getLocal(x, y);
        if (Math.abs(result.x) < getWidth() / 2.1f
                && Math.abs(result.y) < getHeight() / 2f
                && !(getNode().getLocalScale().x == 0)
                && !(getNode().getLocalScale().y == 0)) {
            return true;
        }
        return false;
    }

    /**
     * @param newPenAction the newPenAction to set
     */
    protected void setNewPenAction(boolean newPenAction) {
        this.newPenAction = newPenAction;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the node
     */
    public Node getNode() {
        return node;
    }

    /**
     * @param lockState the lockState to set
     */
    private void setLockState(LockState lockState) {
        this.lockState = lockState;
    }

    /**
     * @return the lockState
     */
    public LockState getLockState() {
        return lockState;
    }

    public boolean inArea(TouchActionEvent touch) {
        return inArea(touch.getX(), touch.getY());
    }

    public boolean inArea(Object object) {
        if (object instanceof Vector2f) {
            return inArea(((Vector2f) object).x, ((Vector2f) object).y);
        } else if (object instanceof ShapeEvent) {
            return inArea((ShapeEvent) object);
        } else if (object instanceof DragEvent) {
            DragEvent e = (DragEvent) object;
            return inArea(e.getOrigin().getX(), e.getOrigin().getY());
        }
        return false;
    }

    /**
     * Checks if shape is on VideoComponent
     *
     * @param shape
     * @return
     * <code>boolean</code> , true if shape is on VideoComponent
     */
    public boolean inArea(ShapeEvent shape) {
        // ----check if shape in area-----
        boolean inArea = true;
        // iteration over all detected shapes
        for (Shape s : shape.getDetectedShapes()) {
            // iteration over all points of the shape
            Point tolerance = new Point((int) (getWidth() * 0.2f),
                    (int) (getHeight() * 0.2f));
            for (Point point : s.getPoints()) {
                if (!inArea(point.x - tolerance.x, this.display.y
                        - (point.y - tolerance.y))
                        && !inArea(point.x - tolerance.x, this.display.y
                        - (point.y + tolerance.y))
                        && !inArea(point.x + tolerance.x, this.display.y
                        - (point.y + tolerance.y))
                        && !inArea(point.x + tolerance.x, this.display.y
                        - (point.y - tolerance.y))) {
                    inArea = false;
                    if (this instanceof VideoComponent) {
                        // log.debug(((VideoComponent)
                        // this).getName()+" shape not in area "+s.getPoints());
                    }
                    break;
                }
            }
        }
        return inArea;
    }

    public boolean inArea(PenActionEvent evt) {
        return inArea(evt.getAbsoluteX(), getDisplay().y - evt.getAbsoluteY());
    }

    /**
     * @param rootNode the rootNode to set
     */
    public void setRootNode(Node rootNode) {
        this.rootNode = rootNode;
    }

    /**
     * @return the rootNode
     */
    public Node getRootNode() {
        return rootNode;
    }

    public boolean cleanUp() {
        return true;
    }

    public void close() {
        node.removeFromParent();
        cleanUp();
    }

    protected abstract void dragAction(DragEvent event);

    protected abstract void rotationAction(RotationGestureEvent event);

    protected abstract void zoomAction(ZoomEvent event);

    protected abstract int getWidth();

    protected abstract int getHeight();

    protected abstract void touchAction(TouchActionEvent e);

    protected abstract void touchDeadAction(int touchId);
}
