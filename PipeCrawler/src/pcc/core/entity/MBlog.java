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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import javax.persistence.Transient;
import java.io.Serializable;
import javax.persistence.Index;
import org.hibernate.annotations.Type;

/**
 *
 * @author yl9
 */
@Entity
@Table(name = "mblog", indexes = {
    @Index ( columnList = "user_id", name="post_user_id_idx"),
    @Index ( columnList = "create_timestamp", name="ctime_idx")
    
    
})
public class MBlog implements Serializable{
    
    @Transient
    private static final long serialVersionUID = 7513452215622776111L;
    
    @Id
    @Column(name = "post_id")
    private long post_id;
    
    @Column(name = "user_id")
    private long user_id;
    
    @Column(name="create_timestamp")
    private int create_timestamp;
    @Column(name="update_timestamp")
    private int update_timestamp;
    
    
    /* Social counts */
    //zhuan fa
    @Column(name="repost_count")
    private int repost_count;
    
    @Column(name="comments_count")
    private int comments_count;
    
    @Column(name="attitudes_count")
    private int attitudes_count;
    
    @Column(name="like_count")
    private int like_count;
    
    //
    @Column(name="picture_count")
    private int picture_count;
    
    @Column(name="is_video",columnDefinition = "TINYINT(1)")
    private boolean is_video;
    @Column(name="is_retweet",columnDefinition = "TINYINT(1)")
    private boolean is_retweet;
    @Column(name="retweet_post_id")
    private long retweet_post_id;
    
    //
    @Column(name="mblogtype", columnDefinition = "TINYINT(1)")
    private byte mblogtype;
    @Column(name="is_long_text",columnDefinition = "TINYINT(1)")
    private boolean is_long_text;
    
    @Column(name="page_title")
    private String page_title;
    
    
    //

    
    //getter and setters
    public long getPostid() {
        return post_id;
    }

    public void setPostid(long postid) {
        this.post_id = postid;
    }

    public long getUserid() {
        return user_id;
    }

    public void setUserid(long userid) {
        this.user_id = userid;
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

    public boolean isIs_video() {
        return is_video;
    }

    public void setIs_video(boolean is_video) {
        this.is_video = is_video;
    }

    
    
}
