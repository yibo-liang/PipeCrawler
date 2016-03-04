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
public class User implements Serializable {
    
    private static final long serialVersionUID = 7513452215622776148L;
    
    //user id
    private long id;
    //container id that is used to get the user page
    private long containerid;
    
    //last time the info of this user is crawled
    private int update_time;
    private int create_time;
    
    private String name;
    private String background;
    private String description;
    
    //verification, used to help distinguish
    //member type, also can be used as filters
    private boolean verified;
    private int v_type;
    
    private int member_type;
    private int member_rank;
    
    //numbers 
    private int blog_num;
    //attention number, guan zhu.
    private int att_num;
    private int fans_num;
    
    //other info
    private int native_place;
    
    
    //0 femail, 1 male, 2 other
    private int gender;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getContainerid() {
        return containerid;
    }

    public void setContainerid(long containerid) {
        this.containerid = containerid;
    }

    public int getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(int update_time) {
        this.update_time = update_time;
    }

    public int getCreate_time() {
        return create_time;
    }

    public void setCreate_time(int create_time) {
        this.create_time = create_time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public int getV_type() {
        return v_type;
    }

    public void setV_type(int v_type) {
        this.v_type = v_type;
    }

    public int getMember_type() {
        return member_type;
    }

    public void setMember_type(int member_type) {
        this.member_type = member_type;
    }

    public int getMember_rank() {
        return member_rank;
    }

    public void setMember_rank(int member_rank) {
        this.member_rank = member_rank;
    }

    public int getBlog_num() {
        return blog_num;
    }

    public void setBlog_num(int blog_num) {
        this.blog_num = blog_num;
    }

    public int getAtt_num() {
        return att_num;
    }

    public void setAtt_num(int att_num) {
        this.att_num = att_num;
    }

    public int getFans_num() {
        return fans_num;
    }

    public void setFans_num(int fans_num) {
        this.fans_num = fans_num;
    }

    public int getNative_place() {
        return native_place;
    }

    public void setNative_place(int native_place) {
        this.native_place = native_place;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }
    
    
}
