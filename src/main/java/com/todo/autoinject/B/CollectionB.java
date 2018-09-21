package com.todo.autoinject.B;

import com.todo.autocollect.annotation.CollectionRegister;
import com.todo.autoinject.CollectionIface;

/**
 * Created by TCG on 2018/9/21.
 */
@CollectionRegister(type = "b",target = CollectionIface.class)
public class CollectionB implements CollectionIface {
    @Override
    public void inject(Object[] args) {

    }
}
