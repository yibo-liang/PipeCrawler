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

/**
 *
 * @author yl9
 */
public class LockedQueueBuffer {

    private final Queue<Object> queue = new LinkedList<>();
    private int maxsize = 0;

    public synchronized int push(Object obj) {
        if (maxsize > 0) {
            if (queue.size() < maxsize) {
                queue.add(obj);
                return 1;
            } else {
                return 0;
            }
        } else {
            queue.add(obj);
            return 1;
        }
    }

    public int getMaxsize() {
        return maxsize;
    }

    public void setMaxsize(int maxsize) {
        this.maxsize = maxsize;
    }

    public synchronized Object peek() {
        return queue.peek();

    }

    public synchronized Object poll() {
        return queue.poll();
    }

    public synchronized List<Object> pollAll() {
        List<Object> result = new ArrayList<>(queue);
        queue.clear();
        return result;
    }

    public synchronized void clear() {
        queue.clear();
    }
}
