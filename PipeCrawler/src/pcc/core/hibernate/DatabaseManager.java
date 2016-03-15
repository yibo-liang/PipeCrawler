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
package pcc.core.hibernate;

import net.sf.ehcache.hibernate.HibernateUtil;
import org.hibernate.*;
import org.hibernate.cfg.*;
import org.hibernate.exception.ConstraintViolationException;
import pcc.core.GlobalControll;
import pcc.http.CrawlerClient;
import pcc.workers.server.ServerConnector;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yl9
 */
public class DatabaseManager {

    public static class DBInterface<T> {

        public Connection getJDBC_Connection() {

            try {
                String JDBC_DRIVER = "com.mysql.jdbc.Driver";
                String DB_URL = "jdbc:mysql://192.168.1.39/ylproj"
                        + "useServerPrepStmts=false"
                        + "&rewriteBatchedStatements=true"
                        + "&useUnicode=true"
                        + "&characterEncoding=UTF-8";
                String USER = "java";
                String PASS = "NE391NDF9";

                Class.forName(JDBC_DRIVER);
                Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                return conn;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

        public void batchInsert(T[] arr) {
            int dup = 0;
            StatelessSession session = getStatelessSession();
            Transaction tx = session.beginTransaction();
            for (int i = 0; i < arr.length; i++) {
                try {

                    session.insert(arr[i]);

                } catch (Exception ex) {
                    dup++;
                }
            }
            tx.commit();
            session.close();
            System.out.println("*Duplicate: " + dup + "/" + arr.length);
        }

        public void Insert(T[] arr) {
            Session session = getSession();
            Transaction tx = session.beginTransaction();
            try {
                for (int i = 0; i < arr.length; i++) {
                    System.out.println(arr[i]);
                    session.saveOrUpdate(arr[i]);

                    if (i % 20 == 0) {
                        session.flush();
                        session.clear();
                    }

                }

            } catch (Exception ex) {
                ServerConnector.logError(ex);
            }

            tx.commit();
            session.close();
        }

    }

    private DatabaseManager() {
        load();
    }

    public static DatabaseManager INSTANCE = new DatabaseManager();

    private static SessionFactory sessionFactory;

    private static void load() {

        if (!GlobalControll.PROCESS_TASK.equals("SERVER")
                && !GlobalControll.PROCESS_TASK.equals("DBINIT")
                && !GlobalControll.PROCESS_TASK.equals("TEST")) {
            return;
        }

        System.out.println("Initiating DB");
        Configuration configuration
                = new Configuration().
                configure("hibernate.cfg.xml")
                .addPackage("pcc.core.entity")
                .addAnnotatedClass(pcc.core.entity.MBlog.class
                )
                .addAnnotatedClass(pcc.core.entity.RawAccount.class
                )
                .addAnnotatedClass(pcc.core.entity.AccountDetail.class
                )
                .addAnnotatedClass(pcc.core.entity.MBlogCrawlInfo.class
                )
                .addAnnotatedClass(pcc.core.entity.DetailCrawlProgress.class)
                ;

        sessionFactory = configuration
                .configure()
                .buildSessionFactory();
    }

    public static Session getSession()
            throws HibernateException {
        if (sessionFactory == null) {
            load();
        }
        return sessionFactory.openSession();
    }

    public static StatelessSession getStatelessSession() throws HibernateException {
        if (sessionFactory == null) {
            load();
        }
        return sessionFactory.openStatelessSession();
    }

}
