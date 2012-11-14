/*
 * ImageUtils.java
 *
 * Copyright (c) 2012, Tobias Zimmermann All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.dfki.covida.covidaflvcreator.utils;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Image utility class
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class ImageUtils {

    /**
     * Creates a new instance of {@link BufferedImage} of the given
     * {@link BufferedImage}
     *
     * @param bi {@link BufferedImage}
     * @return {@link BufferedImage}
     */
    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    /**
     * Converts a {@link BufferedImage} in an Array of {@link Byte}.
     *
     * @param bufferedImage {@link BufferedImage}
     * @return Array of {@link Byte}
     */
    public static byte[] getImageDataFromImage(BufferedImage bufferedImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
        try {
            ImageIO.write(bufferedImage, "png", baos);
        } catch (IOException ex) {
            Logger.getLogger(ImageUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] b = baos.toByteArray();
        return b;
    }

    /**
     * Converts {@link ByteBuffer} and width / height to {@link BufferedImage}
     *
     * @param pixelsRGB {@link ByteBuffer}
     * @param width {@link Integer}
     * @param height {@link Integer}
     * @return {@link BufferedImage}
     */
    public static BufferedImage getBufferedImage(ByteBuffer pixelsRGB, int width, int height) {
        /**
         * Transform the ByteBuffer and get it as pixeldata.
         */
        int[] pixelInts = new int[width * height];
        /**
         * Convert RGB bytes to ARGB ints with no transparency. Flip image
         * vertically by reading the rows of pixels in the byte buffer in
         * reverse - (0,0) is at bottom left in OpenGL.
         *
         * Points to first byte (red) in each row.
         */
        int p = width * height * 4;
        /**
         * Index into ByteBuffer
         */
        int q;
        /**
         * Index into target int[]
         */
        int i = 0;
        /**
         * Number of bytes in each row
         */
        int w3 = width * 4;
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
                        | ((iG & 0xFF) << 8) //g
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

    // This method returns a buffered image with the contents of an image
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();

        // Determine if the image has transparent pixels; for this method's
        // implementation, see Determining If an Image Has Transparent Pixels
        boolean hasAlpha = hasAlpha(image);

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }

            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                    image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }

        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;
    }

    // This method returns true if the specified image has transparent pixels
    public static boolean hasAlpha(Image image) {
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage) image;
            return bimage.getColorModel().hasAlpha();
        }

        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        }

        // Get the image's color model
        ColorModel cm = pg.getColorModel();
        return cm.hasAlpha();
    }

    /**
     * Convert a {@link BufferedImage} of any type, to {@link BufferedImage} of
     * a specified type. If the source image is the same type as the target
     * type, then original image is returned, otherwise new image of the correct
     * type is created and the content of the source image is copied into the
     * new image.
     *
     * @param sourceImage the image to be converted
     * @param targetType the desired BufferedImage type
     *
     * @return a BufferedImage of the specifed target type.
     *
     * @see BufferedImage
     */
    public static BufferedImage convertToType(BufferedImage sourceImage,
            int targetType) {
        BufferedImage image;

        // if the source image is already the target type, return the source image

        if (sourceImage.getType() == targetType) {
            image = sourceImage;
        } // otherwise create a new image of the target type and draw the new
        // image
        else {
            image = new BufferedImage(sourceImage.getWidth(),
                    sourceImage.getHeight(), targetType);
            image.getGraphics().drawImage(sourceImage, 0, 0, null);
        }

        return image;
    }
}
