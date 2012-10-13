/*
 * ContainerInfo.java
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

import com.xuggle.xuggler.*;
import de.dfki.covida.covidaflvcreator.demos.VideoCreatorDemo;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which opens up a media container and saves all information.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class ContainerInfo {

    /**
     * Logger
     */
    protected static Logger log = LoggerFactory.getLogger(VideoCreatorDemo.class);
    /**
     * Number of streams in the media container
     */
    public final int numStreams;
    /**
     * Container duration in microseconds
     */
    public final long duration;
    /**
     * Container start time in microseconds
     */
    public final long startTime;
    /**
     * File size of the container
     */
    public final long fileSize;
    /**
     * Container bit rate
     */
    public final int bitRate;
    /**
     * List of stream information for every stream in the media container
     */
    public List<StreamInfo> streamInfo;

    /**
     * Takes a media container {@code filename} opens it, and saves all
     * information of the container.
     *
     * @param filename file position of the media container
     */
    public ContainerInfo(String filename) {
        long start = System.currentTimeMillis();
        IContainer container = IContainer.make();
        if (container.open(filename, IContainer.Type.READ, null) < 0) {
            log.error("Could not open file: {}." + filename);
        }
        numStreams = container.getNumStreams();
        duration = container.getDuration();
        startTime = container.getStartTime();
        fileSize = container.getFileSize();
        bitRate = container.getBitRate();
        streamInfo = new ArrayList<>();
        for (int i = 0; i < numStreams; i++) {
            IStream stream = container.getStream(i);
            IStreamCoder coder = stream.getStreamCoder();
            StreamInfo info = null;
            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
                info = new AudioStreamInfo();
                ((AudioStreamInfo) info).sampleRate = coder.getSampleRate();
                ((AudioStreamInfo) info).channels = coder.getChannels();
                ((AudioStreamInfo) info).sampleFormat = coder.getSampleFormat();
            } else if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                info = new VideoStreamInfo();
                ((VideoStreamInfo) info).width = coder.getWidth();
                ((VideoStreamInfo) info).height = coder.getHeight();
                ((VideoStreamInfo) info).format = coder.getPixelType();
                ((VideoStreamInfo) info).frameRate = coder.getFrameRate().getDouble();
            }
            if (info != null) {
                streamInfo.add(info);
                info.codecType = coder.getCodecType();
                info.codecID = coder.getCodecID();
                info.duration = stream.getDuration();
                info.startTime = container.getStartTime();
                info.language = stream.getLanguage();
                info.streamTimeBase = stream.getTimeBase();
                info.coderTimeBase = coder.getTimeBase();
            }
        }
        log.debug("Container info retrieval duration: {}ms", System.currentTimeMillis() - start);
    }

    /**
     * Returns the video dimensions of the first video stream in the media
     * container
     *
     * @param filename file location of the media container
     * @return {@link Dimension}
     * @return null if no video dimension could be retreived
     */
    public static Dimension getDimension(String filename) {
        long start = System.currentTimeMillis();
        IContainer container = IContainer.make();
        if (container.open(filename, IContainer.Type.READ, null) < 0) {
            log.error("Could not open file: {}." + filename);
            return null;
        }
        for (int i = 0; i < container.getNumStreams(); i++) {
            IStream stream = container.getStream(i);
            IStreamCoder coder = stream.getStreamCoder();
            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                log.debug("Container info retrieval duration: {}ms", System.currentTimeMillis() - start);
                return new Dimension(coder.getWidth(), coder.getHeight());
            }
        }
        return null;
    }
}
