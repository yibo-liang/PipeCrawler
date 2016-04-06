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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author yl9
 */
public class MBlogTaskResult implements Serializable {

    private static final long serialVersionUID = 3313452215126776147L;

    public static class PostInfo {

        private static final long serialVersionUID = 7213452255622713147L;
        private long postid;
        private int timestamp;

        public long getPostid() {
            return postid;
        }

        public void setPostid(long postid) {
            this.postid = postid;
        }

        public int getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(int timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public int hashCode() {
            return (int) ((this.postid >> 32) ^ this.postid);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof PostInfo)) {
                return false;
            }
            return ((PostInfo) obj).postid == this.postid;
        }

    }

    private AccountDetail account;
    private List<PostInfo> postinfo;

    public AccountDetail getAccount() {
        return account;
    }

    public void setAccount(AccountDetail account) {
        this.account = account;
    }

    
    public List<PostInfo> getPostinfo() {
        return postinfo;
    }

    public void setPostinfo(List<PostInfo> postinfo) {
        this.postinfo = postinfo;
    }

    public synchronized void addPostInfo(PostInfo info) {
        this.postinfo.add(info);
    }

    public synchronized void removeDup() {
        Set<PostInfo> tmp = new HashSet<PostInfo>();
        tmp.addAll(postinfo);
        this.postinfo.clear();
        this.postinfo.addAll(tmp);
    }

}
