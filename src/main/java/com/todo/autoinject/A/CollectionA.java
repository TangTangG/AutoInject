package com.todo.autoinject.A;

import com.todo.autocollect.annotation.CollectionRegister;
import com.todo.autoinject.CollectionIface;

/**
 * Created by TCG on 2018/9/21.
 */
@CollectionRegister(type = "a", target = CollectionIface.class)
public class CollectionA implements CollectionIface{
    @Override
    public void inject(Object[] args) {

    }
}
