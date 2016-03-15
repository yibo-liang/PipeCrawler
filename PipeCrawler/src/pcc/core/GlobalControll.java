/*
 * The MIT License
 *
 * Copyright 2016 yl9.
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

import java.util.HashMap;
import jpipe.util.Pair;

/**
 *
 * @author yl9
 */
public class GlobalControll {

    private static GlobalControll INSTANCE = new GlobalControll();

    private GlobalControll() {

    }

    public static GlobalControll getInstance() {
        return INSTANCE;
    }

    public static final int STARTING = 0;

    public static final int RUNNING = 1;

    public static final int STOPPING = 1;

    private static int GLOBAL_STATE = STARTING;

    public static String PROCESS_TASK;

    public static HashMap<String, String> VARIABLES = new HashMap();

    public static synchronized void setState(int state) {
        GLOBAL_STATE = state;
    }

    public static int getState() {
        return GLOBAL_STATE;
    }

}
