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
public class CloseAnimation {

    public static SpatialTransformer getController(Spatial pivot, float animationDuration, CloseAnimationType type) {
        SpatialTransformer st = new SpatialTransformer(1);
        if (type.equals(CloseAnimationType.LIST_FIELD)) {
            st.setObject(pivot, 0, -1);
            st.setScale(0, 0f, new Vector3f(pivot.getLocalScale()));
            st.setPosition(0, 0f, new Vector3f(pivot.getLocalTranslation()));
            st.setScale(0, (float) animationDuration / 1000.f, new Vector3f(
                    0.0f, 1, 1));
            st.interpolateMissing();
        } else if (type.equals(CloseAnimationType.EDIT_FIELD)) {
            st.setObject(pivot, 0, -1);
            Quaternion x0 = new Quaternion();
            x0.fromAngleAxis(0, new Vector3f(0, 0, 1));
            st.setRotation(0, 0, x0);
            Quaternion x180 = new Quaternion();
            x180.fromAngleAxis(FastMath.DEG_TO_RAD * 180, new Vector3f(0, 0, 1));
            st.setRotation(0, (float) animationDuration / 2000.f, x180);
            st.setScale(0, (float) animationDuration / 2000.f, new Vector3f(
                    0.5f, 0.5f, 0.5f));
            Quaternion x360 = new Quaternion();
            x360.fromAngleAxis(FastMath.DEG_TO_RAD * 360, new Vector3f(0, 0, 1));
            st.setRotation(0, (float) animationDuration / 1000.f, x360);
            st.setScale(0, (float) animationDuration / 1000.f, new Vector3f(
                    0.0f, 0.0f, 0.0f));
            st.interpolateMissing();
        } else if (type.equals(CloseAnimationType.INFO_FIELD)) {
            st.setObject(pivot, 0, -1);
            Quaternion x0 = new Quaternion();
            x0.fromAngleAxis(0, new Vector3f(0, 0, 1));
            st.setRotation(0, 0, x0);
            Quaternion x180 = new Quaternion();
            x180.fromAngleAxis(FastMath.DEG_TO_RAD * 180, new Vector3f(0,
                    0, 1));
            st.setRotation(0, (float) animationDuration / 2000.f, x180);
            st.setScale(0, (float) animationDuration / 2000.f,
                    new Vector3f(0.5f, 0.5f, 0.5f));
            Quaternion x360 = new Quaternion();
            x360.fromAngleAxis(FastMath.DEG_TO_RAD * 360, new Vector3f(0,
                    0, 1));
            st.setRotation(0, (float) animationDuration / 1000.f, x360);
            st.setScale(0, (float) animationDuration / 1000.f,
                    new Vector3f(0.0f, 0.0f, 0.0f));
            st.interpolateMissing();
        } else {
            st.setObject(pivot, 0, -1);
        }
        return st;
    }

    public static SpatialTransformer getController(Spatial pivot, float animationDuration, float width, float height) {
        SpatialTransformer st = new SpatialTransformer(1);
        st.setObject(pivot, 0, -1);
        st.setPosition(0, 0.f, new Vector3f(pivot.getLocalTranslation()));
        st.setPosition(0, 0.5f, new Vector3f(-width / 2.f,
                -height / 2.f, 0));
        st.interpolateMissing();
        return st;
    }
}
