package com.todo.autocollect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author TCG
 * @date 2018/7/18
 */

public final class AutoCollect {

    private static final Map<Class<?>, Constructor<? extends Collector>> COLLECTED = new LinkedHashMap<>();

    public static Collector begin(Object target) {
        return createCollector(target);
    }

    private static Collector createCollector(Object target) {
        Constructor<? extends Collector> collector = getOrBuildConstructor(target.getClass());
        if (collector == null){
            return Collector.EMPTY;
        }
        try {
            return collector.newInstance(target);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Unable to invoke " + collector, e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new RuntimeException("Unable to create collector instance.", cause);
        }
    }

    @SuppressWarnings("unchecked")
    private static Constructor<? extends Collector> getOrBuildConstructor(Class<?> cls) {
        Constructor<? extends Collector> collector = COLLECTED.get(cls);
        if (collector != null) {
            return collector;
        }
        String clsName = cls.getName();
        if (clsName.startsWith("android.") || clsName.startsWith("java.")) {
            Log.print("MISS: Reached framework class. Abandoning search.");
            return null;
        }
        try {
            Class<?> containerCls = cls.getClassLoader().loadClass(clsName + "_Collector");
            collector = (Constructor<? extends Collector>) containerCls.getConstructor(cls);
        } catch (ClassNotFoundException e) {
            Log.print("Try find collector form parent.");
            collector = getOrBuildConstructor(cls.getSuperclass());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to find collector constructor for " + clsName, e);
        }
        COLLECTED.put(cls,collector);
        return collector;
    }

}
