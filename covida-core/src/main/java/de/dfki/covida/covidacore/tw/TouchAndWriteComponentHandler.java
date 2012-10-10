/*
 * TouchAndWriteComponentHandler.java
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
package de.dfki.covida.covidacore.tw;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles the Touch and Write Components
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class TouchAndWriteComponentHandler {
    /**
     * Instance of {@link TouchAndWriteComponentHandler}
     */
    private static TouchAndWriteComponentHandler instance;
    /**
     * {@link Collection} of {@link ITouchAndWriteComponent}
     */
    private Collection<ITouchAndWriteComponent> components;
    
    /**
     * Private constructor of {@link TouchAndWriteComponentHandler}
     */
    private TouchAndWriteComponentHandler(){
        components = new ConcurrentLinkedQueue<>();
    }
    
    /**
     * Returns the instance of the {@link TouchAndWriteComponentHandler}.
     * 
     * @return {@link TouchAndWriteComponentHandler}
     */
    public synchronized static TouchAndWriteComponentHandler getInstance(){
        if(instance == null){
            instance = new TouchAndWriteComponentHandler();
        }
        return instance;
    }
    
    /**
     * Adds a {@link ITouchAndWriteComponent}
     * 
     * @param component {@link ITouchAndWriteComponent}
     */
    public void addComponent(ITouchAndWriteComponent component){
        if(!components.contains(component)){
            components.add(component);
        }
    }
    
    /**
     * Returns all {@link ITouchAndWriteComponent} as {@link Collection}
     * 
     * @return {@link Collection} of {@link ITouchAndWriteComponent}
     */
    public Collection<ITouchAndWriteComponent> getComponents(){
        return components;
    }

    public void removeComponent(ITouchAndWriteComponent component) {
        if(components.contains(component)){
           components.remove(component);
        }
    }
}
