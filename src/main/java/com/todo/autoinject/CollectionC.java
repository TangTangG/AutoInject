package com.todo.autoinject;

import com.todo.autocollect.annotation.CollectionRegister;

/**
 * Created by TCG on 2018/9/21.
 */
@CollectionRegister(type = "c",target = CollectionIface.class)
public class CollectionC implements CollectionIface {
    @Override
    public void inject(Object[] args) {

    }
}
