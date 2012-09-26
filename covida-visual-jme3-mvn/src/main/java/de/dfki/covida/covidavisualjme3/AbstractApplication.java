/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidavisualjme3;

import com.jme3.app.Application;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;

/**
 *
 * @author Tobias
 */
public abstract class AbstractApplication extends Application {

    protected Node rootNode = new Node("Root Node");
    protected Node guiNode = new Node("Gui Node");
    protected boolean showSettings = true;

    public AbstractApplication() {
        super();
    }

    @Override
    public void start() {
        // set some default settings in-case
        // settings dialog is not shown
        boolean loadSettings = false;
        if (settings == null) {
            setSettings(new AppSettings(true));
            loadSettings = true;
        }

        // show settings dialog
        if (showSettings) {
            if (!JmeSystem.showSettingsDialog(settings, loadSettings)) {
                return;
            }
        }
        //re-setting settings they can have been merged from the registry.
        setSettings(settings);
        super.start();
    }
    
    @Override
    public void stop() {
        super.stop();
        System.exit(1);
    }

    /**
     * Retrieves guiNode
     *
     * @return guiNode Node object
     *
     */
    public Node getGuiNode() {
        return guiNode;
    }

    /**
     * Retrieves rootNode
     *
     * @return rootNode Node object
     *
     */
    public Node getRootNode() {
        return rootNode;
    }

    public boolean isShowSettings() {
        return showSettings;
    }

    /**
     * Toggles settings window to display at start-up
     *
     * @param showSettings Sets true/false
     *
     */
    public void setShowSettings(boolean showSettings) {
        this.showSettings = showSettings;
    }

    @Override
    public void initialize() {
        super.initialize();
        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);
        cam.setParallelProjection(true);
        // call user code
        simpleInitApp();
    }

    @Override
    public void update() {
        super.update(); // makes sure to execute AppTasks
        if (speed == 0 || paused) {
            return;
        }
        float tpf = timer.getTimePerFrame() * speed;
        // update states
        stateManager.update(tpf);
        // simple update and root node
        simpleUpdate(tpf);
        rootNode.updateLogicalState(tpf);
        guiNode.updateLogicalState(tpf);
        rootNode.updateGeometricState();
        guiNode.updateGeometricState();
        // render states
        stateManager.render(renderManager);
        renderManager.render(tpf, true);
        simpleRender(renderManager);
        stateManager.postRender();
    }

    public abstract void simpleInitApp();

    public void simpleUpdate(float tpf) {
    }

    public void simpleRender(RenderManager rm) {
    }
}
