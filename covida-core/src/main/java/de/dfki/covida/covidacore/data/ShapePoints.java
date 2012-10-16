/*
 * ShapeList.java
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

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Shape list.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class ShapePoints implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5408416424492049610L;
    /**
     * List of {@link Point}
     */
    @XmlElementWrapper(name = "points")
    @XmlElement(name = "point")
    @XmlJavaTypeAdapter(PointAdapter.class)
    public List<Point> points;

    /**
     * Creates a new {@link ShapePoints} instance.
     */
    public ShapePoints() {
        points = new ArrayList<>();
    }
}

/**
 * Adapter for serialize {@link Point} objects.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
class PointAdapter extends XmlAdapter<String, Point> {

    @Override
    public String marshal(Point point) throws Exception {
        return point.x + "," + point.y;
    }

    @Override
    public Point unmarshal(String str) throws Exception {
        String[] res = str.split(",");
        return new Point(Integer.parseInt(res[0]), Integer.parseInt(res[1]));
    }
}
