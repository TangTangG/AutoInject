package com.todo.autocollect;

import com.squareup.javapoet.CodeBlock;

/**
 * Created by TCG on 2018/7/20.
 */

interface  TargetModel {

    TargetModel generateFieldModel();

    CodeBlock block();
}
