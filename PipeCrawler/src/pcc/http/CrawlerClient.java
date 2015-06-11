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
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import pcc.http.entity.Proxy;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ParseException;

/**
 *
 * @author yl9
 */
public class CrawlerClient {

    private HttpHost proxy;// = new HttpHost("127.0.0.1", 80, "http");

    private List<Header> headers = new ArrayList<>();
    private final int CONNECTION_TIMEOUT = 5 * 1000; // timeout in millis

    public HttpHost getProxy() {
        return proxy;
    }

    public void setProxy(Proxy iproxy) {
        this.proxy = new HttpHost(iproxy.getHost(), Integer.parseInt(iproxy.getPort()), "http");

    }

    public CrawlerClient() {

    }

    public void addHeader(Header header) {
        headers.add(header);
    }

    public void addHeader(String key, String value) {
        headers.add(new BasicHeader(key, value));
    }

    public String wget(String url) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        RequestConfig requestConfig;
        String result = null;
        try {
            HttpGet request = new HttpGet(url);
            if (proxy != null) {

                requestConfig = RequestConfig.custom()
                        .setConnectionRequestTimeout(CONNECTION_TIMEOUT)
                        .setConnectTimeout(CONNECTION_TIMEOUT)
                        .setSocketTimeout(CONNECTION_TIMEOUT)
                        .setProxy(proxy)
                        .build();

            } else {
                requestConfig = RequestConfig.custom()
                        .setConnectionRequestTimeout(CONNECTION_TIMEOUT)
                        .setConnectTimeout(CONNECTION_TIMEOUT)
                        .setSocketTimeout(CONNECTION_TIMEOUT)
                        .build();
            }

            request.setConfig(requestConfig);
            for (Iterator<Header> h = headers.iterator(); h.hasNext();) {
                request.setHeader(h.next());
            }
            //System.out.println("Executing request " + request.getRequestLine());
            try (CloseableHttpResponse response = httpclient.execute(request)) {
                HttpEntity entity = response.getEntity();
                result = entity != null ? EntityUtils.toString(entity) : null;
                
                EntityUtils.consume(entity);
            }

        } catch (IOException | ParseException ex) {
            return null;
        } finally {
            httpclient.close();
        }
        return result;
    }

    /**
     *
     * @param url
     * @return
     * @throws java.io.IOException
     */
    public HttpEntity wgetEntity(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpEntity result;
        try {
            HttpGet request = new HttpGet(url);
            if (proxy != null) {
                RequestConfig config = RequestConfig.custom()
                        .setProxy(proxy)
                        .build();

                request.setConfig(config);
            }
            for (Iterator<Header> h = headers.iterator(); h.hasNext();) {
                request.setHeader(h.next());
            }
            System.out.println("Executing request " + request.getRequestLine());
            try (CloseableHttpResponse response = httpclient.execute(request)) {
                result = response.getEntity();
            }
        } finally {
            httpclient.close();
        }
        return result;
    }

}
