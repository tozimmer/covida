/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.utils;

import com.jme.scene.Controller;
import com.jme.scene.Spatial;
import java.util.concurrent.Callable;

/**
 *
 * @author Tobias
 */
public class RemoveControllerCallable implements Callable {
    private final Spatial spatial;
    private final Controller controller;
    
    public RemoveControllerCallable(Spatial spatial, Controller controller){
        this.spatial = spatial;
        this.controller = controller;
    }

    @Override
    public Void call() throws Exception {
        spatial.removeController(controller);
        return null;
    }
}
