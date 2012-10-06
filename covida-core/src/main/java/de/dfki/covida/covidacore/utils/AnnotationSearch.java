/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.utils;

import de.dfki.covida.covidacore.data.Annotation;
import de.dfki.covida.covidacore.data.AnnotationData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias
 */
public class AnnotationSearch {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(AnnotationSearch.class);

    public static Map<AnnotationData, List<Annotation>> search(List<String> hwrResults, Iterable<AnnotationData> data) {
        Map<AnnotationData, List<Annotation>> searchResult = new HashMap<>();
        if (data != null && hwrResults != null) {
            for (String hwrResult : hwrResults) {
                searchResult = exactSearch(hwrResult, data, searchResult);
                searchResult = caseInsensitiveSearch(hwrResult, data, searchResult);
                searchResult = wrapAroundSearch(hwrResult, data, searchResult);
                searchResult = levenshteinSearch(hwrResult, data, searchResult);
            }
        }
        return searchResult;
    }

    private static Map<AnnotationData, List<Annotation>> exactSearch(String hwrResult, Iterable<AnnotationData> dataList, Map<AnnotationData, List<Annotation>> searchResult) {
        for (AnnotationData data : dataList) {
            if (data != null) {
                for (Annotation annotation : data.getAnnotations()) {
                    if (annotation.description != null) {
                        for (String s : annotation.description.split(" ")) {
                            if (s.equals(hwrResult)) {
                                if (searchResult.containsKey(data)) {
                                    if (!searchResult.get(data).contains(annotation)) {
                                        searchResult.get(data).add(annotation);
                                    }
                                } else {
                                    List<Annotation> list = new ArrayList<>();
                                    list.add(annotation);
                                    searchResult.put(data, list);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        return searchResult;
    }

    private static Map<AnnotationData, List<Annotation>> caseInsensitiveSearch(String hwrResult, Iterable<AnnotationData> dataList, Map<AnnotationData, List<Annotation>> searchResult) {
        for (AnnotationData data : dataList) {
            if (data != null) {
                for (Annotation annotation : data.getAnnotations()) {
                    if (annotation.description != null) {
                        for (String s : annotation.description.split(" ")) {
                            if (s.equalsIgnoreCase(hwrResult)) {
                                if (searchResult.containsKey(data)) {
                                    if (!searchResult.get(data).contains(annotation)) {
                                        searchResult.get(data).add(annotation);
                                    }
                                } else {
                                    List<Annotation> list = new ArrayList<>();
                                    list.add(annotation);
                                    searchResult.put(data, list);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        return searchResult;
    }

    private static Map<AnnotationData, List<Annotation>> wrapAroundSearch(String hwrResult, Iterable<AnnotationData> dataList, Map<AnnotationData, List<Annotation>> searchResult) {
        for (AnnotationData data : dataList) {
            if (data != null) {
                for (Annotation annotation : data.getAnnotations()) {
                    if (annotation.description != null) {
                        for (String s : annotation.description.split(" ")) {
                            if (s.contains(hwrResult)) {
                                if (searchResult.containsKey(data)) {
                                    if (!searchResult.get(data).contains(annotation)) {
                                        searchResult.get(data).add(annotation);
                                    }
                                } else {
                                    List<Annotation> list = new ArrayList<>();
                                    list.add(annotation);
                                    searchResult.put(data, list);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        return searchResult;
    }

    private static Map<AnnotationData, List<Annotation>> levenshteinSearch(String hwrResult, Iterable<AnnotationData> dataList, Map<AnnotationData, List<Annotation>> searchResult) {
        for (AnnotationData data : dataList) {
            if (data != null) {
                for (Annotation annotation : data.getAnnotations()) {
                    if (annotation.description != null) {
                        for (String s : annotation.description.split(" ")) {
                            int distance = StringUtils.getLevenshteinDistance(hwrResult, s);
                            log.debug("SearchString: " + hwrResult
                                    + " - " + s + " distance: "
                                    + distance);
                            if (distance < 3) {
                                if (searchResult.containsKey(data)) {
                                    if (!searchResult.get(data).contains(annotation)) {
                                        searchResult.get(data).add(annotation);
                                    }
                                } else {
                                    List<Annotation> list = new ArrayList<>();
                                    list.add(annotation);
                                    searchResult.put(data, list);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        return searchResult;
    }
}
