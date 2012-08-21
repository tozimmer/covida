/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.ui.components;

import com.jme.animation.SpatialTransformer;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import de.dfki.covida.ui.components.video.VideoComponent;
import org.apache.log4j.Logger;

/**
 *
 * @author Tobias Zimmermann
 */
public class DragAnimationHandler implements Runnable {

    private VideoComponent video;
    private final Object obj;
    private Node pivot;
    private Logger log = Logger.getLogger(de.dfki.covida.ui.components.AnimationHandler.class);

    public Object getObject() {
        return obj;
    }

    public DragAnimationHandler() {
        obj = new Object();
    }

    public void initComponent(VideoComponent video, Node node) {
        this.video = video;
        this.pivot = node;
    }

    public synchronized void run() {
        synchronized (obj) {
            while (video.isDragging()) {
                try {
                    SpatialTransformer st = new SpatialTransformer(1);
                    st.setObject(pivot, 0, -1);
                    Quaternion x0 = new Quaternion();
                    x0.fromAngleAxis(0, new Vector3f(0, 0, 1));
                    st.setScale(0, 0, new Vector3f(1.0f, 1.0f, 1.0f));
                    Quaternion x180 = new Quaternion();
                    x180.fromAngleAxis(FastMath.DEG_TO_RAD * 180, new Vector3f(
                            0, 0, 1));
                    st.setScale(0, 0.25f, new Vector3f(0.9f, 0.9f, 0.9f));
                    Quaternion x360 = new Quaternion();
                    x360.fromAngleAxis(FastMath.DEG_TO_RAD * 360, new Vector3f(
                            0, 0, 1));
                    st.setScale(0, 0.5f, new Vector3f(1.0f, 1.0f, 1.0f));
                    st.interpolateMissing();
                    pivot.addController(st);
                    obj.wait(500);
                    pivot.removeController(0);
                } catch (InterruptedException e) {
                    log.error(e);
                }
            }
            pivot.detachAllChildren();
        }
    }
}
