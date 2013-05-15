/*
 * ImageThumb.java
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

import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.TextureManager;
import de.dfki.covida.covidacore.components.IControlButton;
import de.dfki.covida.covidacore.data.ImageMediaData;
import de.dfki.covida.covidacore.tw.IApplication;
import de.dfki.covida.visualjme2.components.video.ImageComponent;
import de.dfki.covida.visualjme2.utils.DetachChildCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Markus Weber
 */
public class ImageThumb extends JMEComponent implements IThumb {

    private final int width;
    private final int height;
    private Quad borderQuad;
    private Quad thumbQuad;
    private List<TextureState> textureStates;
    private final IControlButton button;
    private final ImageMediaData data;
    private final IApplication app;
    private int thumbPos = 0;

    public ImageThumb(ImageMediaData data, Vector3f local,
            IApplication app, IControlButton button, int width,
            int height, int zOrder) {
        super(data.imageName + " thumb", zOrder);
        textureStates = new ArrayList<>();
        this.node.setLocalTranslation(local);
        this.button = button;
        this.width = width;
        this.height = height;
        this.data = data;
        this.app = app;
        setZOrder(zOrder);

        setTouchable(true);
        Texture videoTexture = TextureManager.loadTexture(
                getClass().getClassLoader()
                .getResource("media/textures/video.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        videoTexture.setWrap(Texture.WrapMode.Clamp);
        TextureState videoTextureState = DisplaySystem.getDisplaySystem()
                .getRenderer().createTextureState();
        videoTextureState.setTexture(videoTexture);
        borderQuad = new Quad((data.imageName + " thumb border quad"), width * 1.1f,
                (int) (height * 1.4));
        borderQuad.setRenderState(videoTextureState);
        borderQuad.setRenderState(JMEUtils.initalizeBlendState());
        borderQuad.updateRenderState();
        attachChild(borderQuad);
        borderQuad.setZOrder(getZOrder() - 1);
        BufferedImage image = null;
         try {
            image = ImageIO.read(new File(data.imageSource));
        } catch (IOException ex) {
            Logger.getLogger(ImageComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        Texture defaultTexture = TextureManager.loadTexture(image,
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear,
                false);
        defaultTexture.setWrap(Texture.WrapMode.Clamp);
        TextureState textureState = DisplaySystem.getDisplaySystem()
                .getRenderer().createTextureState();
        textureState.setTexture(defaultTexture);
        textureStates.add(textureState);

        thumbQuad = new Quad((data.imageName + " thumb quad"), width,
                height);
        if (!textureStates.isEmpty()) {
            thumbQuad.setRenderState(textureStates.get(thumbPos));
        }
        thumbQuad.setRenderState(JMEUtils.initalizeBlendState());
        thumbQuad.updateRenderState();
        attachChild(thumbQuad);
        thumbQuad.setZOrder(getZOrder());
    }

    @Override
    protected int getWidth() {
        return width;
    }

    @Override
    protected int getHeight() {
        return height;
    }

    @Override
    public void touchBirthAction(int id, int x, int y) {
        ColorRGBA c = borderQuad.getDefaultColor();
        c.a = 0.5f;
        borderQuad.setDefaultColor(c);
        c = thumbQuad.getDefaultColor();
        c.a = 0.5f;
        thumbQuad.setDefaultColor(c);
    }

    @Override
    public void touchDeadAction(int id, int x, int y) {
        ColorRGBA c = borderQuad.getDefaultColor();
        c.a = 1.f;
        borderQuad.setDefaultColor(c);
        c = thumbQuad.getDefaultColor();
        c.a = 1.f;
        thumbQuad.setDefaultColor(c);
        button.toggle();
        app.addImage(data);
    }

    @Override
    public void detach() {
        setTouchable(false);
        setDrawable(false);
        if (node.hasChild(thumbQuad)) {
            GameTaskQueueManager.getManager().update(new DetachChildCallable(
                    node, thumbQuad));
        }
        if (node.hasChild(borderQuad)) {
            GameTaskQueueManager.getManager().update(new DetachChildCallable(
                    node, borderQuad));
        }
    }

    public void attach() {
        setTouchable(touchable);
        if (!node.hasChild(borderQuad)) {
            attachChild(borderQuad);
        }
        if (!node.hasChild(thumbQuad)) {
            attachChild(thumbQuad);
        }
    }
}
