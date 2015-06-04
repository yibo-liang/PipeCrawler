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

import java.lang.reflect.Array;
import java.util.HashMap;
import pcc.abstractclass.DPBuffer;

/**
 * DPBuffer = Data parallelism buffer This buffer is a implementation of data
 * parallelism. producer and consumer can access buffer element in a parallel &
 * lock free manner. In order to use this buffer producer and consumer objects
 * have to be registered to it before push/poll any element
 *
 * The method setMaxSize will not set the total space of the buffer but the
 * buffer depth instead. The size of buffer would be LCM( p * c ) * depth, where
 * p and c are the number of producer and consumer respectively
 *
 * @author yl9
 * @param <T>
 */
public class DPBufferLockfree<T> extends DPBuffer {

    T[][] Buffer;
    private final int defaultDepth = 5;
    private boolean isInitialised = false;
    private final int PRODUCER = 1;
    private final int CONSUMER = 2;
    private final Class buferClass;

    private HashMap<Object, Integer> producers;
    private HashMap<Object, Integer> consumers;

    private int pBlockSize;
    private int cBlockSize;

    private Integer[] pIndices;
    private Integer[] cIndices;

    private Integer[] heads;
    private Integer[] tails;

    private int pCount = 0;
    private int cCount = 0;

    private int depth;
    private int width;

    public int getProducerCount() {
        return pCount;
    }

    public int getConsumerCount() {
        return cCount;
    }

    private static int gcd(int a, int b) {
        while (b > 0) {
            int temp = b;
            b = a % b; // % is remainder
            a = temp;
        }
        return a;
    }

    private static int lcm(int a, int b) {
        return a * (b / gcd(a, b));
    }

    //public methods
    public DPBufferLockfree(Class<T> c) {
        buferClass = c;
    }

    private void switchIndex(int id, int number) {
        if (id == PRODUCER) {
            //Horizontally switch
            this.pIndices[number] = pBlockSize * number + (this.pIndices[number] + 1) % pBlockSize;
        } else if (id == CONSUMER) {
            this.cIndices[number] = cBlockSize * number + (this.cIndices[number] + 1) % cBlockSize;
        } else {
            throw new UnsupportedOperationException("Unknown identity! 1 for Producer; 2 for Consumer.");
        }
    }

    /**
     * Push method takes a key to identify caller, then push the element object
     * into corresponding array block.
     *
     * The push will try all slot this caller has, if non of the slot is empty,
     * then return false;
     *
     * @param callerKey
     * @param obj
     * @return
     */
    @Override
    public boolean push(Object callerKey, Object obj) {
        //pNumber is the producer 's number
        int pNumber = this.producers.get(callerKey);
        //pIndex is the pointer of the producer to the array first dimension(width)
        int pIndex = this.pIndices[pNumber];

        //currentHead is the pointer of the producer to the array second dimension(Depth)
        for (int i = 0; i < pBlockSize; i++) {

            int currentTail = this.tails[pNumber];
            int currentHead = this.heads[pNumber];
            //Vertical increment 
            int nextTail = (currentTail + 1) % depth;
            if (currentHead != nextTail) {
                this.Buffer[pIndex][currentTail] = (T) obj;
                this.tails[pNumber] = nextTail;

                return true;
            }
            switchIndex(PRODUCER, pNumber);
        }
        return false;
    }

    /**
     * Unlike push, poll will only try once for current slot of the caller
     * object. The caller is required to call multiple times to get element if
     * necessary
     *
     * @param callerKey
     * @return
     */
    @Override
    public T poll(Object callerKey) {
        T result = null;
        int cNumber = this.consumers.get(callerKey);
        int cIndex = this.cIndices[cNumber];

        int currentTail = this.tails[cNumber];
        int currentHead = this.heads[cNumber];
        int nextHead = (currentHead + 1) % depth;
        if (currentHead != currentTail) {
            this.heads[cNumber] = nextHead;
            result = this.Buffer[cIndex][currentHead];

        }
        switchIndex(CONSUMER, cNumber);
        return result;
    }

    @Override
    public T peek(Object callerKey) {
 T result = null;
        int cNumber = this.consumers.get(callerKey);
        int cIndex = this.cIndices[cNumber];

        int currentTail = this.tails[cNumber];
        int currentHead = this.heads[cNumber];
        
        if (currentHead != currentTail) {
           
            result = this.Buffer[cIndex][currentHead];

        }
        return result;
    }

    @Override
    public int getDepth() {
        return this.depth;
    }

    @Override
    public synchronized boolean setDepth(int depth) {
        if (!isInitialised) {
            this.depth = depth;
            return true;
        } else {
            throw new UnsupportedOperationException("Cannot change depth after initialisation!");
        }
    }

    @Override
    public void clear(Object callerKey) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * This method is synchronised to avoid any concurrent problem before
     * initialisation
     *
     * @param callerKey
     * @param identity
     * @return
     */
    @Override
    public synchronized int register(Object callerKey, int identity) {
        //locked to register
        if (isInitialised) {
            throw new UnsupportedOperationException("Cannot register new object after initialisation!");
        }
        if (callerKey != null) {
            if (identity == PRODUCER) {
                producers.put(callerKey, pCount);
                pCount++;
                return 1;
            } else if (identity == CONSUMER) {
                consumers.put(callerKey, cCount);
                cCount++;
                return 2;
            } else {
                throw new UnsupportedOperationException("Unknown identity! 1 for Producer; 2 for Consumer.");
            }
        } else {
            throw new NullPointerException("Cannot register a null object to the buffer!");
            //return -1;
        }

    }

    /**
     * This method initialise everything for the buffer. After calling this
     * method no more change of settings can be made to the buffer.
     *
     */
    @Override
    public synchronized void initialise() {
        //Has to be locked to initialise
        isInitialised = true;
        this.width = lcm(pCount, cCount);
        pBlockSize = width / pCount;
        cBlockSize = width / cCount;

        this.pIndices = new Integer[this.pCount];
        this.cIndices = new Integer[this.cCount];

        this.heads = new Integer[this.depth];
        this.tails = new Integer[this.depth];
        Buffer = (T[][]) Array.newInstance(buferClass, width, depth);

        //setting up block indices.
        for (int i = 0; i < pCount; i++) {
            this.pIndices[i] = i * pBlockSize;

        }

        for (int i = 0; i < cCount; i++) {
            this.cIndices[i] = i * cBlockSize;

        }

        for (int i = 0; i < width; i++) {
            this.heads[i] = 0;
            this.tails[i] = 0;
        }

        //setting up 
    }
}
