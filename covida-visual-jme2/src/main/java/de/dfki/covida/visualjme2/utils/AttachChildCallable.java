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
public class AttachChildCallable implements Callable<Object> {
    private final Node node;
    private final Spatial spatial;
    
    public AttachChildCallable(Node node, Spatial spatial){
        this.node = node;
        this.spatial = spatial;
    }

    @Override
    public Object call() throws Exception {
        node.attachChild(spatial);
        return null;
    }
}
