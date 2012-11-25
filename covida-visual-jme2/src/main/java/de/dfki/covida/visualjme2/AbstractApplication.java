/*
 * AbstractApplication.java
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

import com.jme.app.AbstractGame;
import com.jme.input.*;
import com.jme.input.joystick.JoystickInput;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.state.LightState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.system.GameSettings;
import com.jme.system.JmeException;
import com.jme.system.PropertiesGameSettings;
import com.jme.util.*;
import com.jme.util.stat.StatCollector;
import com.jmex.audio.AudioSystem;
import de.dfki.covida.covidacore.tw.IApplication;
import de.dfki.covida.visualjme2.utils.CovidaRootNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract application
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public abstract class AbstractApplication extends AbstractGame implements IApplication {

    /**
     * {@link ThrowableHandler} to handle exception throws.
     */
    protected ThrowableHandler throwableHandler;
    /**
     * {@link InputHandler} to handle keyboard and mouse input.
     */
    protected InputHandler input;
    /**
     * Logger
     */
    protected Logger log;
    /**
     * The camera that we see through.
     */
    protected Camera cam;
    /**
     * The root of our normal scene graph. (Renders first/bottom)
     */
    protected Node rootNode;
    /**
     * High resolution timer for jME.
     */
    protected Timer timer;
    /**
     * Alpha bits to use for the renderer. Any changes must be made prior to
     * call of start().
     */
    protected int alphaBits = 0;
    /**
     * Depth bits to use for the renderer. Any changes must be made prior to
     * call of start().
     */
    protected int depthBits = 8;
    /**
     * Stencil bits to use for the renderer. Any changes must be made prior to
     * call of start().
     */
    protected int stencilBits = 0;
    /**
     * Number of samples to use for the multisample buffer. Any changes must be
     * made prior to call of start().
     */
    protected int samples = 0;
    /**
     * Simply an easy way to get at timer.getTimePerFrame(). Also saves math
     * cycles since you don't call getTimePerFrame more than once per frame.
     */
    protected float tpf;
    /**
     * A lightstate to turn on and off for the rootNode
     */
    protected LightState lightState;
    /**
     * boolean for toggling the simpleUpdate and geometric update parts of the
     * game loop on and off.
     */
    protected boolean pause;
    /**
     * Window title of the application
     */
    protected String windowtitle;

    /**
     * Updates the timer, sets tpf, updates the input and updates the fps
     * string. Also checks keys for toggling pause, bounds, normals, lights,
     * wire etc.
     *
     * @param interpolation unused in this implementation
     * @see AbstractGame#update(float interpolation)
     */
    @Override
    protected void update(float interpolation) {
        /**
         * Recalculate the framerate.
         */
        timer.update();
        /**
         * Update tpf to time per frame according to the Timer.
         */
        tpf = timer.getTimePerFrame();

        /**
         * update stats, if enabled.
         */
        if (Debug.stats) {
            StatCollector.update();
        }

        // Execute updateQueue item
        GameTaskQueue update = GameTaskQueueManager.getManager().getQueue(GameTaskQueue.UPDATE);
        update.setExecuteAll(true);
        update.execute();

        if (!pause) {
            /**
             * Call simpleUpdate in any derived classes of SimpleGame.
             */
            simpleUpdate();
            try {
                /**
                 * Update controllers/render states/transforms/bounds for
                 * rootNode.
                 */
                rootNode.updateGeometricState(tpf, true);
            } catch (IndexOutOfBoundsException e) {
                log.warn(e.toString());
            }
        }
    }

    /**
     * Clears stats, the buffers and renders bounds and normals if on.
     *
     * @param interpolation unused in this implementation
     * @see AbstractGame#render(float interpolation)
     */
    @Override
    protected void render(float interpolation) {
        Renderer r = display.getRenderer();
        /**
         * Clears the previously rendered information.
         */
        r.clearBuffers();



        try {
            /**
             * Draw the rootNode and all its children.
             */
            r.draw(rootNode);
        } catch (IndexOutOfBoundsException e) {
            log.warn(e.toString());
        }

        /**
         * Call simpleRender() in any derived classes.
         */
        simpleRender();
        // Execute renderQueue item
        GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER).execute();
    }

    protected void initSystemSubApp(DisplaySystem disp, Camera camera) throws JmeException {
        try {
            /**
             * Get a DisplaySystem acording to the renderer selected in the
             * startup box.
             */
            display = disp;
            cam = camera;

        } catch (JmeException e) {
            /**
             * If the displaysystem can't be initialized correctly, exit
             * instantly.
             */
            log.warn("Could not create displaySystem", e);
            //TODO exit or quit?
            System.exit(1);
        }

        cameraPerspective();
        cameraSetFrame();

        input = new KeyboardLookHandler(cam, 50, 1);

        /**
         * Get a high resolution timer for FPS updates.
         */
        timer = Timer.getTimer();

        /**
         * Sets the title of our display.
         */
        String className = getClass().getName();
        if (className.lastIndexOf('.') > 0) {
            className = className.substring(className.lastIndexOf('.') + 1);
        }
        display.setTitle(className);
    }

    public void takeScreenshot(String target) {
        display.getRenderer().takeScreenShot(target);
    }

    /**
     * Creates display, sets up camera, and binds keys. Called in
     * BaseGame.start() directly after the dialog box.
     *
     * @see AbstractGame#initSystem()
     */
    @Override
    protected void initSystem() throws JmeException {
        log.info(getVersion());
        try {
            /**
             * Get a DisplaySystem acording to the renderer selected in the
             * startup box.
             */
            display = DisplaySystem.getDisplaySystem(settings.getRenderer());
            display.setMinDepthBits(depthBits);
            display.setMinStencilBits(stencilBits);
            display.setMinAlphaBits(alphaBits);
            display.setMinSamples(samples);

            /**
             * Create a window with the startup box's information.
             */
            display.createWindow(settings.getWidth(), settings.getHeight(),
                    settings.getDepth(), settings.getFrequency(), settings.isFullscreen());
            log.info("Running on: " + display.getAdapter());
            log.info("Driver version: " + display.getDriverVersion());
            log.info(display.getDisplayVendor() + " - " + display.getDisplayRenderer() + " - " + display.getDisplayAPIVersion());

            /**
             * Create a camera specific to the DisplaySystem that works with the
             * display's width and height
             */
            cam = display.getRenderer().createCamera(display.getWidth(),
                    display.getHeight());
        } catch (JmeException e) {
            /**
             * If the displaysystem can't be initialized correctly, exit
             * instantly.
             */
            log.warn("Could not create displaySystem", e);
            System.exit(1);
        }
        /**
         * Set a black background.
         */
        display.getRenderer().setBackgroundColor(ColorRGBA.black.clone());
        /**
         * Set up how our camera sees.
         */
        cameraPerspective();
        cameraSetFrame();
        /**
         * Assign the camera to this renderer.
         */
        display.getRenderer().setCamera(cam);
        input = new KeyboardLookHandler(cam, 50, 1);
        /**
         * Get a high resolution timer for FPS updates.
         */
        timer = Timer.getTimer();
        display.setTitle(windowtitle);
    }

    protected void cameraPerspective() {
        cam.setFrustumPerspective(45.0f, (float) display.getWidth()
                / (float) display.getHeight(), 1, 1000);
        cam.setParallelProjection(false);
        cam.update();
    }

    protected void cameraSetFrame() {
        Vector3f loc = new Vector3f(0.0f, 0.0f, 25.0f);
        Vector3f left = new Vector3f(-1.0f, 0.0f, 0.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f dir = new Vector3f(0.0f, 0f, -1.0f);
        /**
         * Move our camera to a correct place and orientation.
         */
        cam.setFrame(loc, left, up, dir);
        /**
         * Signal that we've changed our camera's location/frustum.
         */
        cam.update();
    }

    protected void cameraParallel() {
        cam.setParallelProjection(true);
        float aspect = (float) display.getWidth() / display.getHeight();
        cam.setFrustum(-100, 1000, -50 * aspect, 50 * aspect, -50, 50);
        cam.update();
    }

    /**
     * Creates rootNode, lighting, statistic text, and other basic render
     * states. Called in BaseGame.start() after initSystem().
     *
     * @see AbstractGame#initGame()
     */
    @Override
    protected void initGame() {
        rootNode = CovidaRootNode.node;
        rootNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);

        /**
         * Create a ZBuffer to display pixels closest to the camera above
         * farther ones.
         */
        ZBufferState orthoBuf = display.getRenderer().createZBufferState();
        orthoBuf.setEnabled(true);
        orthoBuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        ZBufferState buf = display.getRenderer().createZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        rootNode.setRenderState(buf);

        /**
         * Attach the light to a lightState and the lightState to rootNode.
         */
        lightState = display.getRenderer().createLightState();
        lightState.setEnabled(false);
        rootNode.setRenderState(lightState);

        /**
         * Let derived classes initialize.
         */
        simpleInitGame();

        timer.reset();


        /**
         * Update geometric and rendering information for both the rootNode and
         * fpsNode.
         */
        try {
            rootNode.updateGeometricState(0.0f, true);
            rootNode.updateRenderState();
        } catch (IndexOutOfBoundsException e) {
            log.warn(e.toString());
        }

        timer.reset();
        setBackground();
    }

    /**
     * Called near end of initGame(). Must be defined by derived classes.
     */
    protected abstract void simpleInitGame();

    /**
     * Can be defined in derived classes for custom updating. Called every frame
     * in update.
     */
    protected void simpleUpdate() {
        // do nothing
    }

    /**
     * Can be defined in derived classes for custom rendering. Called every
     * frame in render.
     */
    protected void simpleRender() {
        // do nothing
    }

    /**
     * unused
     *
     * @see AbstractGame#reinit()
     */
    @Override
    protected void reinit() {
        // do nothing
    }

    /**
     * Cleans up the keyboard.
     *
     * @see AbstractGame#cleanup()
     */
    @Override
    protected void cleanup() {
        log.info("Cleaning up resources.");

        TextureManager.doTextureCleanup();
        if (display != null && display.getRenderer() != null) {
            display.getRenderer().cleanup();
        }
        KeyInput.destroyIfInitalized();
        MouseInput.destroyIfInitalized();
        JoystickInput.destroyIfInitalized();
        if (AudioSystem.isCreated()) {
            AudioSystem.getSystem().cleanup();
        }
    }

    /**
     * The simplest main game loop possible: render and update as fast as
     * possible.
     */
    @Override
    public final void start() {
        log = LoggerFactory.getLogger(getClass());
        log.info("Application started.");
        try {
            getAttributes();
            if (!finished) {
                initSystem();
                assertDisplayCreated();
                DisplaySystem.getSystemProvider().getDisplaySystem();
                initGame();
                // MouseInput.get().addListener(this.clientManager);
                // main loop
                while (!finished && !display.isClosing()) {
                    // update game state, do not use interpolation parameter
                    update(-1.0f);
                    // render, do not use interpolation parameter
                    render(-1.0f);
                    // swap buffers
                    display.getRenderer().displayBackBuffer();
                    Thread.yield();
                }
                // update game state, do not use interpolation parameter
                update(-1.0f);
                // render, do not use interpolation parameter
                render(-1.0f);
                // swap buffers
                display.getRenderer().displayBackBuffer();
            }
        } catch (Throwable t) {
            log.error(this.getClass().toString() + "start()"
                    + "Exception in game loop", t);
            if (throwableHandler != null) {
                throwableHandler.handle(t);
            }
        }
        applicationEndMessage();
        cleanup();
        log.info("Application ending.");

        if (display != null) {
            display.reset();
        }
        quit();
    }

    @Override
    public void close() {
        finished = true;
    }

    public void startSubApp(DisplaySystem display, Camera cam) {
        try {
            getAttributes();

            if (!finished) {
                initSystemSubApp(display, cam);

                assertDisplayCreated();

                initGame();
                // MouseInput.get().addListener(this.clientManager);
                // main loop
                while (!finished && !display.isClosing()) {

                    // update game state, do not use interpolation parameter
                    update(-1.0f);

                    // render, do not use interpolation parameter
                    render(-1.0f);

                    // swap buffers
                    display.getRenderer().displayBackBuffer();

                    Thread.yield();
                }
            }
        } catch (Throwable t) {
            log.error(this.getClass().toString() + "start()"
                    + "Exception in game loop", t);
            if (throwableHandler != null) {
                throwableHandler.handle(t);
            }
        }

        //cleanup();
        log.info("Application ending.");
    }

    /**
     * Closes the display
     *
     * @see AbstractGame#quit()
     */
    @Override
    protected void quit() {
        if (display != null) {
            display.close();
        }
        System.exit(0);
    }

    /**
     * Get the exception handler if one hs been set.
     *
     * @return the exception handler, or {@code null} if not set.
     */
    protected ThrowableHandler getThrowableHandler() {
        return throwableHandler;
    }

    /**
     *
     * @param throwableHandler
     */
    protected void setThrowableHandler(ThrowableHandler throwableHandler) {
        this.throwableHandler = throwableHandler;
    }

    /**
     * @see AbstractGame#getNewSettings()
     */
    @Override
    protected GameSettings getNewSettings() {
        return new BaseGameSettings();
    }

    @Override
    public String getWindowTitle() {
        return windowtitle;
    }

    public abstract void setBackground();

    @Override
    public abstract boolean isReady();

    public abstract void applicationEndMessage();

    public abstract void changeColor(ColorRGBA color);

    /**
     * A PropertiesGameSettings which defaults Fullscreen to TRUE.
     */
    static class BaseGameSettings extends PropertiesGameSettings {

        static {
            // This is how you programmatically override the DEFAULT_*
            // settings of GameSettings.
            // You can also make declarative overrides by using
            // "game-defaults.properties" in a CLASSPATH root directory (or
            // use the 2-param PropertiesGameSettings constructor for any name).
            // (This is all very different from the user-specific
            // "properties.cfg"... or whatever file is specified below...,
            // which is read from the current directory and is
            // session-specific).
            defaultFullscreen = Boolean.TRUE;

            defaultSettingsWidgetImage = "icon-512.png";
        }

        /**
         * Populates the GameSettings from the (session-specific) .properties
         * file.
         */
        BaseGameSettings() {
            super("properties.cfg");
            load();
        }
    }
}
