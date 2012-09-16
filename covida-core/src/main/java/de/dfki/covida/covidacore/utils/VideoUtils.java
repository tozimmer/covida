/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.utils;

/**
 *
 * @author Tobias
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
