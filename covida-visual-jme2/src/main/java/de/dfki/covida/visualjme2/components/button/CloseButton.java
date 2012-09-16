/*
 * CloseButton.java
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
package de.dfki.covida.visualjme2.components.button;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import de.dfki.covida.visualjme2.components.CovidaJMEComponent;
import de.dfki.covida.visualjme2.components.annotation.Field;

/**
 * Sidebar menu for VideoTouchBoard
 *
 * @author Tobias Zimmermann
 *
 */
public class CloseButton extends CovidaButton {
    
    private CovidaJMEComponent component;

    public CloseButton(int width, int height, CovidaJMEComponent component) {
        super(width, height, "media/textures/search.png", "media/textures/search_glow.png");
        super.setAlwaysOnTop(true);
        this.component = component;
        Quaternion q = new Quaternion();
        q.fromAngleAxis(FastMath.DEG_TO_RAD * (-45), new Vector3f(0, 0, 1));
    }

    @Override
    Field getChild() {
        return null;
    }

    @Override
    protected final void toggle() {
        component.close();
    }
}
