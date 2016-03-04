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
package pcc.core.entity;

import java.io.Serializable;

/**
 *
 * @author yl9
 */
public class MBlogCrawlInfo implements Serializable{
    
    private static final long serialVersionUID = 7513452215633776148L;
    
    private long uid;
    private int blog_count;
    private String crawl_state;
    
    public int[] get_crawl_state(){
        int [] result=new int[crawl_state.length()];
        for (int i=0;i<result.length;i++){
            result[i]=Integer.parseInt(crawl_state.substring(i,1));
        }
        return result;
    }
    
    private String get_crawl_str(int[] vec){
        String result="";
        for (int i=0;i<vec.length;i++){
            result=result+vec;
        }
        
        return result;
        
    }
    
    public synchronized void set_crawl_state(int i, int state){
        int[] states=get_crawl_state();
        states[i]=state;
        this.crawl_state=get_crawl_str(states);
               
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public int getBlog_count() {
        return blog_count;
    }

    public void setBlog_count(int blog_count) {
        this.blog_count = blog_count;
    }

    public String getCrawl_state() {
        return crawl_state;
    }

    public void setCrawl_state(String crawl_state) {
        this.crawl_state = crawl_state;
    }
    
    
    
}
