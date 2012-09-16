/*
 * CovidaJMEComponent.java
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

import com.jme.math.Matrix4f;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.system.DisplaySystem;
import de.dfki.covida.covidacore.tw.ITouchAndWriteComponent;
import de.dfki.covida.covidacore.tw.TouchAndWriteComponentHandler;
import de.dfki.touchandwrite.analyser.touch.gestures.events.PanEventImpl;
import de.dfki.touchandwrite.analyser.touch.gestures.events.RotationGestureEventImpl;
import de.dfki.touchandwrite.analyser.touch.gestures.events.ZoomEventImpl;
import de.dfki.touchandwrite.input.pen.event.ShapeEvent;
import java.awt.Dimension;
import org.apache.log4j.Logger;

/**
 *
 * @author Tobias Zimmermann
 *
 */
public abstract class CovidaJMEComponent extends Node implements ITouchAndWriteComponent {

    protected Vector2f display;
    protected boolean newPenAction = false;
    /**
     * Default scale of the {@link VideoComponent} as {@link Vector3f}
     */
    protected Vector3f defaultScale;
    /**
     * Default rotation of the {@link VideoComponent} as {@link Quaternion}.
     */
    protected Quaternion defaultRotation;
    /**
     * Default translation of the {@link VideoComponent} as {@link Vector3f}
     */
    protected Vector3f defaultTranslation;
    /**
     * Logger
     */
    private Logger log = Logger.getLogger(CovidaJMEComponent.class);
    /**
     * Flag which indicates of pen is active.
     */
    private boolean isAlwaysOnTop = false;
    /**
     * id
     */
    private int id;
    protected final JMENodeHandler nodeHandler;

    public CovidaJMEComponent(String nameOfComponent) {
        super(nameOfComponent);
        nodeHandler = JMENodeHandler.getInstance();
        this.display = new Vector2f(
                DisplaySystem.getDisplaySystem().getWidth(),
                DisplaySystem.getDisplaySystem().getHeight());
        registerComponent();
    }

    private void registerComponent() {
        TouchAndWriteComponentHandler.getInstance().addComponent(this);
    }

    @Override
    public final int attachChild(Spatial spatial) {
        nodeHandler.addAttachChildRequest(this, spatial);
        return 0;
    }

    public final void executeDetachChild(Spatial spatial) {
        super.detachChild(spatial);
    }

    public final void executeAttachChild(Spatial spatial) {
        super.attachChild(spatial);
    }

    public final void executeAddController(Controller controller) {
        super.addController(controller);
    }

    public final void executeRemoveController(Controller controller) {
        super.removeController(controller);
    }

    @Override
    public final int detachChild(Spatial spatial) {
        nodeHandler.addDetachChildRequest(this, spatial);
        return 0;
    }

    @Override
    public final void addController(Controller controller) {
        nodeHandler.addAddControllerRequest(this, controller);
    }

    @Override
    public final boolean removeController(Controller controller) {
        nodeHandler.addRemoveControllerRequest(this, controller);
        return true;
    }

    /**
     * Resets position of the VideoComponent
     */
    public final void resetNode() {
        setLocalTranslation(defaultTranslation);
        setLocalScale(defaultScale);
        setLocalRotation(defaultRotation);
    }

    /**
     * Set the default values for rotation / scale and translation to the
     * current node values.
     */
    public final void setDefaultPosition() {
        this.defaultScale = new Vector3f(getLocalScale().x, getLocalScale().y, getLocalScale().z);
        this.defaultRotation = new Quaternion(getLocalRotation().x,
                getLocalRotation().y, getLocalRotation().z,
                getLocalRotation().w);
        this.defaultTranslation = new Vector3f(
                getLocalTranslation().x, getLocalTranslation().y, getLocalTranslation().z);
    }

    /**
     * Move the node from this instance of {@link CovidaJMEComponent} to
     * position x,y
     *
     * @param x
     * @param y
     */
    public void move(float x, float y) {
        setLocalTranslation(x, y, 0);
    }

    @Override
    public final void toFront() {
        while (getParent() != null && getParent().equals(nodeHandler.getRootNode())) {
            Node n = getParent();
            nodeHandler.addDetachChildRequest(n, this);
            nodeHandler.addAttachChildRequest(n, this);
        }
    }

