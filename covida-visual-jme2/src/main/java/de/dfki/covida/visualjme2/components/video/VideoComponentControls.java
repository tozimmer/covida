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

import com.jme.image.Texture;
import com.jme.image.Texture.WrapMode;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import de.dfki.covida.videovlcj.IVideoControls;
import de.dfki.covida.visualjme2.components.CovidaJMEComponent;
import de.dfki.covida.visualjme2.components.video.controls.ControlButton;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private VideoComponent video;
    private List<ControlButton> controls;

    public VideoComponentControls(VideoComponent video) {
        super("Video " + video.getId() + " Control");
        this.width = video.getWidth();
        this.height = video.getHeight();
        this.video = video;
        initalizeOverlayQuads(JMEUtils.initalizeBlendState());
    }

    private void initalizeOverlayQuads(BlendState alpha) {
        Map<String, String> textureList = new HashMap<>();
        textureList.put("media/textures/video_controls_back.png", "Back");
        textureList.put("media/textures/video_controls_stop.png", "Stop");
        textureList.put("media/textures/video_controls_play.png", "Play");
        textureList.put("media/textures/video_controls_forward.png", "Forward");
        textureList.put("media/textures/video_controls_sound.png", "Sound");
        controls = new ArrayList<>();
        for (String texture : textureList.keySet()) {
            controls.add(new ControlButton(textureList.get(texture),
                    texture, width, height));
        }
        for (int i = 0; i < controls.size(); i++) {
            controls.get(i).setLocalTranslation((-width / 2) + (i * (width / 5)),
                    -height / (1.4f), 0);
            nodeHandler.addAttachChildRequest(this, controls.get(i));
        }
    }

    public VideoComponent getVideo() {
        return video;
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
    }

    @Override
    public void highlightPlay() {
    }

    @Override
    public void highlightStop() {
    }
}
