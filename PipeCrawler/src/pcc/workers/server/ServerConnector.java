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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.abstractclass.worker.Worker;
import jpipe.buffer.util.BufferStore;
import pcc.core.CrawlerSetting;
import pcc.core.GlobalControll;
import pcc.core.entity.MessageCarrier;
import pcc.workers.server.common.ServerProtocol;

/**
 *
 * @author yl9
 */
public class ServerConnector extends Worker {

    public interface IServerProtocol {

        //return null so that the socket is closed immediately, otherwise
        //socket will reply with the returned message
        public MessageCarrier handleMsg(MessageCarrier mc);

    }

    private void logError(Exception ex) {
        try {

            PrintWriter pw = new PrintWriter(new File("~/error.txt"));
            ex.printStackTrace(pw);
            pw.close();
        } catch (FileNotFoundException ex1) {
            Logger.getLogger(ServerConnector.class.getName()).log(Level.SEVERE, null, ex1);
        }
    }

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
                ois.close();
                is.close();
                
                if (r != null) {
                    MessageCarrier mc = (MessageCarrier) r;
                    System.out.println("Receive from " + mc.getSender() + ", MSG=" + mc.getMsg());
                    MessageCarrier reply = rohandler.handleMsg(mc);

                    OutputStream os = sock.getOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(os);
                    oos.writeObject(reply);
                    System.out.println("Replied to" + mc.getSender() + ", MSG=" + reply.getMsg() + "/" + reply.getObj().toString());

                    oos.flush();
                    oos.close();
                    os.close();

                }else{
                    try{
                    OutputStream os = sock.getOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(os);
                    MessageCarrier reply = new MessageCarrier("ERROR", "");

                    oos.writeObject(reply);
                    System.out.println("Error received," + ", MSG=" + reply.getMsg() + "/" + reply.getObj().toString());
                    
                    oos.flush();
                    oos.close();
                    os.close();
                    }catch(Exception ex){
                        logError(ex);
                    }
                }
                
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(ServerConnector.class.getName()).log(Level.SEVERE, null, ex);
                logError(ex);

            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    logError(ex);
                    Logger.getLogger(ServerConnector.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }

    ServerSocket server;

    private IServerProtocol rohandler;

    public ServerConnector() {
    }

    @Override
    public int work() {
        if (rohandler == null) {
            rohandler = new ServerProtocol(this);
        }

        try {
            server = new ServerSocket(CrawlerSetting.getHost().getSecond());
        } catch (IOException ex) {
            logError(ex);
            Logger.getLogger(ServerConnector.class.getName()).log(Level.SEVERE, null, ex);
            return Worker.FAIL;
        }

        do {
            try {
                //System.out.println("A");

                Socket sock = server.accept();
                (new Thread(new Receiver(sock))).start();

                //System.out.println("Receiving objects from clients");
            } catch (IOException ex) {
                logError(ex);
                Logger.getLogger(ServerConnector.class.getName()).log(Level.SEVERE, null, ex);
            }

        } while (true);
    }

}
