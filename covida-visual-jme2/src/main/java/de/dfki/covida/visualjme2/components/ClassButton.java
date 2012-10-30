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
import com.jmex.angelfont.BitmapFont;
import de.dfki.covida.covidacore.components.IControlableComponent;
import de.dfki.covida.covidacore.data.AnnotationClass;
import de.dfki.covida.covidacore.tw.ITouchAndWriteComponent;
import de.dfki.covida.covidacore.tw.TouchAndWriteComponentHandler;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.visualjme2.animations.CovidaSpatialController;
import de.dfki.covida.visualjme2.animations.ResetAnimation;
import de.dfki.covida.visualjme2.components.fields.AnnotationSearchField;
import de.dfki.covida.visualjme2.components.video.VideoComponent;
import de.dfki.covida.visualjme2.components.video.fields.InfoFieldComponent;
import de.dfki.covida.visualjme2.utils.AddControllerCallable;
import de.dfki.covida.visualjme2.utils.CovidaZOrder;
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
    private Quad tagQuad;
    private final AnnotationClass tag;
    private int fontSize = 28;
    private final Quad tagTopQuad;
    private final Quad tagShineQuad;
    private final InfoFieldComponent field;
    private final TextComponent tagText;

    public ClassButton(AnnotationClass tag, InfoFieldComponent field, Vector3f local,
            int width, int height, int zOrder) {
        super(tag.name + " class button", zOrder);
        this.fontSize = (int) (fontSize * ((float) height / 50));
        this.field = field;
        this.tag = tag;
        this.node.setLocalTranslation(local);
        this.width = width;
        this.height = height;
        setZOrder(zOrder);
        setTouchable(true);
        setDefaultPosition();
        Texture tagTexture = TextureManager.loadTexture(
                getClass().getClassLoader()
                .getResource("media/textures/class.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        tagTexture.setWrap(Texture.WrapMode.Clamp);
        TextureState tagTextureState = DisplaySystem.getDisplaySystem()
                .getRenderer().createTextureState();
        tagTextureState.setTexture(tagTexture);
        tagQuad = new Quad(tag.name + " quad", width,
                height);
        tagQuad.setRenderState(tagTextureState);
        tagQuad.setRenderState(JMEUtils.initalizeBlendState());
        tagQuad.updateRenderState();
        attachChild(tagQuad);
        tagQuad.setZOrder(getZOrder() - 1);
        Texture tagTopTexture = TextureManager.loadTexture(
                getClass().getClassLoader()
                .getResource("media/textures/class_top.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        tagTopTexture.setWrap(Texture.WrapMode.Clamp);
        TextureState tagTopState = DisplaySystem.getDisplaySystem()
                .getRenderer().createTextureState();
        tagTopState.setTexture(tagTopTexture);
        tagTopQuad = new Quad(tag.name + " quad", width,
                height);
        tagTopQuad.setRenderState(tagTopState);
        tagTopQuad.setRenderState(JMEUtils.initalizeBlendState());
        tagTopQuad.updateRenderState();
        attachChild(tagTopQuad);
        tagTopQuad.setZOrder(getZOrder() - 2);
        Texture tagShineTexture = TextureManager.loadTexture(
                getClass().getClassLoader()
                .getResource("media/textures/class_shine.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        tagShineTexture.setWrap(Texture.WrapMode.Clamp);
        TextureState tagShineState = DisplaySystem.getDisplaySystem()
                .getRenderer().createTextureState();
        tagShineState.setTexture(tagShineTexture);
        tagShineQuad = new Quad(tag.name + " quad", width,
                height);
        tagShineQuad.setRenderState(tagShineState);
        tagShineQuad.setRenderState(JMEUtils.initalizeBlendState());
        tagShineQuad.updateRenderState();
        tagShineQuad.setDefaultColor(ColorRGBA.gray);
        attachChild(tagShineQuad);
        tagShineQuad.setZOrder(getZOrder() - 4);
        tagText = new TextComponent(this, ActionName.NONE, zOrder - 3);
        ColorRGBA color = ColorRGBA.randomColor();
        if (tag.color != null) {
            color = new ColorRGBA(tag.color.getRed() / 255.f, tag.color.getGreen() / 255.f,
                    tag.color.getBlue() / 255.f, tag.color.getAlpha() / 255.f);
        }
        tagTopQuad.setDefaultColor(color);
        tagText.setAlign(BitmapFont.Align.Center);
        tagText.setSize(fontSize);
        tagText.setText(tag.name);
        tagText.setFont(1);
        tagText.setLocalTranslation(0, fontSize * 0.7f, 0);
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
    
    public AnnotationClass getTag(){
        return tag;
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
//        ColorRGBA c = tagQuad.getDefaultColor();
//        c.a = 1.f;
//        tagQuad.setDefaultColor(c);
        tagShineQuad.setDefaultColor(ColorRGBA.gray);
    }

    @Override
    public void touchBirthAction(int id, int x, int y) {
//        ColorRGBA c = tagQuad.getDefaultColor();
//        c.a = 0.5f;
//        tagQuad.setDefaultColor(c);
        tagShineQuad.setDefaultColor(ColorRGBA.white);
        setZOrder(CovidaZOrder.getInstance().getUi_cornermenus());
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
                        || (comp instanceof ControlButton)
                        || (comp instanceof AnnotationSearchField))
                        && comp.inArea(x, y)) {
                    inAreacomponents.put(comp.getZOrder(), comp);
                }
            }
            if (!inAreacomponents.isEmpty()) {
                ITouchAndWriteComponent comp = inAreacomponents.get(inAreacomponents.firstKey());
                if (comp instanceof VideoComponent) {
                    VideoComponent video = (VideoComponent) comp;
                    video.tagAction(tag);
                } else if (comp instanceof InfoFieldComponent) {
                    InfoFieldComponent info = (InfoFieldComponent) comp;
                    info.tagAction(tag);
                } else if (comp instanceof ControlButton) {
                    ControlButton control = (ControlButton) comp;
                    if(control.getAction().equals(ActionName.GARBADGE)
                            && field != null){
                        field.deleteTag(this);
                    }
                } else if (comp instanceof AnnotationSearchField) {
                    AnnotationSearchField search = (AnnotationSearchField) comp;
                }
            }
            resetAnimation();
        }
    }

    public void detach() {
        setTouchable(false);
        if (node.hasChild(tagQuad)) {
            GameTaskQueueManager.getManager().update(new DetachChildCallable(
                    node, tagQuad));
        }
        if (node.hasChild(tagShineQuad)) {
            GameTaskQueueManager.getManager().update(new DetachChildCallable(
                    node, tagShineQuad));
        }
        if (node.hasChild(tagTopQuad)) {
            GameTaskQueueManager.getManager().update(new DetachChildCallable(
                    node, tagTopQuad));
        }
        tagText.detach();
    }

    public void attach() {
        setTouchable(true);
        if (!node.hasChild(tagQuad)) {
            attachChild(tagQuad);
        }
        if (!node.hasChild(tagShineQuad)) {
            attachChild(tagShineQuad);
        }
        if (!node.hasChild(tagTopQuad)) {
            attachChild(tagTopQuad);
        }
        tagText.attach();
    }

    @Override
    public boolean toggle(ActionName action) {
        return false;
    }
}
