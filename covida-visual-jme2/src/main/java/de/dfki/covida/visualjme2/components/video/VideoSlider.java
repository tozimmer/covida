/*
 * VideoSlider.java
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
package de.dfki.covida.visualjme2.components.video;

import com.jme.image.Texture;
import com.jme.image.Texture.WrapMode;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import de.dfki.covida.videovlcj.ISlider;
import de.dfki.covida.visualjme2.components.JMEComponent;
import de.dfki.covida.visualjme2.utils.JMEUtils;

/**
 * Time slider for VideoComponent
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class VideoSlider extends JMEComponent implements ISlider {

    private Quad sliderQuad;
    private VideoComponent video;
    private boolean toPause;
    private final Node sliderNode;

    public VideoSlider(VideoComponent video, int zOrder) {
        super("Video " + video.getId() + " Slider", zOrder);
        this.video = video;
        this.sliderNode = new Node("Slider Node");
        sliderNode.setZOrder(getZOrder());
        attachChild(sliderNode);
        initalizeOverlayQuads(JMEUtils.initalizeBlendState());
        setTouchable(true);
    }

    private void initalizeOverlayQuads(BlendState alpha) {
        Texture overlaySliderTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(
                "media/textures/slider.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlaySliderTexture.setWrap(WrapMode.Clamp);
        TextureState overlaySliderState = DisplaySystem.getDisplaySystem()
                .getRenderer().createTextureState();
        overlaySliderState.setTexture(overlaySliderTexture);
        sliderQuad = new Quad(
                ("Overlay-Video-Slider-Image-Quad-0"), video.getWidth(),
                video.getHeight() / 10.f);
        sliderQuad.setRenderState(
                overlaySliderState);
        sliderQuad.setRenderState(alpha);
        sliderQuad.updateRenderState();
        sliderQuad.setZOrder(getZOrder());
        attachChild(sliderNode, sliderQuad);
    }

    /**
     *
     * @param percentage
     */
    @Override
    public void setSlider(float percentage) {
        move((float) video.getWidth() * percentage);
    }

    /**
     *
     * @param x
     */
    private void move(float x) {
        if (FastMath.abs(x) < getWidth()) {
            sliderNode.setLocalTranslation(x, 0, 0);
        }
    }

    public VideoComponent getVideo() {
        return video;
    }

    @Override
    public void touchBirthAction(int id, int x, int y) {
        touchAction(x, y);
    }

    @Override
    public void touchAliveAction(int id, int x, int y) {
        touchAction(x, y);
    }

    @Override
    public void touchDeadAction(int id, int x, int y) {
        touchAction(x, y);
    }

    private void touchAction(int x, int y) {
        Vector3f result = getLocal(x, y);
        float percentage = (result.x + getWidth() / 2.0f) / getWidth();
        if (percentage <= 1.0f) {
            video.setTimePosition(percentage);
            video.enableTimeCodeOverlay(1000);
        }
    }

    @Override
    protected final int getHeight() {
        return (int) sliderQuad.getHeight();
    }

    @Override
    protected final int getWidth() {
        return (int) sliderQuad.getWidth();
    }
}
