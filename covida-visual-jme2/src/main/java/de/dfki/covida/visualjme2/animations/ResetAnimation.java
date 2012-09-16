/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.animations;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;

/**
 *
 * @author Tobias
 */
public class ResetAnimation {

    public static CovidaSpatialController getController(Spatial pivot, Vector3f defaultScale, float angle, Vector3f defaultTranslation) {
        CovidaSpatialController st = new CovidaSpatialController(pivot, 1);
        st.setObject(pivot, 0, -1);
        st.setPosition(0, 0, pivot.getLocalTranslation());
        st.setRotation(0, 0, pivot.getLocalRotation());
        st.setScale(0, 0, pivot.getLocalScale());
        st.setPosition(0, 1, defaultTranslation);
        Quaternion q = new Quaternion();
        q = q.fromAngleAxis(angle, new Vector3f(0, 0, 1));
        st.setRotation(0, 1, q);
        st.setScale(0, 1, defaultScale);
        st.interpolateMissing();
        return st;
    }
    
    public static CovidaSpatialController getController(Spatial pivot, Vector3f defaultScale, Quaternion defaultRotation, Vector3f defaultTranslation){
        CovidaSpatialController st = new CovidaSpatialController(pivot, 1);
        st.setObject(pivot, 0, -1);
        st.setPosition(0, 0, pivot.getLocalTranslation());
        st.setRotation(0, 0, pivot.getLocalRotation());
        st.setScale(0, 0, pivot.getLocalScale());
        st.setPosition(0, 1, defaultTranslation);
        st.setRotation(0, 1, defaultRotation);
        st.setScale(0, 1, defaultScale);
        st.interpolateMissing();
        return st;
    }
}
