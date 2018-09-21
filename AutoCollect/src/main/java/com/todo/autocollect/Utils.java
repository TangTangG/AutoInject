package com.todo.autocollect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by TCG on 2018/7/20.
 */

public final class Utils {

    private static final Map<Class, Constructor> CACHED_CONSTRUCTOR = new LinkedHashMap<>();

    public static <T> T newFromCls(Class<? extends T> cls, Object[] args) {
        Constructor<? extends T> constructor = findConstructorForClass(cls, args);

        if (constructor == null) {
            throw new RuntimeException("Unable to create binding instance. ");
        }
        try {
            T t;
            if (args.length > 0) {
                t = constructor.newInstance(cls, args);
            } else {
                t = constructor.newInstance();
            }
            return t;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Unable to invoke " + constructor, e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new RuntimeException("Unable to create t instance.", cause);
        }
    }


    private static <T> Constructor<? extends T> findConstructorForClass(Class<?> cls, Object[] args) {
        Constructor<? extends T> tCtor = CACHED_CONSTRUCTOR.get(cls);
        if (tCtor != null) {
            return tCtor;
        }
        String clsName = cls.getName();
        if (clsName.startsWith("android.") || clsName.startsWith("java.")) {
            Log.print("MISS: Reached framework class. Abandoning search.");
            return null;
        }

        int length = args.length;
        Class<?>[] clsArray = null;
        if (length > 0) {
            clsArray = new Class[length];
            for (int i = 0; i < length; i++) {
                Object arg = args[i];
                if (arg == null) {
                    throw new RuntimeException(String.format("Unable create %s from null args.", clsName));
                }
                clsArray[i] = arg.getClass();
            }

        }

        try {
            Class<?> bindingClass = cls.getClassLoader().loadClass(clsName);
            //noinspection unchecked
            if (clsArray == null) {
                tCtor = (Constructor<? extends T>) bindingClass.getConstructor();
            } else {
                tCtor = (Constructor<? extends T>) bindingClass.getConstructor(clsArray);
            }
//            Log.print("HIT: Loaded collected class and constructor.");
        } catch (ClassNotFoundException e) {
            Log.print("Not found. Trying superclass " + cls.getSuperclass().getName());
            tCtor = findConstructorForClass(cls.getSuperclass(), args);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to find constructor for " + clsName, e);
        }
        CACHED_CONSTRUCTOR.put(cls, tCtor);
        return tCtor;
    }

}
