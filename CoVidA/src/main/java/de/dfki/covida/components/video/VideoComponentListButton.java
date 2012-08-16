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
package de.dfki.covida.components.video;

import java.util.ArrayList;
import java.util.Map;

import com.jme.animation.SpatialTransformer;
import com.jme.image.Texture;
import com.jme.image.Texture.WrapMode;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;

import de.dfki.covida.components.CovidaComponent;
import de.dfki.touchandwrite.action.TouchActionEvent;
import de.dfki.touchandwrite.input.touch.event.TouchState;
import de.dfki.touchandwrite.visual.components.ComponentType;

/**
 * Sidebar menu for VideoComponent
 *
 * @author Tobias Zimmermann
 *
 */
public class VideoComponentListButton extends CovidaComponent {

    /**
     *
     */
    private static final long serialVersionUID = 7384780136991918432L;
    private VideoComponent video;
    private ArrayList<Quad> overlayMenu;
    private SpatialTransformer st;
    static final int ANIMATION_DURATION = 500;
    static final int WIDTH = 200;
    static final int HEIGHT = 1000;

    public VideoComponentListButton(VideoComponent video, Node menuNode) {
        super(ComponentType.COMPONENT_2D, "Video Menu Component", menuNode);
        getNode().setLocalScale(
                new Vector3f((float) video.getWidth() / (10.f * (float) WIDTH),
                (float) video.getWidth() / (10.f * (float) WIDTH), 1));
        this.video = video;
    }

    @Override
    public void initComponent() {
        super.initComponent();
        super.setRootNode(video.getRootNode());
        st = new SpatialTransformer(1);
        getNode().addController(st);
        initalizeOverlayQuads(initalizeBlendState());
        getNode().attachChild(this.overlayMenu.get(0));
    }

    private BlendState initalizeBlendState() {
        BlendState alpha = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
        alpha.setEnabled(true);
        alpha.setBlendEnabled(true);

        alpha.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        alpha.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        alpha.setTestEnabled(true);
        alpha.setTestFunction(BlendState.TestFunction.GreaterThan);
        return alpha;
    }

    private void initalizeOverlayQuads(BlendState alpha) {
        this.overlayMenu = new ArrayList<Quad>();
        ArrayList<String> textureList = new ArrayList<String>();
        textureList.add("media/textures/video_menu_0.png");
        textureList.add("media/textures/video_menu_1.png");
        for (int i = 0; i < 2; i++) {
            Texture overlayMenuTexture = TextureManager.loadTexture(getClass().getClassLoader().getResource(textureList.get(i)),
                    Texture.MinificationFilter.BilinearNearestMipMap,
                    Texture.MagnificationFilter.Bilinear);
            overlayMenuTexture.setWrap(WrapMode.Clamp);

            TextureState overlayMenuState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
            overlayMenuState.setTexture(overlayMenuTexture);

            this.overlayMenu.add(new Quad(
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
            if (getNode().hasChild(this.overlayMenu.get(i))) {
                getNode().detachChild(this.overlayMenu.get(i));
            }
        }
    }

    public VideoComponent getVideo() {
        return video;
    }

    public void listDetached() {
        st.setObject(getNode(), 0, -1);
        st.setPosition(0, 0, getNode().getLocalTranslation());
        st.setRotation(0, 0, getNode().getLocalRotation());
        st.setScale(0, 0, getNode().getLocalScale());
        Quaternion x0 = new Quaternion();
        x0.fromAngleAxis(0, new Vector3f(0, 0, 1));
        st.setRotation(0, 0.5f, x0);
        st.interpolateMissing();
    }

    public void listAttached() {
        st.setObject(getNode(), 0, -1);
        st.setPosition(0, 0, getNode().getLocalTranslation());
        st.setRotation(0, 0, getNode().getLocalRotation());
        st.setScale(0, 0, getNode().getLocalScale());
        Quaternion x180 = new Quaternion();
        x180.fromAngleAxis(FastMath.DEG_TO_RAD * 180, new Vector3f(0, 0, 1));
        st.setRotation(0, 0.5f, x180);
        st.interpolateMissing();
    }

    @Override
    protected int getHeight() {
        return HEIGHT * 2;
    }

    @Override
    protected int getWidth() {
        return WIDTH * 2;
    }

    @Override
    protected void touchDeadAction(int touchId) {
        if (getNode().hasChild(this.overlayMenu.get(1))) {
            getNode().detachChild(this.overlayMenu.get(1));
            getNode().attachChild(this.overlayMenu.get(0));
        }
    }

    @Override
    protected void touchAction(TouchActionEvent e) {
        if (!e.getTouchState().equals(TouchState.TOUCH_DEAD)) {
            if (getNode().hasChild(this.overlayMenu.get(0))) {
                getNode().detachChild(this.overlayMenu.get(0));
                getNode().attachChild(this.overlayMenu.get(1));
            }
            if (video.hasList()) {
                video.detachList();
            } else {
                video.attachList();
            }
        } else {
            getLockState().removeTouchLock(e.getID());
        }
    }

    @Override
    protected void dragAction(de.dfki.touchandwrite.analyser.touch.gestures.events.DragEvent event) {
    }

    @Override
    protected void rotationAction(de.dfki.touchandwrite.analyser.touch.gestures.events.RotationGestureEvent event) {
    }

    @Override
    protected void zoomAction(de.dfki.touchandwrite.analyser.touch.gestures.events.ZoomEvent event) {
    }
}
