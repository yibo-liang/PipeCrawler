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
package pcc.workers.client.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.abstractclass.worker.Worker;
import jpipe.util.Pair;
import pcc.core.CrawlerSetting;
import pcc.core.entity.MessageCarrier;

/**
 *
 * @author yl9
 * @param <T>
 */
public class ClientConnector extends Worker {
    
    
    public interface IClientProtocol{
        
        public MessageCarrier messageToServer(ClientConnector connector);
        
        public void messageFromServer(MessageCarrier msg);
    }
    
    
    private final String protocolBuffer;
    
    public ClientConnector(String protocolBuffer) {
        this.protocolBuffer = protocolBuffer;
    }
    
    @Override
    public int work() {
        Buffer<IClientProtocol> cpbuffer= this.getBufferStore().use(protocolBuffer);
        
        try {
            //Get entity from buffer and feed to serverworker
            Pair<String, Integer> host = CrawlerSetting.getHost();
            Socket socket = new Socket(host.getFirst(), host.getSecond());
            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
;           IClientProtocol cp= (IClientProtocol) this.blockedpoll(cpbuffer);
            
            oos.writeObject(cp.messageToServer(this));
            oos.flush();
            
            
            InputStream is = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            MessageCarrier r = (MessageCarrier)ois.readObject();
            cp.messageFromServer(r);
            
            oos.close();
            os.close();
            socket.close();
            
            return Worker.SUCCESS;
        } catch (IOException ex) {
            //Logger.getLogger(ClientConnector.class.getName()).log(Level.SEVERE, null, ex);
            if (ex.getMessage().contains("Connection refused")) {
                System.out.println("Connection Refused by server");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(ClientConnector.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Worker.FAIL;
    }
    
}