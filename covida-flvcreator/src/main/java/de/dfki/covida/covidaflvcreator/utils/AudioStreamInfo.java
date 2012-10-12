/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidaflvcreator.utils;

import com.xuggle.xuggler.IAudioSamples.Format;

/**
 *
 * @author touchandwrite
 */
public class AudioStreamInfo extends StreamInfo {
    public int sampleRate;
    public int channels;
    public Format sampleFormat;
}
