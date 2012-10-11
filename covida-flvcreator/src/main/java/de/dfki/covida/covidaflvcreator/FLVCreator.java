/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidaflvcreator;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;
import de.dfki.covida.covidaflvcreator.utils.ImageUtils;
import java.awt.image.BufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tobias
 */
public class FLVCreator {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(FLVCreator.class);
    private final IRational frameRate;
    private final IContainer outContainer;
    private final IStream outStream;
    private final IStreamCoder outStreamCoder;
    private long firstTimeStamp;

    public FLVCreator(String outFile, int width, int height) {
        firstTimeStamp = -1;
         // Change this to change the frame rate you record at
        frameRate = IRational.make(3, 1);

        outContainer = IContainer.make();

        int retval = outContainer.open(outFile, IContainer.Type.WRITE, null);
        if (retval < 0) {
            throw new RuntimeException("could not open output file");
        }

        ICodec codec = ICodec.guessEncodingCodec(null, null, outFile, null,
                ICodec.Type.CODEC_TYPE_VIDEO);
        if (codec == null) {
            throw new RuntimeException("could not guess a codec");
        }

        log.debug("Codec name is: {}", codec.getName());
        log.debug("Codec   id is: {}", codec.getID());
        log.debug("Codec type is: {}", codec.getType());

        outStream = outContainer.addNewStream(codec);
        outStreamCoder = outStream.getStreamCoder();

        outStreamCoder.setNumPicturesInGroupOfPictures(30);

        outStreamCoder.setBitRate(25000);
        outStreamCoder.setBitRateTolerance(9000);

        outStreamCoder.setPixelType(IPixelFormat.Type.YUV420P);
        outStreamCoder.setHeight(height);
        outStreamCoder.setWidth(width);
        outStreamCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
        outStreamCoder.setGlobalQuality(0);

        outStreamCoder.setFrameRate(frameRate);
        outStreamCoder.setTimeBase(IRational.make(frameRate.getDenominator(),
                frameRate.getNumerator()));

        retval = outStreamCoder.open(null, null);
        if (retval < 0) {
            throw new RuntimeException("could not open input decoder");
        }
        retval = outContainer.writeHeader();
        if (retval < 0) {
            throw new RuntimeException("could not write file header");
        }
    }
    
    /**
     * Encode the given image to the file and increment our time stamp.
     *
     * @param originalImage source image
     */
    public void encodeImage(BufferedImage originalImage) {
        BufferedImage worksWithXugglerBufferedImage = ImageUtils.convertToType(
                originalImage, BufferedImage.TYPE_3BYTE_BGR);
        IPacket packet = IPacket.make();

        long now = System.currentTimeMillis();
        if (firstTimeStamp == -1) {
            firstTimeStamp = now;
        }

        IConverter converter = null;
        try {
            converter = ConverterFactory.createConverter(
                    worksWithXugglerBufferedImage, IPixelFormat.Type.YUV420P);
        } catch (UnsupportedOperationException e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }

        long timeStamp = (now - firstTimeStamp) * 1000; // convert to microseconds
        IVideoPicture outFrame = converter.toPicture(worksWithXugglerBufferedImage,
                timeStamp);

        outFrame.setQuality(0);
        int retval = outStreamCoder.encodeVideo(packet, outFrame, 0);
        if (retval < 0) {
            throw new RuntimeException("could not encode video");
        }
        if (packet.isComplete()) {
            retval = outContainer.writePacket(packet);
            if (retval < 0) {
                throw new RuntimeException("could not save packet to container");
            }
        }

    }

    /**
     * Close out the file we're currently working on.
     */
    public void closeStreams() {
        int retval = outContainer.writeTrailer();
        if (retval < 0) {
            throw new RuntimeException("Could not write trailer to output file");
        }
    }
    
}
