/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.components.video.controls;

import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import de.dfki.covida.visualjme2.components.CovidaJMEComponent;
import de.dfki.covida.visualjme2.utils.JMEUtils;

/**
 *
 * @author Tobias
 */
public class ControlButton extends CovidaJMEComponent {
    private final Quad overlayControls;
    private final int width;
    private final int height;

    public ControlButton(String name, String textureSource, int width, int height) {
        super(name);
        this.width = width;
        this.height = height;
        Texture overlayControlTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(textureSource),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlayControlTexture.setWrap(Texture.WrapMode.Clamp);

        TextureState overlayControlState = DisplaySystem.getDisplaySystem()
                .getRenderer().createTextureState();
        overlayControlState.setTexture(overlayControlTexture);
        overlayControls = new Quad((name+" Quad"),width / 5, (0.15f) * height);
        overlayControls.setRenderState(overlayControlState);
        overlayControls.setRenderState(JMEUtils.initalizeBlendState());
        overlayControls.updateRenderState();
        nodeHandler.addAttachChildRequest(this, overlayControls);
    }

    @Override
    protected int getWidth() {
        return width;
    }

    @Override
    protected int getHeight() {
        return height;
    }
}
