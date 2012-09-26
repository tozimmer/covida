/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.utils;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Tobias
 */
public class ImageUtils {

    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public static byte[] getImageDataFromImage(BufferedImage bufferedImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
        try {
            //FileOutputStream outImg = new FileOutputStream(tempimg);
            ImageIO.write(bufferedImage, "png", baos);
        } catch (IOException ex) {
            Logger.getLogger(ImageUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] b = baos.toByteArray();
        return b;
    }

    public static BufferedImage getBufferedImage(ByteBuffer pixelsRGB, int width, int height) {
        // Transform the ByteBuffer and get it as pixeldata.

        int[] pixelInts = new int[width * height];

        // Convert RGB bytes to ARGB ints with no transparency. 
        // Flip image vertically by reading the
        // rows of pixels in the byte buffer in reverse 
        // - (0,0) is at bottom left in OpenGL.
        //
        // Points to first byte (red) in each row.
        int p = width * height * 4;
        int q; // Index into ByteBuffer
        int i = 0; // Index into target int[]
        int w3 = width * 4; // Number of bytes in each row
        for (int row = 0; row < height; row++) {
            p -= w3;
            q = p;
            for (int col = 0; col < width; col++) {
                int iR = pixelsRGB.get(q++);
                int iG = pixelsRGB.get(q++);
                int iB = pixelsRGB.get(q++);
                int a = pixelsRGB.get(q++);
                pixelInts[i++] = ((a & 0xFF) << 24) //a
                       | ((iR & 0xFF) << 16) //r
                         | ((iG & 0xFF) << 8)  //g
                         | ((iB & 0xFF));      //b
            }
        }

        // Create a new BufferedImage from the pixeldata.
        BufferedImage bufferedImage =
                new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        bufferedImage.setRGB(0, 0, width, height,
                pixelInts, 0, height);

        return bufferedImage;
    }
}
