/*
 * VideoData.java
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
package de.dfki.covida.covidacore.data;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;

/**
 * Video data class.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class VideoData implements Serializable{

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5408416424492049888L;
    /**
     * Size in percentage of display y-axis
     */
    @XmlElement(name = "size")
    public int size;
    /**
     * Position in the 3x2 grid (0-5)
     */
    @XmlElement(name = "position")
    public int position;
    /**
     * {@link Boolean} which indicates if video should be repeated
     */
    @XmlElement(name = "repeating")
    public boolean repeating;
    /**
     * {@link Boolean} which indicates if video is enabled
     */
    @XmlElement(name = "enabled")
    public boolean enabled;
    /**
     * Start time in ms
     */
    @XmlElement(name = "time_start")
    public long time_start;
    /**
     * End time in ms
     */
    @XmlElement(name = "time_end")
    public long time_end;
}
