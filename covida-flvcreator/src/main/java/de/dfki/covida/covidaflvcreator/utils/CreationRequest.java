/*
 * CreationRequest.java
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

import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Class which holds the creation request data.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class CreationRequest implements Serializable {

    public final String filename;
    public final long timeStart;
    public final long timeEnd;
    public final StrokeList strokelist;
    public final String label;

    public CreationRequest(String filename, long timeStart, long timeEnd,
            StrokeList strokelist, String label) {
        this.filename = filename;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.strokelist = strokelist;
        this.label = label;
    }

    public CreationRequest(String filename, long timeStart, long timeEnd,
            String shapeXML, String label) {
        StrokeList list = new StrokeList();
        try {
            JAXBContext jc = JAXBContext.newInstance(StrokeList.class);
            Unmarshaller u = jc.createUnmarshaller();
            Reader r = new StringReader(shapeXML);
            list = (StrokeList) u.unmarshal(r);
        } catch (JAXBException ex) {
            Logger.getLogger(CreationRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.filename = filename;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.strokelist = list;
        this.label = label;
    }
}
