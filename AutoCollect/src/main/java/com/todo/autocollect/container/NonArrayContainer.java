package com.todo.autocollect.container;

/**
 * Created by TCG on 2018/7/20.
 */

public class NonArrayContainer<V> extends Container<Object, V> {

    public V val() {
        return find(NON_ARRAY_KEY);
    }

    public void add(V val) {
        add(NON_ARRAY_KEY, val);
    }

}
