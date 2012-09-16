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
public class OpenAnimation {

    public static SpatialTransformer getController(Spatial pivot, float animationDuration, Vector3f defaultScale) {
        SpatialTransformer st = new SpatialTransformer(1);
        st.setObject(pivot, 0, -1);
        st.setScale(0, 0f, new Vector3f(pivot.getLocalScale()));
        st.setScale(0, (float) animationDuration / 1000.f, defaultScale);
        st.interpolateMissing();
        return st;
    }
    
    public static SpatialTransformer getController(Spatial pivot, float animationDuration){
        SpatialTransformer st = new SpatialTransformer(1);
        st.setObject(pivot, 0, -1);
        st.setPosition(0, 0.f, new Vector3f(pivot.getLocalTranslation()));
        st.setPosition(0, 0.5f, new Vector3f(0, 75, 0));
        st.interpolateMissing();
        return st;
    }
}
