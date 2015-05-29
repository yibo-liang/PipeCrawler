/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ppc.interfaceclass;

import java.util.List;

/**
 *
 * @author Yibo
 * @param <T>
 */
public interface Buffer<T> {

    public abstract void push(T obj);

    public abstract T poll();

    public abstract T peek();

    public abstract void clear();

    public abstract int getMaxsize();

    public abstract void setMaxsize(int maxsize);
    
    public abstract  List<T> pollAll() ;
}
