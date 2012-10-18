/*
 * VideoCreatorRequestDemo.java
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
package de.dfki.covida.covidaflvcreator.demos;

import de.dfki.covida.covidaflvcreator.AnnotatedVideoCreator;
import de.dfki.covida.covidaflvcreator.IVideoReceiver;
import de.dfki.covida.covidaflvcreator.utils.Stroke;
import de.dfki.covida.covidaflvcreator.utils.StrokeList;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstration of the usage of the {@link AnnotatedVideoCreator}.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class VideoCreatorRequestDemo implements IVideoReceiver{
    
    /**
     * Logger
     */
    protected static Logger log = LoggerFactory.getLogger(VideoCreatorRequestDemo.class);
    private long start;
    
    public void create(){
        StrokeList shape = new StrokeList();
        Stroke stroke = new Stroke();
        stroke.points.add(new Point(22, 177));
        stroke.points.add(new Point(200, 22));
        stroke.points.add(new Point(155, 43));
        stroke.points.add(new Point(210, 77));
        stroke.points.add(new Point(244, 17));
        stroke.points.add(new Point(22, 7));
        stroke.points.add(new Point(331, 177));
        stroke.points.add(new Point(321, 173));
        stroke.points.add(new Point(100, 177));
        stroke.points.add(new Point(22, 177));
        shape.strokes.add(stroke);

        AnnotatedVideoCreator annotatedVideoCreator = new AnnotatedVideoCreator(
                "../covida-res/videos/Collaborative Video Annotation.mp4", 
                this);
        annotatedVideoCreator.setIntervall(20000000, 25000000);
        annotatedVideoCreator.setShape(shape);
        annotatedVideoCreator.setText("Test annotation label");
        Thread creatorThread = new Thread(annotatedVideoCreator);
        creatorThread.setName("Annotated video creator");
        creatorThread.start();
        start = System.currentTimeMillis();
    }

    @Override
    public void setVideoFile(String file) {
        log.debug("Movie encoded in {}ms: {}", System.currentTimeMillis() - start,
                file);
    }
    
}
