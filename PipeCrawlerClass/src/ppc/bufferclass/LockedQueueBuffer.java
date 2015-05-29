/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ppc.bufferclass;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import ppc.interfaceclass.Buffer;

/**
 *
 * @author Yibo
 * @param <T>
 */
public class LockedQueueBuffer<T> implements Buffer {

    private final Queue<T> queue = new LinkedList<>();
    private int maxsize = 0;

    @Override
    public synchronized void push(Object obj) {

        if (maxsize > 0) {
            if (queue.size() < maxsize) {
                queue.add((T) obj);
            }
        } else {
            queue.add((T) obj);

        }

    }

    @Override
    public synchronized T poll() {
        return queue.poll();
    }

    @Override
    public synchronized T peek() {
         return queue.peek();
    }

    @Override
    public synchronized void clear() {
        queue.clear();
    }

    @Override
    public synchronized int getMaxsize() {
        return this.maxsize;
    }

    @Override
    public synchronized void setMaxsize(int maxsize) {
         this.maxsize = maxsize;
    }

    @Override
    public synchronized List<T> pollAll() {
        List<T> result = new ArrayList<>(queue);
        queue.clear();
        return result;
    }

}
