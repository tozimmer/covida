/*
 * AnnotatedVideoCreator.java
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

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import de.dfki.covida.covidaflvcreator.utils.ContainerInfo;
import de.dfki.covida.covidaflvcreator.utils.Stroke;
import de.dfki.covida.covidaflvcreator.utils.StrokeList;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which creates the annotated video file.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class AnnotatedVideoCreator extends MediaListenerAdapter implements Runnable {

    /**
     * Default color for the text overlay
     */
    private Color defaultG2DColor = Color.WHITE;
    /**
     * Points of the spape to render in the video
     */
    private StrokeList shapeToDraw;
    /**
     * {@link FontMetrics} for the text overlay
     */
    private FontMetrics fm;
    /**
     * Space between the rows of the text overlay
     */
    private int ascent;
    /**
     * Space between the rows of the text overlay
     */
    private int fh;
    /**
     * Space between the words on the text overlay
     */
    private int space;
    /**
     * If true text overlay is rendered into the video
     */
    private boolean textOverlayEnabled;
    /**
     * {@link String} for the text overlay
     */
    private String text;
    /**
     * {@link VideoEncoder}
     */
    private VideoEncoder creator;
    /**
     * Start time stamp
     */
    private long timeStart;
    /**
     * End time stamp
     */
    private long timeEnd;
    /**
     * Font for text overlay
     */
    private Font f = new Font("Arial", Font.PLAIN, 20);
    /**
     * Logger
     */
    protected Logger log = LoggerFactory.getLogger(getClass());
    /**
     * The video stream index, used to ensure we display frames from one and
     * only one video stream from the media container.
     */
    private int mVideoStreamIndex = -1;
    /**
     * Output file in which the annotated frames should be encoded.
     */
    private String outFile;
    /**
     * Output file in which the annotated frames is encoded.
     */
    private String outputFile;
    /**
     * Indicates if encoding is completed and stream is closed.
     */
    private boolean completed;
    /**
     * Input file from which the frames should be decoded.
     */
    private final String inputFile;
    /**
     * Video dimension
     */
    private Dimension dim;
    /**
     * Requesting class
     */
    private final IVideoReceiver request;
    /**
     * Indicates if {@link AnnotatedVideoCreator} is currently encoding
     */
    private boolean encoding;

    /**
     * Construct a DecodeAndCaptureFrames which reads and captures frames from a
     * video file.
     *
     * @param inFile the name of the media file to read
     */
    public AnnotatedVideoCreator(String inFile, IVideoReceiver request) {
        this.inputFile = inFile;
        this.dim = ContainerInfo.getDimension(inFile);
        this.shapeToDraw = new StrokeList();
        this.request = request;
    }

    public String getInputFile() {
        return inputFile;
    }

    /**
     * If {@link AnnotatedVideoCreator} is currently not encoding this metod
     * sets the shape which will be rendered in the video.
     *
     * @param shape {@link List} of {@link Point}s
     */
    public void setShape(StrokeList shape) {
        if (!encoding) {
            shapeToDraw = shape;
        }
    }

    /**
     * If {@link AnnotatedVideoCreator} is currently not encoding this metod
     * sets the time intervall for the video creation
     *
     * @param timeStart start time in microseconds {@link Long}
     * @param timeEnd end time in microseconds as {@link Long}
     */
    public void setIntervall(long timeStart, long timeEnd) {
        if (!encoding) {
            this.outFile = inputFile + "_" + timeStart + "_" + timeEnd + ".flv";
            this.timeStart = timeStart;
            this.timeEnd = timeEnd;
        }
    }

    /**
     * If {@link AnnotatedVideoCreator} is currently not encoding this metod
     * sets the text which will be displayed on the encoded video.
     *
     * Note that if the parameter is {@code null} or equals "" the text overlay
     * will be disabled.
     *
     * @param text {@link String} which should be rendered
     */
    public void setText(String text) {
        if (!encoding) {
            if (text != null && !text.equals("")) {
                textOverlayEnabled = true;
                this.text = text;
            } else {
                textOverlayEnabled = false;
                this.text = "";
            }
        }
    }

    /**
     * Returns true if the last video creation process is completed
     *
     * @return true
     */
    public boolean isCompleted() {
        return completed;
    }

    public String getOutfile() {
        if (completed) {
            return outputFile;
        } else {
            return null;
        }
    }

    /**
     * Draws {@link String} on the {@link Graphics2D}
     *
     * @param str {@link String}
     * @param g2d {@link Graphics2D}
     * @param centered {@link Boolean}
     * @param y {@link Integer}
     */
    private void drawString(String str, Graphics2D g2d, boolean centered, int y) {
        StringTokenizer st = new StringTokenizer(str);
        int x = 0;
        String word, sp;
        int wordCount = 0;
        String line = "";
        while (st.hasMoreTokens()) {
            word = st.nextToken();
            if (word.equals("<BR>")) {
                drawString(g2d, line, fm.stringWidth(line), y + ascent, centered);
                line = "";
                wordCount = 0;
                x = 0;
                y = y + (fh * 2);
            } else {
                int w = fm.stringWidth(word);
                if ((x + space + w) > dim.width) {
                    drawString(g2d, line, fm.stringWidth(line), y + ascent, centered);
                    line = "";
                    wordCount = 0;
                    x = 0;
                    y = y + fh;
                }
                if (x != 0) {
                    sp = " ";
                } else {
                    sp = "";
                }
                line = line + sp + word;
                x = x + space + w;
                wordCount++;
            }
        }
        drawString(g2d, line, fm.stringWidth(line), y + ascent, centered);
    }

    /**
     * Draws {@link String} on the {@link Graphics2D}
     *
     * @param g2d {@link Graphics2D}
     * @param line {@link String}
     * @param lineW {@link Integer}
     * @param y {@link Integer}
     * @param centered {@link Boolean}
     */
    private void drawString(Graphics2D g2d, String line, int lineW, int y, boolean centered) {
        if (centered) {
            g2d.setColor(Color.black);
            g2d.drawString(line, ((dim.width - lineW) / 2) - 1, y - 1);
            g2d.drawString(line, ((dim.width - lineW) / 2) + 1, y - 1);
            g2d.drawString(line, ((dim.width - lineW) / 2) + 1, y + 1);
            g2d.drawString(line, ((dim.width - lineW) / 2) - 1, y + 1);
            g2d.setColor(defaultG2DColor);
            g2d.drawString(line, (dim.width - lineW) / 2, y);
        } else {
            g2d.setColor(Color.black);
            g2d.drawString(line, 0, y);
            g2d.drawString(line, 0, y);
            g2d.drawString(line, 0, y);
            g2d.drawString(line, 0, y);
            g2d.setColor(defaultG2DColor);
            g2d.drawString(line, 0, y);
        }
    }

    /**
     * Draws {@code pointsToDraw} on {@link Graphics2D}
     *
     * @param g2d {@link Graphics2D}
     */
    private void drawPoints(Graphics2D g2d) {
        for (Stroke stroke : shapeToDraw.strokes) {
            Point lastPoint = null;
            for (Point point : stroke.points) {
                if (lastPoint == null) {
                    lastPoint = point;
                } else {
                    g2d.setColor(Color.black);
                    g2d.drawLine(lastPoint.x + 2, lastPoint.y + 2, point.x + 2, point.y + 2);
                    g2d.drawLine(lastPoint.x - 2, lastPoint.y + 2, point.x - 2, point.y + 2);
                    g2d.drawLine(lastPoint.x + 2, lastPoint.y - 2, point.x + 2, point.y - 2);
                    g2d.drawLine(lastPoint.x - 2, lastPoint.y - 2, point.x - 2, point.y - 2);
                    g2d.setColor(defaultG2DColor);
                    g2d.drawLine(lastPoint.x, lastPoint.y, point.x, point.y);
                    lastPoint = point;
                }
            }
        }
    }

    /**
     * Called after a video frame has been decoded from a media stream.
     * Optionally a BufferedImage version of the frame may be passed if the
     * calling {@link IMediaReader} instance was configured to create
     * BufferedImages.
     *
     * This method blocks, so return quickly.
     */
    @Override
    public void onVideoPicture(IVideoPictureEvent event) {
        // if the stream index does not match the selected stream index,
        // then have a closer look
        if (event.getStreamIndex() != mVideoStreamIndex) {
            log.debug("Stream index differs.");
            // if the selected video stream id is not yet set, go ahead an
            // select this lucky video stream
            if (-1 == mVideoStreamIndex) {
                mVideoStreamIndex = event.getStreamIndex();
            } // otherwise return, no need to show frames from this video stream
            else {
                log.debug("No need to show frames");
                return;
            }
        }
        if (event.getTimeStamp() > timeStart && event.getTimeStamp() < timeEnd) {
            BufferedImage image = event.getImage();
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(defaultG2DColor);
            BasicStroke bs = new BasicStroke(2);
            g2d.setStroke(bs);
            drawPoints(g2d);
            g2d.setFont(f);
            if (fm == null) {
                fm = g2d.getFontMetrics();
                ascent = fm.getAscent();
                fh = ascent + fm.getDescent();
                space = fm.stringWidth(" ");
            }
            if (textOverlayEnabled) {
                if (text != null) {
                    drawString(text, g2d, true, 0);
                } else {
                    log.warn("Can not render title overlay: tile == null");
                    textOverlayEnabled = false;
                }
            }
            creator.encodeImage(image, event.getTimeStamp());
        }
    }

    @Override
    public void run() {
        if (dim != null) {
            creator = new VideoEncoder(outFile, dim.width,
                    dim.height);
            this.encoding = true;
            IMediaReader reader = ToolFactory.makeReader(inputFile);

            // stipulate that we want BufferedImages created in BGR 24bit color space
            reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
            // note that DecodeAndCaptureFrames is derived from
            // MediaReader.ListenerAdapter and thus may be added as a listener
            // to the MediaReader. DecodeAndCaptureFrames implements
            // onVideoPicture().
            boolean addListener = reader.addListener(this);

            if (addListener) {
                // read out the contents of the media file, note that nothing else
                // happens here.  action happens in the onVideoPicture() method
                // which is called when complete video pictures are extracted from
                // the media source

                while (reader.readPacket() == null) {
                    do {
                    } while (false);
                }
            } else {
                log.warn("Attach listener failed.");
            }
            creator.closeStreams();
            completed = true;
        } else {
            outFile = null;
        }
        completed = true;
        encoding = false;
        outputFile = outFile;
        request.setVideoFile(outputFile);
    }
}
