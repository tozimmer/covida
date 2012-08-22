/*
 * SearchButton.java
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
import de.dfki.covida.ui.components.annotation.AnnotationSearch;
import de.dfki.covida.ui.components.annotation.Field;
import de.dfki.covida.ui.components.video.VideoComponent;
import de.dfki.touchandwrite.action.TouchActionEvent;
import java.util.ArrayList;

/**
 * Sidebar menu for VideoTouchBoard
 *
 * @author Tobias Zimmermann
 *
 */
public class SearchButton extends CovidaButton {

    private Node fieldNode;
    /**
     * Search Field
     */
    private AnnotationSearch searchField;
    /**
     * List of VideoComponents on VideoTouchBoard
     */
    private ArrayList<VideoComponent> videos;

    public SearchButton(int width, int height, Node node,
            ArrayList<VideoComponent> videos) {
        super(width, height, node, node, "media/textures/search.png",
                "media/textures/search_glow.png");
        super.setAlwaysOnTop(true);
        this.videos = videos;
        fieldNode = new Node("Searchfield Node");
        Quaternion q = new Quaternion();
        q.fromAngleAxis(FastMath.DEG_TO_RAD * (-45), new Vector3f(0, 0, 1));
        fieldNode.setLocalRotation(q);
        getNode().attachChild(fieldNode);
    }

    @Override
    public void initComponent() {
        super.initComponent();
        searchField = new AnnotationSearch(
                "media/textures/search_field_color.png", fieldNode,
                (int) (display.x / 2.0f), (int) (display.y / 1.5f), videos);
        searchField.initComponent();
        searchField.close();
    }

    public AnnotationSearch getAnnotationSearchField() {
        return searchField;
    }

    public void closeSearch() {
        searchField.close();
    }

    public void openSearch() {
        searchField.open();
    }

    @Override
    protected final void toggle() {
        if (fieldNode.hasChild(searchField)) {
            if (searchField.isClosing()) {
                openSearch();
            } else {
                closeSearch();
            }
        } else {
            fieldNode.attachChild(searchField);
            openSearch();
        }
    }

    @Override
    protected void touchAliveAction(TouchActionEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    Field getChild() {
        return searchField;
    }
}
