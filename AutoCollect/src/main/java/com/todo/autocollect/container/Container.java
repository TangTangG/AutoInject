package com.todo.autocollect.container;

import java.util.HashMap;
import java.util.Map;

/**
 * Record an collector`s val information.
 * All data is store in Map;
 * @author TCG
 * @date 2018/7/13
 */

public class Container<K, V> {

    final Object TEMP = new Object();
    final Object NON_ARRAY_KEY = new Object();

    /**
     * fieldName,Val Map
     */
    protected final Map<K, V> data = new HashMap<>(16);

    V find(K key) {
        return data.get(key);
    }

    public void add(K key, V val) {
        data.put(key, val);
    }

    @SuppressWarnings("unchecked")
    public Map copy() {
        Map<K, V> result = new HashMap<>(16);
        result.putAll(data);
        return result;
    }

}
