/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.components.video.controls;

import de.dfki.covida.covidacore.components.IControlButton;
import de.dfki.covida.covidacore.components.IControlableComponent;
import com.jme.image.Texture;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.TextureManager;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.visualjme2.components.CovidaJMEComponent;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;

/**
 *
 * @author Tobias
 */
public class ControlButton extends CovidaJMEComponent
        implements IControlButton {

    private final Quad controlQuad;
    private final int width;
    private final int height;
    private final TextureState defaultTextureState;
    private final IControlableComponent controlable;
    private final TextureState activeTextureState;
    private boolean enabled;
    private final ActionName action;

    /**
     *
     * @param actionName
     * @param textureSource
     * @param width
     * @param height
     */
    public ControlButton(ActionName actionName, IControlableComponent controlable,
            String texScr, String activeTexSrc, int width, int height) {
        super(actionName.toString());
        this.action = actionName;
        this.width = width;
        this.height = height;
        this.controlable = controlable;
        Texture defaultTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(texScr),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        defaultTexture.setWrap(Texture.WrapMode.Clamp);
        defaultTextureState = DisplaySystem.getDisplaySystem()
                .getRenderer().createTextureState();
        defaultTextureState.setTexture(defaultTexture);
        Texture activeTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(texScr),
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
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, controlQuad));
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
    public void setActive(boolean activated) {
        if (enabled) {
            if (activated) {
                controlQuad.setRenderState(activeTextureState);
            } else {
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
        //TODO
    }
    
    @Override
    public void touchDeadAction(int id, int x, int y) {
        setActive(controlable.toggle(action));
    }
}
