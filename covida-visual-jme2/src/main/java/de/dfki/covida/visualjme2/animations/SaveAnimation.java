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
public class SaveAnimation {

    public static SpatialTransformer getController(Spatial pivot, float animationDuration, CloseAnimationType type) {
        SpatialTransformer st = new SpatialTransformer(1);
        if (type.equals(CloseAnimationType.LIST_FIELD)) {
            st.setObject(pivot, 0, -1);
        } else if (type.equals(CloseAnimationType.EDIT_FIELD)) {
            st.setObject(pivot, 0, -1);
            st.setScale(0, 0.25f, new Vector3f(0.5f, 1, 1));
            st.setScale(0, 0.5f, new Vector3f(0.0f, 1, 1));
            st.interpolateMissing();
        } else if (type.equals(CloseAnimationType.INFO_FIELD)) {
            st.setObject(pivot, 0, -1);
            st.setScale(0, 0.f, new Vector3f(pivot.getLocalScale()));
            st.setScale(0, 0.5f, new Vector3f(0.0f, 1, 1));
            st.interpolateMissing();
        }
        return st;
    }
}
