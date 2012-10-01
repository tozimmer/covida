/*
 * VideoComponentControls.java
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
package de.dfki.covida.visualjme2.components.video;

import com.jme.scene.shape.Quad;
import com.jme.util.GameTaskQueueManager;
import de.dfki.covida.covidacore.components.IControlableComponent;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.videovlcj.IVideoControls;
import de.dfki.covida.visualjme2.components.CovidaJMEComponent;
import de.dfki.covida.visualjme2.components.video.controls.ControlButton;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

public class VideoComponentControls extends CovidaJMEComponent implements IVideoControls {

    /**
     * Logger.
     */
    private Logger log = Logger.getLogger(VideoComponentControls.class);
    protected Quad overlayControlsDefault;
    private int width;
    private int height;
    private IControlableComponent controlable;
    private Map<ActionName, ControlButton> controls;

    public VideoComponentControls(IControlableComponent controlable) {
        super("Video " + controlable.getId() + " Control");
        this.width = controlable.getWidth();
        this.height = controlable.getHeight();
        this.controlable = controlable;
        initalizeControls();
    }

    private void initalizeControls() {
        Map<String, ActionName> controlList = new HashMap<>();
        controlList.put("media/textures/video_controls_back.png", ActionName.BACKWARD);
        controlList.put("media/textures/video_controls_stop.png", ActionName.STOP);
        controlList.put("media/textures/video_controls_pause.png", ActionName.PLAYPAUSE);
        controlList.put("media/textures/video_controls_forward.png", ActionName.FORWARD);
        controlList.put("media/textures/video_controls_soundoff.png", ActionName.SOUND);
        Map<ActionName, String> controlActiveList = new HashMap<>();
        controlActiveList.put(ActionName.BACKWARD, "media/textures/video_controls_back.png");
        controlActiveList.put(ActionName.STOP, "media/textures/video_controls_stop.png");
        controlActiveList.put(ActionName.PLAYPAUSE, "media/textures/video_controls_play.png");
        controlActiveList.put(ActionName.FORWARD, "media/textures/video_controls_forward.png");
        controlActiveList.put(ActionName.SOUND, "media/textures/video_controls_sound.png");
        controls = new HashMap<>();
        int controlHeight = (int) (0.15f * height);
        int controlWidth = width / 5;
        for (String texture : controlList.keySet()) {
            ControlButton control = new ControlButton(controlList.get(texture),
                    controlable, texture, controlActiveList.get(controlList.get(texture)),
                    controlWidth, controlHeight);
            control.setLocalTranslation(controlWidth / 2 + (-width / 2)
                    + (controls.size() * (width / 5)), -height / (1.4f), 0);
            GameTaskQueueManager.getManager().update(new AttachChildCallable(node, control.node));
            controls.put(controlList.get(texture), control);
        }
        controlWidth = (int) (0.15f * height);
        controlList = new HashMap<>();
        controlList.put("media/textures/video_controls_changemedia.png", ActionName.CHANGEMEDIA);
        controlList.put("media/textures/video_controls_close.png", ActionName.CLOSE);
        controlActiveList = new HashMap<>();
        controlActiveList.put(ActionName.CHANGEMEDIA, "media/textures/video_controls_changemedia.png");
        controlActiveList.put(ActionName.CLOSE, "media/textures/video_controls_close.png");
        int start = -width / 2 - controlWidth / 2;
        for (String texture : controlList.keySet()) {
            ControlButton control = new ControlButton(controlList.get(texture),
                    controlable, texture, controlActiveList.get(controlList.get(texture)),
                    controlWidth, controlHeight);
            control.setLocalTranslation(start, height / (1.7f), 0);
            GameTaskQueueManager.getManager().update(new AttachChildCallable(node, control.node));
            controls.put(controlList.get(texture), control);
            start += width + controlWidth;
        }
        controlList = new HashMap<>();
        controlList.put("media/textures/video_controls_list_back.png", ActionName.NONE);
        controlList.put("media/textures/video_controls_list_front.png", ActionName.LIST);
        controlActiveList = new HashMap<>();
        controlActiveList.put(ActionName.NONE, "media/textures/video_controls_list_back.png");
        controlActiveList.put(ActionName.LIST, "media/textures/video_controls_list_front.png");
        for (String texture : controlList.keySet()) {
            ControlButton control = new ControlButton(controlList.get(texture),
                    controlable, texture, controlActiveList.get(controlList.get(texture)),
                    controlWidth, controlHeight);
            control.setLocalTranslation((int) (-width/1.85f), 0, 0);
            if(controlList.get(texture).equals(ActionName.NONE)){
                control.setEnabled(false);
            }
            GameTaskQueueManager.getManager().update(new AttachChildCallable(node, control.node));
            controls.put(controlList.get(texture), control);
            start += width + controlWidth;
        }
    }

    public IControlableComponent getControlable() {
        return controlable;
    }

    @Override
    protected int getHeight() {
        return this.height;
    }

    @Override
    protected int getWidth() {
        return this.width;
    }

    @Override
    public void highlightPause() {
        controls.get(ActionName.PLAYPAUSE).setActive(false);
    }

    @Override
    public void highlightPlay() {
        controls.get(ActionName.PLAYPAUSE).setActive(true);
    }

    @Override
    public void highlightStop() {
        controls.get(ActionName.STOP).setActive(true);
    }
}
