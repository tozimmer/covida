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
import com.jmex.angelfont.BitmapFont;
import de.dfki.covida.covidacore.components.IControlButton;
import de.dfki.covida.covidacore.components.IControlableComponent;
import de.dfki.covida.covidacore.data.AnnotationClass;
import de.dfki.covida.covidacore.tw.IApplication;
import de.dfki.covida.covidacore.tw.ITouchAndWriteComponent;
import de.dfki.covida.covidacore.tw.TouchAndWriteComponentHandler;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.visualjme2.animations.CovidaSpatialController;
import de.dfki.covida.visualjme2.animations.ResetAnimation;
import de.dfki.covida.visualjme2.animations.ScaleAnimation;
import de.dfki.covida.visualjme2.components.fields.AnnotationClipboard;
import de.dfki.covida.visualjme2.components.fields.AnnotationSearchField;
import de.dfki.covida.visualjme2.components.video.VideoComponent;
import de.dfki.covida.visualjme2.components.video.fields.InfoFieldComponent;
import de.dfki.covida.visualjme2.utils.AddControllerCallable;
import de.dfki.covida.visualjme2.utils.DetachChildCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author Tobias
 */
public class ClassButton extends JMEComponent implements IControlableComponent {

    private final int width;
    private final int height;
    private Quad buttonQuad;
    private final AnnotationClass tag;
    private final static int FONT_SIZE = 28;

    public ClassButton(AnnotationClass tag, Vector3f local,
            int width, int height, int zOrder) {
        super(tag.name + " class button", zOrder);
        this.tag = tag;
        this.node.setLocalTranslation(local);
        this.width = width;
        this.height = height;
        setZOrder(zOrder);
        setTouchable(true);
        setDefaultPosition();
        Texture videoTexture = TextureManager.loadTexture(
                getClass().getClassLoader()
                .getResource("media/textures/class.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        videoTexture.setWrap(Texture.WrapMode.Clamp);
        TextureState videoTextureState = DisplaySystem.getDisplaySystem()
                .getRenderer().createTextureState();
        videoTextureState.setTexture(videoTexture);
        buttonQuad = new Quad(tag.name + " quad", width,
                height);
        buttonQuad.setRenderState(videoTextureState);
        buttonQuad.setRenderState(JMEUtils.initalizeBlendState());
        buttonQuad.updateRenderState();
        attachChild(buttonQuad);
        buttonQuad.setZOrder(getZOrder() - 1);
        TextComponent tagText = new TextComponent(this, ActionName.NONE, zOrder - 1);
        ColorRGBA color = ColorRGBA.randomColor();
        if (tag.color != null) {
            color = new ColorRGBA(tag.color.getRed()/255.f, tag.color.getGreen()/255.f,
                    tag.color.getBlue()/255.f, tag.color.getAlpha()/255.f);
        }
        tagText.setAlign(BitmapFont.Align.Center);
        tagText.setSize(FONT_SIZE);
        tagText.setText(tag.name);
        tagText.setFont(1);
        tagText.setColor(color);
        tagText.setLocalTranslation(0, FONT_SIZE/2, 0);
        attachChild(tagText);
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
    public void dragAction(int id, int x, int y, int dx, int dy) {
        Vector3f translation = this.getLocalTranslation();
        Vector3f d = new Vector3f(dx, -dy, 0);
        d = d.divideLocal(node.getWorldScale());
        d = node.getWorldRotation().inverse().mult(d, d);
        translation = translation.add(d);
        node.setLocalTranslation(translation);
    }

    public void resetAnimation() {
        CovidaSpatialController controller =
                ResetAnimation.getController(node, defaultScale,
                defaultRotation, defaultTranslation);
        GameTaskQueueManager.getManager().update(new AddControllerCallable(
                node, controller));
        ColorRGBA c = buttonQuad.getDefaultColor();
        c.a = 1.f;
        buttonQuad.setDefaultColor(c);
    }

    @Override
    public void touchBirthAction(int id, int x, int y) {
        ColorRGBA c = buttonQuad.getDefaultColor();
        c.a = 0.5f;
        buttonQuad.setDefaultColor(c);
    }

    @Override
    public void touchDeadAction(int id, int x, int y) {
        if (getParent() != null) {
            Collection<ITouchAndWriteComponent> components =
                    TouchAndWriteComponentHandler.getInstance().getComponents();
            SortedMap<Integer, ITouchAndWriteComponent> inAreacomponents =
                    new TreeMap<>();
            for (ITouchAndWriteComponent comp : components) {
                if (((comp instanceof InfoFieldComponent)
                        || (comp instanceof VideoComponent)
                        || (comp instanceof AnnotationClipboard)
                        || (comp instanceof AnnotationSearchField)
                        || (comp instanceof ControlButton))
                        && comp.inArea(x, y)) {
                    inAreacomponents.put(comp.getZOrder(), comp);
                }
            }
            if (!inAreacomponents.isEmpty()) {
                ITouchAndWriteComponent comp = inAreacomponents.get(inAreacomponents.firstKey());
                if (comp instanceof VideoComponent) {
                    VideoComponent video = (VideoComponent) comp;
                } else if (comp instanceof InfoFieldComponent) {
                    InfoFieldComponent info = (InfoFieldComponent) comp;
                } else if (comp instanceof AnnotationClipboard) {
                    AnnotationClipboard clipboard = (AnnotationClipboard) comp;
                } else if (comp instanceof AnnotationSearchField) {
                    AnnotationSearchField search = (AnnotationSearchField) comp;
                }
            }
            resetAnimation();
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

    @Override
    public boolean toggle(ActionName action) {
        return false;
    }
}
