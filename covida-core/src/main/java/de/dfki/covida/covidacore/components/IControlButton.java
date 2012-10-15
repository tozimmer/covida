/*
 * IControlButton.java
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
package de.dfki.covida.covidacore.components;

/**
 * Interface for control buttons.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public interface IControlButton {

    /**
     * Sets the {@link IControlButton} activated / inactivated
     *
     * Note that this method does not disable the button, it only changes the
     * internal state e.g. change the texture of the button.
     *
     * @param activated if true button is activated
     */
    public void setActive(boolean activated);

    /**
     * Returns the active state of the {@link IControlButton}
     *
     * @return true if button is in active state
     */
    public boolean getActive();

    /**
     * Disables / enables the {@link IControlButton}
     *
     * @param enabled if true {@link IControlButton} will be enabled
     */
    public void setEnabled(boolean enabled);

    /**
     * Returns the status of the {@link IControlButton}
     *
     * @return true if button is enabled
     */
    public boolean getEnabled();

    /**
     * Method which should be overwritten whitch theaction which should be
     * performed on a button click.
     */
    public void toggle();
}
