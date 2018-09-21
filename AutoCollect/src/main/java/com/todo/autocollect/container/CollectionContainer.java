package com.todo.autocollect.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author TCG
 * @date 2018/7/20
 */

public class CollectionContainer<K> extends Container<K, Object> {

    private String collectionType;

    public CollectionContainer(Class<? extends Collection> c) {
        collectionType = c.getName();
    }

    public Collection copy(Collection target) {
        if (target == null) {
            target = checkTarget();
        }
        Set<K> set = data.keySet();
        Collections.addAll(target, set.toArray());
        return target;
    }

    private Collection checkTarget() {
        if (collectionType.contains("List")) {
            return new ArrayList(16);
        } else if (collectionType.contains("Set")) {
            return new HashSet(16);
        } else {
            throw new UnsupportedOperationException("Unsupported type " + collectionType);
        }
    }


    public void add(K key) {
        add(key, TEMP);
    }

}
