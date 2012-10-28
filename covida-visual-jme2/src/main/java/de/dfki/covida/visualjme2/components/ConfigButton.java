/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.components;

import com.jme.animation.SpatialTransformer;
import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.TextureManager;
import de.dfki.covida.covidacore.components.IControlButton;
import de.dfki.covida.covidacore.data.CovidaConfiguration;
import de.dfki.covida.covidacore.tw.IApplication;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.visualjme2.AbstractApplication;
import de.dfki.covida.visualjme2.animations.OpenAnimation;
import de.dfki.covida.visualjme2.utils.AddControllerCallable;
import de.dfki.covida.visualjme2.utils.DetachChildCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

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
    private List<ConfigButton> buttons;

    public ConfigButton(ActionName action, String texture, Vector3f local,
            IApplication app, IControlButton button, int width,
            int height, int zOrder) {
        super(action + " config button", zOrder);
        this.action = action;
        this.node.setLocalTranslation(local);
        this.button = button;
        this.width = width;
        this.height = height;
        this.buttons = new ArrayList<>();
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
        ColorRGBA color = ColorRGBA.white;
        Color c = CovidaConfiguration.getInstance().uiColor;
        if (CovidaConfiguration.getInstance().uiColor != null) {
            color = new ColorRGBA(c.getRed() / 255.f, c.getGreen() / 255.f,
                    c.getBlue() / 255.f, c.getAlpha() / 255.f);
        }
        buttonQuad.setDefaultColor(color);
        attachChild(buttonQuad);
        buttonQuad.setZOrder(getZOrder() - 1);
        if (action.equals(ActionName.UICOLORLIST)) {
            int zOrderOffset = 1;
            for (Color co : CovidaConfiguration.getInstance().uiColors) {
                if (!co.equals(CovidaConfiguration.getInstance().uiColor)) {
                    ConfigButton b = new ConfigButton(ActionName.UICOLOR, "media/textures/drop.png",
                            new Vector3f(0, 0, 0), app, button, width, width,
                            getZOrder() + zOrderOffset);
                    b.setColor(co);
                    attachChild(b);
                    buttons.add(b);
                    zOrderOffset++;
                }
            }
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
        if (action.equals(ActionName.CLOSEAPP)) {
            button.toggle();
            app.close();
        }
        if (action.equals(ActionName.UICOLORLIST)) {
            Vector3f local = new Vector3f(0, 0, 0);
            for (ConfigButton b : buttons) {
                local = local.add(getWidth(), 0, 0);
                SpatialTransformer trans;
                if (b.getLocalTranslation().x == 0) {
                    trans = OpenAnimation.getController(
                            b.node, 500, defaultScale, new Vector3f(local));
                } else {
//                    b.node.setLocalTranslation(new Vector3f(local));
                    trans = OpenAnimation.getController(
                            b.node, 500, defaultScale, new Vector3f(0, 0, 0));
                }
                GameTaskQueueManager.getManager().update(
                        new AddControllerCallable(b.node, trans));
            }
        }
        if (action.equals(ActionName.UICOLOR)) {
            button.toggle();
            if (app instanceof AbstractApplication) {
                AbstractApplication application = (AbstractApplication) app;
                application.changeColor(buttonQuad.getDefaultColor());
                ColorRGBA c = buttonQuad.getDefaultColor();
                CovidaConfiguration.getInstance().uiColor =
                        new Color(c.r, c.g, c.b, c.a);
            }
        }
    }

    void detach() {
        setTouchable(false);
        for (ConfigButton b : buttons) {
            b.detach();
        }
        buttons.clear();
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

    public void setColor(Color color) {
        ColorRGBA colorRGBA = ColorRGBA.white;
        if (color != null) {
            colorRGBA = new ColorRGBA(color.getRed() / 255.f, color.getGreen() / 255.f,
                    color.getBlue() / 255.f, color.getAlpha() / 255.f);
        }
        setColor(colorRGBA);
    }

    public void setColor(ColorRGBA color) {
        if (color != null) {
            buttonQuad.setDefaultColor(color);
        } else {
            buttonQuad.setDefaultColor(ColorRGBA.white);
        }
    }
}
