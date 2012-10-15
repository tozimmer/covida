/*
 * DataTest.java
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
package de.dfki.covida.covidacore.data.test;

import de.dfki.covida.covidacore.data.Annotation;
import de.dfki.covida.covidacore.data.AnnotationData;
import de.dfki.covida.covidacore.data.CovidaConfiguration;
import java.awt.Point;
import java.util.ArrayList;

/**
 * Data test class.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class DataTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        AnnotationData data = AnnotationData.load(new DataTestVideoComponent());
        Annotation annotation = new Annotation();
        annotation.description = "Data Test DFKI";
        annotation.shapePoints = new ArrayList<>();
        annotation.shapePoints.add(new Point(24, 30));
        annotation.shapePoints.add(new Point(98, 32));
        annotation.shapePoints.add(new Point(100, 121));
        annotation.shapePoints.add(new Point(22, 119));
        annotation.shapePoints.add(new Point(24, 30));
        annotation.time_end = (long) 456343;
        annotation.time_start = (long) 455322;
        data.save(annotation);
        data.save();
        CovidaConfiguration conf = CovidaConfiguration.getInstance();
        conf.save();
    }
}