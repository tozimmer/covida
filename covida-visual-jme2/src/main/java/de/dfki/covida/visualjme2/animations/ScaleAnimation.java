/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.animations;

import com.jme.animation.SpatialTransformer;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;

/**
 *
 * @author Tobias
 */
public class ScaleAnimation {

    public static SpatialTransformer getController(Spatial pivot, float scale, float time) {
        SpatialTransformer st = new SpatialTransformer(1);
        st.setObject(pivot, 0, -1);
        st.setScale(0, 0f, new Vector3f(pivot.getLocalScale()));
        st.setScale(0, time / 2.f, new Vector3f(scale, 2.0f, 1));
        st.setScale(0, time, new Vector3f(1.0f, 1, 1));
        st.interpolateMissing();
        return st;
    }
}
