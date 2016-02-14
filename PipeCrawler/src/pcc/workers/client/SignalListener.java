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
package pcc.workers.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.abstractclass.worker.Worker;
import pcc.core.CrawlerSetting;
import pcc.core.GlobalControll;

/**
 *
 * @author yl9
 */
public class SignalListener implements Runnable {

    public int work() {
        try {
            ServerSocket server;
            Socket client = null;
            BufferedReader in;
            server = new ServerSocket(CrawlerSetting.controllerPort);
            do {

                try {
                    client = server.accept();
                    in = new BufferedReader(new InputStreamReader(
                            client.getInputStream()));
                    String line = in.readLine();
                    if (line.equals("STOP")) {
                        System.out.println("Received Stop, Crawler Client will now exit.");
                        GlobalControll.setState(GlobalControll.STOPPING);
                        break;
                    }
                } catch (IOException e) {
                        //System.out.println("Accept failed: 4321");
                    //System.exit(-1);
                }

            } while (true);
            return Worker.SUCCESS;
        } catch (IOException ex) {
            Logger.getLogger(SignalListener.class.getName()).log(Level.SEVERE, null, ex);
            return Worker.FAIL;
        }
    }

    @Override
    public void run() {
        work();
    }

}
