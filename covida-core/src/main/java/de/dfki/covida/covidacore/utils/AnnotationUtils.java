/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.utils;

import de.dfki.covida.covidacore.data.AnnotationData;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tobias
 */
public class AnnotationUtils {

    public static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(AnnotationData.class);

    public static String getCurrentDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(cal.getTime());
    }
    
    public static String getDateString(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }
    
    public static Date getDate(String string){
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        try {
            return sdf.parse(string);
        } catch (ParseException ex) {
            log.error("",ex);
            return Calendar.getInstance().getTime();
        }
    }
}
