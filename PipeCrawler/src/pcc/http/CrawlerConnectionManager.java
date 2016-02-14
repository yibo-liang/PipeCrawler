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
package pcc.http;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author yl9
 */
public class CrawlerConnectionManager {

    private static CrawlerConnectionManager instance = null;
    private static final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();

    public static void StartConnectionMonitor() {
        IdleConnectionMonitorThread staleMonitor = new IdleConnectionMonitorThread(connManager);
        staleMonitor.start();
    }

    public static void setMaxConnection(int num) {
        connManager.setMaxTotal(num);
        connManager.setDefaultMaxPerRoute(50);

    }

    private CrawlerConnectionManager() {

        Logger.getLogger("httpclient.wire.header").setLevel(Level.WARN);
        Logger.getLogger("httpclient.wire.content").setLevel(Level.WARN);
    }

    public static CrawlerConnectionManager getInstance() {
        if (instance == null) {
            instance = new CrawlerConnectionManager();
        }
        return instance;
    }

    public static PoolingHttpClientConnectionManager Manger() {
        return connManager;
    }

     public static CrawlerClient getNewClient() {
          RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).build();
        
        return new CrawlerClient(
                HttpClients
                .custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connManager)
                .setConnectionManagerShared(true)
                .build());
    }

    public void shutdown() {
        connManager.shutdown();
    }
}
