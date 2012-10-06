/*
 * CloseAnimation.java
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
package de.dfki.covida.visualjme2.animations;

import com.jme.animation.SpatialTransformer;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;

/**
 * CloseAnimation
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
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
