/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.test;

import de.dfki.covida.videovlcj.AbstractVideoHandler;
import java.awt.Point;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tobias
 */
public class DrawTest implements Runnable {
    private final AbstractVideoHandler video;
    
    public DrawTest(AbstractVideoHandler video){
        this.video = video;
    }

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(DrawTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            video.draw(new Point((int) (Math.random()*video.getWidth()), (int) (Math.random()*video.getHeight())));
        }
    }
    
}
