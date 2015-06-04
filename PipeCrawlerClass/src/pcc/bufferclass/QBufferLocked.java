/*
 * The MIT License
 *
 * Copyright 2015 yl9.
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
package pcc.bufferclass;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import pcc.abstractclass.TPBuffer;

/**
 * This class is a locked implementation of buffer queue It has a maximum size
 * It would return null while polling, if nothing is in the buffer It would
 * return false if a push action failed Maximum size is by default 10 A
 * constructor with integer parameter can change it
 *
 *
 * @author Yibo
 * @param <T>
 */
public class QBufferLocked<T> extends TPBuffer {

    private final Queue<T> queue;
    private int maxsize = 0;
    private int count = 0;

    public QBufferLocked() {
        this.maxsize = 10;
        queue = new LinkedList<>();
    }

    public QBufferLocked(Class<T> c, int maxsize) {
        this.maxsize = maxsize;
        queue = new LinkedList<>();
    }

    @Override
    public synchronized boolean push(Object obj) {

        if (maxsize > 0) {
            if (queue.size() < maxsize) {
                queue.add((T) obj);

            } else {
                return false;
            }
        } else {
            queue.add((T) obj);

        }
        count++;
        return true;
    }

    @Override
    public synchronized T poll() {
        count--;
        return queue.poll();

    }

    @Override
    public synchronized T peek() {
        return queue.peek();
    }

    @Override
    public synchronized void clear() {
        count = 0;
        queue.clear();
    }

    @Override
    public synchronized int getMaxsize() {
        return this.maxsize;
    }

    @Override
    public synchronized boolean setMaxsize(int maxsize) {
        //only change when the new size is larger than 
        //the count of the element in the queue
        if (maxsize >= count) {
            this.maxsize = maxsize;
            return true;
        }
        return false;
    }

    @Override
    public synchronized List<T> pollAll() {
        List<T> result = new ArrayList<>(queue);
        count = 0;
        queue.clear();
        return result;
    }

}
