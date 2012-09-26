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
package de.dfki.covida.visualjme2.components.button;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.util.GameTaskQueueManager;
import de.dfki.covida.visualjme2.components.annotation.AnnotationSearchField;
import de.dfki.covida.visualjme2.components.annotation.Field;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;

/**
 * Sidebar menu for VideoTouchBoard
 *
 * @author Tobias Zimmermann
 *
 */
public class SearchButton extends CovidaButton {

    /**
     * Search Field
     */
    private AnnotationSearchField searchField;

    public SearchButton(int width, int height) {
        super(width, height, "media/textures/search.png",
                "media/textures/search_glow.png");
        super.setAlwaysOnTop(true);
        Quaternion q = new Quaternion();
        q.fromAngleAxis(FastMath.DEG_TO_RAD * (-45), new Vector3f(0, 0, 1));
        setLocalRotation(q);
        searchField = new AnnotationSearchField(
                "media/textures/search_field_color.png",
                (int) (display.x / 2.0f), (int) (display.y / 1.5f));
        searchField.close();
    }

    public AnnotationSearchField getAnnotationSearchField() {
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
        if (node.hasChild(searchField.node)) {
            if (searchField.isClosing()) {
                openSearch();
            } else {
                closeSearch();
            }
        } else {
            GameTaskQueueManager.getManager().update(new AttachChildCallable(node, searchField.node));
            openSearch();
        }
    }

    @Override
    Field getChild() {
        return searchField;
    }
}
