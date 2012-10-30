/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import de.dfki.covida.covidacore.data.VideoMediaData;
import de.dfki.covida.covidacore.tw.IApplication;
import de.dfki.covida.visualjme2.utils.DetachChildCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Tobias
 */
public class VideoThumb extends JMEComponent {

    private final int width;
    private final int height;
    private Quad borderQuad;
    private Quad thumbQuad;
    private List<TextureState> textureStates;
    private final IControlButton button;
    private final VideoMediaData data;
    private final IApplication app;
    private int thumbPos = 0;
    private final Timer timer;

    public VideoThumb(VideoMediaData data, Vector3f local,
            IApplication app, IControlButton button, int width,
            int height, int zOrder) {
        super(data.videoName + " thumb", zOrder);
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
        borderQuad = new Quad((data.videoName + " thumb border quad"), width + 15,
                height + 15);
        borderQuad.setRenderState(videoTextureState);
        borderQuad.setRenderState(JMEUtils.initalizeBlendState());
        borderQuad.updateRenderState();
        attachChild(borderQuad);
        borderQuad.setZOrder(getZOrder() - 1);

        for (Image image : data.thumbs) {
            if (image != null) {
                Texture defaultTexture = TextureManager.loadTexture(image,
                        Texture.MinificationFilter.BilinearNearestMipMap,
                        Texture.MagnificationFilter.Bilinear,
                        false);
                defaultTexture.setWrap(Texture.WrapMode.Clamp);
                TextureState textureState = DisplaySystem.getDisplaySystem()
                        .getRenderer().createTextureState();
                textureState.setTexture(defaultTexture);
                textureStates.add(textureState);
            }
        }
        thumbQuad = new Quad((data.videoName + " thumb quad"), width,
                height);
        if (!textureStates.isEmpty()) {
            thumbQuad.setRenderState(textureStates.get(thumbPos));
        }
        thumbQuad.setRenderState(JMEUtils.initalizeBlendState());
        thumbQuad.updateRenderState();
        attachChild(thumbQuad);
        thumbQuad.setZOrder(getZOrder());
        timer = new Timer();
        timer.schedule(new Task(), 500, 500);
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
        app.addVideo(data);
    }

    void detach() {
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
        timer.cancel();
        timer.purge();
    }

    public void attach() {
        setTouchable(touchable);
        timer.schedule(new Task(), 500);
        if (!node.hasChild(borderQuad)) {
            attachChild(borderQuad);
        }
        if (!node.hasChild(thumbQuad)) {
            attachChild(thumbQuad);
        }
    }

    class Task extends TimerTask {

        @Override
        public void run() {
            thumbPos++;
            if (thumbPos < textureStates.size()) {
                thumbQuad.setRenderState(textureStates.get(thumbPos));
                thumbQuad.updateRenderState();
            } else if (!textureStates.isEmpty()) {
                thumbPos = 0;
                thumbQuad.setRenderState(textureStates.get(thumbPos));
                thumbQuad.updateRenderState();
            }
        }
    }
}
