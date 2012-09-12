/*
 * VideoComponentControls.java
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
package de.dfki.covida.visual.components.video;

import com.jme.image.Texture;
import com.jme.image.Texture.WrapMode;
import com.jme.math.Matrix4f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import de.dfki.covida.visual.components.CovidaComponent;
import de.dfki.touchandwrite.action.TouchActionEvent;
import de.dfki.touchandwrite.visual.components.ComponentType;
import java.util.ArrayList;
import org.apache.log4j.Logger;

public class VideoComponentControls extends CovidaComponent {

    /**
     *
     */
    private static final long serialVersionUID = 7384780136991918432L;
    protected Quad overlayControlsDefault;
    private int width;
    private int height;
    private VideoComponent video;
    private Node node;
    private ArrayList<Quad> overlayControls;
    /**
     * Logger.
     */
    private Logger log = Logger.getLogger(VideoComponentControls.class);

    public VideoComponentControls(VideoComponent video, Node node) {
        super(ComponentType.COMPONENT_2D, "Video Control Component", node);
        super.initComponent();
        super.setRootNode(video.getRootNode());
        this.width = video.getWidth();
        this.height = video.getHeight();
        this.video = video;
        this.node = node;
    }

    @Override
    public void initComponent() {
        initalizeOverlayQuads(initalizeBlendState());
    }

    private void initalizeOverlayQuads(BlendState alpha) {

        // Overlay video menu default
        Texture overlayVideoControlDefault = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(
                "media/textures/video_controls.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlayVideoControlDefault.setWrap(WrapMode.Clamp);

        TextureState overlayVideoControlDefaultState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlayVideoControlDefaultState.setTexture(overlayVideoControlDefault);

        this.overlayControlsDefault = new Quad(
                "Overlay-Video-Controls-Image-Quad", width, (0.15f) * height);

        overlayControlsDefault.setRenderState(overlayVideoControlDefaultState);
        overlayControlsDefault.setRenderState(alpha);
        overlayControlsDefault.updateRenderState();
        overlayControlsDefault.getLocalTranslation().set(
                new Vector3f(0, -height / (1.4f), 0));

        this.overlayControls = new ArrayList<>();
        ArrayList<String> textureList = new ArrayList<>();
        textureList.add("media/textures/video_controls_0.png");
        textureList.add("media/textures/video_controls_1.png");
        textureList.add("media/textures/video_controls_2.png");
        textureList.add("media/textures/video_controls_3.png");
        textureList.add("media/textures/video_controls_4.png");

        for (int i = 0; i < 5; i++) {
            Texture overlayControlTexture = TextureManager.loadTexture(
                    getClass().getClassLoader().getResource(
                    textureList.get(i)),
                    Texture.MinificationFilter.BilinearNearestMipMap,
                    Texture.MagnificationFilter.Bilinear);
            overlayControlTexture.setWrap(WrapMode.Clamp);

            TextureState overlayControlState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
            overlayControlState.setTexture(overlayControlTexture);

            this.overlayControls.add(new Quad(
                    ("Overlay-Video-Controls-Image-Quad-0" + i), width,
                    (0.15f) * height));

            overlayControls.get(overlayControls.size() - 1).setRenderState(
                    overlayControlState);
            overlayControls.get(overlayControls.size() - 1).setRenderState(
                    alpha);
            overlayControls.get(overlayControls.size() - 1).updateRenderState();
            overlayControls.get(overlayControls.size() - 1).getLocalTranslation().set(new Vector3f(0, -height / (1.4f), 0));
        }
    }

    public void attachControls() {
        video.attachChild(this.overlayControlsDefault);
    }

    public void detachControls() {
        if (video.hasChild(overlayControlsDefault)) {
            video.detachChild(this.overlayControlsDefault);
        }
        for (int i = 0; i < 5; i++) {
            if (video.hasChild(this.overlayControls.get(i))) {
                video.detachChild(this.overlayControls.get(i));
            }
        }
    }

    @Override
    public boolean inArea(float x, float y) {
        Vector3f result = video.getLocal(x, y);
        if (Math.abs(result.x) < this.video.getWidth() / 2
                && result.y + this.video.getHeight() / 2.0f > -this.video.getHeight() * 0.65f
                && result.y + this.video.getHeight() / 2.0f < -this.video.getHeight() * 0.1f) {
            return true;
        }
        return false;
    }

    private boolean isAttached() {
        for (Quad quad : overlayControls) {
            if (video.hasChild(quad)) {
                return true;
            }
        }
        if (video.hasChild(overlayControlsDefault)) {
            return true;
        }
        return false;
    }

    public VideoComponent getVideo() {
        return video;
    }

    @Override
    protected int getHeight() {
        return this.height;
    }

    @Override
    protected int getWidth() {
        return this.width;
    }

    @Override
    protected void touchDeadAction(int touchId) {
        if (!video.hasChild(this.overlayControlsDefault) && isAttached()) {
            for (int i = 0; i < 5; i++) {
                if (video.hasChild(this.overlayControls.get(i))) {
                    video.detachChild(this.overlayControls.get(i));
                    video.attachChild(this.overlayControlsDefault);
                }
            }
            video.attachChild(this.overlayControlsDefault);
        } else {
            // TODO
        }
    }

    @Override
    protected void touchBirthAction(TouchActionEvent e) {
        Matrix4f store = new Matrix4f();
        node.getLocalToWorldMatrix(store);
        store = store.invert();
        Vector3f result = store.mult(new Vector3f(e.getX(), e.getY(), 0));
        int x = (int) (((result.x + video.getWidth() / 2) / video.getWidth()) * 5);
        if (inArea(e.getX(), e.getY())) {
            if (video.hasChild(this.overlayControlsDefault)) {
                if (x == 0) {
                    video.detachChild(this.overlayControlsDefault);
                    video.attachChild(this.overlayControls.get(0));
                    if ((video.getTime() - video.getMaxTime() / 20) > 0) {
                        video.setTimePosition(video.getTime()
                                - video.getMaxTime() / 20);
                    } else {
                        video.setTimePosition(0);
                    }
                } else if (x == 1) {
                    video.detachChild(this.overlayControlsDefault);
                    video.attachChild(this.overlayControls.get(1));
                    video.stop();
                } else if (x == 2) {
                    video.detachChild(this.overlayControlsDefault);
                    video.attachChild(this.overlayControls.get(2));
                    video.pause();
                } else if (x == 3) {
                    video.detachChild(this.overlayControlsDefault);
                    video.attachChild(this.overlayControls.get(3));
                    video.resume();
                } else if (x == 4) {
                    video.detachChild(this.overlayControlsDefault);
                    video.attachChild(this.overlayControls.get(4));
                    if ((video.getTime() + video.getMaxTime() / 100) < video.getMaxTime()) {
                        video.setTimePosition(video.getTime()
                                + video.getMaxTime() / 100);
                    } else {
                        video.setTimePosition(video.getMaxTime());
                    }
                }
            } else {
                // TODO
            }
        }
    }

    @Override
    protected void touchAliveAction(TouchActionEvent e) {
    }

    @Override
    protected void touchDeadAction(TouchActionEvent e) {
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
