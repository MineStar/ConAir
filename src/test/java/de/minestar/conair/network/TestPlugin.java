/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Minestar.de
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.minestar.conair.network;

import de.minestar.conair.api.plugin.ConAirPlugin;


public class TestPlugin extends ConAirPlugin {

    /**
     * Called on client side ONLY.
     */
    public void onConnected() {
        System.out.println("onConnect: " + getPacketSender().getName());
    }


    /**
     * Called on client side ONLY.
     */
    public void onDisconnected() {
        System.out.println("onDisconnect: " + getPacketSender().getName());
    }


    public void onLoad() {
        System.out.println("onLoad: " + getPacketSender().getName());
    }


    public void onPreEnable() {
        System.out.println("onPreEnable: " + getPacketSender().getName());
    }


    public void onEnable() {
        System.out.println("onEnable: " + getPacketSender().getName());
    }


    public void onPostEnable() {
        System.out.println("onPostEnable: " + getPacketSender().getName());
    }


    public void onPreDisable() {
        System.out.println("onPreDisable: " + getPacketSender().getName());
    }


    public void onDisable() {
        System.out.println("onDisable: " + getPacketSender().getName());
    }


    public void onPostDisable() {
        System.out.println("onPostDisable: " + getPacketSender().getName());
    }

}
