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
package de.dfki.covida.components.button;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;

import de.dfki.covida.components.CovidaComponent;
import de.dfki.touchandwrite.action.TouchAction;
import de.dfki.touchandwrite.action.TouchActionEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.RotationGestureEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.ZoomEvent;
import de.dfki.touchandwrite.gestures.events.DragEvent;
import de.dfki.touchandwrite.visual.input.TouchInputHandler;

/**
 * Sidebar menu for VideoTouchBoard
 * 
 * @author Tobias Zimmermann
 *
 */
public class CloseButton extends CovidaButton {

    /**
     * 
     */
    private static final long serialVersionUID = 7384780136991918432L;
    private Node fieldNode;
    static final int ANIMATION_DURATION = 500;
    /** Touch action */
    private TouchAction touchAction;
    private CovidaComponent component;

    public CloseButton(int width, int height, Node node, CovidaComponent component) {
        super(width, height, node, node, "media/textures/search.png", "media/textures/search_glow.png");
        super.setAlwaysOnTop(true);
        this.component = component;
        fieldNode = new Node("Searchfield Node");
        Quaternion q = new Quaternion();
        q.fromAngleAxis(FastMath.DEG_TO_RAD * (-45), new Vector3f(0, 0, 1));
        fieldNode.setLocalRotation(q);
        touchAction = new TouchAction(this);
        getNode().attachChild(fieldNode);
    }

    @Override
    public void initComponent() {
        super.initComponent();
    }

    @Override
    public void registerWithInputHandler(TouchInputHandler input) {
        input.addAction(touchAction);
    }

    @Override
    protected void touchDeadAction(TouchActionEvent e) {
        component.close();
    }

    @Override
    protected void touchAliveAction(TouchActionEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void touchBirthAction(TouchActionEvent e) {
        // TODO Auto-generated method stub
    }


    @Override
    protected void touchDeadAction(int touchId) {
        // TODO Auto-generated method stub
    }
}
