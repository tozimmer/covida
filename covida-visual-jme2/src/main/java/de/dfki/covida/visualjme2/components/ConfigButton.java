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
import de.dfki.covida.covidacore.tw.IApplication;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.visualjme2.utils.DetachChildCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;

/**
 *
 * @author Tobias
 */
public class ConfigButton extends JMEComponent {

    private final int width;
    private final int height;
    private Quad buttonQuad;
    private final IControlButton button;
    private final IApplication app;
    private final ActionName action;

    public ConfigButton(ActionName action, String texture, Vector3f local,
            IApplication app, IControlButton button, int width,
            int height, int zOrder) {
        super(action + " config button", zOrder);
        this.action = action;
        this.node.setLocalTranslation(local);
        this.button = button;
        this.width = width;
        this.height = height;
        this.app = app;
        setZOrder(zOrder);
        setTouchable(true);
        Texture videoTexture = TextureManager.loadTexture(
                getClass().getClassLoader()
                .getResource(texture),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        videoTexture.setWrap(Texture.WrapMode.Clamp);
        TextureState videoTextureState = DisplaySystem.getDisplaySystem()
                .getRenderer().createTextureState();
        videoTextureState.setTexture(videoTexture);
        buttonQuad = new Quad(action + " config button quad", width,
                height);
        buttonQuad.setRenderState(videoTextureState);
        buttonQuad.setRenderState(JMEUtils.initalizeBlendState());
        buttonQuad.updateRenderState();
        attachChild(buttonQuad);
        buttonQuad.setZOrder(getZOrder() - 1);
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
        if(action.equals(ActionName.CLOSEAPP)){
            app.close();
        }
    }

    void detach() {
        setTouchable(false);
        if (node.hasChild(buttonQuad)) {
            GameTaskQueueManager.getManager().update(new DetachChildCallable(
                    node, buttonQuad));
        }
    }

    public void attach() {
        setTouchable(true);
        if (!node.hasChild(buttonQuad)) {
            attachChild(buttonQuad);
        }
    }
}
