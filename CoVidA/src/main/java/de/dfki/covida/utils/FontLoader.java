/*
 * FontLoader.java
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
package de.dfki.covida.utils;

import com.jmex.angelfont.BitmapFont;
import com.jmex.angelfont.BitmapFontLoader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import org.apache.log4j.Logger;

public class FontLoader {
    
    private static FontLoader instance;
    private ArrayList<BitmapFont> fnt;
    private static final Logger log = Logger.getLogger(FontLoader.class);
    
    private FontLoader() {
        fnt = new ArrayList<>();
        initializeFonts();
    }
    
    private void initializeFonts() {
        ArrayList<URL> fontFileList = new ArrayList<>();
        ArrayList<URL> textureFileList = new ArrayList<>();
        fontFileList.add(BitmapFontLoader.class.getClassLoader()
                .getResource("media/fonts/youngtech_outline.fnt"));
        fontFileList.add(BitmapFontLoader.class.getClassLoader()
                .getResource("media/fonts/ubuntu_outline.fnt"));
        fontFileList.add(BitmapFontLoader.class.getClassLoader()
                .getResource("media/fonts/karabinE_outline.fnt"));
        fontFileList.add(BitmapFontLoader.class.getClassLoader()
                .getResource("media/fonts/youngtech.fnt"));
        fontFileList.add(BitmapFontLoader.class.getClassLoader()
                .getResource("media/fonts/ubuntu.fnt"));
        fontFileList.add(BitmapFontLoader.class.getClassLoader()
                .getResource("media/fonts/karabinE.fnt"));
        textureFileList.add(BitmapFontLoader.class.getClassLoader()
                .getResource("media/fonts/youngtech_outline_0.png"));
        textureFileList.add(BitmapFontLoader.class.getClassLoader()
                .getResource("media/fonts/ubuntu_outline_0.png"));
        textureFileList.add(BitmapFontLoader.class.getClassLoader()
                .getResource("media/fonts/karabinE_outline_0.png"));
        textureFileList.add(BitmapFontLoader.class.getClassLoader()
                .getResource("media/fonts/youngtech_0.png"));
        textureFileList.add(BitmapFontLoader.class.getClassLoader()
                .getResource("media/fonts/ubuntu_0.png"));
        textureFileList.add(BitmapFontLoader.class.getClassLoader()
                .getResource("media/fonts/karabinE_0.png"));
        for (int i = 0; i < fontFileList.size(); i++) {
            try {
                fnt.add(BitmapFontLoader.load(fontFileList.get(i), textureFileList.get(i)));
            } catch (IOException e) {
                fnt.add(BitmapFontLoader.loadDefaultFont());
                log.error("" + e);
            }
        }
    }
    
    public int size() {
        return fnt.size();
    }
    
    public static FontLoader getInstance() {
        if (instance == null) {
            instance = new FontLoader();
        }
        return instance;
    }
    
    public BitmapFont getBitmapFont(int font) {
        if (font < fnt.size()) {
            return fnt.get(font);
        }
        log.debug("font < fnt.size()");
        return BitmapFontLoader.loadDefaultFont();
    }
}
