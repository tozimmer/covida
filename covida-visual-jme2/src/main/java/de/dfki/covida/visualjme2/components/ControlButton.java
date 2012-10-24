/*
 * ControlButton.java
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
package de.dfki.covida.visualjme2.components;

import com.jme.animation.SpatialTransformer;
import com.jme.image.Texture;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.TextureManager;
import de.dfki.covida.covidacore.components.IControlButton;
import de.dfki.covida.covidacore.components.IControlableComponent;
import de.dfki.covida.covidacore.data.CovidaConfiguration;
import de.dfki.covida.covidacore.data.VideoMediaData;
import de.dfki.covida.covidacore.tw.IApplication;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.visualjme2.animations.RotateAnimation;
import de.dfki.covida.visualjme2.animations.ScaleAnimation;
import de.dfki.covida.visualjme2.utils.AddControllerCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ControlButton
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class ControlButton extends JMEComponent
        implements IControlButton {

    private final Quad controlQuad;
    private final int width;
    private final int height;
    private final TextureState defaultTextureState;
    private final IControlableComponent controlable;
    private final TextureState activeTextureState;
    private boolean enabled;
    private final ActionName action;
    private final static float ANIMATIONTIME = 0.25f;
    private List<VideoThumb> videoThumbs;

    /**
     * Creates a new instance of {@link ControlButton}
     *
     * @param actionName Action identifier {@link ActionName}
     * @param controlable Controlable component
     * @param texScr Button texture location
     * @param activeTexSrc Active button texture location
     * @param width width as {@link Integer}
     * @param height height as {@link Integer}
     */
    public ControlButton(ActionName actionName, IControlableComponent controlable,
            String texScr, String activeTexSrc, int width, int height, int zOrder) {
        super(actionName.toString(), zOrder);
        videoThumbs = new ArrayList<>();
        this.action = actionName;
        this.width = width;
        this.height = height;
        this.enabled = true;
        this.controlable = controlable;
        if (actionName.equals(ActionName.NONE)) {
            setTouchable(false);
        } else {
            setTouchable(true);
        }
        Texture defaultTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(texScr),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        defaultTexture.setWrap(Texture.WrapMode.Clamp);
        defaultTextureState = DisplaySystem.getDisplaySystem()
                .getRenderer().createTextureState();
        defaultTextureState.setTexture(defaultTexture);
        Texture activeTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(activeTexSrc),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        activeTexture.setWrap(Texture.WrapMode.Clamp);
        activeTextureState = DisplaySystem.getDisplaySystem()
                .getRenderer().createTextureState();
        activeTextureState.setTexture(activeTexture);
        controlQuad = new Quad((actionName + " Quad"), width, height);
        controlQuad.setRenderState(defaultTextureState);
        controlQuad.setRenderState(JMEUtils.initalizeBlendState());
        controlQuad.updateRenderState();
        attachChild(controlQuad);
        controlQuad.setZOrder(zOrder);
    }

    /**
     * Rotates the {@link ControlButton}
     *
     * @param angle Rotation angle as {@link Float}
     */
    public void rotate(float angle) {
        Quaternion q = new Quaternion();
        q.fromAngleAxis(FastMath.DEG_TO_RAD * (angle), new Vector3f(0, 0, 1));
        setLocalRotation(q);
    }

    public ActionName getAction() {
        return action;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setActive(boolean activated) {
        if (enabled) {
            if (activated) {
                if (action.equals(ActionName.LIST)) {
                    SpatialTransformer controller = RotateAnimation.getController(node,
                            180.f, ANIMATIONTIME);
                    GameTaskQueueManager.getManager().update(new AddControllerCallable(
                            node, controller));
                }
                controlQuad.setRenderState(activeTextureState);
            } else {
                if (action.equals(ActionName.LIST)) {
                    SpatialTransformer controller = RotateAnimation.getController(node,
                            0.f, ANIMATIONTIME);
                    GameTaskQueueManager.getManager().update(new AddControllerCallable(
                            node, controller));
                }
                controlQuad.setRenderState(defaultTextureState);
            }
            controlQuad.updateRenderState();
        }
    }

    @Override
    public boolean getActive() {
        return controlQuad.getRenderState(RenderState.StateType.Texture)
                .equals(activeTextureState);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public void touchBirthAction(int id, int x, int y) {
        if (enabled && getParent() != null) {
            if (action.equals(ActionName.LIST)) {
            } else if (action.equals(ActionName.NONE)) {
            } else if (action.equals(ActionName.GARBADGE)) {
            } else {
                SpatialTransformer controller = ScaleAnimation.getController(controlQuad,
                        2.0f, ANIMATIONTIME);
                GameTaskQueueManager.getManager().update(new AddControllerCallable(
                        controlQuad, controller));
            }
        }
    }

    @Override
    public void touchDeadAction(int id, int x, int y) {
        if (enabled && getParent() != null) {
            if (action.equals(ActionName.LIST)) {
            } else if (action.equals(ActionName.NONE)) {
            } else if (action.equals(ActionName.GARBADGE)) {
            } else {
                SpatialTransformer controller = ScaleAnimation.getController(controlQuad,
                        1.0f, ANIMATIONTIME);
                GameTaskQueueManager.getManager().update(new AddControllerCallable(
                        controlQuad, controller));
            }
            if (inArea(x, y)) {
                toggle();
            }
        }
    }

    @Override
    public void toggle() {
        if (action.equals(ActionName.OPEN) && controlable instanceof IApplication) {
            if (videoThumbs.isEmpty()) {
                Vector3f local = new Vector3f(0, 0, 0);
                for (VideoMediaData data : CovidaConfiguration.getInstance().videos) {
                    float ration = (float) data.width / (float) data.height;
                    local = local.add(0, ((float) height * 1.5f) / ration + 25, 0);
                    IApplication app = (IApplication) controlable;
                    VideoThumb thumb = new VideoThumb(data, local, app, this,
                            (int) (width * 1.5f),
                            (int) (((float) height * 1.5f) / ration),
                            getZOrder());
                    attachChild(thumb);
                    videoThumbs.add(thumb);
                }
            } else {
                for (VideoThumb thumb : videoThumbs) {
                    thumb.detach();
                }
                videoThumbs.clear();
            }
        } else if (controlable != null) {
            setActive(controlable.toggle(action));
        }
    }
}
