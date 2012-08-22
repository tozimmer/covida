/*
 * ClipboardButton.java
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
package de.dfki.covida.ui.components.button;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import de.dfki.covida.ui.components.annotation.AnnotationClipboard;
import de.dfki.covida.ui.components.annotation.Field;
import de.dfki.touchandwrite.action.TouchActionEvent;

/**
 * Sidebar menu for VideoTouchBoard
 *
 * @author Tobias Zimmermann
 *
 */
public class ClipboardButton extends CovidaButton {

    private Node fieldNode;
    /**
     * Search Field
     */
    private AnnotationClipboard clipboard;

    /**
     * List of VideoComponents on VideoTouchBoard
     */
    /**
     * Creates a button and a clipboard field for annotations
     *
     * @param width width of the button
     * @param height height of the button
     * @param node the node which the button should be attached
     * @param videos list of the current videos
     */
    public ClipboardButton(int width, int height, Node node) {
        super(width, height, node, node, "media/textures/arrow.png",
                "media/textures/arrow_glow.png");
        super.setAlwaysOnTop(true);
        fieldNode = new Node("Clipboard Node");
        Quaternion q = new Quaternion();
        q.fromAngleAxis(FastMath.DEG_TO_RAD * (-90), new Vector3f(0, 0, 1));
        fieldNode.setLocalRotation(q);
        getNode().attachChild(fieldNode);
    }

    @Override
    public void initComponent() {
        super.initComponent();
        clipboard = new AnnotationClipboard(
                "media/textures/clipboard_field_color.png", fieldNode,
                (int) (display.x / 2.0f), (int) (display.y / 1.5f));
        clipboard.initComponent();
        clipboard.close();
    }

    public AnnotationClipboard getClipboard() {
        return clipboard;
    }

    public void closeField() {
        clipboard.close();
    }

    public void openField() {
        clipboard.open();
    }

    @Override
    protected final void toggle() {
        if (fieldNode.hasChild(clipboard)) {
            if (clipboard.isClosing()) {
                openField();
            } else {
                closeField();
            }
        } else {
            fieldNode.attachChild(clipboard);
            openField();
        }
    }

    @Override
    protected void touchAliveAction(TouchActionEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    Field getChild() {
        return clipboard;
    }
}
