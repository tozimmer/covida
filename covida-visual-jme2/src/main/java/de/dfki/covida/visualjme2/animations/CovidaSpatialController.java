/*
 * CovidaSpatialController.java
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
import com.jme.scene.Spatial;

/**
 * CovidaSpatialController
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class CovidaSpatialController extends SpatialTransformer {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5408416424492049454L;
    private float lifetime;
    private Spatial spatial;

    public CovidaSpatialController(Spatial spatial, float lifetime) {
        super(1);
        this.lifetime = lifetime;
        this.spatial = spatial;
    }

    /**
     * The update Method gets called every frame. Moves the object upwards and a
     * bit to the left or right. When the lifetime of the object is finished, it
     * gets removed from the scene. An explosion is spawned when the object
     * dies.
     */
    @Override
    public void update(float time) {
        super.update(time);
        lifetime -= time;
        // add hSpeed to the X-Axis and speed to the Y-Axis
        if (lifetime <= 0) {
            // the life has come to an end
            // remove this controller from the object and the object from the scene 
            setActive(false);
        }
    }
}
