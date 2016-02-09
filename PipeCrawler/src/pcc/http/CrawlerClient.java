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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import pcc.http.entity.Proxy;

/**
 *
 * @author yl9
 */
public class CrawlerClient {

    private final CloseableHttpClient client;
    private int timeout = 40 * 1000;
    private RequestConfig requestConfig;
    private final List<Header> headers = new ArrayList<>();

    public void addHeader(Header header) {
        headers.add(header);
    }

    public void addHeader(String key, String value) {
        headers.add(new BasicHeader(key, value));
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public CrawlerClient(CloseableHttpClient client) {
        this.client = client;
        requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(timeout)
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
    }

    /**
     * Set the proxy this client may use. Set null to use no proxy.
     *
     * @param proxy
     */
    public void setProxy(Proxy proxy) {
        if (proxy == null) {
            requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .setSocketTimeout(timeout)
                    .build();
            return;
        }
        HttpHost proxyHost = new HttpHost(proxy.getHost(), Integer.parseInt(proxy.getPort()), "http");
        requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(timeout)
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .setProxy(proxyHost)
                .build();
    }

    public String wget(String url) {
        String result = null;
        try {
            HttpGet request = new HttpGet(url);

            request.setConfig(requestConfig);
            for (Iterator<Header> h = headers.iterator(); h.hasNext();) {
                request.setHeader(h.next());
            }
            //request.setHeader("Connection", "close");
            try (CloseableHttpResponse response = client.execute(request)) {
                HttpEntity entity = response.getEntity();
                result = entity != null ? EntityUtils.toString(entity) : null;
                EntityUtils.consume(entity);
                response.close();

            }
            return result;
        } catch (IOException ex) {
            //Logger.getLogger(CrawlerClient.class.getName()).log(Level.SEVERE, null, ex);
            // ex.printStackTrace();
            return result;
        }
    }

    public void close() {
        try {
            this.client.close();
        } catch (IOException ex) {
            Logger.getLogger(CrawlerClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
