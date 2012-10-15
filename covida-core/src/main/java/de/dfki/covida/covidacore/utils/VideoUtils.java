/*
 * VideoUtils.java
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

/**
 * Video utility class
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class VideoUtils {

    /**
     * Returns the time code as {@link String}
     *
     * @param time time as ms as {@link Long}
     * @return {@link String} in the format hh:mm:ss
     */
    public static String getTimeCode(long time) {
        time = (long) ((float) time / 1000.f);
        int seconds = (int) (time % 60);
        String sString;
        if (seconds < 10) {
            sString = "0" + String.valueOf(seconds);
        } else {
            sString = String.valueOf(seconds);
        }
        time = (long) ((float) time / 60.f);
        int minutes = (int) (time % 60);
        String mString;
        if (minutes < 10) {
            mString = "0" + String.valueOf(minutes);
        } else {
            mString = String.valueOf(minutes);
        }
        time = (long) ((float) time / 60.f);
        int hours = (int) (time % 60);
        String hString;
        if (hours < 10) {
            hString = "0" + String.valueOf(hours);
        } else {
            hString = String.valueOf(hours);
        }
        return (hString + ":" + mString + ":" + sString);
    }
}
