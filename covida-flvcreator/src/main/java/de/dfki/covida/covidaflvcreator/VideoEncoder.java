/*
 * VideoEncoder.java
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
package de.dfki.covida.covidaflvcreator;

import com.xuggle.xuggler.*;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;
import de.dfki.covida.covidaflvcreator.utils.ImageUtils;
import java.awt.image.BufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which encodes the annotated video file.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class VideoEncoder {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(VideoEncoder.class);
    private final IRational frameRate;
    private final IContainer outContainer;
    private final IStream outStream;
    private final IStreamCoder outStreamCoder;

    public VideoEncoder(String outFile, int width, int height) {
         // Change this to change the frame rate you record at
        frameRate = IRational.make(24, 1);

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
    public void encodeImage(BufferedImage originalImage, long timestamp) {
        BufferedImage worksWithXugglerBufferedImage = ImageUtils.convertToType(
                originalImage, BufferedImage.TYPE_3BYTE_BGR);
        IPacket packet = IPacket.make();

        IConverter converter = null;
        try {
            converter = ConverterFactory.createConverter(
                    worksWithXugglerBufferedImage, IPixelFormat.Type.YUV420P);
        } catch (UnsupportedOperationException e) {
            log.error("",e);
        }

        IVideoPicture outFrame = converter.toPicture(
                worksWithXugglerBufferedImage, timestamp);

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
