/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.utils;

import de.dfki.covida.covidacore.data.Stroke;
import java.awt.Point;
import java.awt.Polygon;

/**
 *
 * @author Tobias
 */
public class ShapeUtils {

    public static Polygon getPolygon (Stroke stroke){
        Polygon polygon = new Polygon();
        for(Point point : stroke.points){
            polygon.addPoint(point.x, point.y);
        }
        return polygon;
    }
    
    public static boolean intersect(Polygon origin, Polygon polgyon) {
        return origin.getBounds().intersects(polgyon.getBounds());
    }
    
    public static Polygon getIntersection(Polygon origin, Polygon polygon){
        Polygon intersection = new Polygon();
        for(int i=0; i<origin.npoints; i++){
            if(!polygon.contains(origin.xpoints[i], origin.ypoints[i])){
                intersection.addPoint(origin.xpoints[i], origin.ypoints[i]);
            } 
        }
        for(int i=0; i<polygon.npoints; i++){
            if(origin.contains(polygon.xpoints[i], polygon.ypoints[i])){
                intersection.addPoint(polygon.xpoints[i], polygon.ypoints[i]);
            } 
        }
        return intersection;
    }
}
