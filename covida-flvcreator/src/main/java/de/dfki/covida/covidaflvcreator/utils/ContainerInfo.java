/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidaflvcreator.utils;

import com.xuggle.xuggler.*;
import de.dfki.covida.covidaflvcreator.VideoCreatorTest;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opens up a media container, and prints out a summary of the contents.
 *
 * If you pass -Dxuggle.options we'll also tell you what every configurable
 * option on the container and streams is set to.
 *
 * @author aclarke
 *
 */
public class ContainerInfo {

    /**
     * Logger
     */
    protected static Logger log = LoggerFactory.getLogger(VideoCreatorTest.class);
    public final int numStreams;
    public final long duration;
    public final long startTime;
    public final long fileSize;
    public final int bitRate;
    public List<StreamInfo> streamInfo;

    /**
     * Takes a media container (file) as the first argument, opens it, and tells
     * you what's inside the container.
     *
     * @param args Must contain one string which represents a filename
     */
    public ContainerInfo(String filename) {
        long start = System.currentTimeMillis();
        // Create a Xuggler container object
        IContainer container = IContainer.make();
        // Open up the container
        if (container.open(filename, IContainer.Type.READ, null) < 0) {
            log.error("Could not open file: {}." + filename);
        }
        // query how many streams the call to open found
        numStreams = container.getNumStreams();
        duration = container.getDuration();
        startTime = container.getStartTime();
        fileSize = container.getFileSize();
        bitRate = container.getBitRate();
        streamInfo = new ArrayList<>();
        // and iterate through the streams to print their meta data
        for (int i = 0; i < numStreams; i++) {
            // Find the stream object
            IStream stream = container.getStream(i);
            // Get the pre-configured decoder that can decode this stream;
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
                //Collect meta data
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
}
