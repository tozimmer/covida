/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.test;

import de.dfki.covida.visualjme2.components.video.VideoComponent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tobias
 */
public class AttachTest implements Runnable {
    private final VideoComponent video;
    
    public AttachTest(VideoComponent video){
        this.video = video;
    }

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(650);
            } catch (InterruptedException ex) {
                Logger.getLogger(DrawTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            video.attachAnnotation();
            try {
                Thread.sleep(650);
            } catch (InterruptedException ex) {
                Logger.getLogger(DrawTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            video.attachList();
            try {
                Thread.sleep(650);
            } catch (InterruptedException ex) {
                Logger.getLogger(DrawTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            video.detachAnnotation();
            try {
                Thread.sleep(650);
            } catch (InterruptedException ex) {
                Logger.getLogger(DrawTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            video.detachList();
        }
    }
    
    
}