    public final Vector3f getLocal(Node node, float x, float y) {
        Matrix4f store = new Matrix4f();
        node.getLocalToWorldMatrix(store);
        store = store.invert();
        return store.mult(new Vector3f(x, y, 0));
    }

    /**
     * Returns the index in the list of Children of the root
     * <code>Node</code>
     *
     * @return <code>Integer</code>
     */
    public final int getNodeIndex() {
        if (getRootNode() == null) {
            return -1;
        }
        if (getRootNode().getParent() != null
                && getRootNode().getParent() != getRootNode()
                && getRootNode().getParent().getChildren() != null) {
            return getRootNode().getParent().getChildren().lastIndexOf(getRootNode());
        }
        return -1;
    }

    /**
     * Calculates the local x,y coordinates for the component.
     *
     * @param x global x
     * @param y global y
     * @return {@link  Vector3f} with x,y,0
     */
    public Vector3f getLocal(float x, float y) {
        Matrix4f store = new Matrix4f();
        Vector3f local = new Vector3f(x, y, 0);
        getLocalToWorldMatrix(store);
        store = store.invert();
        return store.mult(local);
    }

    /**
     * Calculates the world x,y coordinates for local x,y
     *
     * @param x local x
     * @param y local y
     * @return {@link  Vector3f} with x,y,0
     */
    public Vector3f getWorld(float x, float y) {
        Matrix4f store = new Matrix4f();
        Vector3f local = new Vector3f(x, y, 0);
        getLocalToWorldMatrix(store);
        return store.mult(local);
    }

    /**
     * @return the isAlwaysOnTop
     */
    @Override
    public boolean isAlwaysOnTop() {
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

    /**
     * Returns the {@link CovidaJMEComponent}s id
     *
     * @return the id as {@link Integer}
     */
    public int getId() {
        return id;
    }

    /**
     * Return the root {@link Node} of the {@link CovidaJMEComponent}
     *
     * @return the rootNode
     */
    private Node getRootNode() {
        return nodeHandler.getRootNode();
    }

    /**
     * Clean up the resources reserved for this component
     */
    public void cleanUp() {
    }

    /**
     * Close the component
     */
    public void close() {
        removeFromParent();
        cleanUp();
    }

    @Override
    public int getZPosition() {
        return getNodeIndex();
    }

    @Override
    public void touchDeadAction(int id, int x, int y) {
    }

    @Override
    public final float getRotationAngle() {
        return getLocalRotation().toAngleAxis(new Vector3f(0, 0, 1));
    }

    @Override
    public final int getPosX() {
        return (int) getLocalTranslation().getX();
    }

    @Override
    public final int getPosY() {
        return (int) getLocalTranslation().getY();
    }

    @Override
    public final Dimension getDimension() {
        int x = (int) (getLocalScale().getX() * getWidth());
        int y = (int) (getLocalScale().getY() * getHeight());
        return new Dimension(x, y);
    }

    @Override
    public boolean inArea(int x, int y) {
        Vector3f local = getLocal(x, display.y - y);
        int xAbs = (int) Math.abs(local.x);
        int yAbs = (int) Math.abs(local.y);
        if (xAbs < getWidth() / 2 && yAbs < getHeight() / 2) {
            return true;
        }
        return false;
    }

    @Override
    public void dragAction(int id, int x, int y, int dx, int dy) {
    }

    @Override
    public void dragEndAction(int id, int x, int y, int dx, int dy) {
    }

    public Vector3f getScale() {
        return getLocalScale();
    }

    @Override
    public final Dimension getDisplaySize() {
        return new Dimension((int) display.getX(), (int) display.getY());
    }

    @Override
    public void hwrAction(String hwr) {
    }

    @Override
    public void onShapeEvent(ShapeEvent event) {
    }

    @Override
    public void draw(int x, int y) {
    }

    @Override
    public void rotateAction(RotationGestureEventImpl event) {
    }

    @Override
    public void zoomAction(ZoomEventImpl event) {
    }

    @Override
    public void panAction(PanEventImpl event) {
    }

    @Override
    public void touchAliveAction(int id, int x, int y) {
    }

    @Override
    public void touchBirthAction(int id, int x, int y) {
    }

    /**
     * Returns the width of the component in pixels on the display
     *
     * @return width of the component in pixels on the display
     */
    protected abstract int getWidth();

    /**
     * Returns the height of the component in pixels on the display
     *
     * @return height of the component in pixels on the display
     */
    protected abstract int getHeight();
}
