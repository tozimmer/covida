/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidavisualswing;


import de.dfki.covida.videovlcj.embedded.EmbeddedVideoHandler;
import java.awt.Canvas;
import java.awt.GridLayout;
import javax.swing.JFrame;

/**
 *
 * @author Tobias
 */
public class VideoFrame extends JFrame {

    public VideoFrame(String title) {
        super(title);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        Canvas canvas = new Canvas();
        EmbeddedVideoHandler video = new EmbeddedVideoHandler("videos/Collaborative Video Annotation.mp4", "Covida Demo", canvas, 500, 400);
        
        Canvas canvas2 = new Canvas();
        EmbeddedVideoHandler video2 = new EmbeddedVideoHandler("videos/Collaborative Video Annotation.mp4", "Covida Demo", canvas2, 500, 400);

        
        getContentPane().setLayout(new GridLayout(2,2));
        getContentPane().add(canvas,0);
        getContentPane().add(canvas2,1);
//        getContentPane().add(canvas,2);
//        getContentPane().add(canvas2,3);
        setSize(1280, 800);
        setVisible(true);
        video.setRepeat(true);
        video.setRepeat(true);
        video.start();
        video2.start();
    }
}
