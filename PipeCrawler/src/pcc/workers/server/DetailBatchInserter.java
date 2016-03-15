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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.abstractclass.worker.Worker;
import pcc.core.entity.AccountDetail;
import pcc.core.entity.RawAccount;
import pcc.core.hibernate.DatabaseManager;

/**
 *
 * @author yl9
 */
public class DetailBatchInserter extends Worker {

    @Override
    public int work() {

        Buffer<AccountDetail> buffer = this.getBufferStore().use("account_detail");
        try {
            int num = 200;
            if (buffer.getCount() >= num) {
                DatabaseManager.DBInterface dbi = new DatabaseManager.DBInterface();
                AccountDetail[] rusers = new AccountDetail[num];
                for (int i = 0; i < num; i++) {
                    rusers[i] = (AccountDetail) blockedpoll(buffer);
                }

                dbi.Insert(rusers);

            }

        } catch (Exception ex) {
            ServerConnector.logError(ex);
        }
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(DetailBatchInserter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Worker.SUCCESS;
    }

}
