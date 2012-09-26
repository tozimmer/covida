/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.animations;

import com.jme.animation.SpatialTransformer;
import com.jme.scene.Spatial;

/**
 *
 * @author Tobias
 */
public class CovidaSpatialController extends SpatialTransformer{
    private float lifetime;
    private Spatial spatial;
    
    public CovidaSpatialController(Spatial spatial, float lifetime){
        super(1);
        this.lifetime = lifetime;
        this.spatial = spatial;
    }
    
    /**
     * The update Method gets called every frame.
     * Moves the object upwards and a bit to the left or right.
     * When the lifetime of the object is finished, it gets removed from the scene.
     * An explosion is spawned when the object dies.
     */
    @Override
    public void update(float time) {
        super.update(time);
        lifetime -= time;
        // add hSpeed to the X-Axis and speed to the Y-Axis
        if (lifetime <= 0) {
            // the life has come to an end
            // remove this controller from the object and the object from the scene 
            setActive(false);
        }
    }
}
