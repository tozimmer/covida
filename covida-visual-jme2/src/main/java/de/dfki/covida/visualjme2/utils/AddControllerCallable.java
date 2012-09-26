/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.utils;

import com.jme.scene.Controller;
import com.jme.scene.Spatial;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author Tobias
 */
public class AddControllerCallable implements Callable {

    private final Spatial spatial;
    private final Controller controller;

    public AddControllerCallable(Spatial spatial, Controller controller) {
        this.spatial = spatial;
        this.controller = controller;
    }

    @Override
    public Void call() throws Exception {
        List<Controller> toRemove = new ArrayList<>();
        for (Iterator<Controller> it = spatial.getControllers().iterator(); it.hasNext();) {
            Controller c = it.next();
            toRemove.add(c);
        }
        for(Controller c : toRemove){
            spatial.removeController(c);
        }
        spatial.addController(controller);
        return null;
    }
}
