/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.test;

import com.jme.math.Vector3f;
import de.dfki.covida.visualjme2.components.video.VideoComponent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tobias
 */
public class RotateTest implements Runnable {

    private final VideoComponent video;

    public RotateTest(VideoComponent video) {
        this.video = video;
    }

    @Override
    public void run() {
        while (true) {
//            Vector3f vec = new Vector3f(0, 0, 1);
//            while (video.getLocalRotation().toAngleAxis(vec) < 360) {
//                try {
//                    Thread.sleep(250);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(DrawTest.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                video.rotate(1.f, vec);
//            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(DrawTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            video.reset(45);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(DrawTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            video.reset(0);
        }
    }
}
