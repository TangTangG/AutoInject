package com.todo.autocollect;


import com.todo.autocollect.container.CollectionContainer;
import com.todo.autocollect.container.Container;
import com.todo.autocollect.container.NonArrayContainer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author TCG
 * @date 2018/7/20
 */

public class Collector {

    static final Collector EMPTY = new Collector();

    private final Map<String, Container> holder = new HashMap<>(16);

    protected <T> T newFromCls(Class<? extends T> cls, Object... args) {
        return Utils.newFromCls(cls, args);
    }

    /**
     * @param c The collection type
     * @param cls as CollectionContainer<K> K
     */
    protected void addToCollection(Class<? extends Collection> c, String fieldName, Object val, Class<?> cls) {
        Container container = holder.get(fieldName);
        if (container == null) {
            container = new CollectionContainer(c);
            holder.put(fieldName,container);
        } else if (!(container instanceof CollectionContainer)) {
            return;
        }
        CollectionContainer collection = (CollectionContainer) container;
        collection.add(cls.cast(val));
    }

    protected void addToMap(String fieldName, Object key, Object val, Class<?> kcls, Class<?> vcls) {
        Container container = holder.get(fieldName);
        if (container == null) {
            container = new Container();
            holder.put(fieldName,container);
        }
        container.add(kcls.cast(key), vcls.cast(val));
    }

    protected void addNoArray(String fieldName, Object val, Class<?> vcls) {
        Container container = holder.get(fieldName);
        if (container == null) {
            container = new NonArrayContainer();
            holder.put(fieldName,container);
        } else if (!(container instanceof NonArrayContainer)) {
            return;
        }
        ((NonArrayContainer) container).add(vcls.cast(val));
    }

    protected <T extends Collection> T findByField(String fieldName, T target) {
        Container container = holder.get(fieldName);
        if (container == null || !(container instanceof CollectionContainer)) {
            return null;
        }
        CollectionContainer collectionContainer = (CollectionContainer) container;
        return (T) collectionContainer.copy(target);
    }

    protected Map findByField(String fieldName) {
        Container container = holder.get(fieldName);
        if (container == null) {
            return null;
        }
        return container.copy();
    }

    protected <T> T findByField(String fieldName, Class<? extends T> cls) {
        Container container = holder.get(fieldName);
        if (container == null || !(container instanceof NonArrayContainer)) {
            return null;
        }
        NonArrayContainer nonArray = (NonArrayContainer) container;
        return cls.cast(nonArray.val());
    }

    protected void doFinal() {
        holder.clear();
    }

}
