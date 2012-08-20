/*
 * StrokeTrace.java
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
package de.dfki.covida.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Generic class to store trace information.
 *
 * @author Markus Weber
 *
 * @param <T> data type of the coordinate information
 */
public class StrokeTrace<T extends Number> implements Serializable {

    /**
     * List of x coordinates
     */
    private List<T> x;
    /**
     * List of y coordinates
     */
    private List<T> y;
    /**
     * Force of the point.
     */
    private List<Float> force;
    /**
     * timestamps of the point.
     */
    private List<Long> timestamps;
    /**
     * Identifier of trace
     */
    private final int id;
    /**
     * Sequencer
     */
    private static int COUNTER = 0;
    /**
     * Page ID
     */
    private final String pageID;

    /**
     * Creates an empty trace.
     */
    public StrokeTrace(String pageid) {
        this(pageid, COUNTER++, new ArrayList<T>(), new ArrayList<T>(),
                new ArrayList<Float>(), new ArrayList<Long>());
    }

    /**
     * Copy constructor.
     *
     * @param pageid
     * @param id
     * @param x
     * @param y
     * @param force
     * @param timestamps
     */
    public StrokeTrace(String pageid, int id, List<T> x, List<T> y,
            List<Float> force, List<Long> timestamps) {
        this.pageID = pageid;
        this.x = x;
        this.y = y;
        this.force = force;
        this.timestamps = timestamps;
        this.id = id;
    }

    /**
     * Unique identifier for the trace
     *
     * @return the id
     */
    public final int getId() {
        return id;
    }

    /**
     * Returns the x coordinates of the trace.
     *
     * @return
     */
    public synchronized List<T> getX() {
        assert x.size() == y.size();
        return x;
    }

    /**
     * Returns the y coordinates of the trace.
     *
     * @return
     */
    public synchronized List<T> getY() {
        assert x.size() == y.size();
        return y;
    }

    /**
     * Returns the forces of the trace.
     *
     * @return
     */
    public synchronized List<Float> getForce() {
        assert x.size() == y.size();
        assert x.size() == force.size();
        return force;
    }

    /**
     * Returns the timestamps of the trace.
     *
     * @return
     */
    public synchronized List<Long> getTimeStamps() {
        assert x.size() == y.size();
        assert x.size() == timestamps.size();
        return this.timestamps;
    }

    /**
     * Returns the x coordinates of the trace.
     *
     * @return
     */
    public T[] getXtoArray(T[] array) {
        return getX().toArray(array);
    }

    /**
     * Returns the y coordinates of the trace.
     *
     * @return
     */
    public T[] getYtoArray(T[] array) {
        return getY().toArray(array);
    }

    /**
     * Returns the forces of the trace.
     *
     * @return
     */
    public Float[] getForcetoArray(Float[] array) {
        return getForce().toArray(array);
    }

    /**
     * Returns the forces of the trace.
     *
     * @return
     */
    public Long[] getTimestampstoArray(Long[] array) {
        return getTimeStamps().toArray(array);
    }

    /**
     * @return the pageID
     */
    public String getPageID() {
        return pageID;
    }

    /**
     * Add a point to the trace.
     *
     * @param x
     * @param y
     * @param force
     * @param timestamp
     */
    public synchronized void addPoint(T x, T y, Float force, Long timestamp) {
        this.x.add(x);
        this.y.add(y);
        this.force.add(force);
        this.timestamps.add(timestamp);
    }

    /**
     * Adds a point with default force 1.0.
     *
     * @param x
     * @param y
     */
    public void addPoint(T x, T y) {
        this.addPoint(x, y, new Float(1.0f), System.currentTimeMillis());
    }

    /**
     * Adds a list of points with their corresponding forces.
     *
     * @param x
     * @param y
     * @param forces
     */
    public synchronized void addPoint(List<T> x, List<T> y, List<Float> forces) {
        this.x.addAll(x);
        this.y.addAll(y);
        this.force.addAll(forces);
    }

    /**
     * Number of elements in trace.
     *
     * @return
     */
    public synchronized int size() {
        assert x.size() == y.size();
        assert x.size() == force.size();
        assert x.size() == timestamps.size();
        return this.x.size();
    }

    /**
     * Clears the trace.
     */
    public synchronized void clear() {
        this.x.clear();
        this.y.clear();
        this.force.clear();
        this.timestamps.clear();
    }

    /**
     * Generates a simple inkML trace.
     *
     * @return
     */
    public synchronized Element toInkMLTraceElement(Document doc) {
        Element trace = doc.createElement("trace");
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < x.size(); i++) {
            buf.append(Math.round(x.get(i).floatValue() * 1000f) + " "
                    + Math.round(y.get(i).floatValue() * 700f) + ", ");
        }
        buf.delete(buf.length() - 2, buf.length());
        trace.appendChild(doc.createTextNode(buf.toString()));
        return trace;

    }

    /**
     * X value of first point.
     *
     * @return
     */
    public T beginX() {
        return getX().get(0);
    }

    /**
     * Y value of first point.
     *
     * @return
     */
    public T beginY() {
        return getY().get(0);
    }

    /**
     * X value of the last point.
     *
     * @return
     */
    public T endX() {
        return getX().get(this.size() - 1);
    }

    /**
     * Y value of the last point.
     *
     * @return
     */
    public T endY() {
        return getY().get(this.size() - 1);
    }

    /**
     * Merges two traces.
     *
     * @param t2
     * @return
     */
    public StrokeTrace<T> merge(StrokeTrace<T> t2) {
        StrokeTrace<T> t3 = new StrokeTrace<T>(t2.getPageID());
        t3.addPoint(this.getX(), this.getY(), this.getForce());
        t3.addPoint(t2.getX(), t2.getY(), t2.getForce());
        return t3;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("Trace [");
        for (int i = 0; i < x.size(); i++) {
            buf.append(x.get(i) + ", " + y.get(i) + "; ");
        }
        buf.delete(buf.length() - 2, buf.length());
        buf.append("]");
        return buf.toString();
    }

    /**
     * Reverses the trace.
     *
     * @return
     */
    public synchronized StrokeTrace<T> reverse() {
        StrokeTrace<T> reverse = new StrokeTrace<T>(this.getPageID());
        for (int i = size() - 1; i >= 0; i--) {
            reverse.addPoint(this.x.get(i), this.y.get(i), this.force.get(i),
                    this.timestamps.get(i));
        }
        return reverse;
    }

    /**
     * Copies content of the trace.
     *
     * @return
     */
    public StrokeTrace<T> copy() {
        return new StrokeTrace<T>(this.pageID, this.id, new ArrayList<T>(this.x),
                new ArrayList<T>(this.y), new ArrayList<Float>(this.force),
                new ArrayList<Long>(this.timestamps));
    }
}
