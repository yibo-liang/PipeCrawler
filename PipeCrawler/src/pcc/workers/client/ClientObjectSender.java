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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.abstractclass.worker.Worker;
import jpipe.util.Pair;
import pcc.core.CrawlerSetting;

/**
 *
 * @author yl9
 * @param <T>
 */
public class ClientObjectSender<T extends Serializable> extends Worker {

    int num = 100;

    private final String buffername;

    public ClientObjectSender(String inputBuffer) {
        this.buffername = inputBuffer;
    }

    @Override
    public int work() {
        Buffer<T> inputBuffer = (Buffer<T>) getBufferStore().use(buffername);

        try {
            //Get entity from buffer and feed to serverworker
            Pair<String, Integer> host = CrawlerSetting.getHost();
            Socket socket = new Socket(host.getFirst(), host.getSecond());
            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);

            //if connected 
            T[] list = (T[]) new Object[num];
            for (int i = 0; i < num; i++) {
                list[i] = (T) blockedpoll(inputBuffer);
            }
            
            oos.writeObject(list);
            oos.close();
            os.close();
            socket.close();

            return Worker.SUCCESS;
        } catch (IOException ex) {
            Logger.getLogger(ClientObjectSender.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Worker.FAIL;
    }

}
