/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.data;

import de.dfki.covida.covidacore.utils.AnnotationUtils;
import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter for serialize {@link Date} objects.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class DateAdapter extends XmlAdapter<String, Date> {

    @Override
    public String marshal(Date date) throws Exception {
        return AnnotationUtils.getDateString(date);
    }

    @Override
    public Date unmarshal(String str) throws Exception {
        return AnnotationUtils.getDate(str);
    }
}
