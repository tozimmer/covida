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
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.scene.Spatial;
import com.jme.scene.TexCoords;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.TextureManager;
import com.jme.util.geom.BufferUtils;
import de.dfki.covida.covidacore.data.CovidaConfiguration;
import de.dfki.covida.covidacore.data.VideoMediaData;
import de.dfki.covida.covidacore.streaming.TCPServer;
import de.dfki.covida.covidacore.tw.ITouchAndWriteComponent;
import de.dfki.covida.visualjme2.animations.PreloadAnimation;
import de.dfki.covida.visualjme2.components.ControlButton;
import de.dfki.covida.visualjme2.components.JMEComponent;
import de.dfki.covida.visualjme2.components.video.VideoComponent;
import de.dfki.covida.visualjme2.utils.*;
import de.dfki.touchandwrite.TouchAndWriteDevice;
import java.awt.Dimension;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Component to display videos.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class CovidaApplication extends ApplicationImpl {

    /**
     * List of all video sources as {@link String}
     */
    private List<VideoMediaData> videoSources;
    /**
     * List of {@link VideoComponent}s which are added to the
     * {@link CovidaApplication}
     */
    private List<VideoComponent> videos;
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

    /**
     * Creates an instance of {@link CovidaApplication}
     *
     * @param device {@link TouchAndWriteDevice}
     * @param windowtitle {@link String} which represents the title of the
     * application window
     */
    public CovidaApplication(TouchAndWriteDevice device, String windowtitle) {
        super(device, windowtitle);
        videoSources = new ArrayList<>();
        CovidaConfiguration configuration = CovidaConfiguration.load("../covida-res/config.xml");
        videoSources = configuration.videoSources;
        videos = new ArrayList<>();
        if (streaming) {
            tcpServer = TCPServer.getInstance();
        }
        snapshotTimer = System.currentTimeMillis();
        sideMenuCount = 0;
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
        // set background Texture
        Texture backgroundTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource("media/textures/1280x800.jpg"),
                Texture.MinificationFilter.BilinearNoMipMaps,
                Texture.MagnificationFilter.Bilinear);
        backgroundTexture.setWrap(Texture.WrapMode.Clamp);
        Vector2f[] texCoords = new Vector2f[4];
        texCoords[0] = new Vector2f(0, 0);
        texCoords[3] = new Vector2f(1, 0);
        texCoords[1] = new Vector2f(0, 1);
        texCoords[2] = new Vector2f(1, 1);
        background.setTextureCoords(TexCoords.makeNew(texCoords));
        TextureState backgroundTextureState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        backgroundTextureState.setTexture(backgroundTexture);
        background.setRenderState(backgroundTextureState);
        background.updateRenderState();
        background.setZOrder(CovidaZOrder.preload);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(CovidaRootNode.node, background));
        Texture overlayDefaultTexture = TextureManager.loadTexture(
                getClass().getClassLoader().getResource(
                "media/textures/logo.png"),
                Texture.MinificationFilter.BilinearNearestMipMap,
                Texture.MagnificationFilter.Bilinear);
        overlayDefaultTexture.setWrap(Texture.WrapMode.Clamp);
        TextureState overlayDefaultState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        overlayDefaultState.setTexture(overlayDefaultTexture);
        this.logo = new Quad("Overlay-Default-Image-Quad", 512, 512);
        logo.setZOrder(CovidaZOrder.background-1);
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
        for(VideoComponent video : videos){
            video.setTimePosition(0);
        }
        GameTaskQueueManager.getManager().update(new RemoveControllerCallable(preloadScreen, stPreload));
        GameTaskQueueManager.getManager().update(new DetachChildCallable(CovidaRootNode.node, preloadScreen));
        preloader.cleanUp();
        background.setZOrder(CovidaZOrder.background);
    }

    public int getWidth() {
        return display.getWidth();
    }

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
            int x = (int) (display.getWidth() / 2.25f) + (videos.size() * display.getWidth() / 3);
            int y = (display.getHeight() / 2 + 150)
                    - (videos.size() * 350);
            videos.add(video);
            video.setVolume(0);
            video.setLocalTranslation(x, y, 0);
            video.setDefaultPosition();
            video.setRepeat(true);
            video.start();
            video.toFront();
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
                    button.setLocalTranslation(button.getWidth(),
                            display.getHeight() / 2, 0);
                    button.rotate(-90);
                    break;
                case 6:
                    button.setLocalTranslation(display.getWidth() - button.getWidth(),
                            display.getHeight() / 2, 0);
                    button.rotate(90);

                    break;
                case 7:
                    button.setLocalTranslation(display.getWidth() / 2,
                            button.getHeight(), 0);
                    button.rotate(0);
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

    /**
     * Returns a list of {@link VideoMediaData}
     *
     * @return list of {@link VideoMediaData}
     */
    public List<VideoMediaData> getVideoSources() {
        return videoSources;
    }

    @Override
    protected void loadingAnimation() {
        // Splash Screen
        preloadScreen = new Quad("Splash-Image-Quad", 512, 512);
        preloadScreen.setZOrder(CovidaZOrder.preload-1);
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
}
