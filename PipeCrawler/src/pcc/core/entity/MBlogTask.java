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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author yl9
 */
public class MBlogTask implements Serializable {

    public class SubTaskController implements Serializable {
        
        private static final long serialVersionUID = 7513452012352776147L;
        private boolean[] subtasks = null;

        private int max_page_num;

        public int getMax_page_num() {
            return max_page_num;
        }

        public void setMax_page_num(int max_page_num) {
            this.max_page_num = max_page_num;
            boolean[] temp = subtasks;
            subtasks = new boolean[max_page_num];
            if (temp != null) {
                for (int i = 0; i < Math.min(temp.length, subtasks.length); i++) {
                    subtasks[i] = temp[i];
                }
            }
        }

        public synchronized void setSubTask_done(int index, List<MBlog> mblogs) {
            subtasks[index - 1] = true;
            taskresult.addAll(mblogs);
        }

        public synchronized boolean allDone() {

            for (int i = 0; i < this.subtasks.length; i++) {
                if (!subtasks[i]) {
                    return false;
                }
            }
            return true;
        }

        public synchronized void printStatus() {
            for (int i = 0; i < this.subtasks.length; i++) {
                System.out.println("[" + i + "]=" + subtasks[i]);
            }
        }

    }

    private static final long serialVersionUID = 1213452215622776147L;
    private long user_id;
    private List<MBlog> taskresult = new ArrayList<>();
    private AccountDetail account;
    private SubTaskController subtask = null;

    private int page_num;

    public MBlogTask() {
        this.subtask = new SubTaskController();

        this.subtask.setMax_page_num(1);
        this.setPage_num(1);
    }

    public MBlogTask(SubTaskController subtask) {
        this.subtask = subtask;
    }

    public synchronized void removeDupResult() {
        Set<MBlog> tmp = new HashSet<MBlog>();
        tmp.addAll(taskresult);
        taskresult.clear();
        taskresult.addAll(tmp);
    }

    public synchronized List<MBlog> getResults() {
        return taskresult;
    }

    public synchronized int getMax_page_num() {
        return this.subtask.getMax_page_num();
    }

    public synchronized void setMax_page_num(int max_page_num) {
        this.subtask.setMax_page_num(max_page_num);
    }

    public int getPage_num() {
        return page_num;
    }

    public void setPage_num(int page_num) {
        this.page_num = page_num;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public SubTaskController getSubtask() {
        return subtask;
    }

    public void setSubtask(SubTaskController subtask) {
        this.subtask = subtask;
    }

    public AccountDetail getAccount() {
        return account;
    }

    public void setAccount(AccountDetail account) {
        this.account = account;
    }

    public synchronized boolean AllDone() {
        return this.subtask.allDone();
    }

}
