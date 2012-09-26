/*
 * ShapePoints.java
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.log4j.Logger;

public class ShapePoints implements List, Cloneable {

    /**
     * Logger
     */
    private Logger log = Logger.getLogger(ShapePoints.class);
    private final List<Point> points;

    public ShapePoints() {
        super();
        points = Collections.synchronizedList(new ArrayList<Point>());
    }

    public List<Point> getShapePoints() {
        synchronized (points) {
            return points;
        }
    }

    public void add(Point point) {
        synchronized (point) {
            points.add(point);
        }
    }

    @Override
    public int size() {
        synchronized (points) {
            return this.points.size();
        }
    }

    @XmlJavaTypeAdapter(PointAdapter.class)
    @XmlElement(name = "point")
    @Override
    public Point get(int index) {
        synchronized (points) {
            return this.points.get(index);
        }
    }

    @Override
    public Iterator<Point> iterator() {
        synchronized (points) {
            return points.iterator();
        }
    }

    @Override
    public ShapePoints clone() {
        ShapePoints shapePointList = new ShapePoints();
        synchronized (points) {
            for (Point point : points) {
                shapePointList.add(point);
            }
        }
        return shapePointList;
    }

    @Override
    public boolean isEmpty() {
        synchronized (points) {
            return points.isEmpty();
        }
    }

    @Override
    public boolean contains(Object o) {
        synchronized (points) {
            return points.contains(o);
        }
    }

    @Override
    public Object[] toArray() {
        synchronized (points) {
            return points.toArray();
        }
    }

    @Override
    public Object[] toArray(Object[] a) {
        synchronized (points) {
            return points.toArray(a);
        }
    }

    @Override
    public boolean add(Object e) {
        synchronized (points) {
            if (e instanceof Point) {
                return points.add((Point) e);
            } else {
                log.warn(this.getClass().getName() + " does only accept "
                        + Point.class.getName() + " objects");
                return false;
            }
        }
    }

    @Override
    public boolean remove(Object o) {
        synchronized (points) {
            return points.remove(o);
        }
    }

    @Override
    public boolean containsAll(Collection c) {
        synchronized (points) {
            return points.containsAll(c);
        }
    }

    @Override
    public boolean addAll(Collection c) {
        synchronized (points) {
            return points.addAll(c);
        }
    }

    @Override
    public boolean addAll(int index, Collection c) {
        synchronized (points) {
            return points.addAll(index, c);
        }
    }

    @Override
    public boolean removeAll(Collection c) {
        synchronized (points) {
            return points.removeAll(c);
        }
    }

    @Override
    public boolean retainAll(Collection c) {
        synchronized (points) {
            return points.retainAll(c);
        }
    }

    @Override
    public void clear() {
        synchronized (points) {
            points.clear();
        }
    }

    @Override
    public Object set(int index, Object element) {
        synchronized (points) {
            if (element instanceof Point) {
                return points.set(index, (Point) element);
            } else {
                log.warn(this.getClass().getName() + " does only accept "
                        + Point.class.getName() + " objects");
                return null;
            }
        }
    }

    @Override
    public void add(int index, Object element) {
        synchronized (points) {
            if (element instanceof Point) {
                points.add((Point) element);
            } else {
                log.warn(this.getClass().getName() + " does only accept "
                        + Point.class.getName() + " objects");
            }
        }
    }

    @Override
    public Object remove(int index) {
        synchronized (points) {
            return points.remove(index);
        }
    }

    @Override
    public int indexOf(Object o) {
        synchronized (points) {
            return points.indexOf(o);
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        synchronized (points) {
            return points.lastIndexOf(o);
        }
    }

    @Override
    public ListIterator listIterator() {
        synchronized (points) {
            return points.listIterator();
        }
    }

    @Override
    public ListIterator listIterator(int index) {
        synchronized (points) {
            return points.listIterator(index);
        }
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        synchronized (points) {
            return points.subList(fromIndex, toIndex);
        }
    }
}

/**
 * Adapter for storing the
 * <code>Point</code>.
 *
 * @author Markus Weber
 *
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
