/*
 * OpenAnimation.java
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
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;

/**
 * OpenAnimation
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
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

    public static SpatialTransformer getController(Spatial pivot,
            float animationDuration, Vector3f defaultScale,
            Vector3f defaultPosition) {
        SpatialTransformer st = new SpatialTransformer(1);
        Vector3f origin = pivot.getLocalTranslation();
        st.setObject(pivot, 0, -1);
        st.setPosition(0, 0.f, origin);
        st.setPosition(0, 0.5f, defaultPosition);
        st.setScale(0, 0f, new Vector3f(pivot.getLocalScale()));
        st.setScale(0, (float) animationDuration / 1000.f, defaultScale);
        st.interpolateMissing();
        return st;
    }
}
