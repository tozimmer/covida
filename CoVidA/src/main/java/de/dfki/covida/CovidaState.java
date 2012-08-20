/*
 * CovidaState.java
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
package de.dfki.covida;

import de.dfki.covida.ui.CovidaBoard;
import com.jme.renderer.Renderer;

import de.dfki.touchandwrite.state.ApplicationState;
import de.dfki.touchandwrite.visual.StateBasedTouchAndWriteApp;

public class CovidaState extends ApplicationState {

    protected CovidaBoard videoTouchBoard;
    private CovidaCMDOptions opt;

    public CovidaState(StateBasedTouchAndWriteApp app, CovidaCMDOptions opt) {
        super("MonitorState", app);
        this.opt = opt;
    }

    @Override
    protected boolean initState() {
        this.setStateRenderQueueMode(Renderer.QUEUE_ORTHO);
        videoTouchBoard = new CovidaBoard(opt);
        videoTouchBoard.initComponent();
        videoTouchBoard.setApp(application);
        return true;
    }

    @Override
    protected void activated() {
        videoTouchBoard.registerWithInputHandler(this.application.getTouchInput());
        videoTouchBoard.registerWithInputHandler(this.application.getPenInput());
        addComponent(videoTouchBoard);
    }

    @Override
    public void deactivated() {
        videoTouchBoard.unRegisterWithInputHandler(this.application.getTouchInput());
        videoTouchBoard.unRegisterWithInputHandler(this.application.getPenInput());
        videoTouchBoard.stopVideos();
        detachComponent(videoTouchBoard);

    }

    @Override
    protected void cleanUpState() {
        videoTouchBoard.cleanUp();
    }
}
