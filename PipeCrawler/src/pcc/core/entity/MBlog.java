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
public class MBlog implements Serializable{
    
    private static final long serialVersionUID = 7513452215622776111L;
    
    private long postid;
    private long userid;
    private int create_timestamp;
    private int update_timestamp;
    
    
    /* Social counts */
    //zhuan fa
    private int repost_count;
    
    private int comments_count;
    
    private int attitudes_count;
    
    private int like_count;
    
    //
    
    private int picture_count;
    
    private boolean is_retweet;
    private long retweet_post_id;
    
    //
    private byte mblogtype;
    private boolean is_long_text;
    
    
    private String page_title;
    
    
    //
    private String object_type;

    
    //getter and setters
    public long getPostid() {
        return postid;
    }

    public void setPostid(long postid) {
        this.postid = postid;
    }

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    public int getCreate_timestamp() {
        return create_timestamp;
    }

    public void setCreate_timestamp(int create_timestamp) {
        this.create_timestamp = create_timestamp;
    }

    public int getUpdate_timestamp() {
        return update_timestamp;
    }

    public void setUpdate_timestamp(int update_timestamp) {
        this.update_timestamp = update_timestamp;
    }

    public int getRepost_count() {
        return repost_count;
    }

    public void setRepost_count(int repost_count) {
        this.repost_count = repost_count;
    }

    public int getComments_count() {
        return comments_count;
    }

    public void setComments_count(int comments_count) {
        this.comments_count = comments_count;
    }

    public int getAttitudes_count() {
        return attitudes_count;
    }

    public void setAttitudes_count(int attitudes_count) {
        this.attitudes_count = attitudes_count;
    }

    public int getLike_count() {
        return like_count;
    }

    public void setLike_count(int like_count) {
        this.like_count = like_count;
    }

    public int getPicture_count() {
        return picture_count;
    }

    public void setPicture_count(int picture_count) {
        this.picture_count = picture_count;
    }

    public boolean isIs_retweet() {
        return is_retweet;
    }

    public void setIs_retweet(boolean is_retweet) {
        this.is_retweet = is_retweet;
    }

    public long getRetweet_post_id() {
        return retweet_post_id;
    }

    public void setRetweet_post_id(long retweet_post_id) {
        this.retweet_post_id = retweet_post_id;
    }

    public byte getMblogtype() {
        return mblogtype;
    }

    public void setMblogtype(byte mblogtype) {
        this.mblogtype = mblogtype;
    }

    public boolean isIs_long_text() {
        return is_long_text;
    }

    public void setIs_long_text(boolean is_long_text) {
        this.is_long_text = is_long_text;
    }

    public String getPage_title() {
        return page_title;
    }

    public void setPage_title(String page_title) {
        this.page_title = page_title;
    }

    public String getObject_type() {
        return object_type;
    }

    public void setObject_type(String object_type) {
        this.object_type = object_type;
    }
    
    
}
