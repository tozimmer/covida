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
package de.dfki.covida.covidacore.utils;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

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
     * Converts a {@link BufferedImage} in an {@link Array} of {@link Byte}.
     * 
     * @param bufferedImage {@link BufferedImage}
     * @return {@link Array} of {@link Byte}
     */
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
         * Convert RGB bytes to ARGB ints with no transparency. 
         * Flip image vertically by reading the
         * rows of pixels in the byte buffer in reverse 
         * - (0,0) is at bottom left in OpenGL.
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
