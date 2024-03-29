/*
 * VideoMediaData.java
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

import java.awt.Image;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * Video media data
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class VideoMediaData implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5408416424492049777L;
    /**
     * Video source as {@link String}
     */
    @XmlElement(name = "source")
    public String videoSource;
    /**
     * Video name as {@link String}
     */
    @XmlElement(name = "name")
    public String videoName;
    /**
     * Time start in ms
     */
    @XmlElement(name = "time_start")
    public long time_start;
    /**
     * Time end in ms
     */
    @XmlElement(name = "time_end")
    public long time_end;
    /**
     * Video width in pixel as {@link Integer}
     */
    @XmlElement(name = "width")
    public int width;
    /**
     * Video height in pixel as {@link Integer}
     */
    @XmlElement(name = "height")
    public int height;
    /**
     * Video repeat status
     */
    @XmlElement(name = "repeat")
    public boolean repeat;
    /**
     * UUID which is generated from the application
     */
    @XmlElement(name = "uuid")
    public UUID uuid;
    
    /**
     * UUID which is generated from the application
     */
    @XmlElementWrapper(name = "thumbs")
    @XmlElement(name = "thumb")
    public List<Image> thumbs;
}
