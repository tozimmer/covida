/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidastreamclient;

import de.dfki.covida.covidacore.utils.ImageUtils;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import org.apache.log4j.Logger;

public class DrawImageOnJFrame extends JFrame implements IStreamingClient {

    /**
     * Logger
     */
    private static final Logger log = Logger.getLogger(DrawImageOnJFrame.class);
    int w = 1152;
    int h = 864;
    Image imageToBeDraw;
    ImageIcon ii;

    public DrawImageOnJFrame() {
        //set JFrame title
        super("Draw Image On JFrame");

        RemoteStreamingApplication app = RemoteStreamingApplication.getInstance();
        app.addListener(this);
        app.connect();

        //Get image. You must change image location follow to image location in your computer.
        imageToBeDraw = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        //Create an ImageIcon object
        ii = new ImageIcon(imageToBeDraw);

        //set close operation for JFrame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //set JFrame size follow the image size
        setSize(ii.getIconWidth(), ii.getIconHeight());

        //make you JFrame cannot resizable
        setResizable(false);

        //make JFrame visible. So we can see it.
        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        //This will draw drawImageIntoJFrame.jpg into JFrame
        g.drawImage(imageToBeDraw, 0, 0, this);
    }

    public static void main(String[] args) {
        DrawImageOnJFrame diojf = new DrawImageOnJFrame();
    }

    @Override
    public void onNewFrame(byte[] bytes) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        log.debug("New Frame :" + bytes.length);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int index = 4 * ((h - y - 1) * w + x);
                int r = ((int) (bytes[index + 0]));
                int g = ((int) (bytes[index + 1]));
                int b = ((int) (bytes[index + 2]));
                int a = ((int) (bytes[index + 3]));
                int argb = ((a & 0xFF) << 24) //a
                        | ((r & 0xFF) << 16) //r
                        | ((g & 0xFF) << 8) //g
                        | ((b & 0xFF));      //b

                img.setRGB(x, y, argb);
            }
        }
        imageToBeDraw = ImageUtils.deepCopy(img);
    }

    @Override
    public void setScreenSize(Dimension dimension) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
