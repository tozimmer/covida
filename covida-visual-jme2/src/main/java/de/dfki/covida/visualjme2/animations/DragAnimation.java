/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.animations;

import com.jme.animation.SpatialTransformer;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;

/**
 *
 * @author Tobias
 */
public class DragAnimation {
    
    public static SpatialTransformer getController(Spatial pivot) {
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
        st.setRepeatType(Controller.RT_CYCLE);

        return st;
    }

}
