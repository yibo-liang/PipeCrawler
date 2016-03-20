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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Index;
import javax.persistence.Transient;

/**
 *
 * @author yl9
 */
@Entity
@Table(name = "account_detail", indexes = {
    @Index(columnList = "id", name = "table_id_idx"),
    @Index(columnList = "uid", name = "user_id_idx")
})
public class AccountDetail implements Serializable {

    @Transient
    private static final long serialVersionUID = 7533152215622776148L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    //user id
    @Column(name = "uid", unique = true)
    private long uid;
    //container id that is used to get the user page
    @Column(name = "container_id")
    private long container_id;
    //last time the info of this user is crawled
    @Column(name = "update_time")
    private int update_time;
    @Column(name = "create_time")
    private int create_time;
    @Column(name = "name")
    private String name;
    @Column(name = "avatar_img", length = 127)
    private String avatar_img;
    @Column(name = "background")
    private String background;
    @Column(name = "description")
    private String description;
    //verification, used to help distinguish
    //member type, also can be used as filters
    @Column(name = "verified", columnDefinition = "TINYINT(1)")
    private boolean verified;
    @Column(name = "v_type")
    private int v_type;
    @Column(name = "member_type")
    private int member_type;
    @Column(name = "member_rank")
    private int member_rank;
    //numbers 
    @Column(name = "blog_num")
    private int blog_num;
    //attention number, guan zhu.
    @Column(name = "att_num")
    private int att_num;
    @Column(name = "fans_num")
    private int fans_num;
    //other info
    @Column(name = "native_place", length = 10)
    private String native_place;

    //0 femail, 1 male, 2 other
    @Column(name = "gender")
    private int gender;

    public AccountDetail(){}
    
    public AccountDetail(BasicDBObject obj){
       this.id=obj.getLong("id");
       this.uid=obj.getLong("uid");
       this.container_id=obj.getLong("container_id");
       this.update_time=obj.getInt("update_time");
       this.create_time=obj.getInt("create_time");
       this.name=obj.getString("name");
       this.avatar_img=obj.getString("avatar_img");
       this.background=obj.getString("background");
       this.description=obj.getString("description");
       this.verified=obj.getBoolean("verified");
       this.v_type=obj.getInt("v_type");
       this.member_type=obj.getInt("member_type");
       this.blog_num=obj.getInt("blog_num");
       this.att_num=obj.getInt("att_num");
       this.fans_num=obj.getInt("fans_num");
       this.native_place=obj.getString("native_place");
       this.gender=obj.getInt("gender");
    }
    
    public BasicDBObject toMongDBObj() {
        BasicDBObject result = new BasicDBObject();
        result.put("id", id);
        result.put("uid", uid);
        result.put("container_id", container_id);
        result.put("update_time", update_time);
        result.put("create_time", create_time);
        result.put("name", name);
        result.put("avatar_img", avatar_img);
        result.put("background", background);
        result.put("description", description);
        result.put("verified", verified);
        result.put("v_type", v_type);
        result.put("member_type", member_type);
        result.put("blog_num", blog_num);
        result.put("att_num", att_num);
        result.put("fans_num", fans_num);
        result.put("native_place", native_place);
        result.put("gender", gender);
        return result;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getContainerid() {
        return container_id;
    }

    public void setContainerid(long containerid) {
        this.container_id = containerid;
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

    public long getContainer_id() {
        return container_id;
    }

    public void setContainer_id(long container_id) {
        this.container_id = container_id;
    }

    public String getAvatar_img() {
        return avatar_img;
    }

    public void setAvatar_img(String avatar_img) {
        this.avatar_img = avatar_img;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = (background.length() <= 255) ? background : background.substring(1, 255);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (description.length() <= 255) ? description : description.substring(1, 255);
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

    public String getNative_place() {
        return native_place;
    }

    public void setNative_place(String native_place) {
        this.native_place = native_place;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        String result = "";
        result += this.getGender() + ",";
        result += this.getUid() + ",";
        result += this.getName() + ",";
        result += this.getDescription() + ",";
        return result;
    }
}
