/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.animations;

import com.jme.animation.SpatialTransformer;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;

/**
 *
 * @author Tobias
 */
public class RotateAnimation {

    public static SpatialTransformer getController(Spatial pivot, float angle, float time) {
        Quaternion q = new Quaternion();
        q.fromAngleAxis(FastMath.DEG_TO_RAD * (angle), new Vector3f(0, 0, 1));
        SpatialTransformer st = new SpatialTransformer(1);
        st.setObject(pivot, 0, -1);
        Quaternion origin = pivot.getLocalRotation();
        st.setRotation(0, 0f, origin);
        st.setRotation(0, time, q);
        st.interpolateMissing();
        return st;
    }
}
