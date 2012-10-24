/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.components;

import com.jme.image.Texture;
import com.jme.math.Vector3f;
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
import java.io.File;

/**
 *
 * @author Tobias
 */
public class VideoThumb extends JMEComponent {

    private final int width;
    private final int height;
    private Quad videoQuad;
    private Quad thumbQuad;
    private final IControlButton button;
    private final VideoMediaData data;
    private final IApplication app;

    public VideoThumb(VideoMediaData data, Vector3f local,
            IApplication app, IControlButton button, int width,
            int height, int zOrder) {
        super(data.videoName + " thumb", zOrder);
        this.node.setLocalTranslation(local);
        this.button = button;
        this.width = width;
        this.height = height;
        this.data = data;
        this.app = app;
        setZOrder(zOrder);
        File file = new File(data.videoSource + ".png");
        if (file.isFile()) {
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
            videoQuad = new Quad((data.videoName + " thumb quad"), width + 15,
                    height + 15);
            videoQuad.setRenderState(videoTextureState);
            videoQuad.setRenderState(JMEUtils.initalizeBlendState());
            videoQuad.updateRenderState();
            attachChild(videoQuad);
            videoQuad.setZOrder(getZOrder() - 1);
            Texture defaultTexture = TextureManager.loadTexture(
                    file.getAbsolutePath(),
                    Texture.MinificationFilter.BilinearNearestMipMap,
                    Texture.MagnificationFilter.Bilinear);
            defaultTexture.setWrap(Texture.WrapMode.Clamp);
            TextureState textureState = DisplaySystem.getDisplaySystem()
                    .getRenderer().createTextureState();
            textureState.setTexture(defaultTexture);
            thumbQuad = new Quad((data.videoName + " thumb quad"), width,
                    height);
            thumbQuad.setRenderState(textureState);
            thumbQuad.setRenderState(JMEUtils.initalizeBlendState());
            thumbQuad.updateRenderState();
            attachChild(thumbQuad);
            thumbQuad.setZOrder(getZOrder());
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

    @Override
    public void touchBirthAction(int id, int x, int y) {
    }

    @Override
    public void touchDeadAction(int id, int x, int y) {
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
        if (node.hasChild(videoQuad)) {
            GameTaskQueueManager.getManager().update(new DetachChildCallable(
                    node, videoQuad));
        }
    }

    public void attach() {
        if (!node.hasChild(videoQuad)) {
            attachChild(videoQuad);
        }
        if (!node.hasChild(thumbQuad)) {
            attachChild(thumbQuad);
        }
    }
}
