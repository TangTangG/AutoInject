package com.todo.autoinject;

import com.todo.autocollect.AutoCollect;

public class Main {

    public static void main(String[] args) {
        CollectionManager manager = new CollectionManager();
        AutoCollect.begin(manager);
        System.out.println(manager.collections.size());
    }

}
