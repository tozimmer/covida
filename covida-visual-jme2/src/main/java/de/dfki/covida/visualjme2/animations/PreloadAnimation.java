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
public class PreloadAnimation {

    public static SpatialTransformer getController(Spatial pivot) {
        SpatialTransformer st = new SpatialTransformer(1);
        st.setObject(pivot, 0, -1);
        st.setScale(0, 0, new Vector3f(2.0f, 2.0f, 2.0f));
        st.setScale(0, 2.0f, new Vector3f(1.6f, 1.6f, 1.6f));
        st.setScale(0, 4.0f, new Vector3f(1.0f, 1.0f, 1.0f));
        st.interpolateMissing();
        st.setRepeatType(SpatialTransformer.RT_CYCLE);
        return st;
    }
}
