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
package pcc.workers.server;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.abstractclass.worker.Worker;
import org.bson.Document;
import pcc.core.CrawlerSetting;
import pcc.core.GlobalControll;
import pcc.core.entity.AccountDetail;
import pcc.core.entity.MBlog;
import pcc.core.entity.MBlogTask;
import pcc.core.entity.RawAccount;
import pcc.core.hibernate.DatabaseManager;

/**
 *
 * @author yl9
 */
public class MBlogBatchInserter extends Worker {

    private void saveToMySQL(MBlogTask task) throws Exception {

        MBlog[] mblogs = task.getResults().toArray(new MBlog[task.getResults().size()]);
        DatabaseManager.DBInterface dbi = new DatabaseManager.DBInterface();
        try (Connection conn = dbi.getJDBC_Connection()) {
            PreparedStatement ps = null;
            for (int i = 0; i < mblogs.length; i++) {
                ps = conn.prepareStatement(
                        "INSERT IGNORE INTO mblog "
                        + "(post_id,"
                        + "user_id,"
                        + "create_timestamp,"
                        + "update_timestamp,"
                        + "repost_count,"
                        + "comments_count,"
                        + "attitudes_count,"
                        + "like_count,"
                        + "picture_count,"
                        + "is_video,"
                        + "is_retweet,"
                        + "retweet_post_id,"
                        + "mblogtype,"
                        + "is_long_text,"
                        + "page_title,"
                        + "text) "
                        + "VALUES "
                        + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                MBlog b = mblogs[i];
                ps.setLong(1, b.getPost_id());
                ps.setLong(2, b.getUser_id());
                ps.setInt(3, b.getCreate_timestamp());
                ps.setInt(4, b.getUpdate_timestamp());
                ps.setInt(5, b.getRepost_count());
                ps.setInt(6, b.getComments_count());
                ps.setInt(7, b.getAttitudes_count());
                ps.setInt(8, b.getLike_count());
                ps.setInt(9, b.getPicture_count());
                ps.setInt(10, b.isIs_video() ? 1 : 0);
                ps.setInt(11, b.isIs_retweet() ? 1 : 0);
                ps.setLong(12, b.getRetweet_post_id());
                ps.setInt(13, b.getMblogtype());
                ps.setInt(14, b.isIs_long_text() ? 1 : 0);
                ps.setString(15, b.getPage_title());
                ps.setString(16, b.getText());
                ps.addBatch();
                //System.out.println("Add Batch" + b.getPost_id() + ":" + b.getText());
                ps.executeBatch();
            }
            if (ps != null) {
                ps.executeBatch();
            }
            ServerConnector.log("LOG: Saved to MySQL for uid=" + task.getUser_id() + ", n=" + mblogs.length);
            conn.close();

        } catch (Exception ex) {
            Logger.getLogger(RawUserBatchInserter.class.getName()).log(Level.SEVERE, null, ex);
            ServerConnector.logError(ex);
            throw ex;
        }
    }

    MongoClient mongoClient;

    private boolean mongoInit = false;

    private void MongoInit() {
        if (!mongoInit) {
            mongoClient = new MongoClient("192.168.1.39");
            MongoDatabase db = mongoClient.getDatabase("ylproj");
            try {
                db.getCollection("accounts").createIndex(new Document("uid", 1));
            } catch (Exception ex) {
            }
            mongoInit = true;
        }
    }

    private void saveToMongDB(MBlogTask task) {
        MongoInit();
        MongoDatabase db = mongoClient.getDatabase("ylproj");
        Document doc = task.getAccount().toBSONDocument();
        List<MBlog> mblogs = task.getResults();
        List<Document> mblog_docs = new ArrayList<>();
        for (MBlog m : mblogs) {
            mblog_docs.add(m.toDocument());
        }
        doc.append("mblogs", mblog_docs);
        db.getCollection("accounts").insertOne(doc);
    }

    @Override
    public int work() {

        Buffer<MBlogTask> buffer = this.getBufferStore().use("mblogresult");

        if (buffer.getCount() >= 1) {

            int num = buffer.getCount();
            MBlogTask[] finishedTasks = new MBlogTask[num];
            for (int i = 0; i < num; i++) {
                finishedTasks[i] = (MBlogTask) blockedpoll(buffer);

            }

            for (MBlogTask task : finishedTasks) {

                try {
                    saveToMySQL(task);
                    saveToMongDB(task);
                } catch (Exception ex) {
                    Logger.getLogger(MBlogBatchInserter.class.getName()).log(Level.SEVERE, null, ex);
                    ServerConnector.logError(ex);
                }

            }

        }
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(RawUserBatchInserter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Worker.SUCCESS;

    }
}
