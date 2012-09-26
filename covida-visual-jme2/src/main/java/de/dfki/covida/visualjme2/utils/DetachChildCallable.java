/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.utils;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import java.util.concurrent.Callable;

/**
 *
 * @author Tobias
 */
public class DetachChildCallable implements Callable {
    private final Node node;
    private final Spatial spatial;
    
    public DetachChildCallable(Node node, Spatial spatial){
        this.node = node;
        this.spatial = spatial;
    }

    @Override
    public Void call() throws Exception {
        node.detachChild(spatial);
        return null;
    }
}
