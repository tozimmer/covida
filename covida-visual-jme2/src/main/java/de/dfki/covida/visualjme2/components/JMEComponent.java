/*
 * JMEComponent.java
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

import com.jme.math.FastMath;
import com.jme.math.Matrix4f;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import de.dfki.covida.covidacore.tw.ITouchAndWriteComponent;
import de.dfki.covida.covidacore.tw.TouchAndWriteComponentHandler;
import de.dfki.covida.visualjme2.components.video.VideoComponent;
import de.dfki.covida.visualjme2.utils.AddControllerCallable;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import de.dfki.covida.visualjme2.utils.CovidaRootNode;
import de.dfki.covida.visualjme2.utils.DetachChildCallable;
import de.dfki.covida.visualjme2.utils.RemoveControllerCallable;
import de.dfki.touchandwrite.analyser.touch.gestures.events.PanEventImpl;
import de.dfki.touchandwrite.analyser.touch.gestures.events.RotationGestureEventImpl;
import de.dfki.touchandwrite.analyser.touch.gestures.events.ZoomEventImpl;
import de.dfki.touchandwrite.input.pen.event.ShapeEvent;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMEComponent
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public abstract class JMEComponent implements ITouchAndWriteComponent {

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
    protected boolean touchable;
    protected boolean drawable;
    /**
     * Logger
     */
    protected Logger log;
    /**
     * Flag which indicates of pen is active.
     */
    private boolean isAlwaysOnTop = false;
    /**
     * id
     */
    private int id;
    public final Node node;
    private int zOrder;
    private List<Spatial> spatials;
    private List<JMEComponent> components;

    public JMEComponent(String nameOfComponent, int zOrder) {
        node = new Node(nameOfComponent);
        node.setZOrder(zOrder);
        spatials = new ArrayList<>();
        components = new ArrayList<>();
        this.zOrder = zOrder;
        log = LoggerFactory.getLogger(getClass());
        this.display = new Vector2f(
                DisplaySystem.getDisplaySystem().getWidth(),
                DisplaySystem.getDisplaySystem().getHeight());
        registerComponent();
        setDefaultPosition();
    }

    public Vector3f getLocalTranslation() {
        return node.getLocalTranslation();
    }

    @Override
    public String getName() {
        return node.getName();
    }

    @Override
    public boolean isTouchable() {
        return touchable;
    }

    @Override
    public void setTouchable(boolean touchable) {
        this.touchable = touchable;
    }

    @Override
    public boolean isDrawable() {
        return drawable;
    }

    @Override
    public void setDrawable(boolean drawable) {
        this.drawable = drawable;
    }

    public void setLocalTranslation(Vector3f translation) {
        node.setLocalTranslation(translation);
    }

    private void registerComponent() {
        TouchAndWriteComponentHandler.getInstance().addComponent(this);
    }

    public final int attachChild(Spatial spatial) {
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node,
                spatial));
        if (!spatials.contains(spatial)) {
            spatials.add(spatial);
        }
        return 0;
    }
    
    public final int attachChild(Node parent, Spatial child) {
        GameTaskQueueManager.getManager().update(new AttachChildCallable(parent,
                child));
        if (!spatials.contains(child)) {
            spatials.add(child);
        }
        return 0;
    }


    public final int attachChild(JMEComponent component) {
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node,
                component.node));
        if (!components.contains(component)) {
            components.add(component);
        }
        return 0;
    }

    public final int detachChild(Spatial spatial) {
        GameTaskQueueManager.getManager().update(new DetachChildCallable(node,
                spatial));
        return 0;
    }
    
    private void removeFromParent() {
        GameTaskQueueManager.getManager().update(new DetachChildCallable(getParent(),
                node));
    }

    public final void addController(Controller controller) {
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node,
                controller));
    }

    public final boolean removeController(Controller controller) {
        GameTaskQueueManager.getManager().update(new RemoveControllerCallable(node,
                controller));
        return true;
    }

    public void setLocalTranslation(float x, float y, float z) {
        node.setLocalTranslation(new Vector3f(x, y, z));
    }

    public Node getParent() {
        return node.getParent();
    }

    /**
     * Rotate the node from this instance of VideoComponent
     *
     * @param angle angle to which the component should be rotated
     * @param axis the Axis on which the component should be rotated
     */
    public void rotate(float angle, Vector3f axis) {
        Quaternion rotation = new Quaternion();
        rotation.fromAngleAxis(FastMath.DEG_TO_RAD * angle, axis);
        setLocalRotation(rotation);
    }

    public void setLocalScale(Vector3f scale) {
        node.setLocalScale(scale);
    }

    public void setLocalScale(float f) {
        setLocalScale(new Vector3f(f, f, f));
    }

    public void setLocalRotation(Quaternion rotation) {
        node.setLocalRotation(rotation);
    }

    public Vector3f getLocalScale() {
        return node.getLocalScale();
    }

    public Quaternion getLocalRotation() {
        return node.getLocalRotation();
    }

    public void getLocalToWorldMatrix(Matrix4f matrix) {
        node.getLocalToWorldMatrix(matrix);
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
     * Move the node from this instance of {@link JMEComponent} to position x,y
     *
     * @param x
     * @param y
     */
    public void move(float x, float y) {
        setLocalTranslation(x, y, 0);
    }

    /**
     * Returns the index in the list of Children of the root
     * <code>Node</code>
     *
     * @return <code>Integer</code>
     */
    public final int getNodeIndex() {

        if (getParent().equals(CovidaRootNode.node)) {
            return CovidaRootNode.node.getChildren().lastIndexOf(node);
        } else {
            Node currentNode = node;
            while (currentNode.getParent() != null
                    && !currentNode.getParent().equals(CovidaRootNode.node)) {
                currentNode = currentNode.getParent();
            }
            if (currentNode.getParent() != null) {
                if (currentNode.getParent() != null) {
                    return CovidaRootNode.node.getChildren().lastIndexOf(currentNode);
                } else {
                    return Integer.MAX_VALUE;
                }
            } else {
                return Integer.MAX_VALUE;
            }
        }
    }

    /**
     * Calculates the local x,y coordinates for the component.
     *
     * @param x global x
     * @param y global y
     * @return {@link  Vector3f} with x,y,0
     */
    public Vector3f getLocal(float x, float y) {
        Vector3f local = new Vector3f(x, display.y - y, 0);
        Vector3f result = new Vector3f();
        result = node.worldToLocal(local, result);
        if (this instanceof TextComponent) {
            TextComponent text = (TextComponent) this;
            result = result.add(new Vector3f(0, +text.getFontSize(), 0));
        }
        return result;
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
        getLocalToWorldMatrix(store);
        Vector3f result = store.mult(new Vector3f(x, y, 0));
        return result;
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
     * Returns the {@link JMEComponent}s id
     *
     * @return the id as {@link Integer}
     */
    public int getId() {
        return id;
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
        TouchAndWriteComponentHandler.getInstance().removeComponent(this);
        cleanUp();
    }

    @Override
    public int getZOrder() {
        return zOrder;
    }

    @Override
    public void setZOrder(int zOrder) {
        for (Spatial spatial : spatials) {
            int diff = spatial.getZOrder() - getZOrder();
            spatial.setZOrder(zOrder + diff);
        }
        for (JMEComponent component : components) {
            int diff = component.getZOrder() - getZOrder();
            component.setZOrder(zOrder + diff);
        }
        this.zOrder = zOrder;
        node.setZOrder(zOrder);
        if(this instanceof VideoComponent){
            VideoComponent video = (VideoComponent) this;
            video.setFieldZOrder();
        }
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
        Node n = node;
        if(getDimension().getWidth() == 0 || getDimension().getHeight() == 0){
            return false;
        }
        while (n.getParent() != null && !n.getParent()
                .equals(CovidaRootNode.node)) {
            n = n.getParent();
        }
        if (n.getParent() != null) {
            if (n.getParent().equals(CovidaRootNode.node)) {
                Vector3f local = getLocal(x, y);
                int xAbs = (int) Math.abs(local.x);
                int yAbs = (int) Math.abs(local.y);
                if (xAbs < getWidth() / 2 && yAbs < getHeight() / 2) {
                    return true;
                }
            }
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
    public void hwrAction(String id, String hwr) {
    }

    @Override
    public void onShapeEvent(ShapeEvent event) {
    }

    @Override
    public void draw(int x, int y) {
    }

    @Override
    public void drawEnd(int x, int y) {
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
