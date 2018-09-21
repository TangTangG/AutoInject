package com.todo.autocollect;


/**
 *
 * <p>
 * description:
 *
 * @author Caigao.Tang
 * @date 2018/7/19
 */
public final class Log {

    private final static boolean debug = true;

    public static void print(String msg) {
        if (debug) {
            System.out.println(msg);
        }
    }

}
