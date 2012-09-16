/*
 * VideoComponentListButton.java
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

import com.jme.animation.SpatialTransformer;
import com.jme.image.Texture;
import com.jme.image.Texture.WrapMode;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import de.dfki.covida.visualjme2.components.CovidaJMEComponent;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import java.util.ArrayList;

/**
 * Sidebar menu for VideoComponent
 *
 * @author Tobias Zimmermann
 *
 */
public class VideoComponentListButton extends CovidaJMEComponent {

    private VideoComponent video;
    private ArrayList<Quad> overlayMenu;
    static final int ANIMATION_DURATION = 500;
    static final int WIDTH = 200;
    static final int HEIGHT = 1000;
    private SpatialTransformer stOpen;
    private SpatialTransformer stClose;

    public VideoComponentListButton(VideoComponent video) {
        super("Video "+video.getId()+" List Button");
        setLocalScale(
                new Vector3f((float) video.getWidth() / (10.f * (float) WIDTH),
                (float) video.getWidth() / (10.f * (float) WIDTH), 1));
        this.video = video;
        initalizeOverlayQuads(JMEUtils.initalizeBlendState());
        nodeHandler.addAttachChildRequest(this, overlayMenu.get(0));
    }

    private void initalizeOverlayQuads(BlendState alpha) {
        overlayMenu = new ArrayList<>();
        ArrayList<String> textureList = new ArrayList<>();
        textureList.add("media/textures/video_menu_0.png");
        textureList.add("media/textures/video_menu_1.png");
        for (int i = 0; i < 2; i++) {
            Texture overlayMenuTexture = TextureManager.loadTexture(getClass()
                    .getClassLoader().getResource(textureList.get(i)),
                    Texture.MinificationFilter.BilinearNearestMipMap,
                    Texture.MagnificationFilter.Bilinear);
            overlayMenuTexture.setWrap(WrapMode.Clamp);

            TextureState overlayMenuState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
            overlayMenuState.setTexture(overlayMenuTexture);

            overlayMenu.add(new Quad(
                    ("Overlay-Video-Menu-Image-Quad-0" + i), WIDTH, HEIGHT));

            overlayMenu.get(overlayMenu.size() - 1).setRenderState(
                    overlayMenuState);
            overlayMenu.get(overlayMenu.size() - 1).setRenderState(alpha);
            overlayMenu.get(overlayMenu.size() - 1).updateRenderState();
            overlayMenu.get(overlayMenu.size() - 1).getLocalTranslation().set(new Vector3f(0, 0, 0));
        }
    }

    public void detachMenu() {
        for (int i = 0; i < 2; i++) {
            if (hasChild(this.overlayMenu.get(i))) {
                nodeHandler.addDetachChildRequest(this, overlayMenu.get(i));
            }
        }
    }

    public VideoComponent getVideo() {
        return video;
    }

    public void detachAnimation() {
        if (getControllers().contains(stClose)) {
            nodeHandler.addRemoveControllerRequest(this, stClose);
        }
        if (getControllers().contains(stOpen)) {
            nodeHandler.addRemoveControllerRequest(this, stOpen);
        }
        stClose = new SpatialTransformer(1);
        stClose.setObject(this, 0, -1);
        stClose.setPosition(0, 0, getLocalTranslation());
        stClose.setRotation(0, 0, getLocalRotation());
        stClose.setScale(0, 0, getLocalScale());
        Quaternion x0 = new Quaternion();
        x0.fromAngleAxis(0, new Vector3f(0, 0, 1));
        stClose.setRotation(0, 0.5f, x0);
        stClose.interpolateMissing();
        nodeHandler.addAddControllerRequest(this, stClose);
    }

    @Override
    public void touchDeadAction(int id, int x, int y) {
        if (!video.hasList()) {
            video.attachList();
        } else {
            video.detachList();
        }
    }

    public void attachAnimation() {
        if (getControllers().contains(stClose)) {
            nodeHandler.addRemoveControllerRequest(this, stClose);
        }
        if (getControllers().contains(stOpen)) {
            nodeHandler.addRemoveControllerRequest(this, stOpen);
        }
        stOpen = new SpatialTransformer(1);
        stOpen.setObject(this, 0, -1);
        stOpen.setPosition(0, 0, getLocalTranslation());
        stOpen.setRotation(0, 0, getLocalRotation());
        stOpen.setScale(0, 0, getLocalScale());
        Quaternion x180 = new Quaternion();
        x180.fromAngleAxis(FastMath.DEG_TO_RAD * 180, new Vector3f(0, 0, 1));
        stOpen.setRotation(0, 0.5f, x180);
        stOpen.interpolateMissing();
        nodeHandler.addAddControllerRequest(this, stOpen);
    }

    @Override
    protected int getHeight() {
        return HEIGHT * 2;
    }

    @Override
    protected int getWidth() {
        return WIDTH * 2;
    }

   
}
