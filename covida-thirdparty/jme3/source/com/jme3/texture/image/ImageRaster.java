package com.jme3.texture.image;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.system.JmeSystem;
import com.jme3.texture.Image;
import java.nio.ByteBuffer;

/**
 * Utility class for reading and writing from jME3 {@link Image images}.
 * <br>
 * Allows directly manipulating pixels of the image by writing and 
 * reading {@link ColorRGBA colors} at any coordinate, without
 * regard to the underlying {@link Image.Format format} of the image.
 * NOTE: compressed and depth formats are <strong>not supported</strong>.
 * Special RGB formats like RGB111110F and RGB9E5 are not supported
 * at the moment, but may be added later on. For now 
 * use RGB16F_to_RGB111110F and RGB16F_to_RGB9E5 to handle
 * the conversion on the GPU.
 * <p>
 * If direct manipulations are done to the image, such as replacing
 * the image data, or changing the width, height, or format, then
 * all current instances of <code>ImageReadWrite</code> become invalid, and
 * new instances must be created in order to properly access
 * the image data.
 * 
 * Usage example:<br>
 * <code>
 * Image myImage = ...
 * ImageRaster raster = ImageRaster.create(myImage);
 * raster.setPixel(1, 5, ColorRGBA.Green);
 * System.out.println( raster.getPixel(1, 5) ); // Will print [0.0, 1.0, 0.0, 1.0].
 * </code>
 * 
 * @author Kirill Vainer
 */
public abstract class ImageRaster {

    /**
     * Create new image reader / writer.
     *
     * @param image The image to read / write to.
     * @param slice Which slice to use. Only applies to 3D images, 2D image
     * arrays or cubemaps.
     */
    public static ImageRaster create(Image image, int slices) {
        return JmeSystem.createImageRaster(image, slices);
    }
    
    /**
     * Create new image reader / writer for 2D images.
     * 
     * @param image The image to read / write to.
     */
    public static ImageRaster create(Image image) {
        if (image.getData().size() > 1) {
            throw new IllegalStateException("Use constructor that takes slices argument to read from multislice image");
        }
        return JmeSystem.createImageRaster(image, 0);
    }
    
    public ImageRaster() {
    }
    
    /**
     * Sets the pixel at the given coordinate to the given color.
     * <p>
     * For all integer based formats (those not ending in "F"), the 
     * color is first clamped to 0.0 - 1.0 before converting it to
     * an integer to avoid overflow. For floating point based formats, 
     * components larger than 1.0 can be represented, but components
     * lower than 0.0 are still not allowed (as all formats are unsigned).
     * <p>
     * If the underlying format is grayscale (e.g. one of the luminance formats,
     * such as {@link Image.Format#Luminance8}) then a color to grayscale
     * conversion is done first, before writing the result into the image.
     * <p>
     * If the image does not have some of the components in the color (such
     * as alpha, or any of the color components), then these components
     * will be ignored. The only exception to this is luminance formats
     * for which the color is converted to luminance first (see above).
     * <p>
     * After writing the color, the image shall be marked as requiring an
     * update. The next time it is used for rendering, all pixel changes
     * will be reflected when the image is rendered.
     * 
     * @param x The x coordinate, from 0 to width - 1.
     * @param y The y coordinate, from 0 to height - 1.
     * @param color The color to write. 
     * @throws IllegalArgumentException If x or y are outside the image dimensions.
     */
    public abstract void setPixel(int x, int y, ColorRGBA color);
    
    /**
     * Retrieve the color at the given coordinate.
     * <p>
     * Any components that are not defined in the image format
     * will be set to 1.0 in the returned color. For example,
     * reading from an {@link Image.Format#Alpha8} format will
     * return a ColorRGBA with the R, G, and B components set to 1.0, and
     * the A component set to the alpha in the image.
     * <p>
     * For grayscale or luminance formats, the luminance value is replicated
     * in the R, G, and B components. 
     * <p>
     * Integer formats are converted to the range 0.0 - 1.0, based
     * on the maximum possible integer value that can be represented
     * by the number of bits the component has.
     * For example, the {@link Image.Format#RGB5A1} format can
     * contain the integer values 0 - 31, a conversion to floating point
     * is done by diving the integer value by 31 (done with floating point
     * precision).
     * 
     * @param x The x coordinate, from 0 to width - 1.
     * @param y The y coordinate, from 0 to height - 1.
     * @param store Storage location for the read color, if <code>null</code>, 
     * then a new ColorRGBA is created and returned with the read color.
     * @return The store parameter, if it is null, then a new ColorRGBA
     * with the read color.
     * @throws IllegalArgumentException If x or y are outside the image dimensions.
     */
    public abstract ColorRGBA getPixel(int x, int y, ColorRGBA store);
    
    /**
     * Retrieve the color at the given coordinate.
     * <p>
     * Convenience method that does not take a store argument. Equivalent
     * to calling getPixel(x, y, null). 
     * See {@link #getPixel(int, int, com.jme3.math.ColorRGBA) } for
     * more information.
     * 
     * @param x The x coordinate, from 0 to width - 1.
     * @param y The y coordinate, from 0 to height - 1.
     * @return A new ColorRGBA with the read color.
     * @throws IllegalArgumentException If x or y are outside the image dimensions
     */
    public ColorRGBA getPixel(int x, int y) { 
        return getPixel(x, y, null);
    }
}
