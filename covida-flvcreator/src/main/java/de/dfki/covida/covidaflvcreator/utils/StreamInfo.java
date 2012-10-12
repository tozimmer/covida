/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidaflvcreator.utils;

import com.xuggle.xuggler.ICodec.ID;
import com.xuggle.xuggler.ICodec.Type;
import com.xuggle.xuggler.IRational;

/**
 *
 * @author touchandwrite
 */
public class StreamInfo {
    public Type codecType;
    public ID codecID;
    public long duration;
    public long startTime;
    public String language;
    public IRational streamTimeBase;
    public IRational coderTimeBase;
}
