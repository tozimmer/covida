/*
 * AnnotationSearch.java
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
 * Annotation search procedure class
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class AnnotationSearch {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(AnnotationSearch.class);

    /**
     * Search after the occurence of a {@link List} of search terms in a
     * {@link List} of {@link AnnotationData}.
     *
     * @param hwrResults search terms
     * @param dataList {@link List} of {@link AnnotationData}
     * @return {@link Map} of {@link AnnotationData} to {@link List} of
     * {@link Annotation}
     */
    public static Map<AnnotationData, List<Annotation>> search(List<String> hwrResults, Iterable<AnnotationData> dataList) {
        Map<AnnotationData, List<Annotation>> searchResult = new HashMap<>();
        if (dataList != null && hwrResults != null) {
            for (String hwrResult : hwrResults) {
                searchResult = exactSearch(hwrResult, dataList, searchResult);
                searchResult = caseInsensitiveSearch(hwrResult, dataList, searchResult);
                searchResult = wrapAroundSearch(hwrResult, dataList, searchResult);
                searchResult = levenshteinSearch(hwrResult, dataList, searchResult);
            }
        }
        return searchResult;
    }

    /**
     * Exact search after the occurence of a search term
     * in a {@link List} of {@link AnnotationData}.
     *
     * @param hwrResult search term
     * @param dataList {@link List} of {@link AnnotationData}
     * @param searchResult {@link Map} of {@link AnnotationData} to {@link List} of
     * {@link Annotation}
     * @return {@link Map} of {@link AnnotationData} to {@link List} of
     * {@link Annotation}
     */
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

    /**
     * Case insensitive search after the occurence of a search term
     * in a {@link List} of {@link AnnotationData}.
     *
     * @param hwrResult search term
     * @param dataList {@link List} of {@link AnnotationData}
     * @param searchResult {@link Map} of {@link AnnotationData} to {@link List} of
     * {@link Annotation}
     * @return {@link Map} of {@link AnnotationData} to {@link List} of
     * {@link Annotation}
     */
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

    /**
     * Wrap around search after the occurence of a search term
     * in a {@link List} of {@link AnnotationData}.
     *
     * @param hwrResult search term
     * @param dataList {@link List} of {@link AnnotationData}
     * @param searchResult {@link Map} of {@link AnnotationData} to {@link List} of
     * {@link Annotation}
     * @return {@link Map} of {@link AnnotationData} to {@link List} of
     * {@link Annotation}
     */
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

    /**
     * Levenshtein distance search after the occurence of a search term
     * in a {@link List} of {@link AnnotationData}.
     *
     * @param hwrResult search term
     * @param dataList {@link List} of {@link AnnotationData}
     * @param searchResult {@link Map} of {@link AnnotationData} to {@link List} of
     * {@link Annotation}
     * @return {@link Map} of {@link AnnotationData} to {@link List} of
     * {@link Annotation}
     */
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
