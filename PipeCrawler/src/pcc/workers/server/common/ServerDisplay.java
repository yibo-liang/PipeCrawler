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
package pcc.workers.server.common;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yl9
 */
public class ServerDisplay {

    private ServerDisplay() {
    }
    public static ServerDisplay INSTANCE = new ServerDisplay();

    private static  HashMap<String, String> messageMap = new HashMap<>();
    private static  HashMap<String, Long> timeMap = new HashMap<>();

    private static boolean updated = false;
    private static String suffix = "";

    public static  void update(String host, String msg) {
        messageMap.put(host, msg);
        long unixTime = System.currentTimeMillis();
        timeMap.put(host, unixTime);
        updated = true;
    }

    public static void changeSuffix(String s) {

        suffix = s;
        updated = true;
    }

    public static boolean isUpdated(){
        return updated;
    }
    
    public static void show() {
        System.out.print("\033[2J\033[1;1H");
        System.out.println("Pipecrawler Server");
        System.out.println("Worker\t\tLast MSG\t\tUpdate at");
        List<String> list = new ArrayList<>(messageMap.keySet());
        Collections.sort(list);
        for (int i = 0; i < list.size(); i++) {
            String worker = list.get(i);
            Date date = new Date(timeMap.get(worker));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            String formattedDate = sdf.format(date);
            System.out.println(String.format("%1$10s", worker) + 
                    ":\t" + String.format("%1$12s",messageMap.get(worker)) + "\t\t" + formattedDate);
        }
        System.out.println("");
        System.out.println(suffix);
        updated = false;
    }

}