/*
 * CovidaApplicationPreloader.java
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
package de.dfki.covida.visualjme2;

import com.jme.util.GameTaskQueueManager;
import de.dfki.covida.covidacore.data.CovidaConfiguration;
import de.dfki.covida.covidacore.data.VideoMediaData;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.videovlcj.preload.VideoPreload;
import de.dfki.covida.visualjme2.components.ControlButton;
import de.dfki.covida.visualjme2.components.fields.AnnotationClasses;
import de.dfki.covida.visualjme2.components.fields.AnnotationClipboard;
import de.dfki.covida.visualjme2.components.fields.AnnotationSearchField;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import de.dfki.covida.visualjme2.utils.CovidaZOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CovidaApplicationPreloader
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class CovidaApplicationPreloader implements Runnable {

    private CovidaApplication application;
    private Logger log = LoggerFactory.getLogger(CovidaApplicationPreloader.class);

    public CovidaApplicationPreloader(CovidaApplication application) {
        this.application = application;
    }

    private void createSideMenus() {
        AnnotationClipboard clipboard = new AnnotationClipboard(
                "media/textures/clipboard_field_color.png",
                application.getWidth() / 2,
                (int) (application.getHeight() / 1.5f),
                CovidaZOrder.getInstance().getUi_cornermenus());
        clipboard.setLocalTranslation(0, 50, 0);
        clipboard.setDefaultPosition();
        ControlButton clipboardButton = new ControlButton(ActionName.CLOSE,
                clipboard, "media/textures/arrow.png",
                "media/textures/arrow.png", 64, 64,
                CovidaZOrder.getInstance().getUi_cornermenus());
        GameTaskQueueManager.getManager().update(new AttachChildCallable(
                clipboardButton.node, clipboard.node));
        application.addComponent(clipboardButton);
        clipboard.close();
        AnnotationSearchField search = new AnnotationSearchField(
                "media/textures/search_field_color.png",
                application.getWidth() / 2,
                (int) (application.getHeight() / 1.5f),
                CovidaZOrder.getInstance().getUi_cornermenus());
        search.setLocalTranslation(0, 100, 0);
        search.setDefaultPosition();
        ControlButton searchButton = new ControlButton(ActionName.CLOSE,
                search, "media/textures/search.png",
                "media/textures/search.png", 64, 64,
                CovidaZOrder.getInstance().getUi_cornermenus());
        GameTaskQueueManager.getManager().update(new AttachChildCallable(
                searchButton.node, search.node));
        application.addComponent(searchButton);
        search.close();
        ControlButton garbadge = new ControlButton(ActionName.GARBADGE,
                null, "media/textures/garbadge.png",
                "media/textures/garbadge.png", 64, 64,
                CovidaZOrder.getInstance().getUi_cornermenus());
        application.addComponent(garbadge);
        ControlButton openNew = new ControlButton(ActionName.OPEN,
                application, "media/textures/new.png",
                "media/textures/new.png", 64, 64,
                CovidaZOrder.getInstance().getUi_cornermenus());
        application.addComponent(openNew);

        ControlButton configButton = new ControlButton(ActionName.CONFIG,
                application, "media/textures/config.png",
                "media/textures/config.png", 64, 64,
                CovidaZOrder.getInstance().getUi_cornermenus());
        application.addComponent(configButton);
        AnnotationClasses classes = new AnnotationClasses(
                "media/textures/classes_field.png",
                application.getWidth() / 2,
                (int) (application.getHeight() / 1.5f),
                CovidaZOrder.getInstance().getUi_cornermenus());
        classes.setLocalTranslation(0, -100, 0);
        classes.setDefaultPosition();
        ControlButton classesButton = new ControlButton(ActionName.CLOSE,
                classes, "media/textures/classes.png",
                "media/textures/classes.png", 64, 64,
                CovidaZOrder.getInstance().getUi_cornermenus());
        GameTaskQueueManager.getManager().update(new AttachChildCallable(
                classesButton.node, classes.node));
        application.addComponent(classesButton);
//        classes.close();
        
//        AnnotationClasses classes2 = new AnnotationClasses(
//                "media/textures/classes_field.png",
//                application.getWidth() / 2, 
//                (int) (application.getHeight() / 1.5f), 
//                CovidaZOrder.getInstance().getUi_cornermenus());
//        classes2.setLocalTranslation(0, 50, 0);
//        classes2.setDefaultPosition();
//        ControlButton classesButton2 = new ControlButton(ActionName.CLOSE,
//                classes2, "media/textures/classes.png",
//                "media/textures/classes.png", 64, 64, 
//                CovidaZOrder.getInstance().getUi_cornermenus());
//        GameTaskQueueManager.getManager().update(new AttachChildCallable(
//                classesButton2.node, classes2.node));
//        application.addComponent(classesButton2);
//        classes2.close();  
//        ControlButton configButton2 = new ControlButton(ActionName.CONFIG,
//                application, "media/textures/config.png",
//                "media/textures/config.png", 64, 64, 
//                CovidaZOrder.getInstance().getUi_cornermenus());
//        application.addComponent(configButton2);
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName() + " Thread");
        preloadVideos();
        createSideMenus();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            log.error("", ex);
        }
        application.endLoadingAnimation();
    }

    private void preloadVideos() {
        for (VideoMediaData data : CovidaConfiguration.getInstance().videos) {
            VideoPreload preload = new VideoPreload(data);
            preload.run();
        }
    }
}
