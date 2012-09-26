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
import com.jme.util.GameTaskQueueManager;
import com.jme.util.TextureManager;
import com.jmex.angelfont.BitmapFont.Align;
import de.dfki.covida.videovlcj.ISlider;
import de.dfki.covida.visualjme2.components.CovidaJMEComponent;
import de.dfki.covida.visualjme2.components.TextOverlay;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import java.util.ArrayList;

/**
 * Time slider for VideoComponent
 *
 * @author Tobias Zimmermann
 *
 */
public class VideoSlider extends CovidaJMEComponent implements ISlider {

    private ArrayList<Quad> overlaySlider;
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 100;
    private VideoComponent video;
    private TextOverlay textOverlay;
    private TextOverlay timeOverlay;
    private Node sliderNode;

    public VideoSlider(VideoComponent video) {
        super("Video " + video.getId() + " Slider");
        setLocalScale(
                new Vector3f((float) video.getWidth() / ((float) getWidth()*1.5f),
                (float) video.getWidth() / ((float) getWidth()*1.5f), 1));
        this.video = video;
        timeOverlay = new TextOverlay(this);
        timeOverlay.setLocalTranslation(-getWidth() * 0.45f, getHeight(), 0);
        timeOverlay.setAlign(Align.Left);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, timeOverlay.node));
        textOverlay = new TextOverlay(this);
        textOverlay.setLocalTranslation(-getWidth() * 0.515f, getHeight(), 0);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, textOverlay.node));
        textOverlay.setAlign(Align.Right);
        initalizeOverlayQuads(JMEUtils.initalizeBlendState());
    }

    private void initalizeOverlayQuads(BlendState alpha) {
        sliderNode = new Node("Slider Node");
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, sliderNode));
        overlaySlider = new ArrayList<>();
        ArrayList<String> textureList = new ArrayList<>();
        textureList.add("media/textures/slider.png");
        for (int i = 0; i < textureList.size(); i++) {
            Texture overlaySliderTexture = TextureManager.loadTexture(
                    getClass().getClassLoader().getResource(
                    textureList.get(i)),
                    Texture.MinificationFilter.BilinearNearestMipMap,
                    Texture.MagnificationFilter.Bilinear);
            overlaySliderTexture.setWrap(WrapMode.Clamp);
            TextureState overlaySliderState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
            overlaySliderState.setTexture(overlaySliderTexture);
            overlaySlider.add(new Quad(
                    ("Overlay-Video-Slider-Image-Quad-0" + i), getWidth(),
                    getHeight()));
            overlaySlider.get(overlaySlider.size() - 1).setRenderState(
                    overlaySliderState);
            overlaySlider.get(overlaySlider.size() - 1).setRenderState(alpha);
            overlaySlider.get(overlaySlider.size() - 1).updateRenderState();
            GameTaskQueueManager.getManager().update(new AttachChildCallable(sliderNode, overlaySlider.get(overlaySlider.size() - 1)));
        }
    }

    /**
     *
     * @param percentage
     */
    @Override
    public void setSlider(float percentage) {
        move((float) getWidth() * percentage);
    }

    /**
     *
     * @param x
     */
    public void move(float x) {
        if (FastMath.abs(x) < getWidth()) {
            sliderNode.setLocalTranslation(x, 0, 0);
        }
    }

    public VideoComponent getVideo() {
        return video;
    }

    @Override
    protected final int getHeight() {
        return HEIGHT;
    }

    @Override
    protected final int getWidth() {
        return WIDTH;
    }
}
