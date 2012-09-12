/*
 * DetachHandler.java
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
package de.dfki.covida.visual.components.annotation;

import org.apache.log4j.Logger;

/**
 *
 * @author Tobias Zimmermann
 */
public class DetachHandler implements Runnable {

    private Object field;
    private final Object obj;
    private int delay;
    private Logger log = Logger.getLogger(DetachHandler.class);

    public Object getObject() {
        return obj;
    }

    public DetachHandler(Field field, int delay) {
        this.field = field;
        this.delay = delay;
        obj = new Object();
    }

    @Override
    public void run() {
        synchronized (obj) {
            try {
                obj.wait(delay);
            } catch (InterruptedException e) {
                log.error(e);
            }
            if (field instanceof AnnotationSearchField) {
                AnnotationSearchField search = (AnnotationSearchField) field;
                if (search.isClosing()) {
                    search.detach();
                }
            } else if (field instanceof AnnotationClipboard) {
                AnnotationClipboard clipboard = (AnnotationClipboard) field;
                if (clipboard.isClosing()) {
                    clipboard.detach();
                }
            }
        }
    }
}
