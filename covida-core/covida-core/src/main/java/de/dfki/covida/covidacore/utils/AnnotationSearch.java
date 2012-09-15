/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.utils;

import de.dfki.covida.covidacore.data.SearchResult;
import de.dfki.covida.covidacore.data.VideoAnnotationData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Tobias
 */
public class AnnotationSearch {

    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(AnnotationSearch.class);
    
    public static SearchResult search(List<String> hwrResults, ArrayList<VideoAnnotationData> data) {
        SearchResult searchResult = new SearchResult();
        if (data != null && hwrResults != null) {
            Map<Integer, ArrayList<String>> resultString = new HashMap<>();
            Map<Integer, ArrayList<Integer>> result = new HashMap<>();
            searchResult.result = result;
            searchResult.resultString = resultString;
            for (String hwrResult : hwrResults) {
                searchResult = exactSearch(hwrResult, data, searchResult);
                searchResult = caseInsensitiveSearch(hwrResult, data, searchResult);
                searchResult = wrapAroundSearch(hwrResult, data, searchResult);
                searchResult = levenshteinSearch(hwrResult, data, searchResult);
            }
        }
        return searchResult;
    }

    private static SearchResult exactSearch(String hwrResult, ArrayList<VideoAnnotationData> data, SearchResult searchResult) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) != null) {
                for (int j = 0; j < data.get(i).size(); j++) {
                    if (data.get(i).annotations.get(j).description != null) {
                        for (String s : data.get(i).annotations.get(j).description.split(" ")) {
                            if (s.equals(hwrResult)) {
                                if (searchResult.result.containsKey(new Integer(i))) {
                                    searchResult.result.get(new Integer(i)).add(j);
                                    searchResult.resultString.get(new Integer(i)).add(s);
                                } else {
                                    ArrayList<Integer> list = new ArrayList<>();
                                    ArrayList<String> stringList = new ArrayList<>();
                                    list.add(j);
                                    stringList.add(s);
                                    searchResult.result.put(new Integer(i), list);
                                    searchResult.resultString.put(new Integer(i),
                                            stringList);
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

    private static SearchResult caseInsensitiveSearch(String hwrResult, ArrayList<VideoAnnotationData> data, SearchResult searchResult) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) != null) {
                for (int j = 0; j < data.get(i).size(); j++) {
                    if (data.get(i).annotations.get(j).description != null) {
                        for (String s : data.get(i).annotations.get(j).description.split(" ")) {
                            if (s.equalsIgnoreCase(hwrResult)) {
                                if (searchResult.result.containsKey(new Integer(i))) {
                                    if (!searchResult.result.get(new Integer(i)).contains(new Integer(j))) {
                                        searchResult.result.get(new Integer(i)).add(
                                                j);
                                        searchResult.resultString.get(new Integer(i)).add(s);
                                    } else {
                                    }
                                } else {
                                    ArrayList<Integer> list = new ArrayList<>();
                                    ArrayList<String> stringList = new ArrayList<>();
                                    list.add(j);
                                    stringList.add(s);
                                    searchResult.result.put(new Integer(i), list);
                                    searchResult.resultString.put(new Integer(i),
                                            stringList);
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

    private static SearchResult wrapAroundSearch(String hwrResult, ArrayList<VideoAnnotationData> data, SearchResult searchResult) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) != null) {
                for (int j = 0; j < data.get(i).size(); j++) {
                    if (data.get(i).annotations.get(j).description != null) {
                        for (String s : data.get(i).annotations.get(j).description.split(" ")) {
                            if (s.contains(hwrResult)) {
                                if (searchResult.result.containsKey(new Integer(i))) {
                                    if (!searchResult.result.get(new Integer(i)).contains(new Integer(j))) {
                                        searchResult.result.get(new Integer(i)).add(
                                                j);
                                        searchResult.resultString.get(new Integer(i)).add(s);
                                    } else {
                                    }
                                } else {
                                    ArrayList<Integer> list = new ArrayList<>();
                                    ArrayList<String> stringList = new ArrayList<>();
                                    list.add(j);
                                    stringList.add(s);
                                    searchResult.result.put(new Integer(i), list);
                                    searchResult.resultString.put(new Integer(i),
                                            stringList);
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

    private static SearchResult levenshteinSearch(String hwrResult, ArrayList<VideoAnnotationData> data, SearchResult searchResult) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) != null) {
                for (int j = 0; j < data.get(i).size(); j++) {
                    if (data.get(i).annotations.get(j).description != null) {
                        for (String s : data.get(i).annotations.get(j).description.split(" ")) {
                            int distance = StringUtils.getLevenshteinDistance(hwrResult, s);
                            log.debug("SearchString: " + hwrResult
                                    + " - " + s + " distance: "
                                    + distance);
                            if (distance < 3) {
                                if (searchResult.result.containsKey(new Integer(i))) {
                                    if (!searchResult.result.get(new Integer(i)).contains(new Integer(j))) {
                                        searchResult.result.get(new Integer(i)).add(
                                                j);
                                        searchResult.resultString.get(new Integer(i)).add(s);
                                    } else {
                                    }
                                } else {
                                    ArrayList<Integer> list = new ArrayList<>();
                                    ArrayList<String> stringList = new ArrayList<>();
                                    list.add(j);
                                    stringList.add(s);
                                    searchResult.result.put(new Integer(i), list);
                                    searchResult.resultString.put(new Integer(i),
                                            stringList);
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
