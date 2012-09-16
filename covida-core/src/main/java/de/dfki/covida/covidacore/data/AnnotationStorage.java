/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.data;

import de.dfki.covida.covidacore.components.IVideoComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Tobias
 */
public class AnnotationStorage {
    private static AnnotationStorage instance;
    private List<IVideoComponent> components;
    private Map<IVideoComponent, AnnotationData> dataList;

    private AnnotationStorage() {
        components = new ArrayList<>();
        dataList = new HashMap<>();
    }
    
    public static AnnotationStorage getInstance(){
        if (instance==null){
            instance = new AnnotationStorage();
        }
        return instance;
    }
    
    public void addAnnotation(IVideoComponent component, Annotation annotation){
        if(components.contains(component)){
            dataList.get(component).annotations.add(annotation);
        }else{
            AnnotationData data = new AnnotationData();
            data.annotations.add(annotation);
            dataList.put(component, data);
        }
    }
    
    public AnnotationData getAnnotationData(IVideoComponent component){
        if(!components.contains(component)){
            AnnotationData data = new AnnotationData();
            dataList.put(component, data);
        }
        return dataList.get(component);
    }
    
    public Iterable<AnnotationData> getAnnotationDatas(){
        return dataList.values();
    }
    
    public IVideoComponent getVideo(AnnotationData data){
        if(dataList.containsValue(data)){
            for(IVideoComponent video : dataList.keySet()){
                if(dataList.get(video).equals(data)){
                    return video;
                }
            }
        }
        return null;
    }
}
