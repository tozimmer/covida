/*
 * CovidaApplication.java
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
package de.dfki.covida.visualjme2;

import com.acarter.scenemonitor.SceneMonitor;
import com.jme.animation.SpatialTransformer;
import com.jme.image.Image;
import com.jme.image.Texture;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.TextureManager;
import com.jme.util.geom.BufferUtils;
import de.dfki.covida.covidacore.components.IControlableComponent;
import de.dfki.covida.covidacore.components.IVideoComponent;
import de.dfki.covida.covidacore.data.CovidaConfiguration;
import de.dfki.covida.covidacore.data.PenData;
import de.dfki.covida.covidacore.data.VideoMediaData;
import de.dfki.covida.covidacore.streaming.TCPServer;
import de.dfki.covida.covidacore.tw.ITouchAndWriteComponent;
import de.dfki.covida.covidacore.tw.TouchAndWriteComponentHandler;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.visualjme2.animations.PreloadAnimation;
import de.dfki.covida.visualjme2.components.ConfigButton;
import de.dfki.covida.visualjme2.components.ControlButton;
import de.dfki.covida.visualjme2.components.DrawingOverlay;
import de.dfki.covida.visualjme2.components.JMEComponent;
import de.dfki.covida.visualjme2.components.TextComponent;
import de.dfki.covida.visualjme2.components.video.VideoComponent;
import de.dfki.covida.visualjme2.utils.*;
import de.dfki.touchandwrite.TouchAndWriteDevice;
import java.awt.Color;
import java.awt.Dimension;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Component to display videos.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class CovidaApplication extends ApplicationImpl implements IControlableComponent {

    /**
     * {@link Quad} for displaying the splash animation
     */
    private Quad preloadScreen;
    private CovidaApplicationPreloader preloader;
    private SpatialTransformer stPreload;
    private TCPServer tcpServer;
    private long snapshotTimer;
    private int sideMenuCount;
    private final boolean streaming = false;
    private final boolean scenemonitor = false;
    private Quad background;
    private Quad logo;
    private final CovidaConfiguration configuration;
    private DrawingOverlay loginOverlay;
    private TextComponent loginInfo;
    private TextComponent loginText;
    private Random random;

    /**
     * Creates an instance of {@link CovidaApplication}
     *
     * @param device {@link TouchAndWriteDevice}
     * @param windowtitle {@link String} which represents the title of the
     * application window
     */
    public CovidaApplication(TouchAndWriteDevice device, String windowtitle) {
        super(device, windowtitle);
        configuration = CovidaConfiguration.load();
        if (streaming) {
            tcpServer = TCPServer.getInstance();
        }
        snapshotTimer = System.currentTimeMillis();
        sideMenuCount = 0;
        random = new Random();
    }

    @Override
    public void setBackground() {
        // Initialization
        Quaternion q = new Quaternion();
        // Rotation need because of ImageGraphics
        q.fromAngles(0f, (float) Math.toRadians(180),
                (float) Math.toRadians(180));
        background = new Quad("Background-Image-Quad", display.getWidth(),
                display.getHeight());
        background.getLocalTranslation().set(display.getWidth() / 2, display.getHeight() / 2, 0);
        background.rotatePoints(q);
        background.setCullHint(Spatial.CullHint.Inherit);
        ColorRGBA color = ColorRGBA.white;
        Color c = CovidaConfiguration.getInstance().uiColor;
        if (CovidaConfiguration.getInstance().uiColor != null) {
            color = new ColorRGBA(c.getRed() / 255.f, c.getGreen() / 255.f,
                    c.getBlue() / 255.f, c.getAlpha() / 255.f);
        }
        background.setDefaultColor(color);
        // set background Texture
//        Texture backgroundTexture = TextureManager.loadTexture(
//                getClass().getClassLoader().getResource("media/textures/1280x800.png"),
//                Texture.MinificationFilter.Trilinear,
//                Texture.MagnificationFilter.Bilinear);
//        backgroundTexture.setWrap(Texture.WrapMode.Clamp);
//        backgroundTexture.setBlendColor(ColorRGBA.white);
//        backgroundTexture.setAnisotropicFilterPercent(0.5f);
//        Vector2f[] texCoords = new Vector2f[4];
//        texCoords[0] = new Vector2f(0, 0);
//        texCoords[3] = new Vector2f(1, 0);
//        texCoords[1] = new Vector2f(0, 1);
//        texCoords[2] = new Vector2f(1, 1);
//        background.setTextureCoords(TexCoords.makeNew(texCoords));
//        TextureState backgroundTextureState = DisplaySystem.getDisplaySystem()
//                .getRenderer().createTextureState();
//        backgroundTextureState.setTexture(backgroundTexture);
//        background.setRenderState(backgroundTextureState);
        background.setSolidColor(new ColorRGBA(0.62f, 0.62f, 0.62f, 1.f));
        background.updateRenderState();
        background.setZOrder(CovidaZOrder.getInstance().getPreload());
        GameTaskQueueManager.getManager().update(new AttachChildCallable(CovidaRootNode.node, background));
        Texture overlayDefaultTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(
                "media/textures/logo.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlayDefaultTexture.setWrap(Texture.WrapMode.Clamp);
        TextureState overlayDefaultState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlayDefaultState.setTexture(overlayDefaultTexture);
        this.logo = new Quad("Overlay-Default-Image-Quad", display.getWidth() / 2.f, display.getHeight() / 2.f);
        logo.setZOrder(CovidaZOrder.getInstance().getBackground());
        logo.setRenderState(overlayDefaultState);
        logo.setRenderState(JMEUtils.initalizeBlendState());
        logo.updateRenderState();
        logo.getLocalTranslation().set(0, 0, 0);
        logo.getLocalTranslation().set(display.getWidth() / 2, display.getHeight() / 2, 0);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(CovidaRootNode.node, logo));

    }

    /**
     * Ends the loading animation
     */
    public void endLoadingAnimation() {
        GameTaskQueueManager.getManager().update(new RemoveControllerCallable(
                preloadScreen, stPreload));
        GameTaskQueueManager.getManager().update(new DetachChildCallable(
                CovidaRootNode.node, preloadScreen));
        loginInfo = new TextComponent(this, ActionName.NONE,
                0);
        loginInfo.setFont(2);
        loginInfo.setSize(75);
        loginInfo.setText("Write to login");
        loginInfo.setLocalTranslation(getWidth() / 2, getHeight() / 2, 0);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(
                CovidaRootNode.node, loginInfo.node));
        TouchAndWriteComponentHandler.getInstance().setLogin(true);
        loginOverlay = new DrawingOverlay("Login", getWidth(), getHeight(), 0);
        loginOverlay.setLocalTranslation(getWidth() / 2, getHeight() / 2, 0);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(
                CovidaRootNode.node, loginOverlay));
        if (configuration.autologon) {
            TouchAndWriteComponentHandler.getInstance().setLogin(false);
            if (configuration.pens.isEmpty()) {
                PenData pen = new PenData();
                pen.penColor = Color.WHITE;
                pen.penThickness = 1;
                pen.userlogin = configuration.defaultlogin;
                login("1", getWidth() / 2, getHeight() / 2,
                        configuration.defaultlogin);
            } else {
                for (PenData pen : configuration.pens) {
                    pen.userlogin = configuration.defaultlogin;
                }
                login(configuration.pens.get(0).id, getWidth() / 2,
                        getHeight() / 2, configuration.defaultlogin);
            }
            background.setZOrder(CovidaZOrder.getInstance().getBackground());
            logo.setZOrder(CovidaZOrder.getInstance().getBackground());
        }
    }

    @Override
    public void draw(String id, int x, int y, boolean penUp) {
        if (loginOverlay != null) {
            loginOverlay.updateImage(x, y, id);
            if (penUp) {
                loginOverlay.endDrawStroke();
            }
        }
    }

    @Override
    public void login(String id, int x, int y, String login) {
        GameTaskQueueManager.getManager().update(new DetachChildCallable(
                CovidaRootNode.node, loginOverlay));
        GameTaskQueueManager.getManager().update(new DetachChildCallable(
                CovidaRootNode.node, loginInfo.node));
        loginInfo.detach();
        background.setZOrder(CovidaZOrder.getInstance().getBackground());
        loginText = new TextComponent(this, ActionName.NONE,
                CovidaZOrder.getInstance().getUi_text());
        loginText.setFont(1);
        loginText.setSize(18);
        loginText.setText("User: " + login);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(
                CovidaRootNode.node, loginText.node));
        loginText.setLocalTranslation(getWidth() / 2, getHeight(), 0);
        loginText.setDefaultPosition();
        loginText.setLocalTranslation(x, y, 0);
        loginText.resetAnimation();
        CovidaConfiguration.getInstance().setUser(id, login);
    }

    @Override
    public int getWidth() {
        return display.getWidth();
    }

    @Override
    public int getHeight() {
        return display.getHeight();
    }

    /**
     * Adds a {@link JMEComponent} to the {@link CovidaApplication}
     *
     * @param component
     */
    public void addComponent(ITouchAndWriteComponent component) {
        if (component instanceof VideoComponent) {
            VideoComponent video = (VideoComponent) component;
            if (video.getTitle().equals("ERmed-Cavallaro")) {
                video.setVolume(100);
            } else {
                video.setVolume(0);
            }
            video.setDefaultPosition();
        } else if (component instanceof ControlButton) {
            ControlButton button = (ControlButton) component;
            GameTaskQueueManager.getManager().update(
                    new AttachChildCallable(CovidaRootNode.node,
                    button.node));
            switch (sideMenuCount) {
                case 0:
                    button.setLocalTranslation(button.getWidth(),
                            button.getHeight(), 0);
                    button.rotate(0);
                    break;
                case 1:
                    button.setLocalTranslation(display.getWidth()
                            - button.getWidth(), button.getHeight(), 0);
                    button.rotate(45);
                    break;
                case 2:
                    button.setLocalTranslation(display.getWidth()
                            - button.getWidth(), display.getHeight()
                            - button.getHeight(), 0);
                    button.rotate(180);
                    break;
                case 3:
                    button.setLocalTranslation(button.getWidth(),
                            display.getHeight() - button.getHeight(), 0);
                    button.rotate(215);
                    break;
                case 4:
                    button.setLocalTranslation(display.getWidth() / 2,
                            display.getHeight() - button.getHeight(), 0);
                    button.rotate(-180);
                    break;
                case 5:
                    button.setLocalTranslation(display.getWidth() / 2,
                            button.getHeight(), 0);
                    button.rotate(0);
                    break;
                case 6:
                    button.setLocalTranslation(display.getWidth() - button.getWidth(),
                            display.getHeight() / 2, 0);
                    button.rotate(90);

                    break;
                case 7:
                    button.setLocalTranslation(button.getWidth(),
                            display.getHeight() / 2, 0);
                    button.rotate(-90);
                    break;
                default:
                    GameTaskQueueManager.getManager().update(
                            new DetachChildCallable(CovidaRootNode.node,
                            button.node));
                    break;
            }
            sideMenuCount++;
        }
    }

    @Override
    protected void loadingAnimation() {
        // Splash Screen
        preloadScreen = new Quad("Splash-Image-Quad", 512, 512);
        preloadScreen.setZOrder(CovidaZOrder.getInstance().getPreload() - 50);
        // set splash screen background Texture
        Texture splashTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource("media/textures/loading.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        splashTexture.setWrap(Texture.WrapMode.Repeat);
        preloadScreen.getLocalRotation().set(0, 0, 0, 1);
        preloadScreen.getLocalTranslation().set(display.getWidth() / 2,
                display.getHeight() / 2, 0);
        preloadScreen.getLocalScale().set(1, 1, 1);
        preloadScreen.setCullHint(Spatial.CullHint.Inherit);
        TextureState splashTextureState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        splashTextureState.setTexture(splashTexture);
        preloadScreen.setRenderState(splashTextureState);
        preloadScreen.setRenderState(JMEUtils.initalizeBlendState());
        preloadScreen.updateRenderState();
        GameTaskQueueManager.getManager().update(new AttachChildCallable(rootNode, preloadScreen));
        stPreload = PreloadAnimation.getController(preloadScreen);
        GameTaskQueueManager.getManager().update(new AddControllerCallable(preloadScreen, stPreload));
        preloader = new CovidaApplicationPreloader(this);
        Thread preloadThread = new Thread(preloader);
        preloadThread.start();
    }

    @Override
    protected void simpleInitGame() {
        super.simpleInitGame();
        if (scenemonitor) {
            SceneMonitor.getMonitor().registerNode(rootNode, "Root Node");
            SceneMonitor.getMonitor().showViewer(true);
        }
        if (streaming) {
            tcpServer.start();
            tcpServer.setScreenSize(new Dimension(display.getWidth(), display.getHeight()));
        }
    }

    @Override
    protected void simpleUpdate() {
        super.simpleUpdate();
        if (streaming) {
            if (System.currentTimeMillis() - snapshotTimer > 500) {
                snapshotTimer = System.currentTimeMillis();
                GameTaskQueueManager.getManager().update(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        int depth = 3;
                        ByteBuffer buff = BufferUtils.createByteBuffer(display.getWidth()
                                * display.getWidth() * depth);
                        display.getRenderer().grabScreenContents(buff, Image.Format.RGB4, 0, 0, display.getWidth(), display.getWidth());
                        if (buff != null) {
                            tcpServer.writeByteBuffer(buff, display.getWidth(), display.getHeight(), depth);
                        }
                        buff.clear();
                        return null;
                    }
                });
            }
        }
        if (scenemonitor) {
            SceneMonitor.getMonitor().updateViewer(tpf);
        }
    }

    @Override
    protected void simpleRender() {
        super.simpleRender();
        if (scenemonitor) {
            SceneMonitor.getMonitor().renderViewer(display.getRenderer());
        }
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        if (scenemonitor) {
            SceneMonitor.getMonitor().cleanup();
        }
    }

    @Override
    public boolean toggle(ActionName action) {
        VideoMediaData videoData = null;
        if (action.equals(ActionName.OPEN)) {
            for (VideoMediaData data : CovidaConfiguration.getInstance().videos) {
                boolean open = false;
                for (IVideoComponent video :
                        TouchAndWriteComponentHandler.getInstance().getVideos()) {
                    if (video.getSource().equals(input)) {
                        open = true;
                        break;
                    }
                }
                if (!open) {
                    videoData = data;
                    break;
                }
            }
            if (videoData == null) {
                int ran = random.nextInt(
                        CovidaConfiguration.getInstance().videos.size());
                videoData = CovidaConfiguration.getInstance().videos.get(ran);
            }
            VideoComponent video = new VideoComponent(videoData,
                    CovidaZOrder.getInstance().getUi_node());
            GameTaskQueueManager.getManager().update(new AttachChildCallable(
                    CovidaRootNode.node, video.node));
            video.node.setLocalTranslation(getWidth() / 2, getHeight() / 2, 0);
            video.open();
            addComponent(video);
        }
        return false;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String getName() {
        return windowtitle;
    }

    @Override
    public void addVideo(VideoMediaData data) {
        if (data == null) {
            int ran = random.nextInt(
                    CovidaConfiguration.getInstance().videos.size());
            data = CovidaConfiguration.getInstance().videos.get(ran);
        }
        VideoComponent video = new VideoComponent(data,
                CovidaZOrder.getInstance().getUi_node());
        GameTaskQueueManager.getManager().update(new AttachChildCallable(
                CovidaRootNode.node, video.node));
        video.node.setLocalTranslation(getWidth() / 2, getHeight() / 2, 0);
        video.open();
        addComponent(video);
    }

    @Override
    public void clearDrawings() {
        loginOverlay.clear();
    }

    @Override
    public void changeColor(ColorRGBA color) {
        for (ITouchAndWriteComponent comp : TouchAndWriteComponentHandler.getInstance().getComponents()) {
            if (comp instanceof ControlButton) {
                ControlButton button = (ControlButton) comp;
                button.setColor(color);
            }
            if (comp instanceof ConfigButton) {
                ConfigButton button = (ConfigButton) comp;
                button.setColor(color);
            }
        }
        background.setDefaultColor(color);
    }

    @Override
    public void applicationEndMessage() {
        background.setZOrder(CovidaZOrder.getInstance().getPreload());
        logo.setZOrder(CovidaZOrder.getInstance().getPreload());
        Texture overlayDefaultTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(
                "media/textures/goodbye.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlayDefaultTexture.setWrap(Texture.WrapMode.Clamp);
        TextureState overlayDefaultState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlayDefaultState.setTexture(overlayDefaultTexture);
        logo.setRenderState(overlayDefaultState);
        logo.updateRenderState();
        // update game state, do not use interpolation parameter
        update(-1.0f);
        // render, do not use interpolation parameter
        render(-1.0f);
        // swap buffers
        display.getRenderer().displayBackBuffer();
        long time = System.currentTimeMillis();
        ColorRGBA color = logo.getDefaultColor();
        while (System.currentTimeMillis() - time < 900) {
            color.a = 1.f - ((System.currentTimeMillis() - time) / 900.f);
            logo.setDefaultColor(color);
            // update game state, do not use interpolation parameter
            update(-1.0f);
            // render, do not use interpolation parameter
            render(-1.0f);
            // swap buffers
            display.getRenderer().displayBackBuffer();
        }
    }
}
