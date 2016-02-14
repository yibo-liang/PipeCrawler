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
package pcc.workers.server;

import pcc.workers.client.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.abstractclass.worker.Worker;
import jpipe.buffer.util.BufferStore;
import pcc.core.CrawlerSetting;
import pcc.core.GlobalControll;

/**
 *
 * @author yl9
 */
public class ServerObjectReceiver<T> extends Worker {

    private class Receiver implements Runnable {

        Socket sock;

        public Receiver(Socket sock) {
            this.sock = sock;
        }

        @Override
        public void run() {
            InputStream is = null;
            try {
                is = sock.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                Object r = ois.readObject();
                if (r != null) {
                    T[] receivedlist = (T[]) r;

                    for (int i = 0; i < receivedlist.length; i++) {
                        blockedpush(outputbuffer, receivedlist[i]);
                    }

                    System.out.println("Received " + receivedlist.length
                            + " " + receivedlist[0].getClass().getSimpleName());
                }
                ois.close();
                is.close();
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(ServerObjectReceiver.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    Logger.getLogger(ServerObjectReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }

    ServerSocket server;
    private final String outputBuffername;
    Buffer<T> outputbuffer;

    public ServerObjectReceiver(String outputBuffer, BufferStore bs) {
        this.outputBuffername = outputBuffer;
        this.setBufferStore(bs);
        this.outputbuffer = (Buffer<T>) getBufferStore().use(outputBuffername);
    }

    @Override
    public int work() {

        do {
            try {
                System.out.println("A");
                server = new ServerSocket(CrawlerSetting.getHost().getSecond());

                Socket sock = server.accept();
                (new Thread(new Receiver(sock))).start();

                System.out.println("Receiving objects from clients");

            } catch (IOException ex) {
                Logger.getLogger(ServerObjectReceiver.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerObjectReceiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (true);
    }

}
