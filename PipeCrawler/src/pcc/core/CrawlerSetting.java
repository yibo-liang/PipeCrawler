/*
 * The MIT License
 *
 * Copyright 2015 yl9.
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pcc.core;

import jpipe.util.Pair;

/**
 *
 * @author yl9
 */
public class CrawlerSetting {

    private CrawlerSetting instance = null;

    private CrawlerSetting() {

    }

    public CrawlerSetting getInstance() {
        if (instance == null) {
            instance = new CrawlerSetting();

        }
        return instance;
    }

    public static boolean USE_PROXY = true;

    private static final String server = "hw-u4-yl-proj-host.ddns.net";
    private static final int port = 36525;
    
    public static int controllerPort=8988;

    public static Pair<String, Integer> getHost() {
        return new Pair<>(server, port);
    }
}
