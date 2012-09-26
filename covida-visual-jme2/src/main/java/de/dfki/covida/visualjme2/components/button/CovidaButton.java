/*
 * CovidaButton.java
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

import com.jme.animation.SpatialTransformer;
import com.jme.image.Texture;
import com.jme.image.Texture.WrapMode;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.TextureManager;
import de.dfki.covida.visualjme2.components.CovidaJMEComponent;
import de.dfki.covida.visualjme2.components.annotation.Field;
import de.dfki.covida.visualjme2.utils.AddControllerCallable;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import de.dfki.covida.visualjme2.utils.DetachChildCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import java.util.ArrayList;

/**
 * Sidebar menu for VideoTouchBoard
 *
 * @author Tobias Zimmermann
 *
 */
public abstract class CovidaButton extends CovidaJMEComponent {

    private int width;
    private int height;
    private ArrayList<Quad> overlay;
    private SpatialTransformer st;
    static final int ANIMATION_DURATION = 500;
    private ArrayList<String> textureList;

    /**
     * List of VideoComponents on VideoTouchBoard
     */
    public CovidaButton(int width, int height, String inactive, String active) {
        super("Button Component");
        super.setAlwaysOnTop(true);
        this.width = width;
        this.height = height;
        textureList = new ArrayList<>();
        textureList.add(inactive);
        textureList.add(active);
        st = new SpatialTransformer(1);
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node, st));
        initalizeOverlayQuads(JMEUtils.initalizeBlendState());
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, overlay.get(0)));
    }

    private void initalizeOverlayQuads(BlendState alpha) {
        this.overlay = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Texture overlayMenuTexture =
                    TextureManager.loadTexture(getClass().getClassLoader().getResource(textureList.get(i)),
                    Texture.MinificationFilter.BilinearNearestMipMap,
                    Texture.MagnificationFilter.Bilinear);
            overlayMenuTexture.setWrap(WrapMode.Clamp);

            TextureState overlayMenuState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
            overlayMenuState.setTexture(overlayMenuTexture);

            this.overlay.add(new Quad(("Overlay-Video-Menu-Image-Quad-0" + i), width, height));

            overlay.get(overlay.size() - 1).setRenderState(overlayMenuState);
            overlay.get(overlay.size() - 1).setRenderState(alpha);
            overlay.get(overlay.size() - 1).updateRenderState();
            overlay.get(overlay.size() - 1).getLocalTranslation().set(new Vector3f(0, 0, 0));
        }
    }

    public void detachButton() {
        for (int i = 0; i < 2; i++) {
            if (node.hasChild(overlay.get(i))) {
                GameTaskQueueManager.getManager().update(new DetachChildCallable(node, overlay.get(i)));
            }
        }
    }

    abstract Field getChild();

    public void changeOverlay() {
        if (node.hasChild(this.overlay.get(1))) {
            GameTaskQueueManager.getManager().update(new DetachChildCallable(node, overlay.get(1)));
        } else {
            GameTaskQueueManager.getManager().update(new AttachChildCallable(node, overlay.get(1)));
        }
    }

    @Override
    protected int getWidth() {
        return width;
    }

    @Override
    protected int getHeight() {
        return height;
    }

    protected abstract void toggle();
}
